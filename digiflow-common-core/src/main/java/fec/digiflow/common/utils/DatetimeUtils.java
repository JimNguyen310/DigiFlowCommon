package fec.digiflow.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

public final class DatetimeUtils {
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String HUMAN_DATE_FORMAT = "dd/MM/yyyy";
    public static final String HUMAN_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";

    private DatetimeUtils() {}

    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) return null;
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static String format(LocalDate date, String pattern) {
        if (date == null) return null;
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime parseLocalDateTime(String dateTimeString, String pattern) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDate parseLocalDate(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) return null;
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    public static LocalDateTime tryParseLocalDateTime(String dateTimeString, String... patterns) {
        if (dateTimeString == null || dateTimeString.isEmpty()) return null;
        for (String pattern : patterns) {
            try {
                return parseLocalDateTime(dateTimeString, pattern);
            } catch (DateTimeParseException ignored) {}
        }
        throw new DateTimeParseException("Failed to parse date-time string", dateTimeString, 0);
    }

    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) return null;
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) return null;
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    public static long getDaysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }
}
