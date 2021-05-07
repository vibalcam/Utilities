package com.vibal.utilities.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class DiffCallback<T extends DiffDbUsable<T>> extends DiffUtil.Callback {
    private final List<? extends T> oldList;
    private final List<? extends T> newList;

    public DiffCallback(List<? extends T> oldList, List<? extends T> newList) {
        this.oldList = oldList;
        this.newList = newList;
    }

    @Override
    public int getOldListSize() {
        return oldList == null ? 0 : oldList.size();
    }

    @Override
    public int getNewListSize() {
        return newList == null ? 0 : newList.size();
    }

    @Override
    public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).areItemsTheSame(newList.get(newItemPosition));
    }

    @Override
    public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).areContentsTheSame(newList.get(newItemPosition));
    }

    @Nullable
    @Override
    public Bundle getChangePayload(int oldItemPosition, int newItemPosition) {
        return oldList.get(oldItemPosition).getChangePayload(newList.get(newItemPosition));
    }

    public static class DiffResultWithList<T extends DiffDbUsable<T>> {
        private final DiffUtil.DiffResult diffResult;
        private final List<? extends T> newList;

        public DiffResultWithList(DiffUtil.DiffResult diffResult, List<? extends T> newList) {
            this.diffResult = diffResult;
            this.newList = newList;
        }

        public static <T extends DiffDbUsable<T>> DiffResultWithList<T> calculateDiff(
                List<? extends T> oldList, List<? extends T> newList, boolean detectMoves) {
            return new DiffResultWithList<>(
                    DiffUtil.calculateDiff(new DiffCallback<>(oldList, newList), detectMoves), newList);
        }

        public DiffUtil.DiffResult getDiffResult() {
            return diffResult;
        }

        public List<? extends T> getNewList() {
            return newList;
        }
    }
}
