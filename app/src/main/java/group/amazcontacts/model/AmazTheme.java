package group.amazcontacts.model;

import android.util.Pair;

import java.util.ArrayList;

import group.amazcontacts.R;

public class AmazTheme {
    public static int BLUE_ACCENT = R.color.blueAccent;
    public static int BANANA_YELLOW = R.color.bananaYellow;
    public static int REPLY_ORANGE = R.color.replyOrange;
    public static int SHRINE_PINK = R.color.shrinePink;
    public static int BASIL_GREEN = R.color.basilGreen;

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

    public static ArrayList<AmazTheme> autoGenerateThemeList() {
        ArrayList<AmazTheme> listThemes = new ArrayList<>();
        AmazTheme s1 = new AmazTheme("Blue Accent", AmazTheme.BLUE_ACCENT);
        AmazTheme s2 = new AmazTheme("Banana Yellow", AmazTheme.BANANA_YELLOW);
        AmazTheme s3 = new AmazTheme("Reply Orange", AmazTheme.REPLY_ORANGE);
        AmazTheme s4 = new AmazTheme("Shrine Pink", AmazTheme.SHRINE_PINK);
        AmazTheme s5 = new AmazTheme("Basil Green", AmazTheme.BASIL_GREEN);
        listThemes.add(s1);
        listThemes.add(s2);
        listThemes.add(s3);
        listThemes.add(s4);
        listThemes.add(s5);
        return listThemes;
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
