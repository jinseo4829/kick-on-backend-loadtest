package kr.kickon.api.admin.migration.dto;


import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.kickon.api.global.common.enums.LeagueType;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드를 무시
@Slf4j
public class ApiLeagueDTO {
    private Long id;
    private String name;
    private LeagueType type;
    private String logo;

    @JsonCreator
    public ApiLeagueDTO(
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("type") String type,
            @JsonProperty("logo") String logo
            ) {

        this.id = Long.valueOf(id);
        this.name = name;
        this.type = LeagueType.valueOf(type);
        this.logo = logo;
    }
}
