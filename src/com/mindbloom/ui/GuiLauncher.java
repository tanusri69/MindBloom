package com.mindbloom.ui;

import com.mindbloom.service.MoodTrackerService;
import com.mindbloom.service.HabitTrackerService;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.time.LocalDate;


public class GuiLauncher {
    private static final MoodTrackerService moodService = new MoodTrackerService();
    private static final HabitTrackerService habitService = new HabitTrackerService();

    public static void main(String[] args) {
        
        try {
            Class<?> fxClass = Class.forName("com.mindbloom.ui.MindBloomApp");
            System.out.println("JavaFX application class found; attempting to launch JavaFX app...");
            
        } catch (Throwable t) {
            SwingUtilities.invokeLater(GuiLauncher::createAndShowGui);
        }
    }

    private static void createAndShowGui() {
        JFrame frame = new JFrame("MindBloom — Quick GUI (Swing fallback)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 520);
        frame.setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(10, 10));
        root.setBorder(new EmptyBorder(12, 12, 12, 12));

        JLabel title = new JLabel("MindBloom", SwingConstants.LEFT);
        title.setFont(title.getFont().deriveFont(Font.BOLD, 24f));
        root.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(2, 3, 12, 12));

        center.add(makeCard("Dashboard", e -> openDashboard()));
        center.add(makeCard("Mood Log", e -> openMoodLog()));
        center.add(makeCard("Journal", e -> showMessage("Journal", "Not implemented in fallback.")));
        center.add(makeCard("Habit Garden", e -> openHabitsWindow()));
        center.add(makeCard("Meditation", e -> showMessage("Meditation", "Not implemented in fallback.")));
        center.add(makeCard("Analytics", e -> showMessage("Analytics", "Not implemented in fallback.")));

        root.add(center, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton exit = new JButton("Exit");
        exit.addActionListener(e -> System.exit(0));
        bottom.add(exit);
        root.add(bottom, BorderLayout.SOUTH);

        frame.setContentPane(root);
        frame.setVisible(true);
    }

    private static JPanel makeCard(String title, java.util.function.Consumer<ActionEvent> onClick) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Color.LIGHT_GRAY), new EmptyBorder(8,8,8,8)));
        JLabel l = new JLabel(title, SwingConstants.CENTER);
        l.setFont(l.getFont().deriveFont(16f));
        JButton b = new JButton("Open");
        b.addActionListener(onClick::accept);
        p.add(l, BorderLayout.CENTER);
        p.add(b, BorderLayout.SOUTH);
        return p;
    }

    private static void openDashboard() {
        showMessage("Dashboard", "Welcome to MindBloom\n(Quick Swing Dashboard)");
    }

    private static void openMoodLog() {
        JDialog d = new JDialog((Frame) null, "Mood Log", true);
        d.setSize(480, 300);
        d.setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(8,8,8,8));

        JPanel input = new JPanel(new GridLayout(3,2,8,8));
        input.add(new JLabel("Mood:"));
        JTextField moodField = new JTextField();
        input.add(moodField);
        input.add(new JLabel("Note:"));
        JTextField noteField = new JTextField();
        input.add(noteField);
        input.add(new JLabel("Date (YYYY-MM-DD) or blank for today:"));
        JTextField dateField = new JTextField();
        input.add(dateField);

        p.add(input, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton save = new JButton("Save");
        save.addActionListener(ev -> {
            String mood = moodField.getText().trim();
            String note = noteField.getText().trim();
            String dateText = dateField.getText().trim();
            try {
                LocalDate date = dateText.isEmpty() ? LocalDate.now() : LocalDate.parse(dateText);
                moodService.addEntry(date, mood, note);
                JOptionPane.showMessageDialog(d, "Mood saved: " + mood);
                d.dispose();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(ev -> d.dispose());
        buttons.add(cancel);
        buttons.add(save);
        p.add(buttons, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private static void openHabitsWindow() {
        JDialog d = new JDialog((Frame) null, "Habits", true);
        d.setSize(520, 360);
        d.setLocationRelativeTo(null);
        JPanel p = new JPanel(new BorderLayout(8,8));
        p.setBorder(new EmptyBorder(8,8,8,8));

        DefaultListModel<String> model = new DefaultListModel<>();
        habitService.getAll().forEach(h -> model.addElement(h.getName() + " (Streak: " + h.getStreak() + ")"));
        JList<String> list = new JList<>(model);
        p.add(new JScrollPane(list), BorderLayout.CENTER);

        JPanel controls = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JTextField newHabit = new JTextField(18);
        JButton add = new JButton("Add Habit");
        add.addActionListener(ev -> {
            String name = newHabit.getText().trim();
            if (!name.isEmpty()) {
                habitService.addHabit(name);
                model.addElement(name + " (Streak: 0)");
                newHabit.setText("");
            }
        });
        JButton tick = new JButton("Mark Done");
        tick.addActionListener(ev -> {
            String sel = list.getSelectedValue();
            if (sel == null) { JOptionPane.showMessageDialog(d, "Select a habit first."); return; }
                String name = sel.split(" \\(")[0];
            try {
                habitService.tickHabit(name);
                
                model.clear();
                habitService.getAll().forEach(h -> model.addElement(h.getName() + " (Streak: " + h.getStreak() + ")"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(d, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        controls.add(newHabit);
        controls.add(add);
        controls.add(tick);
        p.add(controls, BorderLayout.SOUTH);

        d.setContentPane(p);
        d.setVisible(true);
    }

    private static void showMessage(String title, String msg) {
        JOptionPane.showMessageDialog(null, msg, title, JOptionPane.INFORMATION_MESSAGE);
    }
}
