package course.service;

import course.model.Course;
import course.model.Student;
import course.model.TimeSlot;

import java.util.*;

/**
 * 业务层：课程浏览、选课、退课、课表。
 *
 * <p>为什么把这层单独拆出来：菜单只负责读输入、打印输出；人数统计、冲突判断、一致性维护
 * 都集中在这里。面试时面试官改一个边界条件（比如“同节次算冲突”），你只需要动一个方法。
 */
public class CourseService {

    private final List<Course> courses;
    private final Map<String, Student> students;

    public CourseService(List<Course> courses, Map<String, Student> students) {
        this.courses = courses;
        this.students = students;
    }

    public List<Course> getCourses() {
        return courses;
    }

    public Collection<Student> getStudents() {
        return students.values();
    }

    public boolean containsStudent(String studentId) {
        return students.containsKey(studentId);
    }

    public Student student(String studentId) {
        return students.get(studentId);
    }

    /** 大小写不敏感地查课程，找不到返回 null。 */
    public Course findCourse(String rawCourseId) {
        String id = Course.normalizeId(rawCourseId);
        for (Course course : courses) {
            if (course.getId().equals(id)) {
                return course;
            }
        }
        return null;
    }

    /**
     * 浏览课程：学分降序，学分相同按课程编号升序。
     *
     * <p>注意 Comparator 链的写法：先按学分降序，再用 thenComparing 处理平手情况，
     * 保证排序结果稳定、可复现（面试官很可能用同分课程验证）。
     */
    public List<Course> sortedCourses() {
        return courses.stream()
                .sorted(Comparator.comparingDouble(Course::getCredit).reversed()
                        .thenComparing(Course::getId))
                .toList();
    }

    /**
     * 选课。
     *
     * <p>TODO 待你实现，按这个顺序校验，失败时抛 EnrollmentException，且不修改任何数据：
     * <ol>
     *   <li>课程编号不存在；</li>
     *   <li>课程已满（enrolled &gt;= capacity）；</li>
     *   <li>本人已选过该课；</li>
     *   <li>与本人已选课程时间冲突（TimeSlot.conflictsWith）。</li>
     * </ol>
     * 全部通过后：course.enrollOne() 且 student.addCourse(id)。
     *
     * <p>注意失败路径不能留下“人数 +1 但课表没加”这种半成品状态。
     */
    public void enroll(Student student, String rawCourseId) throws EnrollmentException {
        Course course = findCourse(rawCourseId);

        if (course == null) {
            throw new EnrollmentException("课程编号不存在: " + rawCourseId);
        }if(course.isFull()){
            throw new EnrollmentException("课程已满: " + course.getName());
        }
        if(student.hasCourse(course.getId())) {
            throw new EnrollmentException("本人已选过该课: " + course.getName());
        }
        for(String c : student.getCourseIds()){
            Course selected = findCourse(c);
            if(selected == null){
                continue;
            }
            if (course.getTimeSlot().conflictsWith(selected.getTimeSlot())) {
                throw new EnrollmentException("时间冲突：与 " + selected.getId() + " "
                        + selected.getName() + "（" + selected.getTimeSlot().getText() + "）冲突");
            }
        }
        // 通过了所有校验，开始修改数据
        course.enrollOne();
        student.addCourse(course.getId());
    }

    /**
     * 退课。
     *
     * <p>TODO 待你实现：本人没选该课 → EnrollmentException("你未选该课程")；
     * 否则 course.dropOne() 且 student.removeCourse(id)。
     * 课程编号不存在同样要给出明确提示。
     */
    public void drop(Student student, String rawCourseId) throws EnrollmentException {
        Course course = findCourse(rawCourseId);
        if (course == null) {
            throw new EnrollmentException("课程编号不存在: " + rawCourseId);
        }
        if (!student.hasCourse(course.getId())) {
            throw new EnrollmentException("你未选该课程: " + rawCourseId);
        }
        course.dropOne();
        student.removeCourse(course.getId());

    }

    /**
     * 个人课表：按周一 → 周日、同一天按开始节次升序。
     *
     * <p>TODO 待你实现：从 student.getCourseIds() 找到对应 Course，过滤掉已不存在的课程，
     * 然后按 (dayOfWeek, startPeriod) 排序返回。空课表返回空集合，由 UI 层决定怎么提示。
     */
    public List<Course> timetable(Student student) {
        List<Course> selected = student.getCourseIds().stream()
                .map(this::findCourse)
                .filter(course -> course != null)
                .toList();
        return selected.stream()
                .sorted(Comparator.comparingInt((Course c) -> c.getTimeSlot().getDay())
                        .thenComparingInt(c -> c.getTimeSlot().getStartPeriod()))
                .toList();

    }

    /**
     * 启动时用「学生选课记录」重算每门课的已选人数。
     *
     * <p>设计决策：courses.txt 里的“已选人数”只是快照，唯一真相是 students.txt。
     * 重算可以同时修掉三类不一致：人数被手改、学生记录里出现不存在的课程、课程已满但记录仍在。
     * 这里直接改写学生记录并打印警告，保证重启后课程人数与课表永远对得上。
     */
    public void rebuildEnrollment() {
        for (Course course : courses) {
            course.resetEnrolled();
        }
        for (Student student : students.values()){
            List<String> invalid = new ArrayList<>();
            for(String courseId : student.getCourseIds()){
                Course course = findCourse(courseId);
                if (course == null) {
                    System.out.println("[警告]学生 " + student.getId() + " 选了不存在的课程 " + courseId);
                    invalid.add(courseId);
                    continue;
                }
                if(course.isFull()){
                    System.out.println("[警告]学生 " + student.getId() + " 选了已满的课程 " + courseId);
                    invalid.add(courseId);
                    continue;
                }
                course.enrollOne();
            } invalid.forEach(student::removeCourse);
        }

    }
}

