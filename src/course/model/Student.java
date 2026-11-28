package course.model;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class Student {
    private final String id;
    private final Set<String> courseIds=new LinkedHashSet<>();

    public Student(String id) {
        if(id==null||!id.matches("\\d{7}")){
            throw new IllegalArgumentException("学生编号必须是7位数字");
        }
        this.id=id;
    }

    public String getId() {
        return id;
    }

    public boolean hasCourse(String courseId) {
        return courseIds.contains(Course.normalizeId(courseId));
    }

    public void addCourse(String courseId) {
        courseIds.add(Course.normalizeId(courseId));
    }

    public void removeCourse(String courseId) {
        courseIds.remove(Course.normalizeId(courseId));
    }
    /** 只读视图，防止外部直接改集合绕过课程人数统计。
     * 因此在 CoureseService 中，addCourse/removeCourse 之后要手动更新 Course.enrolled。
     */

    
    public Set<String> getCourseIds() {
        return Collections.unmodifiableSet(courseIds);
    }

    public int getCourseCount() {
        return courseIds.size();
    }

    @Override
    public String toString() {
        return id + " " + courseIds;
    }

}
