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

    private static final int MAX_ATTEMPTS = 60;
    private static final boolean VERBOSE = false;

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
        List<String> desired = new ArrayList<>(mostDesired);
        desired.addAll(alsoOk);
        String output;

        while (attempts < MAX_ATTEMPTS) {
            attempts++;
            Collections.shuffle(desired);
            for (String newCourse : desired) {
                if (currentCourses.contains(newCourse)) {
                    continue;
                }
                String toDrop = pickCourseToDrop();
                if (toDrop != null) {
                    output = registrar.tryDrop(id, toDrop);
                    if (VERBOSE) {
                        System.out.println(output);
                    }
                    System.out.println(registrar.tryDrop(id, toDrop));
                    // can put sleep here to simulate time between dropping and adding again
                    String addResponse = registrar.tryAdd(id, newCourse);
                    if (VERBOSE) {
                        System.out.println(addResponse);
                    }

                    if (addResponse.startsWith("[INFO] Enrolled student")) {
                        break;
                    } else {
                        output = registrar.tryAdd(id, toDrop);
                        if (VERBOSE) {
                            System.out.println(output);
                        }
                    }
                } else {
                    output = registrar.tryAdd(id, newCourse);
                        if (VERBOSE) {
                            System.out.println(output);
                        }
                }
            }
            if (currentCourses.containsAll(mostDesired)) {
                System.out.println("Student '" + id + "' got into all they're most desired classes in " + attempts
                        + " attempts. Courseload is: " + currentCourses);
                return;
            }

            // time between attempted actions by student threads
            try {
                Student.sleep(random.nextInt(100));
            } catch (InterruptedException ignored) {
            }
        }

        if (currentCourses.containsAll(mostDesired)) {
            System.out.println("After " + MAX_ATTEMPTS + " attempts, student '" + id
                    + "' got into all they're most desired classes. Courseload is: " + currentCourses);
        } else if (desired.containsAll(currentCourses)) {
            System.out.println("After " + MAX_ATTEMPTS + " attempts, student '" + id
                    + "' is at least ok with every course they are enrolled in. Courseload is: " + currentCourses);
        } else {
            System.out.println("After " + MAX_ATTEMPTS + " attempts, student '" + id
                    + "' is enrolled in some courses they are not ok with. Courseload is: " + currentCourses);
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

    public List<String> getMostDesired() {
        return mostDesired;
    }

    public List<String> getOk() {
        return alsoOk;
    }

    public String getStudId() {
        return id;
    }

    public Registrar getRegistrar() {
        return registrar;
    }

    public Set<String> getCurrentCourses() {
        return currentCourses;
    }

}
