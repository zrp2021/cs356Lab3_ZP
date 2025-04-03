// package Lab3;
package com.example;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Represents a student running in its own thread, trying to improve its course
 * schedule.
 */
public class Student extends Thread {

    private final String id;
    private final Registrar registrar;
    private final List<String> mostDesired;
    private final List<String> alsoOk;
    private final Set<String> currentCourses;
    private final Random random;

    public static int numStudents = 0;

    public Student(String id, Registrar registrar, List<String> mostDesired, List<String> alsoOk, Set<String> initialCourses) {
        this.id = id;
        this.registrar = registrar;
        this.mostDesired = mostDesired;
        this.alsoOk = alsoOk;
        this.currentCourses = initialCourses;
        this.random = new Random();
        numStudents++;
    }

    public boolean addMostDesired(String courseName) {
        return mostDesired.add(courseName);
    }

    public boolean addAlsoOk(String courseName) {
        return alsoOk.add(courseName);
    }

    public boolean addCurrent(String courseName) {
        return currentCourses.add(courseName);
    }

    public boolean dropCurrent(String courseName) {
        return currentCourses.remove(courseName);
    }

    @Override
    public void run() {
        int attempts = 0;
        while (attempts < 60) {
            attempts++;
            List<String> desired = new ArrayList<>(mostDesired);
            desired.addAll(alsoOk);
            Collections.shuffle(desired);
            for (String newCourse : desired) {
                if (currentCourses.contains(newCourse)) {
                    continue;
                }
                String toDrop = pickCourseToDrop();
                if (toDrop != null) {
                    registrar.tryDrop(id, toDrop);
                    // can put sleep here to simulate time between dropping and adding again
                    String addResponse = registrar.tryAdd(id, newCourse);
                    if (addResponse.startsWith("[INFO] Enrolled student")) {
                        break;
                    } else {
                        registrar.tryAdd(id, toDrop);
                    }
                } else {
                    registrar.tryAdd(id, newCourse);
                }
            }
            if (currentCourses.containsAll(mostDesired)) {
                System.out.println(id + " :-) " + currentCourses);
                return;
            }

            // time between attempted actions by student threads
            try {
                Student.sleep(random.nextInt(100));
            } catch (InterruptedException ignored) {
            }
        }
        if (currentCourses.containsAll(mostDesired) || currentCourses.containsAll(alsoOk)) {
            System.out.println(id + " :-| " + currentCourses);
        } else {
            System.out.println(id + " :-( " + currentCourses);
        }
    }

    private String pickCourseToDrop() {
        for (String c : currentCourses) {
            if (!mostDesired.contains(c)) {
                return c;
            }
        }
        return null;
    }

    @SuppressWarnings("rawtypes")
    public List getMostDesired() {
        return mostDesired;
    }

    @SuppressWarnings("rawtypes")
    public List getOk() {
        return alsoOk;
    }
}
