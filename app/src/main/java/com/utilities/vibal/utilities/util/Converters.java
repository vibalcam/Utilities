package com.utilities.vibal.utilities.util;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import java.util.Calendar;

public class Converters {
    @TypeConverter
    public static Calendar fromTimestamp(long millis) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(millis);
        return calendar;
    }

    @TypeConverter
    public static long calendarToTimestamp(@NonNull Calendar calendar) {
        return calendar.getTimeInMillis();
    }
}
