package com.mindbloom.ui;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Popup;
import javafx.stage.Window;
import javafx.util.Duration;


public class ToastUtil {
    public static void show(Window owner, String message) {
        show(owner, message, 2200);
    }

    public static void show(Window owner, String message, int millis) {
        if (owner == null) return;
        Platform.runLater(() -> {
            Popup popup = new Popup();
            popup.setAutoFix(true);
            popup.setAutoHide(true);
            popup.setHideOnEscape(true);

            Label lbl = new Label(message);
            lbl.setStyle("-fx-background-color: rgba(30,30,30,0.86); -fx-text-fill: white; -fx-padding: 10 14 10 14; -fx-background-radius: 8;");
            lbl.setFont(Font.font(13));

            StackPane content = new StackPane(lbl);
            content.setPadding(new Insets(6));
            content.setStyle("-fx-background-radius: 8;");

            popup.getContent().add(content);

            
            double x = owner.getX() + owner.getWidth() - 320;
            double y = owner.getY() + owner.getHeight() - 100;
            popup.show(owner, x, y);

            Timeline t = new Timeline(new KeyFrame(Duration.millis(millis), ev -> {
                try { popup.hide(); } catch (Exception ignored) {}
            }));
            t.play();
        });
    }

    public static void show(Scene scene, String message) {
        if (scene == null) return;
        show(scene.getWindow(), message);
    }
}
