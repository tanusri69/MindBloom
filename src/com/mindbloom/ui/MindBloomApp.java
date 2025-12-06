package com.mindbloom.ui;

import com.mindbloom.service.HabitTrackerService;
import com.mindbloom.service.MoodTrackerService;
import com.mindbloom.service.StorageService;
import com.mindbloom.service.UserService;
import com.mindbloom.threads.AutoSaveThread;
import com.mindbloom.threads.ReminderThread;
import javafx.application.Application;
import com.mindbloom.ui.AnimationUtil;
import javafx.animation.*;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.*;
import javafx.scene.paint.Color;
import javafx.scene.chart.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.text.Text;
import javafx.scene.control.Slider;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;


public class MindBloomApp extends Application {
    private static HabitTrackerService habitService;
    private static MoodTrackerService moodService;
    private static AutoSaveThread autosaveThread;
    private static ReminderThread reminderThread;
    private static UserService userService;
    private static String currentUser;
    private static javafx.collections.ObservableList<String> remindersUIList;
    private static Stage primaryStage;
    private static MindBloomApp instance;
    private static java.util.Queue<String> reminderQueue = new java.util.LinkedList<>();
    private static volatile boolean reminderShowing = false;
    private static volatile boolean remindersShownThisSession = false;

    
    private static class Delta { double x, y; }

    
    public static void addReminderNotification(String message) {
        if (remindersUIList != null) {
            
            String timestamp = java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss"));
            String entry = "[" + timestamp + "] " + message;
            reminderQueue.offer(entry);
            
            javafx.application.Platform.runLater(() -> {
                remindersUIList.add(0, entry);
                if (remindersUIList.size() > 200) remindersUIList.remove(remindersUIList.size() - 1);
            });
            
            showNextReminder();
        }
    }

    private static void showNextReminder() {
        if (reminderShowing) return;
        String next = reminderQueue.poll();
        if (next == null) return;
        reminderShowing = true;
        javafx.application.Platform.runLater(() -> {
            try {
                
                ToastUtil.show(primaryStage, next, 3000);
            } catch (Exception ignored) {}
            
            javafx.animation.Timeline t = new javafx.animation.Timeline(new javafx.animation.KeyFrame(javafx.util.Duration.millis(3200), ev -> {
                reminderShowing = false;
                
                showNextReminder();
            }));
            t.play();
        });
    }

    
    public static void setRemindersUI(javafx.collections.ObservableList<String> list) {
        remindersUIList = list;
    }

    
    private static void showPendingRemindersOnLogin() {
        if (remindersShownThisSession) return;
        try {
            com.mindbloom.service.ReminderManager rm = new com.mindbloom.service.ReminderManager(habitService, moodService, 10);
            java.util.List<String> pending = rm.getPendingHabitReminders();
            for (String habitName : pending) {
                String msg = rm.formatHabitReminder(habitName);
                addReminderNotification(msg);
            }
            String stress = rm.getStressMotivation();
            if (stress != null) addReminderNotification(stress);
        } catch (Exception ignored) {}
        remindersShownThisSession = true;
    }

    private static void selectTabByKeyword(TabPane tabPane, String keyword) {
        if (tabPane == null || keyword == null) return;
        for (Tab t : tabPane.getTabs()) {
            if (t.getText() != null && t.getText().toLowerCase().contains(keyword.toLowerCase())) {
                tabPane.getSelectionModel().select(t);
                return;
            }
        }
    }

    
    private void performLogout() {
        try {
            
            shutdown();
            
            
            currentUser = null;
            remindersUIList = null;

            
            try {
                if (primaryStage != null && primaryStage.getScene() != null) {
                    primaryStage.getScene().setRoot(new javafx.scene.layout.BorderPane());
                }
            } catch (Exception ignored) {}
            
            
            LoginScreen loginScreen = new LoginScreen(userService);
            String newUser = loginScreen.showLoginDialog(primaryStage);
            
            if (newUser == null) {
                System.out.println("Login cancelled.");
                System.exit(0);
            }
            
            currentUser = newUser;
            
            
            habitService = new HabitTrackerService();
            moodService = new MoodTrackerService();
            
            
            String userDataFile = currentUser + "_mindbloom.dat";
            try {
                StorageService.loadFromFile(userDataFile, moodService, habitService);
            } catch (Exception e) {
                System.out.println("No existing data found for user. Starting fresh.");
            }
            
            
            autosaveThread = new AutoSaveThread(moodService, habitService);
            autosaveThread.start();

            reminderThread = new ReminderThread(habitService, moodService);
            reminderThread.start();

            
            try {
                javafx.application.Platform.runLater(() -> showPendingRemindersOnLogin());
            } catch (Exception ignored) {}

            
            javafx.application.Platform.runLater(() -> {
                try {
                    BorderPane root = createUI(primaryStage);
                    Scene scene = primaryStage.getScene();
                    if (scene == null) {
                        scene = new Scene(root, 1000, 700);
                        primaryStage.setScene(scene);
                    } else {
                        scene.setRoot(root);
                    }
                    try { ThemeUtil.applyTheme(scene); } catch (Exception ignored) {}
                    primaryStage.setTitle("🌱 MindBloom - Mental Health & Habit Tracker [User: " + currentUser + "]");
                    primaryStage.show();
                } catch (Exception e) {
                    System.err.println("ERROR rebuilding UI after logout: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            
        } catch (Exception e) {
            System.err.println("ERROR during logout: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void start(Stage stage) {
        try {
            instance = this;
            primaryStage = stage;
            
            
            primaryStage.initStyle(javafx.stage.StageStyle.UNDECORATED);
            primaryStage.setResizable(true);
            primaryStage.getIcons().add(new javafx.scene.image.Image(new java.io.ByteArrayInputStream(
                new byte[]{-119, 80, 78, 71, 13, 10, 26, 10} 
            )));
            
            
            userService = new UserService();
            LoginScreen loginScreen = new LoginScreen(userService);
            currentUser = loginScreen.showLoginDialog(primaryStage);
            
            if (currentUser == null) {
                System.out.println("Login cancelled.");
                System.exit(0);
            }

            
            habitService = new HabitTrackerService();
            moodService = new MoodTrackerService();

            
            String userDataFile = currentUser + "_mindbloom.dat";
            try {
                StorageService.loadFromFile(userDataFile, moodService, habitService);
            } catch (Exception e) {
                System.out.println("No existing data found for user. Starting fresh.");
            }

            
            autosaveThread = new AutoSaveThread(moodService, habitService);
            autosaveThread.start();

            reminderThread = new ReminderThread(habitService, moodService);
            reminderThread.start();

            
            BorderPane root = createUI(primaryStage);

            
            Scene scene = new Scene(root, 1200, 850);
            try {
                ThemeUtil.applyTheme(scene);
            } catch (Exception ignored) {}
            
            primaryStage.setMaximized(true);
            primaryStage.setTitle("🌱 MindBloom - Mental Health & Habit Tracker [User: " + currentUser + "]");
            primaryStage.setScene(scene);
            primaryStage.setOnCloseRequest(event -> shutdown());
            primaryStage.show();
        } catch (Exception e) {
            System.err.println("ERROR in start(): " + e.getMessage());
            e.printStackTrace();
        }
    }

    private BorderPane createUI(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: #ecf0f1;");

        
        HBox titleBar = new HBox(10);
        titleBar.setPadding(new Insets(8, 12, 8, 12));
        titleBar.setStyle("-fx-background-color: linear-gradient(to right, #3498db, #2980b9); -fx-text-fill: white;");
        titleBar.setAlignment(Pos.CENTER_LEFT);
        
        Label appTitle = new Label("🌱 MindBloom");
        appTitle.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-text-fill: white;");
        
        javafx.scene.layout.Region spacer = new javafx.scene.layout.Region();
        javafx.scene.layout.HBox.setHgrow(spacer, javafx.scene.layout.Priority.ALWAYS);
        
        Button minimizeBtn = new Button("_");
        Button maximizeBtn = new Button("□");
        Button closeBtn = new Button("✕");
        minimizeBtn.setPrefWidth(30);
        maximizeBtn.setPrefWidth(30);
        closeBtn.setPrefWidth(30);
        minimizeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2;");
        maximizeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2;");
        closeBtn.setStyle("-fx-font-size: 12px; -fx-padding: 2; -fx-text-fill: #e74c3c;");
        
        minimizeBtn.setOnAction(evt -> primaryStage.setIconified(true));
        maximizeBtn.setOnAction(evt -> {
            boolean currently = primaryStage.isMaximized();
            primaryStage.setMaximized(!currently);
            
            maximizeBtn.setText(primaryStage.isMaximized() ? "❐" : "□");
        });
        closeBtn.setOnAction(evt -> { shutdown(); try { primaryStage.close(); } catch (Exception ignored) {} });

        
        final Delta dragDelta = new Delta();
        titleBar.setOnMousePressed(evt -> {
            if (primaryStage.isMaximized()) return; 
            dragDelta.x = evt.getSceneX();
            dragDelta.y = evt.getSceneY();
        });
        titleBar.setOnMouseDragged(evt -> {
            if (primaryStage.isMaximized()) return;
            primaryStage.setX(evt.getScreenX() - dragDelta.x);
            primaryStage.setY(evt.getScreenY() - dragDelta.y);
        });
        titleBar.setOnMouseClicked(evt -> {
            if (evt.getClickCount() == 2) {
                primaryStage.setMaximized(!primaryStage.isMaximized());
                maximizeBtn.setText(primaryStage.isMaximized() ? "❐" : "□");
            }
        });
        
        titleBar.getChildren().addAll(appTitle, spacer, minimizeBtn, maximizeBtn, closeBtn);
        
        
        MenuBar menuBar = new MenuBar();
        ThemeUtil.styleMenuBar(menuBar);
        VBox topBox = new VBox(titleBar, menuBar);
        root.setTop(topBox);
        
        Menu fileMenu = new Menu("File");
        MenuItem exportMoodsItem = new MenuItem("Export Moods as CSV");
        exportMoodsItem.setOnAction(evt -> exportMoodsCSV());
        MenuItem exportHabitsItem = new MenuItem("Export Habits as CSV");
        exportHabitsItem.setOnAction(evt -> exportHabitsCSV());
        MenuItem importItem = new MenuItem("Import Data...");
        importItem.setOnAction(evt -> importData());
        MenuItem saveItem = new MenuItem("Save");
        saveItem.setOnAction(evt -> saveData());
        MenuItem logoutItem = new MenuItem("Logout");
        logoutItem.setOnAction(evt -> instance.performLogout());
        MenuItem exitItem = new MenuItem("Exit");
        exitItem.setOnAction(evt -> System.exit(0));
        fileMenu.getItems().addAll(exportMoodsItem, exportHabitsItem, importItem, saveItem, 
                                     new SeparatorMenuItem(), logoutItem, exitItem);
        
        Menu helpMenu = new Menu("Help");
        MenuItem aboutItem = new MenuItem("About");
        aboutItem.setOnAction(evt -> showAbout());
        helpMenu.getItems().add(aboutItem);

        
        Menu viewMenu = new Menu("View");
        javafx.scene.control.CheckMenuItem animationsToggle = new javafx.scene.control.CheckMenuItem("Enable Animations");
        animationsToggle.setSelected(com.mindbloom.ui.AnimationUtil.isEnabled());
        animationsToggle.setOnAction(evt -> com.mindbloom.ui.AnimationUtil.setEnabled(animationsToggle.isSelected()));
        viewMenu.getItems().add(animationsToggle);

        menuBar.getMenus().addAll(fileMenu, viewMenu, helpMenu);

        
        TabPane tabPane = new TabPane();
        tabPane.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        
        tabPane.setPrefWidth(Double.MAX_VALUE);
        tabPane.setPrefHeight(Double.MAX_VALUE);
        try {
            
            tabPane.prefWidthProperty().bind(root.widthProperty());
            tabPane.prefHeightProperty().bind(root.heightProperty().subtract(topBox.heightProperty()).subtract(20));
        } catch (Exception ignored) {}

        
        
        Tab dashboardTab = new Tab("📊 Dashboard");
        VBox dashboardContent = new VBox(12);
        dashboardContent.setPadding(new Insets(20));
        
        Label welcomeLabel = new Label("Welcome, " + currentUser + "! 🌱");
        welcomeLabel.setStyle("-fx-font-size:22px; -fx-font-weight:700; -fx-text-fill: #123c37;");

        
        Polygon leaf = new Polygon();
        leaf.getPoints().addAll(new Double[]{0.0,8.0, 14.0,0.0, 28.0,8.0, 14.0,22.0});
        leaf.setFill(ThemeUtil.PRIMARY);
        leaf.setScaleX(0.9); leaf.setScaleY(0.9);
        HBox leafBox = new HBox(8, leaf, welcomeLabel);
        leafBox.setAlignment(Pos.CENTER_LEFT);
        Label statsLabel = new Label();
        statsLabel.setStyle("-fx-font-size:14px; -fx-text-fill: #34495e;");
        Runnable updateDashboard = () -> {
            int moodCount = moodService.getAll().size();
            int habitCount = habitService.getAll().size();
            int totalStreak = habitService.getAll().stream().mapToInt(h -> h.getStreak()).sum();
            statsLabel.setText(String.format("📊 Stats: %d moods logged | %d habits tracked | %d total streak",
                moodCount, habitCount, totalStreak));
        };
        updateDashboard.run();
        Button refreshDash = new Button("Refresh");
        refreshDash.setOnAction(evt -> updateDashboard.run());
        
        HBox featureCards = new HBox(12);
        featureCards.setPadding(new Insets(10, 0, 0, 0));
        
        VBox card1 = new VBox(6, new Label("Moods"), new Label("Log your feelings"));
        VBox card2 = new VBox(6, new Label("Habits"), new Label("Track progress"));
        VBox card3 = new VBox(6, new Label("Meditate"), new Label("Breathe & relax"));
        for (VBox c : new VBox[]{card1, card2, card3}) {
            c.setMinWidth(220);
            c.setMinHeight(80);
            ThemeUtil.styleCard(c);
        }
        featureCards.getChildren().addAll(card1, card2, card3);

        dashboardContent.getChildren().addAll(
            welcomeLabel,
            new Label("Track your mood, build better habits, and improve your mental health."),
            statsLabel,
            refreshDash,
            featureCards
        );
        dashboardTab.setContent(dashboardContent);
        ThemeUtil.styleCard(dashboardContent);

        
        
        javafx.application.Platform.runLater(() -> {
            try {
                com.mindbloom.ui.AnimationUtil.fadeInScale(welcomeLabel, 700);

                
                javafx.scene.shape.Circle moodIcon = new javafx.scene.shape.Circle(18, ThemeUtil.PRIMARY);
                javafx.scene.shape.Circle habitIcon = new javafx.scene.shape.Circle(18, ThemeUtil.ACCENT);
                HBox iconBox = new HBox(16, moodIcon, habitIcon);
                iconBox.setAlignment(Pos.CENTER_LEFT);
                iconBox.setPadding(new Insets(6,0,0,0));
                
                dashboardContent.getChildren().add(1, leafBox);

                
                RotateTransition rt = new RotateTransition(javafx.util.Duration.seconds(4), leaf);
                rt.setByAngle(8); rt.setAutoReverse(true); rt.setCycleCount(Animation.INDEFINITE); rt.play();
                TranslateTransition tb = new TranslateTransition(javafx.util.Duration.seconds(3), leaf);
                tb.setByY(-6); tb.setAutoReverse(true); tb.setCycleCount(Animation.INDEFINITE); tb.play();

                
                TranslateTransition bob1 = new TranslateTransition(javafx.util.Duration.seconds(2), moodIcon);
                bob1.setByY(-8);
                bob1.setCycleCount(Animation.INDEFINITE);
                bob1.setAutoReverse(true);
                bob1.play();

                TranslateTransition bob2 = new TranslateTransition(javafx.util.Duration.seconds(2.4), habitIcon);
                bob2.setByY(-6);
                bob2.setCycleCount(Animation.INDEFINITE);
                bob2.setAutoReverse(true);
                bob2.play();

                
                ScaleTransition s1 = new ScaleTransition(javafx.util.Duration.millis(320), card1);
                s1.setFromX(0.86); s1.setFromY(0.86); s1.setToX(1); s1.setToY(1);
                FadeTransition f1 = new FadeTransition(javafx.util.Duration.millis(320), card1);
                f1.setFromValue(0); f1.setToValue(1);

                ScaleTransition s2 = new ScaleTransition(javafx.util.Duration.millis(320), card2);
                s2.setFromX(0.86); s2.setFromY(0.86); s2.setToX(1); s2.setToY(1);
                FadeTransition f2 = new FadeTransition(javafx.util.Duration.millis(320), card2);
                f2.setFromValue(0); f2.setToValue(1);

                ScaleTransition s3 = new ScaleTransition(javafx.util.Duration.millis(320), card3);
                s3.setFromX(0.86); s3.setFromY(0.86); s3.setToX(1); s3.setToY(1);
                FadeTransition f3 = new FadeTransition(javafx.util.Duration.millis(320), card3);
                f3.setFromValue(0); f3.setToValue(1);

                SequentialTransition seq = new SequentialTransition(
                        new ParallelTransition(s1, f1),
                        new ParallelTransition(s2, f2),
                        new ParallelTransition(s3, f3)
                );
                seq.setDelay(javafx.util.Duration.millis(250));
                seq.play();

                
            } catch (Exception ignored) {}
        });

        
        Tab moodsTab = new Tab("😊 Moods");
        VBox moodsContent = new VBox(10);
        moodsContent.setPadding(new Insets(15));

        
        StackPane moodInputStack = new StackPane();
        javafx.scene.shape.Rectangle moodBg = new javafx.scene.shape.Rectangle();
        moodBg.setArcWidth(12);
        moodBg.setArcHeight(12);
        moodBg.setFill(javafx.scene.paint.Color.TRANSPARENT);

        HBox moodInput = new HBox(8);
        moodInput.setPadding(new Insets(8));
        ComboBox<String> moodCombo = new ComboBox<>();
        moodCombo.getItems().addAll("happy", "sad", "calm", "anxious", "energetic", "tired", "angry", "grateful");
        moodCombo.setEditable(true);
        moodCombo.setPrefWidth(180);
        TextField noteField = new TextField();
        noteField.setPromptText("Optional note...");

        
        Slider intensity = new Slider(0, 10, 5);
        intensity.setPrefWidth(160);
        Label emoji = new Label("🙂");
        emoji.setStyle("-fx-font-size:28px;");
        Button addMoodBtn = new Button("Save Mood");

        moodInput.getChildren().addAll(new Label("Mood:"), moodCombo, new Label("Intensity:"), intensity, emoji, noteField, addMoodBtn);
        
        try {
            moodBg.widthProperty().bind(moodInput.widthProperty().add(20));
            moodBg.heightProperty().bind(moodInput.heightProperty().add(12));
        } catch (Exception ignored) {}
        StackPane.setMargin(moodInput, new Insets(6));
        moodInputStack.getChildren().addAll(moodBg, moodInput);

        TableView<com.mindbloom.model.MoodEntry> moodTable = new TableView<>();
        TableColumn<com.mindbloom.model.MoodEntry, java.time.LocalDate> dateCol = new TableColumn<>("Date");
        dateCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getDate()));
        TableColumn<com.mindbloom.model.MoodEntry, String> moodCol = new TableColumn<>("Mood");
        moodCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getMood()));
        TableColumn<com.mindbloom.model.MoodEntry, String> noteCol = new TableColumn<>("Note");
        noteCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getNote()));
        moodTable.getColumns().addAll(dateCol, moodCol, noteCol);
        
        moodTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        dateCol.setPrefWidth(120);
        moodCol.setPrefWidth(120);
        noteCol.setPrefWidth(360);
        moodTable.setPlaceholder(new Label("No mood entries yet"));

        
        javafx.scene.chart.CategoryAxis moodXAxis = new javafx.scene.chart.CategoryAxis();
        javafx.scene.chart.NumberAxis moodYAxis = new javafx.scene.chart.NumberAxis(0, 10, 1);
        javafx.scene.chart.AreaChart<String, Number> moodTrend = new javafx.scene.chart.AreaChart<>(moodXAxis, moodYAxis);
        moodTrend.setTitle("Mood Trend");
        moodTrend.setLegendVisible(false);
        moodTrend.setAnimated(false);
        moodTrend.setPrefHeight(180);
        moodTrend.setCreateSymbols(false);
        moodTrend.getStyleClass().add("area-chart");

        
        Runnable refreshMoodTable = () -> moodTable.setItems(javafx.collections.FXCollections.observableArrayList(moodService.getAll()));
        refreshMoodTable.run();

        
        javafx.scene.chart.PieChart moodDist = new javafx.scene.chart.PieChart();
        moodDist.setTitle("Mood Distribution");
        moodDist.setLabelsVisible(false);
        moodDist.setLegendSide(javafx.geometry.Side.RIGHT);

        Runnable updateMoodDistribution = () -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    java.util.Map<String, Long> counts = moodService.getAll().stream()
                            .map(m -> m.getMood().toLowerCase())
                            .collect(java.util.stream.Collectors.groupingBy(s -> s, java.util.stream.Collectors.counting()));
                    javafx.collections.ObservableList<javafx.scene.chart.PieChart.Data> pieData = javafx.collections.FXCollections.observableArrayList();
                    counts.forEach((k, v) -> pieData.add(new javafx.scene.chart.PieChart.Data(capitalize(k), v)));
                    moodDist.setData(pieData);
                } catch (Exception ignored) {}
            });
        };

        Runnable animateMoodTrend = () -> {
            javafx.application.Platform.runLater(() -> {
                try {
                    moodTrend.getData().clear();
                    javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
                    java.util.List<com.mindbloom.model.MoodEntry> entries = moodService.getAll();
                    
                    entries.sort(java.util.Comparator.comparing(com.mindbloom.model.MoodEntry::getDate));
                    for (com.mindbloom.model.MoodEntry me : entries) {
                        final String label = me.getDate().toString();
                        final int score = mapMoodToScore(me.getMood());
                        series.getData().add(new javafx.scene.chart.XYChart.Data<>(label, score));
                    }
                    moodTrend.getData().add(series);
                    
                    javafx.scene.Node seriesNode = series.getNode();
                    if (seriesNode != null) seriesNode.setStyle("-fx-stroke-width: 3px; -fx-stroke: #2ecc71; -fx-opacity: 0.95;");
                    
                    FadeTransition ft = new FadeTransition(javafx.util.Duration.millis(500), moodTrend);
                    ft.setFromValue(0); ft.setToValue(1); ft.play();
                    updateMoodDistribution.run();
                } catch (Exception ignored) {}
            });
        };

        
        intensity.valueProperty().addListener((obs, oldV, newV) -> {
            double v = newV.doubleValue();
            javafx.scene.paint.Color low = javafx.scene.paint.Color.web("#9ad0ff");
            javafx.scene.paint.Color high = javafx.scene.paint.Color.web("#ff9a66");
            javafx.scene.paint.Color interp = low.interpolate(high, v / 10.0);
            javafx.animation.FillTransition ft = new javafx.animation.FillTransition(javafx.util.Duration.millis(250), moodBg, (javafx.scene.paint.Color) moodBg.getFill(), interp);
            ft.play();
            double scale = 1.0 + (v / 20.0);
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(180), emoji);
            st.setToX(scale); st.setToY(scale); st.play();
        });

        
        moodCombo.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) return;
            String e = "🙂";
            String nv = newV.toLowerCase();
            if (nv.contains("happy") || nv.contains("ener")) e = "😄";
            else if (nv.contains("sad")) e = "😔";
            else if (nv.contains("anx")) e = "😟";
            else if (nv.contains("calm") || nv.contains("grate")) e = "😌";
            emoji.setText(e);
            javafx.animation.ScaleTransition pst = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(300), emoji);
            pst.setFromX(0.8); pst.setFromY(0.8); pst.setToX(1.2); pst.setToY(1.2); pst.setAutoReverse(true); pst.setCycleCount(2); pst.play();
            javafx.scene.effect.DropShadow ds = new javafx.scene.effect.DropShadow(10, javafx.scene.paint.Color.rgb(255,200,120,0.7));
            emoji.setEffect(ds);
            javafx.animation.Timeline glow = new javafx.animation.Timeline(
                    new javafx.animation.KeyFrame(javafx.util.Duration.ZERO, new javafx.animation.KeyValue(ds.radiusProperty(), 4)),
                    new javafx.animation.KeyFrame(javafx.util.Duration.millis(600), new javafx.animation.KeyValue(ds.radiusProperty(), 18))
            );
            glow.setAutoReverse(true); glow.setCycleCount(2); glow.play();
        });

        addMoodBtn.setOnAction(evt -> {
            String mood = moodCombo.getEditor().getText();
            String note = noteField.getText();
            if (mood == null || mood.trim().isEmpty()) {
                ToastUtil.show(primaryStage, "Please enter a mood.");
                return;
            }
            javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(260), emoji);
            st.setFromX(1.0); st.setFromY(1.0); st.setToX(1.25); st.setToY(1.25); st.setAutoReverse(true); st.setCycleCount(2); st.play();
            javafx.animation.FillTransition ft = new javafx.animation.FillTransition(javafx.util.Duration.millis(300), moodBg);
            ft.setToValue(javafx.scene.paint.Color.web("#dff7e6")); ft.play();

            moodService.addMood(mood.trim(), note == null ? "" : note.trim());
            noteField.clear();
            moodCombo.setValue(null);
            refreshMoodTable.run();
            animateMoodTrend.run();
            ToastUtil.show(primaryStage, "Mood saved");
        });

        Button deleteMoodBtn = new Button("Delete Selected");
        deleteMoodBtn.setOnAction(evt -> {
            com.mindbloom.model.MoodEntry sel = moodTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ToastUtil.show(primaryStage, "Select a mood to delete.");
                return;
            }
            try {
                java.util.List<com.mindbloom.model.MoodEntry> allMoods = new java.util.ArrayList<>(moodService.getAll());
                allMoods.remove(sel);
                moodService.getAll().clear();
                moodService.getAll().addAll(allMoods);
                refreshMoodTable.run();
                animateMoodTrend.run();
                ToastUtil.show(primaryStage, "Mood entry deleted.");
            } catch (Exception e) {
                ToastUtil.show(primaryStage, "Error deleting mood.");
            }
        });

        HBox trendBox = new HBox(12, moodTrend, moodDist);
        HBox.setHgrow(moodTrend, javafx.scene.layout.Priority.ALWAYS);
        moodTrend.setMaxWidth(Double.MAX_VALUE);
        moodDist.setPrefWidth(260);
        moodsContent.getChildren().addAll(new Label("Track your mood"), moodInputStack, moodTable, trendBox, deleteMoodBtn);
        moodsTab.setContent(moodsContent);
        ThemeUtil.styleCard(moodsContent);

        
        Tab habitsTab = new Tab("✅ Habits");
        VBox habitsContent = new VBox(12);
        habitsContent.setPadding(new Insets(15));

        HBox addHabitBox = new HBox(8);
        TextField habitNameField = new TextField();
        habitNameField.setPromptText("New habit name");
        Button addHabitBtn = new Button("Add Habit");
        addHabitBox.getChildren().addAll(new Label("Habit:"), habitNameField, addHabitBtn);

        TableView<com.mindbloom.model.Habit> habitTable = new TableView<>();
        TableColumn<com.mindbloom.model.Habit, String> nameCol = new TableColumn<>("Habit");
        nameCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleStringProperty(cd.getValue().getName()));
        TableColumn<com.mindbloom.model.Habit, Integer> streakCol = new TableColumn<>("Streak");
        streakCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleIntegerProperty(cd.getValue().getStreak()).asObject());
        TableColumn<com.mindbloom.model.Habit, java.time.LocalDate> lastCol = new TableColumn<>("Last Completed");
        lastCol.setCellValueFactory(cd -> new javafx.beans.property.SimpleObjectProperty<>(cd.getValue().getLastCompleted()));
        habitTable.getColumns().addAll(nameCol, streakCol, lastCol);
        habitTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        nameCol.setPrefWidth(260);
        streakCol.setPrefWidth(100);
        lastCol.setPrefWidth(160);
        habitTable.setPlaceholder(new Label("No habits yet"));

        Runnable refreshHabitTable = () -> habitTable.setItems(javafx.collections.FXCollections.observableArrayList(habitService.getAll()));
        refreshHabitTable.run();

        addHabitBtn.setOnAction(evt -> {
            String name = habitNameField.getText();
            if (name == null || name.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Please enter a habit name.", ButtonType.OK).showAndWait();
                return;
            }
            habitService.addHabit(name.trim());
            habitNameField.clear();
            refreshHabitTable.run();
        });

        Button markDoneBtn = new Button("Mark Selected Done");
        Button deleteHabitBtn = new Button("Delete Selected");
        markDoneBtn.setOnAction(evt -> {
            com.mindbloom.model.Habit sel = habitTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ToastUtil.show(primaryStage, "Select a habit first.");
                return;
            }
            try {
                habitService.tickHabit(sel.getName());
                refreshHabitTable.run();
                ToastUtil.show(primaryStage, "Habit completed!");
            } catch (Exception e) {
                ToastUtil.show(primaryStage, "Error: " + e.getMessage());
            }
        });
        deleteHabitBtn.setOnAction(evt -> {
            com.mindbloom.model.Habit sel = habitTable.getSelectionModel().getSelectedItem();
            if (sel == null) {
                ToastUtil.show(primaryStage, "Select a habit to delete.");
                return;
            }
            try {
                
                java.util.List<com.mindbloom.model.Habit> allHabits = new java.util.ArrayList<>(habitService.getAll());
                allHabits.removeIf(h -> h.getName().equals(sel.getName()));
                
                habitService.getAll().clear();
                habitService.getAll().addAll(allHabits);
                refreshHabitTable.run();
                ToastUtil.show(primaryStage, "Habit deleted: " + sel.getName());
            } catch (Exception e) {
                ToastUtil.show(primaryStage, "Error deleting habit.");
            }
        });

        HBox habitButtons = new HBox(10);
        habitButtons.getChildren().addAll(markDoneBtn, deleteHabitBtn);
        habitsContent.getChildren().addAll(new Label("Manage your habits"), addHabitBox, habitTable, habitButtons);
        habitsTab.setContent(habitsContent);
        ThemeUtil.styleCard(habitsContent);

        
        Tab quotesTab = new Tab("💬 Quotes");
        VBox quotesContent = new VBox(10);
        quotesContent.setPadding(new Insets(15));
        com.mindbloom.motivation.QuotesManager quoteMgr = new com.mindbloom.motivation.QuotesManager();
        TextArea quoteArea = new TextArea();
        quoteArea.setWrapText(true);
        quoteArea.setPrefRowCount(4);
        quoteArea.setEditable(false);
        Label quoteLabel = new Label();
        Button randomQuoteBtn = new Button("Random Quote");
        Button addFavBtn = new Button("Add to Favorites");
        Button removeFavBtn = new Button("Remove Favorite");

        
        javafx.collections.ObservableList<String> cats = javafx.collections.FXCollections.observableArrayList(quoteMgr.getCategories());
        javafx.collections.FXCollections.sort(cats);
        ComboBox<String> categoryCombo = new ComboBox<>(cats);
        categoryCombo.setPromptText("Choose category...");
        Button categoryGetBtn = new Button("Get Quote");
        ListView<String> categoryList = new ListView<>();
        categoryList.setPrefWidth(360);

        
        ListView<String> favoritesList = new ListView<>();
        favoritesList.setPrefWidth(280);
        favoritesList.setItems(javafx.collections.FXCollections.observableArrayList(quoteMgr.getFavorites()));

        
        try {
            if (autosaveThread != null) {
                autosaveThread.addSaveAction(() -> { try { quoteMgr.persistFavorites(); } catch (Exception ignored) {} });
            }
        } catch (Exception ignored) {}

        randomQuoteBtn.setOnAction(evt -> {
            String q = quoteMgr.getRandomQuote();
            if (q != null) { quoteArea.setText(q); quoteLabel.setText("Quote #" + System.currentTimeMillis()); }
        });
        addFavBtn.setOnAction(evt -> {
            String q = quoteArea.getText();
            if (q != null && !q.isEmpty()) {
                quoteMgr.addFavorite(q);
                ToastUtil.show(primaryStage, "✓ Added to Favorites!");
                
                favoritesList.setItems(javafx.collections.FXCollections.observableArrayList(quoteMgr.getFavorites()));
            } else {
                ToastUtil.show(primaryStage, "Please select a quote first.");
            }
        });
        removeFavBtn.setOnAction(evt -> {
            String sel = favoritesList.getSelectionModel().getSelectedItem();
            if (sel == null) { ToastUtil.show(primaryStage, "Select a favorite to remove."); return; }
            quoteMgr.removeFavorite(sel);
            favoritesList.setItems(javafx.collections.FXCollections.observableArrayList(quoteMgr.getFavorites()));
            ToastUtil.show(primaryStage, "Removed from favorites.");
        });

        categoryCombo.setOnAction(evt -> {
            String c = categoryCombo.getValue();
            if (c != null) {
                java.util.List<String> list = quoteMgr.getQuotesForCategory(c);
                categoryList.setItems(javafx.collections.FXCollections.observableArrayList(list));
            }
        });
        categoryGetBtn.setOnAction(evt -> {
            String c = categoryCombo.getValue();
            if (c == null) { ToastUtil.show(primaryStage, "Select a category first."); return; }
            String q = quoteMgr.getRandomQuoteByCategory(c);
            if (q != null) { quoteArea.setText(q); quoteLabel.setText("Quote: " + c); }
        });
        categoryList.setOnMouseClicked(evt -> {
            String s = categoryList.getSelectionModel().getSelectedItem(); if (s != null) quoteArea.setText(s);
        });
        favoritesList.setOnMouseClicked(evt -> {
            String s = favoritesList.getSelectionModel().getSelectedItem(); if (s != null) quoteArea.setText(s);
        });

        HBox listsBox = new HBox(12);
        VBox leftBox = new VBox(8, new Label("Categories"), categoryCombo, categoryGetBtn, categoryList);
        VBox centerBox = new VBox(8, new Label("Quote"), quoteArea, new HBox(8, randomQuoteBtn, addFavBtn, removeFavBtn));
        VBox rightBox = new VBox(8, new Label("Favorites"), favoritesList);
        leftBox.setPrefWidth(420);
        centerBox.setPrefWidth(560);
        listsBox.getChildren().addAll(leftBox, centerBox, rightBox);

        quotesContent.getChildren().addAll(new Label("Daily Quotes"), listsBox);
        quotesTab.setContent(quotesContent);
        ThemeUtil.styleCard(quotesContent);

        
        Tab journalTab = new Tab("📔 Journal");
        VBox journalContent = new VBox(10);
        journalContent.setPadding(new Insets(15));

        TextArea journalArea = new TextArea();
        journalArea.setPromptText("Write your journal entry here...");
        journalArea.setWrapText(true);
        journalArea.setPrefRowCount(10);

        
        try {
            if (autosaveThread != null) {
                autosaveThread.addSaveAction(() -> {
                    try {
                        java.nio.file.Files.write(java.nio.file.Paths.get(currentUser + "_journal_draft.txt"),
                                journalArea.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } catch (Exception ignored) {}
                });
            }
        } catch (Exception ignored) {}

        ListView<String> entryList = new ListView<>();
        entryList.setPrefHeight(150);
        Runnable loadJournalEntries = () -> {
            java.io.File jf = new java.io.File(currentUser + "_journal.txt");
            if (jf.exists()) {
                try {
                    java.util.List<String> lines = java.nio.file.Files.readAllLines(jf.toPath());
                    entryList.setItems(javafx.collections.FXCollections.observableArrayList(lines));
                } catch (Exception e) { }
            }
        };
        loadJournalEntries.run();

        HBox journalButtons = new HBox(8);
        Button saveJournal = new Button("Save Entry");
        Button clearJournal = new Button("Clear");
        Button exportJournal = new Button("Export...");
        journalButtons.getChildren().addAll(saveJournal, clearJournal, exportJournal);

        saveJournal.setOnAction(evt -> {
            String text = journalArea.getText();
            if (text == null || text.trim().isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Cannot save empty entry.", ButtonType.OK).showAndWait();
                return;
            }
            try (java.io.PrintWriter pw = new java.io.PrintWriter(new java.io.FileWriter(currentUser + "_journal.txt", true))) {
                pw.println("[" + java.time.LocalDateTime.now() + "]");
                pw.println(text);
                pw.println("---");
                new Alert(Alert.AlertType.INFORMATION, "Journal entry saved.", ButtonType.OK).showAndWait();
                journalArea.clear();
                loadJournalEntries.run();
            } catch (Exception e) {
                new Alert(Alert.AlertType.ERROR, "Error saving journal: " + e.getMessage(), ButtonType.OK).showAndWait();
            }
        });

        clearJournal.setOnAction(evt -> journalArea.clear());
        exportJournal.setOnAction(evt -> {
            javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
            fc.setTitle("Export Journal");
            fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Text Files", "*.txt"));
            java.io.File out = fc.showSaveDialog(null);
            if (out != null) {
                try {
                    java.nio.file.Files.write(out.toPath(), journalArea.getText().getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    new Alert(Alert.AlertType.INFORMATION, "Exported.", ButtonType.OK).showAndWait();
                } catch (Exception e) {
                    new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK).showAndWait();
                }
            }
        });

        journalContent.getChildren().addAll(new Label("Journal Entries"), entryList, new Label("New Entry:"), journalArea, journalButtons);
        journalTab.setContent(journalContent);
        ThemeUtil.styleCard(journalContent);

        
        Tab analyticsTab = new Tab("📈 Analytics");
        VBox analyticsContent = new VBox(12);
        analyticsContent.setPadding(new Insets(15));

        
        javafx.scene.chart.LineChart<String, Number> moodChart;
        {
            javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
            javafx.scene.chart.NumberAxis yAxis = new javafx.scene.chart.NumberAxis(0, 10, 1);
            xAxis.setLabel("Date");
            yAxis.setLabel("Mood (score)");
            moodChart = new javafx.scene.chart.LineChart<>(xAxis, yAxis);
            moodChart.setTitle("Mood Trend");
        }

        
        javafx.scene.chart.BarChart<String, Number> habitChart;
        {
            javafx.scene.chart.CategoryAxis x2 = new javafx.scene.chart.CategoryAxis();
            javafx.scene.chart.NumberAxis y2 = new javafx.scene.chart.NumberAxis();
            x2.setLabel("Habit");
            y2.setLabel("Streak");
            habitChart = new javafx.scene.chart.BarChart<>(x2, y2);
            habitChart.setTitle("Habit Streaks");
        }

        Runnable refreshAnalyticsData = () -> {
            
            javafx.scene.chart.XYChart.Series<String, Number> series = new javafx.scene.chart.XYChart.Series<>();
            series.setName("Mood Score");
            for (com.mindbloom.model.MoodEntry me : moodService.getAll()) {
                int score = mapMoodToScore(me.getMood());
                series.getData().add(new javafx.scene.chart.XYChart.Data<>(me.getDate().toString(), score));
            }
            moodChart.getData().clear();
            moodChart.getData().add(series);

            
            javafx.scene.chart.XYChart.Series<String, Number> hseries = new javafx.scene.chart.XYChart.Series<>();
            hseries.setName("Streaks");
            for (com.mindbloom.model.Habit h : habitService.getAll()) {
                hseries.getData().add(new javafx.scene.chart.XYChart.Data<>(h.getName(), h.getStreak()));
            }
            habitChart.getData().clear();
            habitChart.getData().add(hseries);
        };

        Button refreshAnalytics = new Button("Refresh Analytics");
        refreshAnalytics.setOnAction(evt -> refreshAnalyticsData.run());

        
        analyticsTab.setOnSelectionChanged(evt -> {
            if (analyticsTab.isSelected()) {
                refreshAnalyticsData.run();
            }
        });

        Label analyticsTitle = new Label("Analytics Overview");
        analyticsTitle.setStyle("-fx-font-size:14px; -fx-font-weight:bold;");
        analyticsContent.getChildren().addAll(analyticsTitle, refreshAnalytics, moodChart, habitChart);
        analyticsTab.setContent(analyticsContent);
        ThemeUtil.styleCard(analyticsContent);
        
        
        refreshAnalyticsData.run();

        
        Tab meditationTab = new Tab("🧘 Meditation");
        VBox meditationContent = new VBox(16);
        meditationContent.setPadding(new Insets(20));
        meditationContent.setAlignment(Pos.TOP_CENTER);

        Label medTitle = new Label("Meditation & Breathing");
        medTitle.setStyle("-fx-font-size:16px; -fx-font-weight:bold;");
        
        javafx.scene.layout.StackPane meditatePane = new javafx.scene.layout.StackPane();
        meditatePane.setPrefHeight(280);
        meditatePane.setStyle("-fx-background-color: #f0f8ff; -fx-border-radius: 12; -fx-background-radius: 12;");
        
        javafx.scene.shape.Circle circle = new javafx.scene.shape.Circle(90, javafx.scene.paint.Color.web("#8fd3c7"));
        javafx.scene.text.Text breatheText = new javafx.scene.text.Text("Breathe...");
        breatheText.setStyle("-fx-font-size:22px; -fx-font-weight:bold; -fx-fill: #333;");
        
        meditatePane.getChildren().addAll(circle, breatheText);

        Label medTimer = new Label("00:00");
        medTimer.setStyle("-fx-font-size:48px; -fx-font-weight:bold; -fx-text-fill: #2c3e50;");
        
        TextArea guideText = new TextArea();
        guideText.setWrapText(true);
        guideText.setPrefRowCount(6);
        guideText.setEditable(false);
        guideText.setStyle("-fx-control-inner-background: #f9f9f9; -fx-font-size: 14; -fx-padding: 10; -fx-border-radius:6;");
        guideText.setText("🧘 Meditation Guide:\n\n4-4-4 Breathing Technique:\n• Inhale for 4 seconds\n• Hold for 4 seconds\n• Exhale for 4 seconds\n\nRepeat for the duration of your session.\nFocus on your breath and let go of stress.\n\nTips:\n- Sit comfortably with a straight spine.\n- Close your eyes and soften your jaw.\n- If your mind wanders, gently bring attention back to the breath.");
        ThemeUtil.styleTitle(medTitle);

        HBox medControls = new HBox(12);
        medControls.setAlignment(Pos.CENTER);
        Button medStart5 = new Button("Start (5 min)");
        Button medStart10 = new Button("Start (10 min)");
        Button medStart20 = new Button("Start (20 min)");
        Button medStop = new Button("Stop Session");
        medStart5.setPrefWidth(100);
        medStart10.setPrefWidth(100);
        medStart20.setPrefWidth(100);
        medStop.setPrefWidth(100);
        medControls.getChildren().addAll(medStart5, medStart10, medStart20, medStop);

        final int[] seconds = {0};
        final javafx.animation.Timeline[] runningTimeline = {null};
        
        java.util.function.IntConsumer startMeditation = (duration) -> {
            if (runningTimeline[0] != null) runningTimeline[0].stop();
            seconds[0] = duration;
            breatheText.setText("Inhale...");
            medStart5.setDisable(true);
            medStart10.setDisable(true);
            medStart20.setDisable(true);
            
            runningTimeline[0] = new javafx.animation.Timeline(
                new javafx.animation.KeyFrame(javafx.util.Duration.seconds(1), evt -> {
                    seconds[0]--;
                    int m = seconds[0] / 60;
                    int s = seconds[0] % 60;
                    medTimer.setText(String.format("%02d:%02d", m, s));
                    
                    if (seconds[0] <= 0) {
                        breatheText.setText("Session Complete! 🙏");
                        medTimer.setText("00:00");
                        medStart5.setDisable(false);
                        medStart10.setDisable(false);
                        medStart20.setDisable(false);
                        ToastUtil.show(primaryStage, "Great work on meditating! 🧘");
                    } else if (seconds[0] % 12 == 0) {
                        breatheText.setText("Exhale...");
                    } else if (seconds[0] % 8 == 0) {
                        breatheText.setText("Hold...");
                    } else if (seconds[0] % 4 == 0) {
                        breatheText.setText("Inhale...");
                    }
                })
            );
            runningTimeline[0].setCycleCount(duration);
            runningTimeline[0].play();
        };
        
        medStart5.setOnAction(ev -> startMeditation.accept(300));
        medStart10.setOnAction(ev -> startMeditation.accept(600));
        medStart20.setOnAction(ev -> startMeditation.accept(1200));
        medStop.setOnAction(ev -> {
            if (runningTimeline[0] != null) runningTimeline[0].stop();
            seconds[0] = 0;
            medTimer.setText("00:00");
            breatheText.setText("Ready to meditate?");
            medStart5.setDisable(false);
            medStart10.setDisable(false);
            medStart20.setDisable(false);
        });
        
        meditationContent.getChildren().addAll(medTitle, meditatePane, medTimer, guideText, medControls);
        meditationTab.setContent(meditationContent);
        ThemeUtil.styleCard(meditationContent);

        
        Tab remindersTab = new Tab("🔔 Reminders");
        VBox remindersContent = new VBox(12);
        remindersContent.setPadding(new Insets(15));
        
        javafx.collections.ObservableList<String> reminders = javafx.collections.FXCollections.observableArrayList();
        
        Label remindersLabel = new Label("Active Reminders: Checking every 1 minute");
        remindersLabel.setStyle("-fx-font-size:15px; -fx-font-weight:bold;");
        
        Label remindersInfo = new Label("Reminders are running in the background. They will appear here when triggered. Click the X button to delete.");
        remindersInfo.setStyle("-fx-font-size:13px; -fx-text-fill: #666; -fx-wrap-text: true;");
        
        ListView<javafx.scene.layout.HBox> remindersHistory = new ListView<>();
        remindersHistory.setPrefHeight(350);
        javafx.collections.ObservableList<javafx.scene.layout.HBox> reminderItems = javafx.collections.FXCollections.observableArrayList();
        remindersHistory.setItems(reminderItems);
        
        Runnable refreshReminders = () -> {
            reminderItems.clear();
            for (String r : reminders) {
                javafx.scene.layout.HBox row = new javafx.scene.layout.HBox(10);
                row.setPadding(new Insets(8));
                row.setStyle("-fx-border-color: #ddd; -fx-border-radius: 4; -fx-background-color: #f5f5f5;");

                
                Label timeLabel = null;
                Label msgLabel = new Label();
                msgLabel.setWrapText(true);
                msgLabel.setMaxWidth(520);
                if (r != null && r.startsWith("[") && r.contains("] ")) {
                    int idx = r.indexOf("] ");
                    String ts = r.substring(0, idx + 1);
                    String msg = r.substring(idx + 2);
                    timeLabel = new Label(ts);
                    timeLabel.setStyle("-fx-font-size:11px; -fx-text-fill:#6b6f73;");
                    msgLabel.setText(msg);
                } else {
                    msgLabel.setText(r == null ? "" : r);
                }
                if (r != null && r.contains("✓")) msgLabel.setStyle("-fx-text-fill: green; -fx-font-weight: bold;");
                else msgLabel.setStyle("-fx-text-fill: #d35400;");

                Button deleteBtn = new Button("✕ Delete");
                deleteBtn.setStyle("-fx-padding: 4 8 4 8; -fx-font-size: 11;");
                deleteBtn.setOnAction(ev -> { reminderItems.remove(row); reminders.remove(r); ToastUtil.show(primaryStage, "Reminder deleted."); });

                javafx.scene.layout.Region reminderSpacer = new javafx.scene.layout.Region();
                javafx.scene.layout.HBox.setHgrow(reminderSpacer, javafx.scene.layout.Priority.ALWAYS);

                if (timeLabel != null) row.getChildren().addAll(timeLabel, msgLabel, reminderSpacer, deleteBtn);
                else row.getChildren().addAll(msgLabel, reminderSpacer, deleteBtn);

                reminderItems.add(row);
            }
        };
        
        
        javafx.collections.ListChangeListener<String> reminderListener = change -> refreshReminders.run();
        reminders.addListener(reminderListener);
        
        HBox remindersControls = new HBox(10);
        Button clearReminders = new Button("Clear All");
        clearReminders.setOnAction(evt -> { reminders.clear(); reminderItems.clear(); ToastUtil.show(primaryStage, "Reminders cleared."); });
        Button refreshRemindersBtn = new Button("Refresh");
        refreshRemindersBtn.setOnAction(evt -> refreshReminders.run());
        remindersControls.getChildren().addAll(clearReminders, refreshRemindersBtn);
        
        
        MindBloomApp.setRemindersUI(reminders);
        
        remindersContent.getChildren().addAll(remindersLabel, remindersInfo, remindersHistory, remindersControls);
        remindersTab.setContent(remindersContent);
        ThemeUtil.styleCard(remindersContent);

        
        Tab streaksTab = new Tab("🔥 Streaks");
        VBox streaksContent = new VBox(12);
        streaksContent.setPadding(new Insets(15));
        ListView<String> streaksList = new ListView<>();
        Runnable updateStreaks = () -> {
            java.util.List<String> streaks = new java.util.ArrayList<>();
            for (com.mindbloom.model.Habit h : habitService.getAll()) {
                String bar = "█".repeat(Math.min(h.getStreak(), 20)) + (h.getStreak() > 0 ? " x" + h.getStreak() : "");
                streaks.add(h.getName() + ": " + bar);
            }
            streaksList.setItems(javafx.collections.FXCollections.observableArrayList(streaks));
        };
        updateStreaks.run();
        Button refreshStreaks = new Button("Refresh");
        refreshStreaks.setOnAction(evt -> updateStreaks.run());
        streaksContent.getChildren().addAll(new Label("Habit Streaks"), streaksList, refreshStreaks);
        streaksTab.setContent(streaksContent);
        ThemeUtil.styleCard(streaksContent);

        tabPane.getTabs().addAll(dashboardTab, moodsTab, habitsTab, analyticsTab, quotesTab, journalTab, meditationTab, remindersTab);
        root.setCenter(tabPane);

        
        tabPane.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            try {
                if (newTab != null && newTab.getContent() != null) {
                    com.mindbloom.ui.AnimationUtil.tabFadeIn(newTab.getContent());
                }
                if (oldTab != null && oldTab.getContent() != null) {
                    
                    com.mindbloom.ui.AnimationUtil.slideInFromLeft(oldTab.getContent());
                }
            } catch (Exception ignored) { }
        });

        
        HBox statusBar = new HBox(10);
        statusBar.setStyle("-fx-background-color: #ecf0f1; -fx-padding: 10;");
        Label statusLabel = new Label("Ready");
        statusBar.getChildren().add(statusLabel);
        ThemeUtil.styleStatusBar(statusBar);
        root.setBottom(statusBar);

        
        try {
            AnimationUtil.slideInFromLeft(tabPane);
            AnimationUtil.fadeIn(welcomeLabel);
            AnimationUtil.pulse(refreshDash);

            AnimationUtil.fadeIn(moodTable);
            AnimationUtil.pulse(addMoodBtn);

            AnimationUtil.fadeIn(habitTable);
            AnimationUtil.pulse(addHabitBtn);
            AnimationUtil.slideInFromLeft(markDoneBtn);

            AnimationUtil.fadeIn(quoteArea);
            AnimationUtil.pulse(randomQuoteBtn);

            AnimationUtil.fadeIn(journalArea);
            AnimationUtil.pulse(saveJournal);

            AnimationUtil.fadeIn(moodChart);
            AnimationUtil.fadeIn(habitChart);
            AnimationUtil.pulse(refreshAnalytics);

            AnimationUtil.pulse(meditatePane);

            AnimationUtil.fadeIn(remindersHistory);
            AnimationUtil.pulse(clearReminders);

            AnimationUtil.fadeIn(streaksList);
            AnimationUtil.pulse(refreshStreaks);

            AnimationUtil.fadeIn(statusLabel);
        } catch (Exception ignored) {
            
        }

        
        try {
            java.util.List<Button> allButtons = new java.util.ArrayList<>();
            allButtons.add(refreshDash);
            allButtons.add(addMoodBtn);
            allButtons.add(addHabitBtn);
            allButtons.add(markDoneBtn);
            allButtons.add(deleteHabitBtn);
            allButtons.add(deleteMoodBtn);
            allButtons.add(randomQuoteBtn);
            allButtons.add(addFavBtn);
            allButtons.add(saveJournal);
            allButtons.add(clearJournal);
            allButtons.add(exportJournal);
            allButtons.add(refreshAnalytics);
            allButtons.add(medStart5);
            allButtons.add(medStart10);
            allButtons.add(medStart20);
            allButtons.add(medStop);
            allButtons.add(clearReminders);
            allButtons.add(refreshStreaks);
            for (Button b : allButtons) {
                if (b != null) {
                    com.mindbloom.ui.AnimationUtil.hoverEffect(b);
                    com.mindbloom.ui.ThemeUtil.styleButton(b);
                }
            }
        } catch (Exception ignored) { }

        return root;
    }

    
    private void shutdown() {
        try {
            StorageService.saveToFile("mindbloom.dat", moodService.getAll(), habitService.getAll());
        } catch (Exception e) {
            System.out.println("Error saving data: " + e.getMessage());
        }

        if (autosaveThread != null) {
            autosaveThread.stopAutoSave();
        }
        if (reminderThread != null) {
            reminderThread.stopReminders();
        }
    }

    
    private static int mapMoodToScore(String mood) {
        if (mood == null) return 5;
        String s = mood.toLowerCase();
        if (s.contains("happy")) return 8;
        if (s.contains("joy") || s.contains("glad") || s.contains("cheer")) return 8;
        if (s.contains("calm") || s.contains("relax")) return 6;
        if (s.contains("grate")) return 7;
        if (s.contains("ener")) return 9;
        if (s.contains("tire") || s.contains("sleep")) return 4;
        if (s.contains("anxi") || s.contains("nerv") || s.contains("stress")) return 3;
        if (s.contains("sad") || s.contains("down") || s.contains("blue")) return 2;
        if (s.contains("angr") || s.contains("mad") || s.contains("irrit")) return 1;
        
        return 5;
    }

    
    private void exportMoodsCSV() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Moods as CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName(currentUser + "_moods.csv");
        java.io.File out = fc.showSaveDialog(primaryStage);
        if (out == null) return;
        try {
            StringBuilder csv = new StringBuilder("Date,Mood,Note\n");
            for (com.mindbloom.model.MoodEntry me : moodService.getAll()) {
                csv.append(me.getDate()).append(",").append(escapeCsv(me.getMood())).append(",").append(escapeCsv(me.getNote())).append("\n");
            }
            java.nio.file.Files.write(out.toPath(), csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Moods exported to " + out.getAbsolutePath(), ButtonType.OK);
            a.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    private static String capitalize(String s) {
        if (s == null || s.isEmpty()) return "";
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    
    private void exportHabitsCSV() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Export Habits as CSV");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        fc.setInitialFileName(currentUser + "_habits.csv");
        java.io.File out = fc.showSaveDialog(primaryStage);
        if (out == null) return;
        try {
            StringBuilder csv = new StringBuilder("Habit,Streak,Last Completed\n");
            for (com.mindbloom.model.Habit h : habitService.getAll()) {
                csv.append(escapeCsv(h.getName())).append(",").append(h.getStreak()).append(",").append(h.getLastCompleted()).append("\n");
            }
            java.nio.file.Files.write(out.toPath(), csv.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Habits exported to " + out.getAbsolutePath(), ButtonType.OK);
            a.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Export failed: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    
    private void importData() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Import Data (CSV or .dat)");
        fc.getExtensionFilters().addAll(
                new javafx.stage.FileChooser.ExtensionFilter("All Files", "*.*"),
                new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"),
                new javafx.stage.FileChooser.ExtensionFilter("Data File", "*.dat")
        );
        java.io.File file = fc.showOpenDialog(primaryStage);
        if (file == null) return;
        String path = file.getAbsolutePath();
        try {
            if (path.toLowerCase().endsWith(".dat")) {
                
                StorageService.loadFromFile(path, moodService, habitService);
                saveData();
                Alert a = new Alert(Alert.AlertType.INFORMATION, "Imported datastore from " + path + ". Please refresh relevant tabs.", ButtonType.OK);
                a.showAndWait();
                return;
            }
            
            java.util.List<String> lines = java.nio.file.Files.readAllLines(file.toPath(), java.nio.charset.StandardCharsets.UTF_8);
            if (lines.isEmpty()) { new Alert(Alert.AlertType.WARNING, "Empty file.", ButtonType.OK).showAndWait(); return; }
            
            javafx.scene.control.ChoiceDialog<String> typeDialog = new javafx.scene.control.ChoiceDialog<>("Moods", java.util.List.of("Moods","Habits"));
            typeDialog.setTitle("Import CSV");
            typeDialog.setHeaderText("Select CSV type to import");
            java.util.Optional<String> choice = typeDialog.showAndWait();
            if (choice.isEmpty()) return;
            String type = choice.get();
            
            javafx.scene.control.ChoiceDialog<String> modeDialog = new javafx.scene.control.ChoiceDialog<>("Append", java.util.List.of("Append","Replace"));
            modeDialog.setTitle("Import Mode");
            modeDialog.setHeaderText("Append to existing data or Replace?");
            java.util.Optional<String> modeOpt = modeDialog.showAndWait();
            if (modeOpt.isEmpty()) return;
            String mode = modeOpt.get();

            if (type.equals("Moods")) {
                java.util.List<com.mindbloom.model.MoodEntry> parsed = new java.util.ArrayList<>();
                for (int i=0;i<lines.size();i++){
                    String line = lines.get(i).trim();
                    if (i==0 && line.toLowerCase().startsWith("date")) continue; 
                    if (line.isEmpty()) continue;
                    String[] cols = parseCsvLine(line);
                    String dateS = cols.length>0?cols[0].trim():"";
                    String mood = cols.length>1?cols[1].trim():"";
                    String note = cols.length>2?cols[2].trim():"";
                    java.time.LocalDate d = null;
                    try { d = java.time.LocalDate.parse(dateS); } catch (Exception ex) { d = java.time.LocalDate.now(); }
                    try {
                        moodService.addEntry(d, mood, note);
                    } catch (Exception ex) {
                        
                    }
                }
            } else {
                
                java.util.List<com.mindbloom.model.Habit> parsedHabits = new java.util.ArrayList<>();
                for (int i=0;i<lines.size();i++){
                    String line = lines.get(i).trim();
                    if (i==0 && line.toLowerCase().startsWith("habit")) continue;
                    if (line.isEmpty()) continue;
                    String[] cols = parseCsvLine(line);
                    String name = cols.length>0?cols[0].trim():null;
                    int streak = 0;
                    try { if (cols.length>1) streak = Integer.parseInt(cols[1].trim()); } catch (Exception ex) { streak = 0; }
                    java.time.LocalDate last = null;
                    try { if (cols.length>2 && !cols[2].trim().isEmpty()) last = java.time.LocalDate.parse(cols[2].trim()); } catch (Exception ex) { last = null; }
                    if (name != null && !name.isEmpty()) {
                        com.mindbloom.model.Habit h = createHabitWithState(name, streak, last);
                        parsedHabits.add(h);
                    }
                }
                if (!parsedHabits.isEmpty()) {
                    if (mode.equals("Replace")) {
                        habitService.addHabit(parsedHabits);
                    } else {
                        for (com.mindbloom.model.Habit h : parsedHabits) {
                            habitService.addHabit(h.getName());
                            
                            try {
                                for (com.mindbloom.model.Habit existing : habitService.getAll()) {
                                    if (existing.getName().equalsIgnoreCase(h.getName())) {
                                        java.lang.reflect.Field sf = com.mindbloom.model.Habit.class.getDeclaredField("streak");
                                        java.lang.reflect.Field lf = com.mindbloom.model.Habit.class.getDeclaredField("lastCompleted");
                                        sf.setAccessible(true); lf.setAccessible(true);
                                        sf.setInt(existing, h.getStreak());
                                        lf.set(existing, h.getLastCompleted());
                                        break;
                                    }
                                }
                            } catch (Exception ex) { }
                        }
                    }
                }
            }
            
            saveData();
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Import complete. Data saved. Refresh tabs if needed.", ButtonType.OK);
            a.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Import failed: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    
    private void saveData() {
        try {
            StorageService.saveToFile(currentUser + "_mindbloom.dat", moodService.getAll(), habitService.getAll());
            Alert a = new Alert(Alert.AlertType.INFORMATION, "Data saved successfully.", ButtonType.OK);
            a.showAndWait();
        } catch (Exception e) {
            Alert a = new Alert(Alert.AlertType.ERROR, "Save failed: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }

    
    private void showAbout() {
        Alert a = new Alert(Alert.AlertType.INFORMATION, 
                "MindBloom v1.0\n\nA mental health and habit tracking application.\n\nFeatures:\n- Mood logging\n- Habit tracking\n- Journal\n- Meditation\n- Analytics",
                ButtonType.OK);
        a.setTitle("About MindBloom");
        a.showAndWait();
    }

    
    private static String escapeCsv(String s) {
        if (s == null) return "";
        if (s.contains(",") || s.contains("\"") || s.contains("\n")) {
            return "\"" + s.replace("\"", "\"\"") + "\"";
        }
        return s;
    }

    private static String[] parseCsvLine(String line) {
        
        java.util.List<String> out = new java.util.ArrayList<>();
        boolean inQuotes = false;
        StringBuilder cur = new StringBuilder();
        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') { inQuotes = !inQuotes; continue; }
            if (c == ',' && !inQuotes) { out.add(cur.toString()); cur.setLength(0); continue; }
            cur.append(c);
        }
        out.add(cur.toString());
        return out.stream().map(String::trim).toArray(String[]::new);
    }

    private static com.mindbloom.model.Habit createHabitWithState(String name, int streak, java.time.LocalDate last) {
        com.mindbloom.model.Habit h = new com.mindbloom.model.Habit(name);
        try {
            java.lang.reflect.Field sf = com.mindbloom.model.Habit.class.getDeclaredField("streak");
            java.lang.reflect.Field lf = com.mindbloom.model.Habit.class.getDeclaredField("lastCompleted");
            sf.setAccessible(true); lf.setAccessible(true);
            sf.setInt(h, streak);
            lf.set(h, last);
        } catch (Exception ignored) {}
        return h;
    }

    public static void main(String[] args) {
        launch(args);
    }
}
