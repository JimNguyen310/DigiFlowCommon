package fec.digiflow.common.utils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.ChronoUnit;
import java.util.Date;

/**
 * A utility class for handling date and time operations using the Java 8+ java.time API.
 * This class is thread-safe and provides static methods for common date-time tasks such as
 * formatting, parsing, and conversion.
 */
public final class DatetimeUtils {

    // --- Predefined Format Patterns ---
    public static final String ISO_DATE_FORMAT = "yyyy-MM-dd";
    public static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";
    public static final String HUMAN_DATE_FORMAT = "dd/MM/yyyy";
    public static final String HUMAN_DATETIME_FORMAT = "dd/MM/yyyy HH:mm:ss";


    private DatetimeUtils() {
        // Private constructor to prevent instantiation
    }

    // --- Formatting Methods (Object to String) ---

    /**
     * Formats a LocalDateTime object into a string using a specified pattern.
     *
     * @param dateTime The LocalDateTime to format. Can be null.
     * @param pattern  The pattern to use for formatting.
     * @return The formatted date-time string, or null if the input was null.
     */
    public static String format(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        return dateTime.format(DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Formats a LocalDate object into a string using a specified pattern.
     *
     * @param date    The LocalDate to format. Can be null.
     * @param pattern The pattern to use for formatting.
     * @return The formatted date string, or null if the input was null.
     */
    public static String format(LocalDate date, String pattern) {
        if (date == null) {
            return null;
        }
        return date.format(DateTimeFormatter.ofPattern(pattern));
    }

    // --- Parsing Methods (String to Object) ---

    /**
     * Parses a date-time string into a LocalDateTime object using a specified pattern.
     *
     * @param dateTimeString The string to parse.
     * @param pattern        The pattern to use for parsing.
     * @return The parsed LocalDateTime object.
     * @throws DateTimeParseException if the string cannot be parsed.
     */
    public static LocalDateTime parseLocalDateTime(String dateTimeString, String pattern) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        return LocalDateTime.parse(dateTimeString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Parses a date string into a LocalDate object using a specified pattern.
     *
     * @param dateString The string to parse.
     * @param pattern    The pattern to use for parsing.
     * @return The parsed LocalDate object.
     * @throws DateTimeParseException if the string cannot be parsed.
     */
    public static LocalDate parseLocalDate(String dateString, String pattern) {
        if (dateString == null || dateString.isEmpty()) {
            return null;
        }
        return LocalDate.parse(dateString, DateTimeFormatter.ofPattern(pattern));
    }

    /**
     * Tries to parse a date-time string using multiple patterns. It will return the result
     * from the first pattern that successfully parses the string.
     *
     * @param dateTimeString The string to parse.
     * @param patterns       An array of patterns to try.
     * @return The parsed LocalDateTime object.
     * @throws DateTimeParseException if the string cannot be parsed with any of the provided patterns.
     */
    public static LocalDateTime tryParseLocalDateTime(String dateTimeString, String... patterns) {
        if (dateTimeString == null || dateTimeString.isEmpty()) {
            return null;
        }
        for (String pattern : patterns) {
            try {
                return parseLocalDateTime(dateTimeString, pattern);
            } catch (DateTimeParseException e) {
                // Ignore and try the next pattern
            }
        }
        throw new DateTimeParseException("Failed to parse date-time string with any of the provided patterns", dateTimeString, 0);
    }

    // --- Conversion Methods ---

    /**
     * Converts a legacy java.util.Date object to a modern LocalDateTime object.
     * Uses the system's default time zone for conversion.
     *
     * @param date The java.util.Date to convert. Can be null.
     * @return The converted LocalDateTime, or null if the input was null.
     */
    public static LocalDateTime toLocalDateTime(Date date) {
        if (date == null) {
            return null;
        }
        return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    /**
     * Converts a modern LocalDateTime object to a legacy java.util.Date object.
     * Uses the system's default time zone for conversion.
     *
     * @param localDateTime The LocalDateTime to convert. Can be null.
     * @return The converted java.util.Date, or null if the input was null.
     */
    public static Date toDate(LocalDateTime localDateTime) {
        if (localDateTime == null) {
            return null;
        }
        return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
    }

    // --- Calculation Methods ---

    /**
     * Calculates the number of full days between two LocalDate objects.
     *
     * @param start The start date (inclusive).
     * @param end   The end date (exclusive).
     * @return The total number of days between the two dates.
     */
    public static long getDaysBetween(LocalDate start, LocalDate end) {
        return ChronoUnit.DAYS.between(start, end);
    }

    /**
     * Calculates the number of full hours between two LocalDateTime objects.
     *
     * @param start The start date-time (inclusive).
     * @param end   The end date-time (exclusive).
     * @return The total number of hours between the two date-times.
     */
    public static long getHoursBetween(LocalDateTime start, LocalDateTime end) {
        return ChronoUnit.HOURS.between(start, end);
    }
}
