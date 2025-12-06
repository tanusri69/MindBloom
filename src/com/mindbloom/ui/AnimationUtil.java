package com.mindbloom.ui;

import javafx.animation.*;
import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.input.MouseEvent;
import javafx.util.Duration;


public class AnimationUtil {
    private static volatile boolean enabled = true;

    public static void setEnabled(boolean on) {
        enabled = on;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    public static void fadeIn(Node node) {
        if (!enabled) return;
        fadeIn(node, 600);
    }

    public static void fadeIn(Node node, double millis) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.play();
        });
    }

    public static void slideInFromLeft(Node node) {
        if (!enabled) return;
        slideInFromLeft(node, 300, 400);
    }

    public static void slideInFromLeft(Node node, double distance, double millis) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.setTranslateX(-distance);
            TranslateTransition tt = new TranslateTransition(Duration.millis(millis), node);
            tt.setFromX(-distance);
            tt.setToX(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.play();
        });
    }

    public static void pulse(Node node) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            ScaleTransition st1 = new ScaleTransition(Duration.millis(200), node);
            st1.setToX(1.03);
            st1.setToY(1.03);
            ScaleTransition st2 = new ScaleTransition(Duration.millis(200), node);
            st2.setToX(1.0);
            st2.setToY(1.0);
            SequentialTransition seq = new SequentialTransition(st1, st2);
            seq.setCycleCount(2);
            seq.play();
        });
    }

    public static void shake(Node node) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            TranslateTransition tt1 = new TranslateTransition(Duration.millis(60), node);
            tt1.setByX(-8);
            TranslateTransition tt2 = new TranslateTransition(Duration.millis(60), node);
            tt2.setByX(16);
            TranslateTransition tt3 = new TranslateTransition(Duration.millis(60), node);
            tt3.setByX(-8);
            SequentialTransition seq = new SequentialTransition(tt1, tt2, tt3);
            seq.play();
        });
    }

    public static void hoverEffect(Node node) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.addEventHandler(MouseEvent.MOUSE_ENTERED, e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                st.setToX(1.06);
                st.setToY(1.06);
                st.play();
            });
            node.addEventHandler(MouseEvent.MOUSE_EXITED, e -> {
                ScaleTransition st = new ScaleTransition(Duration.millis(150), node);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
            });
        });
    }

    public static void slideInFromBottom(Node node) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.setTranslateY(100);
            TranslateTransition tt = new TranslateTransition(Duration.millis(400), node);
            tt.setFromY(100);
            tt.setToY(0);
            tt.setInterpolator(Interpolator.EASE_OUT);
            tt.play();
        });
    }

    public static void fadeInScale(Node node, double millis) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.setOpacity(0);
            node.setScaleX(0.98);
            node.setScaleY(0.98);
            FadeTransition ft = new FadeTransition(Duration.millis(millis), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ScaleTransition st = new ScaleTransition(Duration.millis(millis), node);
            st.setToX(1.0);
            st.setToY(1.0);
            ParallelTransition pt = new ParallelTransition(ft, st);
            pt.play();
        });
    }

    public static void tabFadeIn(Node node) {
        if (!enabled || node == null) return;
        Platform.runLater(() -> {
            node.setOpacity(0);
            FadeTransition ft = new FadeTransition(Duration.millis(300), node);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setInterpolator(Interpolator.EASE_BOTH);
            ft.play();
        });
    }
}
