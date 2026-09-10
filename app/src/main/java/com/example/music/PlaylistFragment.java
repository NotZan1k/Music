package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.music.databinding.PlaylistDetailPageBinding;

public class PlaylistFragment extends Fragment {

    private PlaylistDetailPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = PlaylistDetailPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.playlistButtonHome.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_playlistFragment_to_homeFragment)
        );

        binding.playlistButtonSearch.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_playlistFragment_to_searchFragment)
        );

        binding.playlistButtonProfile.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_playlistFragment_to_profileFragment)
        );
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
