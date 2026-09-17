package com.example.music;

public class User {
    private String name, surname, login, password;

    public User(String name, String surname, String login, String password) {
        this.name = name;
        this.surname = surname;
        this.login = login;
        this.password = password;
    }

    public boolean login(String login, String password) {
        return this.login.equals(login) && this.password.equals(password);
    }

    public String getLogin() {
        return login;
    }
}
