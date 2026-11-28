package course.service;

/** 选课/退课这类「用户可预期的失败」用受检异常表达，菜单层捕获后只打印 message。 */
public class EnrollmentException extends Exception {

    public EnrollmentException(String message) {
        super(message);
    }
}
