package com.example.music;

import android.app.*;
import android.database.Cursor;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.PlaylistDetailPageBinding;

public class PlaylistFragment extends Fragment {
    private PlaylistDetailPageBinding binding;
    private DatabaseHelper db;
    private long pid;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = PlaylistDetailPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        pid = AppState.selectedPlaylistId;
        load();
        binding.playlistButtonHome.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_playlistFragment_to_homeFragment));
        binding.playlistButtonSearch.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_playlistFragment_to_searchFragment));
        binding.playlistButtonProfile.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_playlistFragment_to_profileFragment));
        binding.playlistButtonComment.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_playlistFragment_to_commentsFragment));
        binding.playlistEditTextSearch.addTextChangedListener(new android.text.TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int c, int d) {
            }

            public void onTextChanged(CharSequence s, int a, int c, int d) {
                loadSongs(s.toString());
            }

            public void afterTextChanged(android.text.Editable e) {
            }
        });
    }

    private void load() {
        if (pid < 0) return;
        Cursor p = db.getReadableDatabase().rawQuery("SELECT p.*,u.login author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.id=?", new String[]{String.valueOf(pid)});
        try {
            if (p.moveToFirst()) {
                String name = p.getString(p.getColumnIndexOrThrow("name"));
                binding.playlistName.setText(name);
                binding.playlistAuthor.setText("@" + p.getString(p.getColumnIndexOrThrow("author")));
                String type = p.getString(p.getColumnIndexOrThrow("type"));
                binding.playlistButtonComment.setVisibility("PUBLIC".equals(type) ? View.VISIBLE : View.GONE);
                if (p.getInt(p.getColumnIndexOrThrow("user_id")) == (int) Session.id(requireContext())) {
                    Button edit = SimpleUi.button(requireContext(), "✎ Rediģēt");
                    Button del = SimpleUi.button(requireContext(), "🗑 Dzēst");
                    ((ViewGroup) binding.playlistHeader).addView(edit);
                    ((ViewGroup) binding.playlistHeader).addView(del);
                    edit.setOnClickListener(v -> editPlaylist(name));
                    del.setOnClickListener(v -> new AlertDialog.Builder(requireContext()).setMessage("Dzēst šo sarakstu?").setPositiveButton("Dzēst", (d, w) -> {
                        db.deletePlaylist((int) pid);
                        Navigation.findNavController(v).navigate(R.id.action_playlistFragment_to_homeFragment);
                    }).setNegativeButton("Atcelt", null).show());
                }
            }
        } finally {
            p.close();
        }
        loadSongs("");
    }

    private void editPlaylist(String old) {
        EditText e = new EditText(requireContext());
        e.setText(old);
        e.setHint("Nosaukums");
        new AlertDialog.Builder(requireContext()).setTitle("Rediģēt sarakstu").setView(e).setPositiveButton("Saglabāt", (d, w) -> {
            String n = e.getText().toString().trim();
            if (!n.isEmpty()) {
                db.updatePlaylistName((int) pid, n);
                load();
            }
        }).setNegativeButton("Atcelt", null).show();
    }

    private void loadSongs(String q) {
        LinearLayout box = binding.playlistMainContent;
        box.removeAllViews();
        Cursor c = db.getPlaylistSongs((int) pid);
        try {
            while (c.moveToNext()) {
                String n = c.getString(c.getColumnIndexOrThrow("name"));
                if (!q.isEmpty() && !n.toLowerCase().contains(q.toLowerCase())) continue;
                long sid = c.getLong(c.getColumnIndexOrThrow("id"));
                String a = c.getString(c.getColumnIndexOrThrow("author")), len = c.getString(c.getColumnIndexOrThrow("length"));
                LinearLayout r = SimpleUi.row(requireContext());
                TextView t = SimpleUi.text(requireContext(), n + "\n" + a + " • " + len, 17);
                r.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
                Button play = SimpleUi.button(requireContext(), "▶");
                r.addView(play);
                play.setOnClickListener(v -> TonePlayer.play(requireContext(), new long[]{sid}, null));
                box.addView(r);
            }
        } finally {
            c.close();
        }
        Button playAll = SimpleUi.button(requireContext(), "▶ Atskaņot visu");
        box.addView(playAll, new LinearLayout.LayoutParams(-1, -2));
        playAll.setOnClickListener(v -> playAllSongs());
    }

    private void playAllSongs() {
        Cursor c = db.getPlaylistSongs((int) pid);
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        try {
            while (c.moveToNext()) ids.add(c.getLong(c.getColumnIndexOrThrow("id")));
        } finally {
            c.close();
        }
        long[] a = new long[ids.size()];
        for (int i = 0; i < a.length; i++) a[i] = ids.get(i);
        if (a.length == 0) {
            Toast.makeText(requireContext(), "Sarakstā nav dziesmu", Toast.LENGTH_SHORT).show();
            return;
        }
        TonePlayer.play(requireContext(), a, () -> Toast.makeText(requireContext(), "Atskaņošana pabeigta", Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
