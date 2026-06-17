package com.example.music;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import com.example.music.databinding.LoginPageBinding;

public class LoginFragment extends Fragment {

    private LoginPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = LoginPageBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.LoginButton.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_homeFragment));

        binding.LoginToRegButton.setOnClickListener(v ->
            Navigation.findNavController(v).navigate(R.id.action_loginFragment_to_registrationFragment));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
