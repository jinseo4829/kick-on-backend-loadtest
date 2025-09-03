package kr.kickon.api;

import kr.kickon.api.global.auth.jwt.admin.AdminJwtAuthenticationFilter;
import kr.kickon.api.global.auth.jwt.admin.AdminJwtTokenProvider;
import kr.kickon.api.global.auth.jwt.user.JwtAuthenticationFilter;
import kr.kickon.api.global.auth.jwt.user.JwtTokenProvider;
import kr.kickon.api.global.common.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;


@ExtendWith(SpringExtension.class)
public abstract class AbstractSecurityMockSetup {

    User mockUser = User.builder()
            .id("user1")
            .nickname("mockUser")
            .build();

    @MockitoBean
    protected JwtTokenProvider jwtTokenProvider;

    @MockitoBean
    protected AdminJwtTokenProvider adminJwtTokenProvider;

    @MockitoBean
    protected JwtAuthenticationFilter jwtAuthenticationFilter;

    @MockitoBean
    protected AdminJwtAuthenticationFilter adminJwtAuthenticationFilter;


    @BeforeEach
    void setupMocks() {
        given(jwtTokenProvider.validateToken("mock-jwt-token")).willReturn(true);
        given(jwtTokenProvider.getAuthentication("mock-jwt-token"))
                .willReturn(new UsernamePasswordAuthenticationToken(mockUser, null, List.of()));
    }
}