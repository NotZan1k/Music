package com.example.music;

import android.content.Context;
import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class SimpleUi {
    private SimpleUi() {
    }

    public static TextView text(Context c, String s, float size) {
        TextView v = new TextView(c);
        v.setText(s);
        v.setTextSize(size);
        v.setTextColor(Color.DKGRAY);
        v.setPadding(12, 12, 12, 12);
        return v;
    }

    public static Button button(Context c, String s) {
        Button b = new Button(c);
        b.setText(s);
        b.setAllCaps(false);
        return b;
    }

    public static LinearLayout row(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setGravity(Gravity.CENTER_VERTICAL);
        l.setPadding(8, 8, 8, 8);
        return l;
    }

    public static LinearLayout column(Context c) {
        LinearLayout l = new LinearLayout(c);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setPadding(16, 8, 16, 8);
        return l;
    }

    public static void add(LinearLayout parent, View child) {
        parent.addView(child, new LinearLayout.LayoutParams(-1, -2));
    }
}
