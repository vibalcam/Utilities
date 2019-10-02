package com.utilities.vibal.utilities.util;

import android.util.Log;

import androidx.annotation.NonNull;

import com.utilities.vibal.utilities.BuildConfig;

public class LogUtil {
    public static void debug(final String tag, @NonNull String message) {
        if (BuildConfig.DEBUG_MODE) {
            Log.d(tag, message);
        }
    }

    public static void error(final String tag, String message, Throwable e) {
        if (BuildConfig.DEBUG_MODE) {
            Log.e(tag, message, e);
        }
    }
}
