package group.amazcontacts.model;

import android.util.Pair;

import group.amazcontacts.R;

public class AmazTheme {
    public static int BLUE_ACCENT = R.color.blueAccent;
    public static int BANANA_YELLOW = R.color.bananaYellow;
    public static int REPLY_ORANGE = R.color.replyOrange;

    private String name;
    private int color;

    public AmazTheme(String name, int color) {
        this.name = name;
        this.color = color;
    }

    public AmazTheme() {
        this.name = "Default";
        this.color = BLUE_ACCENT;
    }

    @Override
    public String toString() {
        return name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getColor() {
        return color;
    }

    public void setColor(int color) {
        this.color = color;
    }
}
