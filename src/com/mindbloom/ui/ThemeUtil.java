package com.mindbloom.ui;

import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.MenuBar;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;


public class ThemeUtil {
    
    public static final Color PRIMARY = Color.web("#8fd3c7");
    public static final Color ACCENT = Color.web("#ffd166");
    public static final Color CARD_BG = Color.web("rgba(255,255,255,0.9)");
    public static final Color BG_START = Color.web("#fbfdff");
    public static final Color BG_END = Color.web("#f2f8f7");
    public static final Color MUTED = Color.web("#6c6f75");

    public static void applyTheme(Scene scene) {
        if (scene == null) return;
        Region root = (Region) scene.getRoot();
        
        root.setBackground(new Background(new BackgroundFill(
                new javafx.scene.paint.LinearGradient(0,0,0,1,true, javafx.scene.paint.CycleMethod.NO_CYCLE,
                        new javafx.scene.paint.Stop(0, BG_START), new javafx.scene.paint.Stop(1, BG_END)),
                CornerRadii.EMPTY, Insets.EMPTY)));
        
        try {
            root.setStyle("-fx-font-family: 'Segoe UI', system; -fx-font-size: 14px; -fx-text-fill: #243b3a;");
        } catch (Exception ignored) {}
    }

    public static void styleCard(Region node) {
        if (node == null) return;
        node.setBackground(new Background(new BackgroundFill(CARD_BG, new CornerRadii(10), Insets.EMPTY)));
        node.setPadding(new Insets(12));
        node.setEffect(new DropShadow(8, Color.rgb(21,28,41,0.06)));
    }

    public static void styleButton(Button b) {
        if (b == null) return;
        b.setStyle("-fx-background-color: linear-gradient(to bottom, #ffffff, " + toRgbString(ACCENT) + "); -fx-background-radius:8; -fx-text-fill:#143642; -fx-font-weight:600;");
        b.setPadding(new Insets(6,10,6,10));
    }

    public static void styleMenuBar(MenuBar mb) {
        if (mb == null) return;
        mb.setBackground(Background.EMPTY);
        mb.setPadding(new Insets(6,10,6,10));
    }

    public static void styleStatusBar(Region bar) {
        if (bar == null) return;
        bar.setBackground(Background.EMPTY);
        bar.setPadding(new Insets(8));
    }

    public static void styleTitle(Label lbl) {
        if (lbl == null) return;
        lbl.setStyle("-fx-font-size:18px; -fx-font-weight:bold; -fx-text-fill:#143642;");
    }

    private static String toRgbString(Color c) {
        int r = (int) Math.round(c.getRed() * 255);
        int g = (int) Math.round(c.getGreen() * 255);
        int b = (int) Math.round(c.getBlue() * 255);
        return String.format("rgb(%d,%d,%d)", r, g, b);
    }
}
