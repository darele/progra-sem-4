package info.kgeorgiy.ja.piche_kruz.student;

import info.kgeorgiy.java.advanced.student.GroupName;
import info.kgeorgiy.java.advanced.student.Student;
import info.kgeorgiy.java.advanced.student.StudentQuery;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class StudentDB implements StudentQuery {
    private final Comparator<? super Student> BY_NAME_COMPARATOR =
            Comparator.comparing(Student::getLastName, String::compareTo).reversed()
                    .thenComparing(Comparator.comparing(Student::getFirstName, String::compareTo).reversed())
                    .thenComparingInt(Student::getId);

    private Stream<Student> getSortedStreamFromCollection(Collection<Student> coll, Comparator<? super Student> comparator) {
        return coll.stream().sorted(comparator);
    }

    private <T> Stream<T> getAttributeStream(Collection<Student> students, Function<Student, T> f) {
        return students.stream().map(f);
    }

    private <T> List<T> getAttributeList(List<Student> students, Function<Student, T> f) {
        return getAttributeStream(students, f).toList();
    }

    private <T> List<Student> filterGroupAndOrder(Collection<Student> students, Function<Student, T> f, T compareValue) {
        return students.stream().filter(getStudentPredicate(f, compareValue)).sorted(BY_NAME_COMPARATOR).toList();
    }

    private static <T> Predicate<Student> getStudentPredicate(Function<Student, T> f, T compareValue) {
        return a -> f.apply(a).equals(compareValue);
    }

    @Override
    public List<String> getFirstNames(List<Student> students) {
        return getAttributeList(students, Student::getFirstName);
    }

    @Override
    public List<String> getLastNames(List<Student> students) {
        return getAttributeList(students, Student::getLastName);
    }

    @Override
    public List<GroupName> getGroups(List<Student> students) {
        return getAttributeList(students, Student::getGroup);
    }

    @Override
    public List<String> getFullNames(List<Student> students) {
        return getAttributeList(students, a -> a.getFirstName() + " " + a.getLastName());
    }

    @Override
    public Set<String> getDistinctFirstNames(List<Student> students) {
        return getAttributeStream(students, Student::getFirstName).collect(Collectors.toCollection(TreeSet::new));
    }

    @Override
    public String getMaxStudentFirstName(List<Student> students) {
        return students.stream().max(Student::compareTo).map(Student::getFirstName).orElse("");
    }

    @Override
    public List<Student> sortStudentsById(Collection<Student> students) {
        return getSortedStreamFromCollection(students, Student::compareTo).toList();
    }

    @Override
    public List<Student> sortStudentsByName(Collection<Student> students) {
        return getSortedStreamFromCollection(students, BY_NAME_COMPARATOR).toList();
    }

    @Override
    public List<Student> findStudentsByFirstName(Collection<Student> students, String name) {
        return filterGroupAndOrder(students, Student::getFirstName, name);
    }

    @Override
    public List<Student> findStudentsByLastName(Collection<Student> students, String name) {
        return filterGroupAndOrder(students, Student::getLastName, name);
    }

    @Override
    public List<Student> findStudentsByGroup(Collection<Student> students, GroupName group) {
        return filterGroupAndOrder(students, Student::getGroup, group);
    }

    @Override
    public Map<String, String> findStudentNamesByGroup(Collection<Student> students, GroupName group) {
        return students.stream()
                .filter(s -> s.getGroup().equals(group))
                .collect(Collectors.toMap(
                        Student::getLastName,
                        Student::getFirstName,
                        BinaryOperator.minBy(String::compareTo)));
    }
}
