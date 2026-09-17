package com.example.music;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;

import java.security.MessageDigest;
import java.util.Locale;

/**
 * Local SQLite storage for ARA MUSIC.
 */
public class DatabaseHelper extends SQLiteOpenHelper implements MusicRepository {
    private static final String DB_NAME = "ara_music.db";
    private static final int DB_VERSION = 2;
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, surname TEXT NOT NULL, login TEXT UNIQUE NOT NULL, password TEXT NOT NULL, role TEXT NOT NULL DEFAULT 'USER', photo_path TEXT)");
        db.execSQL("CREATE TABLE songs (id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT NOT NULL, length TEXT NOT NULL, author TEXT NOT NULL, genre TEXT NOT NULL, photo_path TEXT)");
        db.execSQL("CREATE TABLE playlists (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER NOT NULL, name TEXT NOT NULL, photo_path TEXT, type TEXT NOT NULL, FOREIGN KEY(user_id) REFERENCES users(id))");
        db.execSQL("CREATE TABLE playlist_songs (playlist_id INTEGER NOT NULL, song_id INTEGER NOT NULL, PRIMARY KEY(playlist_id,song_id), FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE comments (id INTEGER PRIMARY KEY AUTOINCREMENT, playlist_id INTEGER NOT NULL, user_id INTEGER NOT NULL, comment TEXT NOT NULL, created_at INTEGER NOT NULL, FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE, FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE statistics (user_id INTEGER NOT NULL, song_id INTEGER NOT NULL, listening_score INTEGER NOT NULL DEFAULT 0, PRIMARY KEY(user_id,song_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(song_id) REFERENCES songs(id) ON DELETE CASCADE)");
        db.execSQL("CREATE TABLE favorites (user_id INTEGER NOT NULL, playlist_id INTEGER NOT NULL, PRIMARY KEY(user_id,playlist_id), FOREIGN KEY(user_id) REFERENCES users(id) ON DELETE CASCADE, FOREIGN KEY(playlist_id) REFERENCES playlists(id) ON DELETE CASCADE)");
        seed(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("CREATE TABLE IF NOT EXISTS favorites (user_id INTEGER NOT NULL, playlist_id INTEGER NOT NULL, PRIMARY KEY(user_id,playlist_id))");
        }
    }

    private void seed(SQLiteDatabase db) {
        ContentValues admin = user("Admin", "ARA", "admin", "admin123", "ADMIN");
        db.insert("users", null, admin);
        ContentValues demo = user("Demo", "User", "user", "user123", "USER");
        db.insert("users", null, demo);
        long demoId = findUserId(db, "user");
        String[] genres = {"Pop", "Rock", "Hip-Hop", "Jazz", "Electronic", "Classical", "Indie", "R&B", "Metal", "Lo-fi"};
        for (int i = 1; i <= 50; i++) {
            ContentValues s = new ContentValues();
            s.put("name", "Demo Song " + i);
            s.put("length", String.format(Locale.US, "%d:%02d", 2 + (i % 4), (i * 7) % 60));
            s.put("author", "Artist " + ((i - 1) % 10 + 1));
            s.put("genre", genres[(i - 1) % genres.length]);
            db.insert("songs", null, s);
        }
        String[] playlists = {"Morning Mix", "Night Drive", "Study Time", "Rock Energy", "Chill Vibes"};
        for (int i = 0; i < playlists.length; i++) {
            ContentValues p = new ContentValues();
            p.put("user_id", demoId);
            p.put("name", playlists[i]);
            p.put("type", "PUBLIC");
            long pid = db.insert("playlists", null, p);
            db.execSQL("UPDATE playlists SET type='PUBLIC' WHERE id=?", new Object[]{pid});
            for (int j = 1; j <= 5; j++) {
                long songId = (i * 5) + j;
                ContentValues ps = new ContentValues();
                ps.put("playlist_id", pid);
                ps.put("song_id", songId);
                db.insert("playlist_songs", null, ps);
            }
        }
    }

    private ContentValues user(String name, String surname, String login, String password, String role) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("surname", surname);
        v.put("login", login);
        v.put("password", hash(password));
        v.put("role", role);
        return v;
    }

    private long findUserId(SQLiteDatabase db, String login) {
        try (Cursor c = db.rawQuery("SELECT id FROM users WHERE login=?", new String[]{login})) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }

    private static String hash(String value) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] b = md.digest(value.getBytes("UTF-8"));
            StringBuilder s = new StringBuilder();
            for (byte x : b) s.append(String.format("%02x", x));
            return s.toString();
        } catch (Exception e) {
            return value;
        }
    }

    @Override
    public long registerUser(String login, String password, String name, String surname, String photoPath) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password) || TextUtils.isEmpty(name) || TextUtils.isEmpty(surname))
            return -1;
        ContentValues v = user(name, surname, login, password, "USER");
        if (photoPath != null) v.put("photo_path", photoPath);
        try {
            return getWritableDatabase().insertOrThrow("users", null, v);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean loginUser(String login, String password) {
        return login(login, password, "USER") > 0;
    }

    public long login(String login, String password, String role) {
        if (TextUtils.isEmpty(login) || TextUtils.isEmpty(password)) return -1;
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id FROM users WHERE login=? AND password=? AND role=?", new String[]{login, hash(password), role})) {
            return c.moveToFirst() ? c.getLong(0) : -1;
        }
    }

    public String getRole(long id) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT role FROM users WHERE id=?", new String[]{String.valueOf(id)})) {
            return c.moveToFirst() ? c.getString(0) : "USER";
        }
    }

    @Override
    public Cursor getUserById(int userId) {
        return getReadableDatabase().rawQuery("SELECT * FROM users WHERE id=?", new String[]{String.valueOf(userId)});
    }

    @Override
    public boolean updateUserPhoto(int userId, String path) {
        ContentValues v = new ContentValues();
        v.put("photo_path", path);
        return getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(userId)}) > 0;
    }

    @Override
    public long registerAdmin(String login, String password, String name, String surname) {
        ContentValues v = user(name, surname, login, password, "ADMIN");
        try {
            return getWritableDatabase().insertOrThrow("users", null, v);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public boolean loginAdmin(String login, String password) {
        return login(login, password, "ADMIN") > 0;
    }

    @Override
    public long createPlaylist(int userId, String name, String photoPath, String type) {
        if (TextUtils.isEmpty(name)) return -1;
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("name", name);
        v.put("type", type == null ? "PRIVATE" : type);
        if (photoPath != null) v.put("photo_path", photoPath);
        return getWritableDatabase().insert("playlists", null, v);
    }

    public long createPlaylist(int userId, String name, String type) {
        return createPlaylist(userId, name, null, type);
    }

    @Override
    public Cursor getUserPlaylists(int userId) {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.user_id=? OR p.id IN (SELECT playlist_id FROM favorites WHERE user_id=?) ORDER BY p.id DESC", new String[]{String.valueOf(userId), String.valueOf(userId)});
    }

    public Cursor getOwnPlaylists(int userId) {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.user_id=? ORDER BY p.id DESC", new String[]{String.valueOf(userId)});
    }

    public Cursor getFavoritePlaylists(int userId) {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id JOIN favorites f ON f.playlist_id=p.id WHERE f.user_id=? ORDER BY p.id DESC", new String[]{String.valueOf(userId)});
    }

    @Override
    public Cursor getAllPlaylists() {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.type='PUBLIC' ORDER BY p.id DESC", null);
    }

    public Cursor searchPlaylists(String q) {
        String x = "%" + (q == null ? "" : q) + "%";
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.type='PUBLIC' AND (p.name LIKE ? OR u.login LIKE ?) ORDER BY p.id DESC", new String[]{x, x});
    }

    public Cursor getPendingPlaylists() {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id WHERE p.type='PENDING' ORDER BY p.id", null);
    }

    public boolean setPlaylistStatus(int id, String status) {
        ContentValues v = new ContentValues();
        v.put("type", status);
        return getWritableDatabase().update("playlists", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deletePlaylist(int id) {
        return getWritableDatabase().delete("playlists", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    @Override
    public boolean updatePlaylistPhoto(int playlistId, String photoPath) {
        ContentValues v = new ContentValues();
        v.put("photo_path", photoPath);
        return getWritableDatabase().update("playlists", v, "id=?", new String[]{String.valueOf(playlistId)}) > 0;
    }

    public boolean updatePlaylistName(int id, String name) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        return getWritableDatabase().update("playlists", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    @Override
    public long addSong(String name, String length, String author, String genre, String photoPath) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("length", length);
        v.put("author", author);
        v.put("genre", genre);
        if (photoPath != null) v.put("photo_path", photoPath);
        return getWritableDatabase().insert("songs", null, v);
    }

    @Override
    public Cursor getAllSongs() {
        return getReadableDatabase().rawQuery("SELECT * FROM songs ORDER BY name", null);
    }

    public Cursor searchSongs(String q, String genre, String author) {
        String x = "%" + (q == null ? "" : q) + "%";
        String g = genre == null ? "" : genre;
        String a = author == null ? "" : author;
        return getReadableDatabase().rawQuery("SELECT * FROM songs WHERE name LIKE ? AND (?='' OR genre=?) AND (?='' OR author=?) ORDER BY name", new String[]{x, g, g, a, a});
    }

    @Override
    public boolean updateSongPhoto(int songId, String photoPath) {
        ContentValues v = new ContentValues();
        v.put("photo_path", photoPath);
        return getWritableDatabase().update("songs", v, "id=?", new String[]{String.valueOf(songId)}) > 0;
    }

    public boolean updateSong(int id, String name, String length, String author, String genre) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("length", length);
        v.put("author", author);
        v.put("genre", genre);
        return getWritableDatabase().update("songs", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteSong(int id) {
        return getWritableDatabase().delete("songs", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean songExists(String name, String author) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT id FROM songs WHERE name=? AND author=?", new String[]{name, author})) {
            return c.moveToFirst();
        }
    }

    @Override
    public long addComment(int playlistId, int userId, String text) {
        if (TextUtils.isEmpty(text) || text.trim().isEmpty()) return -1;
        ContentValues v = new ContentValues();
        v.put("playlist_id", playlistId);
        v.put("user_id", userId);
        v.put("comment", text.trim());
        v.put("created_at", System.currentTimeMillis());
        return getWritableDatabase().insert("comments", null, v);
    }

    @Override
    public Cursor getPlaylistComments(int playlistId) {
        return getReadableDatabase().rawQuery("SELECT c.*,u.login FROM comments c JOIN users u ON u.id=c.user_id WHERE c.playlist_id=? ORDER BY c.created_at DESC", new String[]{String.valueOf(playlistId)});
    }

    public boolean deleteComment(int id) {
        return getWritableDatabase().delete("comments", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    @Override
    public long updateListeningScore(int userId, int score) {
        return -1;
    }

    public long incrementListeningScore(int userId, int songId) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("INSERT INTO statistics(user_id,song_id,listening_score) VALUES(?,?,1) ON CONFLICT(user_id,song_id) DO UPDATE SET listening_score=listening_score+1", new Object[]{userId, songId});
        return 1;
    }

    @Override
    public int getUserListeningScore(int userId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT COALESCE(SUM(listening_score),0) FROM statistics WHERE user_id=?", new String[]{String.valueOf(userId)})) {
            return c.moveToFirst() ? c.getInt(0) : 0;
        }
    }

    public Cursor getTopSongs(int userId) {
        return getReadableDatabase().rawQuery("SELECT s.name,s.author,s.genre,st.listening_score FROM statistics st JOIN songs s ON s.id=st.song_id WHERE st.user_id=? ORDER BY st.listening_score DESC,s.name LIMIT 5", new String[]{String.valueOf(userId)});
    }

    public Cursor getStats(int userId) {
        return getReadableDatabase().rawQuery("SELECT s.name,s.author,s.genre,st.listening_score FROM statistics st JOIN songs s ON s.id=st.song_id WHERE st.user_id=? ORDER BY st.listening_score DESC", new String[]{String.valueOf(userId)});
    }

    @Override
    public long addSongToPlaylist(int playlistId, int songId) {
        ContentValues v = new ContentValues();
        v.put("playlist_id", playlistId);
        v.put("song_id", songId);
        try {
            return getWritableDatabase().insertOrThrow("playlist_songs", null, v);
        } catch (Exception e) {
            return -1;
        }
    }

    @Override
    public Cursor getPlaylistSongs(int playlistId) {
        return getReadableDatabase().rawQuery("SELECT s.* FROM songs s JOIN playlist_songs ps ON ps.song_id=s.id WHERE ps.playlist_id=? ORDER BY ps.rowid", new String[]{String.valueOf(playlistId)});
    }

    public boolean isFavorite(int userId, int playlistId) {
        try (Cursor c = getReadableDatabase().rawQuery("SELECT 1 FROM favorites WHERE user_id=? AND playlist_id=?", new String[]{String.valueOf(userId), String.valueOf(playlistId)})) {
            return c.moveToFirst();
        }
    }

    public boolean addFavorite(int userId, int playlistId) {
        ContentValues v = new ContentValues();
        v.put("user_id", userId);
        v.put("playlist_id", playlistId);
        try {
            return getWritableDatabase().insertOrThrow("favorites", null, v) > 0;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean removeFavorite(int userId, int playlistId) {
        return getWritableDatabase().delete("favorites", "user_id=? AND playlist_id=?", new String[]{String.valueOf(userId), String.valueOf(playlistId)}) > 0;
    }

    public Cursor getUsers() {
        return getReadableDatabase().rawQuery("SELECT id,name,surname,login,role FROM users ORDER BY login", null);
    }

    public boolean updateUser(long id, String name, String surname, String password, String role) {
        ContentValues v = new ContentValues();
        v.put("name", name);
        v.put("surname", surname);
        if (password != null && !password.isEmpty()) v.put("password", hash(password));
        v.put("role", role);
        return getWritableDatabase().update("users", v, "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deleteUser(long id) {
        return getWritableDatabase().delete("users", "id=?", new String[]{String.valueOf(id)}) > 0;
    }

    public Cursor getAllSongsAdmin() {
        return getAllSongs();
    }

    public Cursor getAllPlaylistsAdmin() {
        return getReadableDatabase().rawQuery("SELECT p.*,u.login AS author FROM playlists p JOIN users u ON u.id=p.user_id ORDER BY p.id DESC", null);
    }
}
