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
 * Represents the registrar that handles course enrollments and drops (SAFE VERSION)
 */
public class Registrar {

    public final Map<String, Course> courses;
    public final Map<String, Student> students;
    public static final int DEFAULT_MAX_CAPACITY = 5;
    public static final int DEFAULT_MAX_CART = 2;

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
     * Tries to add a student to a course. Now thread-safe using synchronized blocks.
     */
    public String tryAdd(String studentId, String courseName) {
        Student stud = students.computeIfAbsent(studentId, k
                -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));

        Course c = getCourseById(courseName);
        if (c == null) {
            return "[ERROR] Course '" + courseName + "' does not exist.\n";
        }

        // locking the Course then Student object, always in this order to prevent potential deadlock.
        synchronized (c) {
            synchronized (stud) {
                if (c.enrolled.contains(studentId)) {
                    return "[WARN] Enrollment failed: Student '" + studentId + "' is already enrolled in course '" + courseName + "'.\n";
                } else if (c.enrolled.size() >= c.capacity) {
                    return "[WARN] Enrollment failed: Course has " + c.capacity + " capacity and " + c.enrolled.size() + " enrolled.\n\tStudent '" + studentId + "' is not enrolled in course '" + courseName + "'.\n";
                } else if (stud.getCurrentCourses().size() >= 4) {
                    return "[WARN] Enrollment failed: Student '" + studentId + "' already has 4 courses.\n";
                } else {
                    c.enrolled.add(studentId);
                    stud.addCurrent(courseName);
                    return "[INFO] Enrolled student '" + studentId + "' in course '" + courseName + "'.\n\tThe enrolled vs capacity ratio is now " + c.enrolled.size() + "/" + c.capacity + ".\n";
                }
            }
        }
    }

    public boolean addPriority(String studentId, String courseName) {
        if (courses.containsKey(courseName)) {
            Student stud = students.computeIfAbsent(studentId, k
                    -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
            synchronized (stud) {
                return stud.addMostDesired(courseName);
            }
        }
        return false;
    }

    public boolean addOk(String studentId, String courseName) {
        if (courses.containsKey(courseName)) {
            Student stud = students.computeIfAbsent(studentId, k
                    -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
            synchronized (stud) {
                return stud.addAlsoOk(courseName);
            }
        }
        return false;
    }

    /**
     * Tries to drop a student from a course (safely).
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

        // locking the Course then Student object, always in this order to prevent potential deadlock.
        synchronized (c) {
            synchronized (stud) {
                stud.dropCurrent(courseName);
                boolean success = c.enrolled.remove(studentId);
                if (success) {
                    return "[INFO] Dropped '" + studentId + "' from '" + courseName + "'.\n";
                }
                return "[WARN] Student '" + studentId + "' not found in course '" + courseName + "'.\n";
            }
        }
    }

    public String getRosterSummary() {
        StringBuilder sb = new StringBuilder();
        for (Course c : courses.values()) {
            synchronized (c) {
                sb.append(c.name).append(" (cap: ").append(c.capacity).append(", enrolled: ")
                        .append(c.enrolled.size()).append(")\n");
                sb.append("   Students: ").append(c.enrolled).append("\n\n");
            }
        }
        return sb.toString();
    }

    public String getCart(String studentId) {
        Student stud = students.computeIfAbsent(studentId, k
                -> new Student(studentId, this, new ArrayList<>(), new ArrayList<>(), new HashSet<>()));
        synchronized (stud) {
            return "Priority: " + stud.getMostDesired() + "\nAlso OK: " + stud.getOk() + "\n\n";
        }
    }

    public List<String> getCourseSubset() {
        List<String> courseNames = new ArrayList<>(courses.keySet());
        Collections.shuffle(courseNames);
        int subsetSize = Math.max(1, courseNames.size() / 3);
        return courseNames.stream()
                .limit(subsetSize)
                .collect(Collectors.toList());
    }

    public String makeRandomStudent(String id) {
        List<String> shuffled = new ArrayList<>(courses.keySet());
        Collections.shuffle(shuffled);

        int total = shuffled.size();
        int desiredCount = Math.min(DEFAULT_MAX_CART, total);
        int okCount = Math.min(DEFAULT_MAX_CART, total);

        List<String> mostDesired = new ArrayList<>(shuffled.subList(0, Math.min(desiredCount, total)));
        List<String> alsoOk = new ArrayList<>(shuffled.subList(Math.min(desiredCount, total), Math.min(desiredCount + okCount, total)));

        Student stud = new Student(id, this, mostDesired, alsoOk, new HashSet<>());
        students.put(id, stud);
        return "[AUTO] Created student '" + id + "'.\n";
    }
}
