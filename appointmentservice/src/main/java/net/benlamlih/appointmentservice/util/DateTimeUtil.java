package net.benlamlih.appointmentservice.util;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class DateTimeUtil {

    private static final ZoneId SYSTEM_DEFAULT_ZONE = ZoneId.systemDefault();

    private DateTimeUtil() {
    }

    public static Date toDate(LocalTime localTime, LocalDate localDate) {
        return Date.from(localTime.atDate(localDate).atZone(SYSTEM_DEFAULT_ZONE).toInstant());
    }

    public static Date convertToDate(LocalDateTime localDateTime) {
        return Date.from(localDateTime.atZone(SYSTEM_DEFAULT_ZONE).toInstant());
    }

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(SYSTEM_DEFAULT_ZONE).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(SYSTEM_DEFAULT_ZONE).toLocalTime();
    }

    public static LocalDateTime toLocalDateTime(Date dateToConvert) {
        return dateToConvert.toInstant().atZone(SYSTEM_DEFAULT_ZONE).toLocalDateTime();
    }
}
