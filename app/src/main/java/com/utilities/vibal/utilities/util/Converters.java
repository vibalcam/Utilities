package com.utilities.vibal.utilities.util;

import androidx.annotation.NonNull;
import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.UUID;

public class Converters {
    @NonNull
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

    @NonNull
    @TypeConverter
    public static String UUIDToString(@NonNull UUID uuid) {
        return uuid.toString();
    }

    @TypeConverter
    public static UUID UUIDFromString(String str) {
        return UUID.fromString(str);
    }
}
