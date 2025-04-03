    // package Lab3;
package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Represents the registrar that handles course enrollments and drops
 */
public class Registrar {

    public final Map<String, Course> courses;
    public final Map<String, Student> students;
    public static final int DEFAULT_MAX_CAPACITY = 5;

    public Registrar(Map<String, Course> courses, Map<String, Student> students) {
        this.courses = courses;
        if (students != null) {
            this.students = students;
        } else {
            this.students = new HashMap<>();
        }
    }

    public Course getCourseById(String courseName) {
        return courses.get(courseName);
    }

    /**
     * Tries to add a student to a course. No checks for concurrency issues
     */
    public String tryAdd(String studentId, String courseName) {
        // make new Student if not already made
        Student stud = students.computeIfAbsent(studentId, k
                -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));

        if (!courses.containsKey(courseName)) {
            return "[ERROR] Course '" + courseName + "' does not exist.\n";
        } else {
            Course c = getCourseById(courseName);
            if (c.enrolled.contains(studentId)) {
                return "[WARN] Enrollment failed: Student '" + studentId + "' is already enrolled in course '" + courseName + "'.\n";
            } else if (c.enrolled.size() >= c.capacity) {
                return "[WARN] Enrollment failed: Course has " + c.capacity + " capacity and "
                        + c.enrolled.size() + " enrolled.\n\tStudent '" + studentId + "' is not enrolled in course '" + courseName + "'.\n";
            } else {
                // delay to expose data race. Will allow too many students to join the course
                try {
                    Student.sleep(30);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                c.enrolled.add(studentId);

                stud.addCurrent(courseName);
                return "[INFO] Enrolled student '" + studentId + "' in course '" + courseName
                        + "'.\n\tNew enrolled/capacity is " + c.enrolled.size() + "/" + c.capacity + ".\n";
            }
        }
    }

    public boolean addPriority(String studentId, String courseName) {
        if (courses.containsKey(courseName)) {
            Student stud = students.computeIfAbsent(studentId, k
                    -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
            return stud.addMostDesired(courseName);
        }
        return false;
    }

    public boolean addOk(String studentId, String courseName) {
        if (courses.containsKey(courseName)) {
            Student stud = students.computeIfAbsent(studentId, k
                    -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
            return stud.addAlsoOk(courseName);
        }
        return false;
    }

    /**
     * Tries to drop a student from a course
     */
    public String tryDrop(String studentId, String courseName) {
        Student stud = students.get(studentId);
        if (stud == null) {
            return "[ERROR] No student found with student id: " + studentId + "'.\n";
        }

        Course c = courses.get(courseName);
        if (c == null) {
            return "[ERROR] Course '" + courseName + "' does not exist.\n";
        }

        stud.dropCurrent(courseName);

        // delay to expose data race. Student thinks they've dropped the course, but still enrolled in shared resource.
        try {
            Student.sleep(30);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean success = c.enrolled.remove(studentId);
        if (success) {
            return "[INFO] Dropped '" + studentId + "' from '" + courseName + "'.\n";
        }
        return "[WARN] Student '" + studentId + "' not found in course '" + courseName + "'.\n";
    }

    /**
     * Returns a string summary of all course rosters
     */
    public String getRosterSummary() {
        StringBuilder sb = new StringBuilder();
        for (Course c : courses.values()) {
            sb.append(c.name).append(" (cap: ").append(c.capacity).append(", enrolled: ")
                    .append(c.enrolled.size()).append(")\n");
            sb.append("   Students: ").append(c.enrolled).append("\n\n");
        }
        return sb.toString();
    }

    public String getCart(String studentId) {
        Student stud = students.computeIfAbsent(studentId, k
                -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
        return "Priority: " + stud.getMostDesired() + "\nAlso OK: " + stud.getOk() + "\n\n";
    }

    public List<String> getCourseSubset() {
        List<String> courseNames = new ArrayList<>(courses.keySet());
        Collections.shuffle(courseNames);

        int subsetSize = courseNames.size() / 3;
        return courseNames.stream()
                .limit(subsetSize)
                .collect(Collectors.toList());
    }

    /**
     * make new student and add to students Map
     */
    public Student makeRandomStudent(String id) {
        List<String> shuffled = new ArrayList<>(courses.keySet());
        Collections.shuffle(shuffled);

        int total = shuffled.size();
        int desiredCount = Math.max(1, total / 3);
        int okCount = Math.max(1, total / 3);

        List<String> mostDesired = new ArrayList<>(shuffled.subList(0, Math.min(desiredCount, total)));
        List<String> alsoOk = new ArrayList<>(shuffled.subList(Math.min(desiredCount, total), Math.min(desiredCount + okCount, total)));

        Student stud = new Student(id, this, mostDesired, alsoOk, new HashSet<>());
        students.put(id, stud);
        return stud;
    }
}
