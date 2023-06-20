package info.kgeorgiy.ja.piche_kruz.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        List<Student> students = new ArrayList<>();
        students.add(new Student(1, "Олеся", "Чеботарева", GroupName.M3238));
        students.add(new Student(3, "Анастасия", "Тушканова", GroupName.M3238));
        StudentDB consultar = new StudentDB();
        List<Student> nuevaLista = consultar.sortStudentsByName(students);
        for (Student i : nuevaLista) {
            System.out.println(i.getFirstName());
        }
    }
}
