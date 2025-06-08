package kr.kickon.api.admin.migration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드를 무시
@Slf4j
public class ApiTeamDTO {
    private Long id;
    private String name;
    private String logo;
    private String code;
    private Integer year;
    private Long leaguePk;
    // JsonCreator로 생성자 지정
    @JsonCreator
    public ApiTeamDTO(
            @JsonProperty("code") String code,
            @JsonProperty("id") Long id,
            @JsonProperty("name") String name,
            @JsonProperty("logo") String logo) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.logo = logo;
        this.year = 0;
        this.leaguePk = 1L;
    }
}
