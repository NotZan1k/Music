package com.example.music;

public class Admin extends User {
    public Admin(String name, String surname, String login, String password) {
        super(name, surname, login, password);
    }

    public boolean confirmPlaylist(Playlist playlist) {
        return playlist != null;
    }
}
