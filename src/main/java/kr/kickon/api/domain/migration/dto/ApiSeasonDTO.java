package kr.kickon.api.domain.migration.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import kr.kickon.api.global.common.enums.OperatingStatus;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;

@Data
@JsonIgnoreProperties(ignoreUnknown = true) // 알 수 없는 필드를 무시
@Slf4j
public class ApiSeasonDTO {
    private Integer year;
    private LocalDate start;
    private LocalDate end;
    private OperatingStatus operatingStatus;
    @JsonCreator
    public ApiSeasonDTO(
            @JsonProperty("year") Integer year,
            @JsonProperty("start") String start,
            @JsonProperty("end") String end,
            @JsonProperty("current") Boolean operatingStatus
    ) {
//        log.error(year + " " + start + " " + end + " " + operatingStatus);
        // year를 int로 변환
        this.year = year;

        // start와 end를 LocalDate로 변환
        this.start = LocalDate.parse(start);
        this.end = LocalDate.parse(end);

        // operatingStatus를 OperatingStatus 타입으로 변환
        this.operatingStatus = operatingStatus ? OperatingStatus.PROCEEDING : OperatingStatus.FINISHED;
    }
}
