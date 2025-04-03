// package Lab3;
package com.example;

import java.util.ArrayList;

/**
 * Lab 3: Concurrency in a Simple Application Version: Unsafe (Contains a Data
 * Race) Description: Multithreaded course registration system simulation.
 * Students attempt to switch courses with no concurrency protection.
 */
import java.util.List;

/**
 * Represents a course with a name, max capacity, and list of enrolled students.
 */
public class Course {

    public final String name;
    public final int capacity;
    public final List<String> enrolled;

    public static int numCourses = 0;

    public Course(String name, int capacity) {
        this.name = name;
        this.capacity = capacity;
        this.enrolled = new ArrayList<>();
        numCourses++;
    }

    @Override
    public String toString() {
        return name + " (" + enrolled.size() + "/" + capacity + ")";
    }
}
