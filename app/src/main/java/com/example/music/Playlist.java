package com.example.music;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String playlistName, playlistType;
    private byte[] photo;
    private final List<Song> songs = new ArrayList<>();

    public Playlist(String playlistName, String playlistType, byte[] photo) {
        this.playlistName = playlistName;
        this.playlistType = playlistType;
        this.photo = photo;
    }

    public void addSong(Song song) {
        if (song != null) songs.add(song);
    }

    public List<Song> getSongs() {
        return songs;
    }

    public void play() {
    }
}
