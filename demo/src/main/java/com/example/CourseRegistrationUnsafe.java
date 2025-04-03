// package Lab3;
package com.example;

import java.util.HashMap;
import java.util.Map;

import java.awt.GridLayout;
import java.awt.BorderLayout;
import java.awt.Font;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * Provides a GUI for manually managing courses and student registrations.
 */
class RegistrationGUI {

    private static final int AUTO_STUDENTS_LOOP = 100;
    private static final int AUTO_COURSES_LOOP = 100;
    private final Registrar registrar;
    private final JFrame frame;
    private final JTextArea output;
    private final Random random = new Random();

    public RegistrationGUI(Registrar registrar) {
        this.registrar = registrar;
        this.frame = new JFrame("Course Registration GUI");
        this.output = new JTextArea(20, 60);
        setupUI();
    }

    private void setupUI() {
        JPanel mainPanel = new JPanel(new BorderLayout());

        JPanel inputPanel = new JPanel(new GridLayout(13, 2, 5, 5));
        inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JTextField courseNameField = new JTextField();
        JTextField capacityField = new JTextField();
        JButton addCourseBtn = new JButton("Add Course");
        JButton autoCourseBtn = new JButton("Auto-generate " + AUTO_COURSES_LOOP + " Course(s)");

        JTextField studentIdField = new JTextField();
        JTextField targetCourseField = new JTextField();
        JButton enrollBtn = new JButton("Enroll Student");
        JButton dropBtn = new JButton("Drop Student");
        JButton autoStudentBtn = new JButton("Auto-generate " + AUTO_STUDENTS_LOOP + " Random Student(s)");

        JButton refreshBtn = new JButton("Refresh Roster View");

        JTextField shoppingCourseField = new JTextField();
        JButton addPriorityBtn = new JButton("Add to shopping cart as priority");
        JButton addOkBtn = new JButton("Add to shopping cart");
        JButton viewCartBtn = new JButton("View student cart");

        JButton startStudentsBtn = new JButton("Start all student threads");

        inputPanel.add(new JLabel("Course Name:"));
        inputPanel.add(courseNameField);
        inputPanel.add(new JLabel("Capacity:"));
        inputPanel.add(capacityField);
        inputPanel.add(addCourseBtn);
        inputPanel.add(autoCourseBtn);

        inputPanel.add(new JLabel("Student ID:"));
        inputPanel.add(studentIdField);
        inputPanel.add(new JLabel("Target Course:"));
        inputPanel.add(targetCourseField);
        inputPanel.add(enrollBtn);
        inputPanel.add(dropBtn);
        inputPanel.add(new JLabel(""));
        inputPanel.add(autoStudentBtn);

        inputPanel.add(new JLabel("Course to Add to Cart:"));
        inputPanel.add(shoppingCourseField);
        inputPanel.add(addPriorityBtn);
        inputPanel.add(addOkBtn);

        inputPanel.add(viewCartBtn);
        inputPanel.add(startStudentsBtn);
        inputPanel.add(new JLabel(""));
        inputPanel.add(refreshBtn);

        output.setEditable(false);
        output.setFont(new Font("Monospaced", Font.PLAIN, 12));
        JScrollPane scroll = new JScrollPane(output);

        mainPanel.add(inputPanel, BorderLayout.NORTH);
        mainPanel.add(scroll, BorderLayout.CENTER);

        frame.getContentPane().add(mainPanel);

        addCourseBtn.addActionListener(e -> {
            String name = courseNameField.getText().trim();
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText().trim());
            } catch (NumberFormatException ex) {
                appendOutput("[ERROR] Invalid capacity.\n");
                return;
            }
            if (registrar.courses.containsKey(name)) {
                appendOutput("[ERROR] Course '" + name + "'' already exists.\n");
            } else {
                registrar.courses.put(name, new Course(name, capacity));
                appendOutput("[INFO] Course '" + name + "' added with capacity " + capacity + ".\n");
            }
        });

        enrollBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String course = targetCourseField.getText().trim();
            appendOutput(registrar.tryAdd(studentId, course));
        });

        dropBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String course = targetCourseField.getText().trim();
            appendOutput(registrar.tryDrop(studentId, course));
        });

        addPriorityBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String course = shoppingCourseField.getText().trim();
            if (registrar.addPriority(studentId, course)) {
                appendOutput("[INFO] Added '" + course + "' to '" + studentId + "'s priority cart.\n");
            } else {
                appendOutput("[ERROR] Course '" + course + "' does not exist.\n");
            }
        });

        addOkBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String course = shoppingCourseField.getText().trim();
            if (registrar.addOk(studentId, course)) {
                appendOutput("[INFO] Added '" + course + "' to '" + studentId + "'s okay cart.\n");
            } else {
                appendOutput("[ERROR] Course '" + course + "' does not exist.\n");
            }
        });

        viewCartBtn.addActionListener(e -> {
            String studentId = studentIdField.getText().trim();
            String cart = registrar.getCart(studentId);
            appendOutput("\n===== Cart for '" + studentId + "' =====\n");
            appendOutput(cart);
        });

        startStudentsBtn.addActionListener(e -> {
            for (Map.Entry<String, Student> entry : registrar.students.entrySet()) {
                Student old = entry.getValue();
                // Make a fresh thread with the same data
                Student fresh = new Student(old.getStudId(), old.getRegistrar(),
                        new ArrayList<>(old.getMostDesired()),
                        new ArrayList<>(old.getOk()),
                        new HashSet<>(old.getCurrentCourses())
                );
                registrar.students.put(entry.getKey(), fresh); // replace with fresh thread
                fresh.start();
            }
            for (Student s : registrar.students.values()) {
                try {
                    s.join();
                } catch (InterruptedException ignored) {
                }
            }
            System.out.println("Student threads done.");
        });

        refreshBtn.addActionListener(e -> {
            appendOutput("\n===== Course Roster View =====\n");
            appendOutput(registrar.getRosterSummary());
        });

        autoCourseBtn.addActionListener(e -> {
            for (int i = 0; i < AUTO_COURSES_LOOP; i++) {
                String name = "CS" + (Course.numCourses + 1);
                int capacity = random.nextInt(Registrar.DEFAULT_MAX_CAPACITY) + 1;
                registrar.courses.put(name, new Course(name, capacity));
                appendOutput("[AUTO] Added course '" + name + "' with capacity " + capacity + ".\n");
            }
        });

        autoStudentBtn.addActionListener(e -> {
            for (int i = 0; i < AUTO_STUDENTS_LOOP; i++) {
                String id = "S" + (Student.numStudents + 1);
                appendOutput(registrar.makeRandomStudent(id));
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }

    private void appendOutput(String text) {
        output.append(text);
        output.setCaretPosition(output.getDocument().getLength());
    }
}

/**
 * Main class to run the simulation with multiple student threads.
 */
public class CourseRegistrationUnsafe {

    public static void main(String[] args) {
        Map<String, Course> courseMap = new HashMap<>();
        Registrar registrar = new Registrar(courseMap, null);
        new RegistrationGUI(registrar);
    }
}
