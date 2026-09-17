package com.example.music;

import android.app.*;
import android.os.*;
import android.text.*;
import android.view.*;
import android.widget.*;
import android.database.Cursor;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.SearchPageBinding;

public class SearchFragment extends Fragment {
    private SearchPageBinding binding;
    private DatabaseHelper db;
    private int mode = 0; // 0 songs, 1 playlists

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = SearchPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        binding.searchEditText.addTextChangedListener(new TextWatcher() {
            public void beforeTextChanged(CharSequence s, int a, int b, int c) {
            }

            public void onTextChanged(CharSequence s, int a, int b, int c) {
                search(s.toString());
            }

            public void afterTextChanged(Editable e) {
            }
        });
        binding.searchFilterButton.setOnClickListener(x -> filterDialog());
        binding.searchButtonHome.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_searchFragment_to_homeFragment));
        binding.searchButtonProfile.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_searchFragment_to_profileFragment));
        search("");
    }

    private void filterDialog() {
        final String[] choices = {"Dziesmas", "Atskaņošanas saraksti"};
        new AlertDialog.Builder(requireContext()).setTitle("Meklēšanas filtrs").setItems(choices, (d, w) -> {
            mode = w;
            binding.searchTitle.setText(w == 0 ? "Songs" : "Playlists");
            if (w == 0) {
                LinearLayout f = SimpleUi.column(requireContext());
                EditText genre = new EditText(requireContext());
                genre.setHint("Žanrs");
                EditText author = new EditText(requireContext());
                author.setHint("Autors");
                f.addView(genre);
                f.addView(author);
                new AlertDialog.Builder(requireContext()).setTitle("Dziesmu kritēriji").setView(f).setPositiveButton("Pielietot", (dd, ww) -> searchSongs(binding.searchEditText.getText().toString(), genre.getText().toString(), author.getText().toString())).setNegativeButton("Atcelt", null).show();
            } else search(binding.searchEditText.getText().toString());
        }).show();
    }

    private void searchSongs(String q, String genre, String author) {
        LinearLayout box = binding.SearchMainContent;
        box.removeAllViews();
        Cursor c = db.searchSongs(q, genre.trim(), author.trim());
        try {
            while (c.moveToNext()) addSong(box, c);
        } finally {
            c.close();
        }
        if (box.getChildCount() == 0)
            box.addView(SimpleUi.text(requireContext(), "Nekas netika atrasts.", 18));
    }

    private void search(String q) {
        LinearLayout box = binding.SearchMainContent;
        box.removeAllViews();
        if (mode == 0) {
            Cursor c = db.searchSongs(q, "", "");
            try {
                while (c.moveToNext()) addSong(box, c);
            } finally {
                c.close();
            }
        } else {
            Cursor c = db.searchPlaylists(q);
            try {
                while (c.moveToNext()) addPlaylist(box, c);
            } finally {
                c.close();
            }
        }
        if (box.getChildCount() == 0)
            box.addView(SimpleUi.text(requireContext(), "Nekas netika atrasts.", 18));
    }

    private void addSong(LinearLayout box, Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        String n = c.getString(c.getColumnIndexOrThrow("name")), a = c.getString(c.getColumnIndexOrThrow("author")), g = c.getString(c.getColumnIndexOrThrow("genre")), len = c.getString(c.getColumnIndexOrThrow("length"));
        LinearLayout row = SimpleUi.row(requireContext());
        TextView t = SimpleUi.text(requireContext(), n + "\n" + a + " • " + g + " • " + len, 17);
        row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Button add = SimpleUi.button(requireContext(), "+");
        row.addView(add, new LinearLayout.LayoutParams(-2, -2));
        add.setOnClickListener(v -> choosePlaylist(id));
        Button play = SimpleUi.button(requireContext(), "▶");
        row.addView(play, new LinearLayout.LayoutParams(-2, -2));
        play.setOnClickListener(v -> playSingle(id, n, len));
        box.addView(row);
    }

    private void addPlaylist(LinearLayout box, Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        String n = c.getString(c.getColumnIndexOrThrow("name")), a = c.getString(c.getColumnIndexOrThrow("author"));
        LinearLayout row = SimpleUi.row(requireContext());
        TextView t = SimpleUi.text(requireContext(), n + "\n@" + a, 18);
        row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Button heart = SimpleUi.button(requireContext(), db.isFavorite((int) Session.id(requireContext()), (int) id) ? "♥" : "♡");
        row.addView(heart, new LinearLayout.LayoutParams(-2, -2));
        heart.setOnClickListener(v -> {
            if (db.isFavorite((int) Session.id(requireContext()), (int) id)) {
                db.removeFavorite((int) Session.id(requireContext()), (int) id);
                heart.setText("♡");
            } else if (db.addFavorite((int) Session.id(requireContext()), (int) id)) {
                heart.setText("♥");
                Toast.makeText(requireContext(), "Pievienots izlasei", Toast.LENGTH_SHORT).show();
            }
        });
        Button open = SimpleUi.button(requireContext(), "Atvērt");
        row.addView(open);
        open.setOnClickListener(v -> {
            AppState.selectedPlaylistId = id;
            Navigation.findNavController(v).navigate(R.id.action_searchFragment_to_playlistFragment2);
        });
        box.addView(row);
    }

    private void choosePlaylist(long songId) {
        Cursor c = db.getOwnPlaylists((int) Session.id(requireContext()));
        java.util.ArrayList<Long> ids = new java.util.ArrayList<>();
        java.util.ArrayList<String> names = new java.util.ArrayList<>();
        try {
            while (c.moveToNext()) {
                ids.add(c.getLong(c.getColumnIndexOrThrow("id")));
                names.add(c.getString(c.getColumnIndexOrThrow("name")));
            }
        } finally {
            c.close();
        }
        if (ids.isEmpty()) {
            Toast.makeText(requireContext(), "Vispirms izveidojiet sarakstu", Toast.LENGTH_SHORT).show();
            return;
        }
        new AlertDialog.Builder(requireContext()).setTitle("Pievienot dziesmu").setItems(names.toArray(new String[0]), (d, w) -> {
            long r = db.addSongToPlaylist(ids.get(w).intValue(), (int) songId);
            Toast.makeText(requireContext(), r > 0 ? "Dziesma pievienota" : "Dziesma jau ir sarakstā", Toast.LENGTH_SHORT).show();
        }).show();
    }

    private void playSingle(long id, String name, String len) {
        TonePlayer.play(requireContext(), new long[]{id}, () -> Toast.makeText(requireContext(), name + " • " + len, Toast.LENGTH_SHORT).show());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
