package com.vibal.utilities.util;

import android.util.Log;

import com.vibal.utilities.BuildConfig;

public class LogUtil {
    private static final String TEMP_TAG = "PruebaTemp";

    public static void debug(String message) {
        debug(TEMP_TAG, message);
    }

    public static void error(String message, Throwable e) {
        error(TEMP_TAG, message, e);
    }

    public static void debug(final String tag, String message) {
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
