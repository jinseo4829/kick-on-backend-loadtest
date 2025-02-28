package kr.kickon.api.domain.migration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드를 무시
public class ApiTeamDTO {
    private Long id;
    private String name;
    private String logo;
    private String code;
    // JsonCreator로 생성자 지정
    @JsonCreator
    public ApiTeamDTO(
            @JsonProperty("code") String code,
            @JsonProperty("id") String id,
            @JsonProperty("name") String name,
            @JsonProperty("logo") String logo) {
        this.id = Long.valueOf(id);
        this.code = code;
        this.name = name;
        this.logo = logo;
    }
}
