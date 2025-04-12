package com.echonymous.util;

import jakarta.validation.ValidationException;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;

public final class DateTimeUtils {

    private DateTimeUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Parses the given cursor string into a LocalDateTime.
     *
     * @param cursor the string representation of the cursor, expected in ISO_LOCAL_DATE_TIME format
     * @return the parsed LocalDateTime
     * @throws ValidationException if the format is invalid
     */
    public static LocalDateTime parseCursor(String cursor) {
        if (cursor != null && !cursor.isEmpty()) {
            try {
                return LocalDateTime.parse(cursor);
            } catch (DateTimeParseException e) {
                throw new ValidationException("Invalid cursor format. Expected ISO_LOCAL_DATE_TIME.");
            }
        }
        return null;
    }
}
