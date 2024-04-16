package net.benlamlih.appointmentservice.util;

import java.time.LocalDate;
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

    public static LocalDate toLocalDate(Date date) {
        return date.toInstant().atZone(SYSTEM_DEFAULT_ZONE).toLocalDate();
    }

    public static LocalTime toLocalTime(Date date) {
        return date.toInstant().atZone(SYSTEM_DEFAULT_ZONE).toLocalTime();
    }

    public static LocalTime convertToLocalTimeViaInstant(Date dateToConvert) {
        return toLocalTime(dateToConvert);
    }
}
