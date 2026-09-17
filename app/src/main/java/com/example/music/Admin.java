package com.example.music;

import android.app.*;
import android.database.Cursor;
import android.os.*;
import android.text.InputType;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.AdminPageBinding;

public class AdminFragment extends Fragment {
    private AdminPageBinding binding;
    private DatabaseHelper db;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = AdminPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        binding.adminUsersButton.setOnClickListener(x -> users());
        binding.adminSongsButton.setOnClickListener(x -> songs());
        binding.adminPendingButton.setOnClickListener(x -> pending());
        binding.adminAddSongButton.setOnClickListener(x -> songDialog(-1, null, null, null, null));
        binding.adminLogoutButton.setOnClickListener(x -> {
            Session.logout(requireContext());
            Navigation.findNavController(x).navigate(R.id.action_adminFragment_to_loginFragment);
        });
        songs();
    }

    private void clear() {
        binding.adminList.removeAllViews();
    }

    private void users() {
        clear();
        Cursor c = db.getUsers();
        try {
            while (c.moveToNext()) {
                long id = c.getLong(0);
                String text = c.getString(1) + " " + c.getString(2) + "\n@" + c.getString(3) + " • " + c.getString(4);
                LinearLayout r = SimpleUi.row(requireContext());
                r.addView(SimpleUi.text(requireContext(), text, 16), new LinearLayout.LayoutParams(0, -2, 1));
                Button edit = SimpleUi.button(requireContext(), "Edit");
                r.addView(edit);
                edit.setOnClickListener(v -> userDialog(id, cached(text)));
                Button del = SimpleUi.button(requireContext(), "Delete");
                r.addView(del);
                del.setOnClickListener(v -> {
                    db.deleteUser(id);
                    users();
                });
                binding.adminList.addView(r);
            }
        } finally {
            c.close();
        }
    }

    private String[] cached(String text) {
        String[] lines = text.split("\\n");
        String full = lines[0];
        String login = lines.length > 1 ? lines[1].replace("@", "").split(" •")[0] : "";
        String role = lines.length > 1 && lines[1].contains("ADMIN") ? "ADMIN" : "USER";
        String[] n = full.split(" ", 2);
        return new String[]{n.length > 0 ? n[0] : "", n.length > 1 ? n[1] : "", login, role};
    }

    private void userDialog(long id, String[] data) {
        LinearLayout f = SimpleUi.column(requireContext());
        EditText n = field("Vārds", data[0]);
        EditText s = field("Uzvārds", data[1]);
        EditText p = field("Jauna parole", "");
        EditText r = field("Loma (USER/ADMIN)", data[3]);
        f.addView(n);
        f.addView(s);
        f.addView(p);
        f.addView(r);
        new AlertDialog.Builder(requireContext()).setTitle("Lietotāja dati").setView(f).setPositiveButton("Save", (d, w) -> {
            db.updateUser(id, n.getText().toString(), s.getText().toString(), p.getText().toString(), r.getText().toString().toUpperCase());
            users();
        }).setNegativeButton("Cancel", null).show();
    }

    private void songs() {
        clear();
        Cursor c = db.getAllSongsAdmin();
        try {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String n = c.getString(c.getColumnIndexOrThrow("name")), a = c.getString(c.getColumnIndexOrThrow("author")), g = c.getString(c.getColumnIndexOrThrow("genre")), l = c.getString(c.getColumnIndexOrThrow("length"));
                LinearLayout r = SimpleUi.row(requireContext());
                r.addView(SimpleUi.text(requireContext(), n + " — " + a + " • " + g + " • " + l, 15), new LinearLayout.LayoutParams(0, -2, 1));
                Button e = SimpleUi.button(requireContext(), "Edit");
                r.addView(e);
                e.setOnClickListener(v -> songDialog(id, n, l, a, g));
                Button d = SimpleUi.button(requireContext(), "Delete");
                r.addView(d);
                d.setOnClickListener(v -> {
                    db.deleteSong(id);
                    songs();
                });
                binding.adminList.addView(r);
            }
        } finally {
            c.close();
        }
    }

    private void songDialog(int id, String oldName, String oldLen, String oldAuthor, String oldGenre) {
        LinearLayout f = SimpleUi.column(requireContext());
        EditText n = field("Nosaukums", oldName == null ? "" : oldName), a = field("Autors", oldAuthor == null ? "" : oldAuthor), g = field("Žanrs", oldGenre == null ? "" : oldGenre), l = field("Garums", oldLen == null ? "" : oldLen);
        f.addView(n);
        f.addView(a);
        f.addView(g);
        f.addView(l);
        new AlertDialog.Builder(requireContext()).setTitle(id < 0 ? "Pievienot dziesmu" : "Rediģēt dziesmu").setView(f).setPositiveButton("Save", (d, w) -> {
            String nn = n.getText().toString().trim(), aa = a.getText().toString().trim();
            if (nn.isEmpty() || aa.isEmpty()) {
                toast("Aizpildiet laukus");
                return;
            }
            if (id < 0) {
                if (db.songExists(nn, aa)) {
                    toast("Šāda dziesma jau pastāv");
                    return;
                }
                db.addSong(nn, l.getText().toString().trim(), aa, g.getText().toString().trim(), null);
                toast("Dziesma veiksmīgi pievienota bibliotēkai");
            } else
                db.updateSong(id, nn, l.getText().toString().trim(), aa, g.getText().toString().trim());
            songs();
        }).setNegativeButton("Cancel", null).show();
    }

    private void pending() {
        clear();
        Cursor c = db.getPendingPlaylists();
        try {
            while (c.moveToNext()) {
                int id = c.getInt(c.getColumnIndexOrThrow("id"));
                String n = c.getString(c.getColumnIndexOrThrow("name")), a = c.getString(c.getColumnIndexOrThrow("author"));
                LinearLayout r = SimpleUi.row(requireContext());
                r.addView(SimpleUi.text(requireContext(), n + "\n@" + a, 16), new LinearLayout.LayoutParams(0, -2, 1));
                Button ok = SimpleUi.button(requireContext(), "Confirm");
                r.addView(ok);
                ok.setOnClickListener(v -> {
                    db.setPlaylistStatus(id, "PUBLIC");
                    pending();
                });
                Button no = SimpleUi.button(requireContext(), "Reject");
                r.addView(no);
                no.setOnClickListener(v -> {
                    db.setPlaylistStatus(id, "PRIVATE");
                    pending();
                });
                binding.adminList.addView(r);
            }
        } finally {
            c.close();
        }
    }

    private EditText field(String hint, String value) {
        EditText e = new EditText(requireContext());
        e.setHint(hint);
        e.setText(value);
        return e;
    }

    private void toast(String s) {
        Toast.makeText(requireContext(), s, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
