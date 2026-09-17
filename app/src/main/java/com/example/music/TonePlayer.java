package com.example.music;

import android.content.Context;
import android.media.AudioManager;
import android.media.ToneGenerator;
import android.os.*;

import java.util.*;

public final class TonePlayer {
    private TonePlayer() {
    }

    public static void play(Context c, long[] songIds, Runnable done) {
        DatabaseHelper db = DatabaseHelper.getInstance(c);
        final int[] i = {0};
        Handler h = new Handler(Looper.getMainLooper());
        Runnable r = new Runnable() {
            public void run() {
                if (i[0] >= songIds.length) {
                    if (done != null) done.run();
                    return;
                }
                long sid = songIds[i[0]++];
                int uid = (int) Session.id(c);
                db.incrementListeningScore(uid, (int) sid);
                ToneGenerator tg = new ToneGenerator(AudioManager.STREAM_MUSIC, 80);
                tg.startTone(ToneGenerator.TONE_PROP_BEEP2, 900);
                h.postDelayed(() -> {
                    tg.release();
                    run();
                }, 1000);
            }
        };
        r.run();
    }
}
