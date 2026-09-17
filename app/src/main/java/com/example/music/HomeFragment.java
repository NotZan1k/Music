package com.example.music;

import android.app.*;
import android.database.Cursor;
import android.content.*;
import android.graphics.Color;
import android.os.*;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.net.Uri;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.HomePageBinding;

public class HomeFragment extends Fragment {
    private HomePageBinding binding;
    private DatabaseHelper db;
    private Uri selectedCover;
    private ActivityResultLauncher<String[]> picker;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = HomePageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        picker = registerForActivityResult(new ActivityResultContracts.OpenDocument(), uri -> {
            selectedCover = uri;
            if (uri != null)
                Toast.makeText(requireContext(), "Vāka attēls izvēlēts", Toast.LENGTH_SHORT).show();
        });
        binding.homeLogSearchButton.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_homeFragment_to_searchFragment));
        binding.homeLogAddButton.setOnClickListener(x -> createPlaylist());
        binding.homeLogFilterButton.setOnClickListener(x -> new AlertDialog.Builder(requireContext()).setTitle("Kārtot").setItems(new String[]{"Jaunākie", "Pēc nosaukuma"}, (d, w) -> loadPlaylists(w == 1)).show());
        binding.homeLogButtonSearch.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_homeFragment_to_searchFragment));
        binding.homeLogButtonProfile.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_homeFragment_to_profileFragment));
        loadPlaylists(false);
    }

    private void loadPlaylists(boolean byName) {
        LinearLayout box = binding.HomeMainContent;
        box.removeAllViews();
        Cursor c = byName ? db.getReadableDatabase().rawQuery("SELECT p.*,u.login author FROM playlists p JOIN users u ON u.id=p.user_id WHERE (p.user_id=? OR p.id IN (SELECT playlist_id FROM favorites WHERE user_id=?)) ORDER BY p.name", new String[]{String.valueOf(Session.id(requireContext())), String.valueOf(Session.id(requireContext()))}) : db.getUserPlaylists((int) Session.id(requireContext()));
        try {
            while (c.moveToNext()) addPlaylist(box, c);
        } finally {
            c.close();
        }
        if (box.getChildCount() == 0)
            box.addView(SimpleUi.text(requireContext(), "Jums vēl nav atskaņošanas sarakstu. Spiediet +, lai izveidotu.", 18));
    }

    private void addPlaylist(LinearLayout box, Cursor c) {
        long id = c.getLong(c.getColumnIndexOrThrow("id"));
        String name = c.getString(c.getColumnIndexOrThrow("name"));
        String author = c.getString(c.getColumnIndexOrThrow("author"));
        String type = c.getString(c.getColumnIndexOrThrow("type"));
        LinearLayout row = SimpleUi.row(requireContext());
        TextView t = SimpleUi.text(requireContext(), name + "\n@" + author + "  •  " + type, 18);
        row.addView(t, new LinearLayout.LayoutParams(0, -2, 1));
        Button open = SimpleUi.button(requireContext(), "Atvērt");
        row.addView(open, new LinearLayout.LayoutParams(-2, -2));
        open.setOnClickListener(v -> {
            AppState.selectedPlaylistId = id;
            Navigation.findNavController(v).navigate(R.id.action_homeFragment_to_playlistFragment2);
        });
        box.addView(row);
    }

    private void createPlaylist() {
        selectedCover = null;
        LinearLayout form = SimpleUi.column(requireContext());
        EditText name = new EditText(requireContext());
        name.setHint("Saraksta nosaukums");
        form.addView(name);
        CheckBox pub = new CheckBox(requireContext());
        pub.setText("Publicēt");
        form.addView(pub);
        Button cover = SimpleUi.button(requireContext(), "Izvēlēties vāka fotoattēlu");
        form.addView(cover);
        cover.setOnClickListener(v -> picker.launch(new String[]{"image/*"}));
        new AlertDialog.Builder(requireContext()).setTitle("Jauns atskaņošanas saraksts").setView(form).setPositiveButton("Saglabāt", (d, w) -> {
            String n = name.getText().toString().trim();
            if (n.isEmpty()) {
                msg("Ievadiet saraksta nosaukumu");
                return;
            }
            String type = pub.isChecked() ? "PENDING" : "PRIVATE";
            long id = db.createPlaylist((int) Session.id(requireContext()), n, selectedCover == null ? null : selectedCover.toString(), type);
            if (id > 0) {
                AppState.selectedPlaylistId = id;
                loadPlaylists(false);
                msg(pub.isChecked() ? "Saraksts saglabāts un gaida administratora apstiprinājumu" : "Atskaņošanas saraksts izveidots");
            } else msg("Neizdevās izveidot sarakstu");
        }).setNegativeButton("Atcelt", null).show();
    }

    private void msg(String s) {
        new AlertDialog.Builder(requireContext()).setMessage(s).setPositiveButton("Labi", null).show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (binding != null && db != null) loadPlaylists(false);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
