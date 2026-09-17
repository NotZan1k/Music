package com.example.music;

import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.RegistrationPageBinding;

public class RegistrationFragment extends Fragment {
    private RegistrationPageBinding binding;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = RegistrationPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        binding.RegButton.setOnClickListener(x -> {
            String n = binding.RegEditTextName.getText().toString().trim(), s = binding.RegEditTextSurname.getText().toString().trim(), u = binding.RegEditTextUsername.getText().toString().trim(), p = binding.RegEditTextPassword1.getText().toString(), p2 = binding.RegEditTextPassword2.getText().toString();
            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(s) || TextUtils.isEmpty(u) || TextUtils.isEmpty(p)) {
                msg("Aizpildiet visus laukus");
                return;
            }
            if (!p.equals(p2)) {
                msg("Paroles nesakrīt");
                return;
            }
            long id = DatabaseHelper.getInstance(requireContext()).registerUser(u, p, n, s, null);
            if (id > 0)
                msg("Reģistrēšana ir veiksmīgi pabeigta!", () -> Navigation.findNavController(x).navigate(R.id.action_registrationFragment_to_loginFragment));
            else msg("Šāds lietotājvārds jau pastāv");
        });
    }

    private void msg(String s) {
        new AlertDialog.Builder(requireContext()).setMessage(s).setPositiveButton("Labi", null).show();
    }

    private void msg(String s, Runnable r) {
        new AlertDialog.Builder(requireContext()).setMessage(s).setPositiveButton("Labi", (d, w) -> r.run()).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
