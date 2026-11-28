package course;

import course.model.Course;
import course.model.Student;
import course.service.CourseService;
import course.store.DataStore;
import course.ui.ConsoleApp;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * 程序入口：读文件 → 重建人数 → 登录与菜单 → 正常退出时写回文件。
 *
 * <p>数据文件放在「当前工作目录」，所以务必在项目根目录运行（或直接用 run.ps1）。
 */
public class Main {

    private static final int STUDENT_COUNT = 50;
    private static final int FIRST_STUDENT_ID = 2024001;

    public static void main(String[] args) {
        DataStore store = new DataStore(Path.of("."));
        try {
            List<Course> courses = store.loadCourses();
            Map<String, List<String>> selections = store.loadStudentSelections();

            CourseService service = new CourseService(courses, buildStudents(selections));
            service.rebuildEnrollment();

            new ConsoleApp(service, new Scanner(System.in)).run();

            store.saveCourses(service.getCourses());
            store.saveStudentSelections(service.getStudents());
            System.out.println("数据已保存：" + store.getCoursesFile().toAbsolutePath()
                    + " / " + store.getStudentsFile().toAbsolutePath());
        } catch (IOException e) {
            System.out.println("读写数据文件失败：" + e.getMessage());
            System.out.println("请检查文件是否被占用，或是否有写入权限。");
        }
    }

    /** 50 名预置学生（2024001—2024050），再用 students.txt 里的记录覆盖选课列表。 */
    private static Map<String, Student> buildStudents(Map<String, List<String>> selections) {
        Map<String, Student> students = new LinkedHashMap<>();
        for (int i = 0; i < STUDENT_COUNT; i++) {
            String id = String.valueOf(FIRST_STUDENT_ID + i);
            students.put(id, new Student(id));
        }
        selections.forEach((studentId, courseIds) -> {
            Student student = students.get(studentId);
            if (student == null) {
                System.out.println("[警告] students.txt 中的学号 " + studentId
                        + " 不在 " + FIRST_STUDENT_ID + "—" + (FIRST_STUDENT_ID + STUDENT_COUNT - 1)
                        + " 范围内，已忽略。");
                return;
            }
            courseIds.forEach(student::addCourse);
        });
        return students;
    }
}
