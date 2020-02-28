package com.vibal.utilities.util;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public class Converters {
    private static Currency defaultCurrency = Currency.getInstance(Locale.getDefault());

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

    @NonNull
    @TypeConverter
    public static String currencyToString(@Nullable Currency currency) {
        return currency == null || currency.equals(defaultCurrency) ? "" : currency.getCurrencyCode();
    }

    @NonNull
    @TypeConverter
    public static Currency currencyFromString(@Nullable String str) {
        return str == null || str.isEmpty() ? defaultCurrency : Currency.getInstance(str);
    }
}
