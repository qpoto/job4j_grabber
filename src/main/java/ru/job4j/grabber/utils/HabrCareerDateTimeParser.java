package ru.job4j.grabber.utils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

public class HabrCareerDateTimeParser implements DateTimeParser {

    @Override
    public LocalDateTime parse(String parse) {
        OffsetDateTime offsetDateTime = OffsetDateTime.parse(parse);
        return offsetDateTime.toLocalDateTime();
    }

    public static void main(String[] args) {
        HabrCareerDateTimeParser habrCareerDateTimeParser = new HabrCareerDateTimeParser();
        System.out.println(habrCareerDateTimeParser.parse("2024-04-20T12:30:03+03:00"));
    }
}