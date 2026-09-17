package com.example.music;

public class Song {
    private String songName, songGenre, songAuthor;
    private int songLength;

    public Song(String songName, String songGenre, String songAuthor, int songLength) {
        this.songName = songName;
        this.songGenre = songGenre;
        this.songAuthor = songAuthor;
        this.songLength = songLength;
    }

    public void playSong() {
    }

    public void increaseListeningScore() {
    }

    public String getSongName() {
        return songName;
    }
}
