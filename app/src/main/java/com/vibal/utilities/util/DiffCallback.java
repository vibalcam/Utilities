package com.vibal.utilities.util;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.DiffUtil;

import java.util.List;

public class DiffCallback<T extends DiffDbUsable<T>> extends DiffUtil.Callback {
    private List<T> oldList;
    private List<T> newList;

    public DiffCallback(List<T> oldList, List<T> newList) {
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
        private DiffUtil.DiffResult diffResult;
        private List<T> newList;

        public DiffResultWithList(DiffUtil.DiffResult diffResult, List<T> newList) {
            this.diffResult = diffResult;
            this.newList = newList;
        }

        public static <T extends DiffDbUsable<T>> DiffResultWithList<T> calculateDiff(
                List<T> oldList, List<T> newList, boolean detectMoves) {
            return new DiffResultWithList<>(
                    DiffUtil.calculateDiff(new DiffCallback<>(oldList, newList), detectMoves), newList);
        }

        public DiffUtil.DiffResult getDiffResult() {
            return diffResult;
        }

        public List<T> getNewList() {
            return newList;
        }
    }
}
