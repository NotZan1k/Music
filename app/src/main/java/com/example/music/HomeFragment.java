package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.music.databinding.HomePageBinding;

public class HomeFragment extends Fragment {

    private HomePageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = HomePageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.homeLogButtonSearch.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_searchFragment));
        
        binding.homeLogButtonProfile.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_profileFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
