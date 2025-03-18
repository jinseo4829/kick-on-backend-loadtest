package kr.kickon.api.global.util;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class AnnotationCopier {
    public static <S, T> void copyAnnotations(S source, T target) {
        Map<String, Annotation[]> annotationMap = new HashMap<>();

        // 원본 객체의 필드에서 어노테이션 가져오기
        for (Field sourceField : source.getClass().getDeclaredFields()) {
            sourceField.setAccessible(true);
            annotationMap.put(sourceField.getName(), sourceField.getAnnotations());
        }

        // 대상 객체의 필드에 어노테이션 적용
        for (Field targetField : target.getClass().getDeclaredFields()) {
            targetField.setAccessible(true);
            if (annotationMap.containsKey(targetField.getName())) {
                Annotation[] annotations = annotationMap.get(targetField.getName());
                for (Annotation annotation : annotations) {
                    System.out.println("필드: " + targetField.getName() + " - 어노테이션: " + annotation);
                }
            }
        }
    }
}