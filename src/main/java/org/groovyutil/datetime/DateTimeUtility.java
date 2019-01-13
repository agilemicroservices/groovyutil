package org.groovyutil.datetime;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateTimeUtility {

    public static LocalTime ToLocalTime(String timeString, String formatter) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter);
        LocalTime localTime = LocalTime.parse(timeString, dateTimeFormatter);
        return localTime;
    }

    public static String FromLocalTime(LocalTime localTime, String formatter) {
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(formatter);
        String localTimeString = localTime.format(dateTimeFormatter);
        return localTimeString;
    }

}
