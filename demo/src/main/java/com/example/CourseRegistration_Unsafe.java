// package Lab3;
package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Main class to run the simulation with multiple student threads.
 */
public class CourseRegistration_Unsafe {

    public static void main(String[] args) {
        Map<String, Course> courseMap = new HashMap<>();
        for (int i = 0; i < 5; i++) {
            courseMap.put("CS" + i, new Course("CS" + i, 3));
        }

        Registrar registrar = new Registrar(courseMap, null);

        List<Student> students = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            String id = "S" + i;
            List<String> most = List.of("CS0", "CS1");
            List<String> ok = List.of("CS2", "CS3", "CS4");
            Set<String> start = new HashSet<>(List.of("CS2", "CS3", "CS4", "CS1"));
            for (String s : start) {
                registrar.tryAdd(id, s);
            }
            students.add(new Student(id, registrar, most, ok, start));
        }

        for (Student s : students) {
            s.start();
        }
        for (Student s : students) {
            try {
                s.join();
            } catch (InterruptedException ignored) {
            }
        }
    }
}
