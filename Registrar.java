package Lab3;

import java.util.Map;

/**
  * Represents the registrar that handles course enrollments and drops.
  */
 public class Registrar {
     public final Map<String, Course> courses;
 
     public Registrar(Map<String, Course> courses) {
         this.courses = courses;
     }
 
     /**
      * Tries to add a student to a course. No checks for concurrency issues.
      */
     public boolean tryAdd(String studentId, String courseName) {
         Course c = courses.get(courseName);
         if (c.enrolled.contains(studentId)) return false;
         if (c.enrolled.size() >= c.capacity) return false;
         c.enrolled.add(studentId);
         return true;
     }
 
     /**
      * Tries to drop a student from a course.
      */
     public void tryDrop(String studentId, String courseName) {
         Course c = courses.get(courseName);
         c.enrolled.remove(studentId);
     }
 }