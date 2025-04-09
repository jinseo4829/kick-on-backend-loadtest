package kr.kickon.api.domain.news.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import kr.kickon.api.global.common.ExampleConstants;
import kr.kickon.api.global.common.enums.NewsCategory;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
@Schema(description = "많이 본 뉴스 리스트 조회를 위한 뉴스 DTO")
public class HotNewsListDTO {
    @Schema(example = "1", description = "뉴스 pk값")
    private Long pk;

    @Schema(example = ExampleConstants.title, description = "뉴스 제목")
    private String title;
    @Schema(example = ExampleConstants.thumbnail, description = "뉴스 썸네일")
    private String thumbnailUrl;

    @Schema(example = "이적", description = "뉴스 카테고리")
    private String category;

    @Schema(example = ExampleConstants.teamnameEn,description = "팀 이름 (영어)")
    private String teamNameEn;

    @Schema(example = ExampleConstants.teamKrName,description = "팀 이름 (한글)")
    private String teamNameKr;

    @Schema(example = ExampleConstants.teamLogoUrl,description = "팀 로고")
    private String teamLogoUrl;

    @Schema(example = "1",description = "팀 pk")
    private Long teamPk;

    @Schema(example = ExampleConstants.leagueKrName,description = "리그 이름 (한글)")
    private String leagueNameKr;

    @Schema(example = "1233", description = "조회수")
    private Integer views;
}
