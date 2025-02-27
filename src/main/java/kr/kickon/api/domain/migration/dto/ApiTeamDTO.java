package kr.kickon.api.domain.migration.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class ApiTeamDTO {
    private Long id;
    private String name;
    private String logo;
    private String code;
}
