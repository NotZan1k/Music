package com.example.music;

import android.os.Bundle;
import android.view.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;

import com.example.music.databinding.HelpPageBinding;

public class HelpFragment extends Fragment {
    private HelpPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = HelpPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
