package com.vibal.utilities.ui.bindingHolder;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.viewbinding.ViewBinding;

import java.lang.reflect.InvocationTargetException;

public abstract class ViewBindingFragment<V extends ViewBinding> extends Fragment {
    private V binding;

    public V getBinding() {
        return binding;
    }

    protected abstract Class<V> getBindingClass();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        try {
            binding = (V) getBindingClass().getMethod("inflate", LayoutInflater.class,
                    ViewGroup.class, boolean.class)
                    .invoke(null, inflater, container, false);
            return binding.getRoot();
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException("Not able to create binding class");
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
