package kr.kickon.api.domain.team;

import com.fasterxml.jackson.databind.ObjectMapper;
import kr.kickon.api.domain.team.dto.TeamDTO;
import kr.kickon.api.domain.team.request.TeamListFilterRequest;
import kr.kickon.api.global.common.entities.Team;
import kr.kickon.api.global.error.handler.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class TeamControllerTest {

    private MockMvc mockMvc;

    @Mock
    private TeamService teamService;

    @BeforeEach
    void setUp() {
        TeamController teamController = new TeamController(teamService);
        mockMvc = MockMvcBuilders.standaloneSetup(teamController)
                .setControllerAdvice(new GlobalExceptionHandler(new ObjectMapper()))
                .build();
    }

    @Test
    @DisplayName("getTeams - 리그 기준 조회 성공")
    void getTeams_byLeague_success() throws Exception {
        // Team 엔티티
        Team teamA = new Team();
        teamA.setPk(1L);
        teamA.setNameKr("Team A");
        teamA.setNameEn("TA");
        teamA.setLogoUrl("logoA.png");

        Team teamB = new Team();
        teamB.setPk(2L);
        teamB.setNameKr("Team B");
        teamB.setNameEn("TB");
        teamB.setLogoUrl("logoB.png");

        List<TeamDTO> teams = List.of(new TeamDTO(teamA), new TeamDTO(teamB));
        Pageable pageable = PageRequest.of(0, 10); // page=0, size=10
        Page<TeamDTO> page = new PageImpl<>(teams, pageable, teams.size());

        when(teamService.getTeamListByFilter(any(TeamListFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/team")
                        .param("league", "1")
                        .param("currentPage", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data[0].nameKr").value("Team A"))
                .andExpect(jsonPath("$.data[1].nameKr").value("Team B"))
                .andExpect(jsonPath("$.meta.currentPage").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(10))
                .andExpect(jsonPath("$.meta.totalItems").value(teams.size()));
    }

    @Test
    @DisplayName("getTeams - 키워드 검색 성공")
    void getTeams_byKeyword_success() throws Exception {
        Team teamA = new Team();
        teamA.setPk(1L);
        teamA.setNameKr("Team A");
        teamA.setNameEn("TA");
        teamA.setLogoUrl("logoA.png");

        List<TeamDTO> teams = List.of(new TeamDTO(teamA));
        Pageable pageable = PageRequest.of(0, 10); // page=0, size=10
        Page<TeamDTO> page = new PageImpl<>(teams, pageable, teams.size());

        when(teamService.getTeamListByFilter(any(TeamListFilterRequest.class), any(Pageable.class)))
                .thenReturn(page);

        mockMvc.perform(get("/api/team")
                        .param("keyword", "Team")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value("GET_SUCCESS"))
                .andExpect(jsonPath("$.data[0].nameKr").value("Team A"))
                .andExpect(jsonPath("$.meta.currentPage").value(1))
                .andExpect(jsonPath("$.meta.pageSize").value(10))
                .andExpect(jsonPath("$.meta.totalItems").value(teams.size()));
    }

    @Test
    @DisplayName("getTeams - league, keyword 둘 다 없으면 BadRequest")
    void getTeams_noParams_badRequest() throws Exception {
        mockMvc.perform(get("/api/team"))
                .andExpect(status().isBadRequest());
    }
}
