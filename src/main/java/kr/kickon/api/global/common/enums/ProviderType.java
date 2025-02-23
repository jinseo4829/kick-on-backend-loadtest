package kr.kickon.api.global.common.enums;

import java.util.Arrays;

public enum ProviderType {
    NAVER, KAKAO;

    public static ProviderType fromString(String provider) {
        return Arrays.stream(ProviderType.values())
                .filter(p -> p.name().equalsIgnoreCase(provider))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid provider type: " + provider));
    }
}
