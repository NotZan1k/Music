package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.music.databinding.SearchPageBinding;

public class SearchFragment extends Fragment {

    private SearchPageBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = SearchPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.searchButtonHome.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_homeFragment));
        
        binding.searchButtonProfile.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_profileFragment));

        binding.searchPlaylist1.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_playlistFragment2));

        binding.searchPlaylist2.setOnClickListener(v ->
                Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_playlistFragment2));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
