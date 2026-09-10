package com.example.music;

import android.database.Cursor;

public interface MusicRepository {
    // Lietotājs
    long registerUser(String login, String password, String name, String surname, String photoPath);
    boolean loginUser(String login, String password);
    Cursor getUserById(int userId);
    boolean updateUserPhoto(int userId, String photoPath);

    // Administrators
    long registerAdmin(String login, String password, String name, String surname);
    boolean loginAdmin(String login, String password);

    // Atskaņošanas saraksts
    long createPlaylist(int userId, String name, String photoPath, String type);
    Cursor getUserPlaylists(int userId);
    Cursor getAllPlaylists();
    boolean updatePlaylistPhoto(int playlistId, String photoPath);

    // Dziesmas
    long addSong(String name, String length, String author, String genre, String photoPath);
    Cursor getAllSongs();
    boolean updateSongPhoto(int songId, String photoPath);

    // Komentāri
    long addComment(int playlistId, int userId, String commentText);
    Cursor getPlaylistComments(int playlistId);

    // Statistika
    long updateListeningScore(int userId, int score);
    int getUserListeningScore(int userId);

    // Bibliotēkas informācija (Satur)
    long addSongToPlaylist(int playlistId, int songId);
    Cursor getPlaylistSongs(int playlistId);
}
