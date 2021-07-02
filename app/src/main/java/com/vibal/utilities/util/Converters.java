package com.vibal.utilities.util;

import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.TypeConverter;

import java.util.Calendar;
import java.util.Currency;
import java.util.Locale;
import java.util.UUID;

public class Converters {
    private static final Currency defaultCurrency = Currency.getInstance(Locale.getDefault());

    public static float getScaleDpToPx(@NonNull Context context) {
        return context.getResources().getDisplayMetrics().density;
    }

    @NonNull
    @TypeConverter
    public static Calendar calendarFromTimestamp(long millis) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
            return new Calendar.Builder().setInstant(millis).build();
        else {
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(millis);
            return calendar;
        }
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
