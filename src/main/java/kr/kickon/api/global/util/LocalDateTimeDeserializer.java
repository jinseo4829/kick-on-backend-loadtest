package kr.kickon.api.global.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Slf4j
public class LocalDateTimeDeserializer extends JsonDeserializer<LocalDateTime> {
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");

    @Override
    public LocalDateTime deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        String dateString = p.getText();
        try {
            return LocalDateTime.parse(dateString, FORMATTER);
        } catch (DateTimeParseException e) {
            throw new InvalidFormatException(p, "잘못된 날짜 형식입니다. yyyy-MM-dd'T'HH:mm:ss'Z' 형식을 사용하세요.", dateString, LocalDateTime.class);
        }
    }
}