package com.vibal.utilities.util;

import android.os.Bundle;

import androidx.annotation.Nullable;

/**
 * Objects with which you can use {@link DiffCallback}
 *
 * @param <T> type of the objects to use in the {@link DiffCallback}
 */
public interface DiffDbUsable<T> {
    boolean areItemsTheSame(T newItem);

    boolean areContentsTheSame(T newItem);

    @Nullable
    default Bundle getChangePayload(T newItem) {
        return null;
    }
}
