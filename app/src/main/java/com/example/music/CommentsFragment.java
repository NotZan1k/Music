package com.example.music;

import android.app.*;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.*;

import androidx.annotation.*;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.music.databinding.CommentsPageBinding;

public class CommentsFragment extends Fragment {
    private CommentsPageBinding binding;
    private DatabaseHelper db;
    private long pid;

    @Override
    public View onCreateView(@NonNull LayoutInflater i, ViewGroup c, Bundle b) {
        binding = CommentsPageBinding.inflate(i, c, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle b) {
        super.onViewCreated(v, b);
        db = DatabaseHelper.getInstance(requireContext());
        pid = AppState.selectedPlaylistId;
        binding.commentsTitle.setText("Komentāri");
        binding.commentsButtonHome.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_commentsFragment_to_homeFragment));
        binding.commentsButtonSearch.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_commentsFragment_to_searchFragment));
        binding.commentsButtonProfile.setOnClickListener(x -> Navigation.findNavController(x).navigate(R.id.action_commentsFragment_to_profileFragment));
        binding.writeCommentEditText.setOnEditorActionListener((a, action, e) -> {
            if (binding.writeCommentEditText.getText().toString().trim().isEmpty()) {
                Toast.makeText(requireContext(), "Komentārs nedrīkst būt tukšs", Toast.LENGTH_SHORT).show();
                return true;
            }
            if (db.addComment((int) pid, (int) Session.id(requireContext()), binding.writeCommentEditText.getText().toString()) > 0) {
                binding.writeCommentEditText.setText("");
                load();
            }
            return true;
        });
        load();
    }

    private void load() {
        LinearLayout box = binding.commentsMainContent;
        box.removeAllViews();
        Cursor c = db.getPlaylistComments((int) pid);
        try {
            while (c.moveToNext()) {
                String login = c.getString(c.getColumnIndexOrThrow("login")), text = c.getString(c.getColumnIndexOrThrow("comment"));
                box.addView(SimpleUi.text(requireContext(), "@" + login + "\n" + text, 17));
            }
        } finally {
            c.close();
        }
        if (box.getChildCount() == 0)
            box.addView(SimpleUi.text(requireContext(), "Pagaidām nav komentāru.", 17));
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
