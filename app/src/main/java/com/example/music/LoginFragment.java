package com.example.music;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.LoginPageBinding;

public class LoginFragment extends Fragment {
    private LoginPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = LoginPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        binding.LoginButton.setOnClickListener(x -> {
            String login = binding.LoginEditTextName.getText().toString().trim(), pass = binding.LoginEditTextPassword.getText().toString();
            if (TextUtils.isEmpty(login) || TextUtils.isEmpty(pass)) {
                msg("Ievadiet lietotājvārdu un paroli");
                return;
            }
            long id = db.login(login, pass, "USER");
            String role = "USER";
            if (id < 0) {
                id = db.login(login, pass, "ADMIN");
                role = "ADMIN";
            }
            if (id > 0) {
                final String finalRole = role;
                Session.login(requireContext(), id, login, finalRole);
                msg("Ieeja ir veiksmīgi pabeigta!", () -> Navigation.findNavController(x).navigate(finalRole.equals("ADMIN") ? R.id.action_loginFragment_to_adminFragment : R.id.action_loginFragment_to_homeFragment));
            } else msg("Nepareizs lietotājvārds vai parole");
        });
        binding.LoginToRegButton.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_loginFragment_to_registrationFragment));
    }

    private void msg(String s) {
        new AlertDialog.Builder(requireContext()).setMessage(s).setPositiveButton("Labi", null).show();
    }

    private void msg(String s, Runnable next) {
        new AlertDialog.Builder(requireContext()).setMessage(s).setPositiveButton("Labi", (d, w) -> next.run()).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
