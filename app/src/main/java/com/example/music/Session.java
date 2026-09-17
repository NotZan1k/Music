package com.example.music;

import android.content.Context;
import android.content.SharedPreferences;

public final class Session {
    private static final String PREF = "ara_session";

    private Session() {
    }

    public static void login(Context c, long id, String login, String role) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        p.edit().putLong("id", id).putString("login", login).putString("role", role).apply();
    }

    public static long id(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getLong("id", -1);
    }

    public static String login(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("login", "");
    }

    public static String role(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE).getString("role", "USER");
    }

    public static boolean logged(Context c) {
        return id(c) > 0;
    }

    public static void logout(Context c) {
        c.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit().clear().apply();
    }
}
