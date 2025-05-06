package kr.kickon.api.global.common.enums;

public enum NewsCategory {
    INJURY("부상"),
    TRANSFER("이적"),
    HEADCOACH("감독 교체"),
    RENEWAL("재계약"),
    UNHAPPY("불화설"),
    RETIRE("은퇴"),
    INTERVIEW("인터뷰"),
    LOCAL("현지 팬 반응"),
    ETC("기타");

    private final String koreanName;

    // 생성자를 통해 한글 이름을 설정
    NewsCategory(String koreanName) {
        this.koreanName = koreanName;
    }

    // 한글 이름을 반환하는 메서드
    public String getKoreanName() {
        return koreanName;
    }
    public static class VALUE {
        public static final String HEADCOACH = "HEADCOACH";
        public static final String RENEWAL = "RENEWAL";
        public static final String UNHAPPY = "UNHAPPY";
        public static final String RETIRE = "RETIRE";
        public static final String LOCAL = "LOCAL";
        public static final String ETC = "ETC";
        public static final String INJURY = "INJURY";
        public static final String TRANSFER = "TRANSFER";
        public static final String INTERVIEW = "INTERVIEW";
    }
}
