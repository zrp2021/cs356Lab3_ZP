package Lab3;

import java.util.HashMap;
import java.util.Map;

import java.awt.GridLayout;
import java.awt.BorderLayout;
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
    private final Registrar registrar;
    private final JFrame frame;
    private final JTextArea output;

    public RegistrationGUI(Registrar registrar) {
        this.registrar = registrar;
        this.frame = new JFrame("Course Registration GUI");
        this.output = new JTextArea(10, 40);
        setupUI();
    }

    private void setupUI() {
        JPanel panel = new JPanel(new GridLayout(6, 2));

        JTextField courseNameField = new JTextField();
        JTextField capacityField = new JTextField();
        JButton addCourseBtn = new JButton("Add Course");

        JTextField studentIdField = new JTextField();
        JTextField targetCourseField = new JTextField();
        JButton enrollBtn = new JButton("Enroll Student");

        panel.add(new JLabel("Course Name:"));
        panel.add(courseNameField);
        panel.add(new JLabel("Capacity:"));
        panel.add(capacityField);
        panel.add(addCourseBtn);

        panel.add(new JLabel("Student ID:"));
        panel.add(studentIdField);
        panel.add(new JLabel("Target Course:"));
        panel.add(targetCourseField);
        panel.add(enrollBtn);

        output.setEditable(false);
        JScrollPane scroll = new JScrollPane(output);

        frame.getContentPane().add(panel, BorderLayout.NORTH);
        frame.getContentPane().add(scroll, BorderLayout.CENTER);

        addCourseBtn.addActionListener(e -> {
            String name = courseNameField.getText();
            int capacity;
            try {
                capacity = Integer.parseInt(capacityField.getText());
            } catch (NumberFormatException ex) {
                output.append("Invalid capacity.\n");
                return;
            }
            if (registrar.courses.containsKey(name)) {
                output.append("Course already exists.\n");
            } else {
                registrar.courses.put(name, new Course(name, capacity));
                output.append("Course " + name + " added.\n");
            }
        });

        enrollBtn.addActionListener(e -> {
            String studentId = studentIdField.getText();
            String course = targetCourseField.getText();
            if (!registrar.courses.containsKey(course)) {
                output.append("Course does not exist.\n");
            } else {
                boolean success = registrar.tryAdd(studentId, course);
                if (success) {
                    output.append("Enrolled " + studentId + " in " + course + ".\n");
                } else {
                    output.append("Failed to enroll " + studentId + " in " + course + ".\n");
                }
            }
        });

        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}

/**
 * Main class to run the simulation with multiple student threads.
 */
public class CourseRegistrationUnsafe {
    public static void main(String[] args) {
        Map<String, Course> courseMap = new HashMap<>();
        Registrar registrar = new Registrar(courseMap);
        new RegistrationGUI(registrar);
    }
}