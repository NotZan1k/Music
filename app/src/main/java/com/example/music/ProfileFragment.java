package com.example.music;

import android.app.*;
import android.database.Cursor;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.ProfilePageBinding;

public class ProfileFragment extends Fragment {
    private ProfilePageBinding binding;
    private DatabaseHelper db;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = ProfilePageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        load();
        binding.profileButtonHome.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_profileFragment_to_homeFragment));
        binding.profileButtonSearch.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_profileFragment_to_searchFragment));
        binding.profileStatsButton.setOnClickListener(x -> showStats());
        binding.profileThemeToggle.setOnClickListener(x -> {
            int n = AppCompatDelegate.getDefaultNightMode() == AppCompatDelegate.MODE_NIGHT_YES ? AppCompatDelegate.MODE_NIGHT_NO : AppCompatDelegate.MODE_NIGHT_YES;
            AppCompatDelegate.setDefaultNightMode(n);
        });
    }

    private void load() {
        Cursor u = db.getUserById((int) Session.id(requireContext()));
        try {
            if (u.moveToFirst()) {
                binding.profileUsername.setText("@" + u.getString(u.getColumnIndexOrThrow("login")));
                binding.profileFullName.setText(u.getString(u.getColumnIndexOrThrow("name")) + " " + u.getString(u.getColumnIndexOrThrow("surname")));
            }
        } finally {
            u.close();
        }
        LinearLayout box = (LinearLayout) binding.profileScrollView.getChildAt(0);
        box.removeAllViews();
        Cursor c = db.getUserPlaylists((int) Session.id(requireContext()));
        try {
            while (c.moveToNext()) {
                long id = c.getLong(c.getColumnIndexOrThrow("id"));
                String n = c.getString(c.getColumnIndexOrThrow("name")), a = c.getString(c.getColumnIndexOrThrow("author"));
                Button b = SimpleUi.button(requireContext(), n + "\n@" + a);
                box.addView(b);
                b.setOnClickListener(v -> {
                    AppState.selectedPlaylistId = id;
                    Navigation.findNavController(v).navigate(R.id.action_profileFragment_to_playlistFragment2);
                });
            }
        } finally {
            c.close();
        }
        if (box.getChildCount() == 0)
            box.addView(SimpleUi.text(requireContext(), "Nav sarakstu.", 17));
    }

    private void showStats() {
        Cursor c = db.getTopSongs((int) Session.id(requireContext()));
        StringBuilder s = new StringBuilder("Top 5 visvairāk klausītās dziesmas\n\n");
        int i = 1;
        try {
            while (c.moveToNext())
                s.append(i++).append(". ").append(c.getString(c.getColumnIndexOrThrow("name"))).append(" — ").append(c.getString(c.getColumnIndexOrThrow("author"))).append(" (").append(c.getInt(c.getColumnIndexOrThrow("listening_score"))).append(")\n");
        } finally {
            c.close();
        }
        if (i == 1) s.append("Vēl nav klausīšanās datu.");
        s.append("\nKopā klausīšanās reižu: ").append(db.getUserListeningScore((int) Session.id(requireContext())));
        new AlertDialog.Builder(requireContext()).setTitle("Statistika").setMessage(s).setPositiveButton("Labi", null).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
