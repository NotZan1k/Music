package com.example.music;

public class Database {
    private final DatabaseHelper helper;

    public Database(DatabaseHelper helper) {
        this.helper = helper;
    }

    public boolean login(String databaseName, String user, String password) {
        return helper.login(user, password, "USER") > 0;
    }

    public long saveUser(User user) {
        return -1;
    }

    public long savePlaylist(Playlist playlist) {
        return -1;
    }

    public long saveSong(Song song) {
        return -1;
    }

    public long saveComment(Comment comment) {
        return -1;
    }
}
