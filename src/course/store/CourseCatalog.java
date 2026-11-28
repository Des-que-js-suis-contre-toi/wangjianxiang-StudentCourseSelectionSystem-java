package course.store;


import course.model.Course;

import java.util.ArrayList;
import java.util.List;

/*这个是用于兜底的类


它没有实例字段、没有状态，唯一对外的东西就是 presetCourses() 这个静态方法——所以“final + 私有构造器 + 全静态方法”这套写法才成立。


一、类上加 final 的好处
public final class CourseCatalog 的意思是：这个类不许被继承

它没有任何可扩展点。 全是静态方法和常量数据，继承它不会产生任何多态价值——子类既不能“重写”静态方法（静态方法只能被隐藏），也不能改变 presetCourses() 的行为。允许继承只会误导读者，以为这里存在扩展设计。

防止“隐藏静态方法”的坑。 静态方法不参与多态。如果允许继承，子类可以声明一个同名 presetCourses() 把父类的藏起来，调用 MyCatalog.presetCourses() 和 CourseCatalog.presetCourses() 行为不同——这种“看起来像重写、其实是两个方法”的代码极难排查。final 直接堵死。

防止有人往里加状态。 工具类一旦被继承，子类很可能塞进实例字段，变成“既是工具类又是对象”的混合体，语义就脏了。

意图声明。 final 是写给维护者看的：“这个类的设计到此为止，不要扩展。” 这类信息靠注释容易丢，靠关键字则由编译器强制执行。


目的是禁止继承
 */
public final class CourseCatalog {


/*    private CourseCatalog() {} 的作用只有一个：禁止 new CourseCatalog()。

      如果不写这个构造器，Java 会送你一个默认的 public 无参构造器

      目地是防止实例化

 */
    private CourseCatalog() {
    }

    public static List<Course> presetCourses() {
        List<Course> courses = new ArrayList<>();
        courses.add(new Course("CS101", "程序设计基础", 4.0, "周一第1-2节", 60, 0));
        courses.add(new Course("CS102", "面向对象程序设计", 3.5, "周二第3-4节", 55, 0));
        courses.add(new Course("CS201", "数据结构", 4.0, "周一第3-4节", 50, 0));
        courses.add(new Course("CS202", "算法设计与分析", 3.0, "周三第1-2节", 45, 0));
        courses.add(new Course("CS301", "操作系统", 3.5, "周二第5-6节", 40, 0));
        courses.add(new Course("CS302", "计算机网络", 3.0, "周四第3-4节", 45, 0));
        courses.add(new Course("CS303", "数据库原理", 3.0, "周三第5-6节", 50, 0));
        courses.add(new Course("CS304", "编译原理", 3.5, "周五第1-2节", 30, 0));
        courses.add(new Course("CS305", "软件工程", 2.5, "周四第5-6节", 55, 0));
        courses.add(new Course("MA101", "高等数学", 4.0, "周一第5-6节", 60, 0));
        courses.add(new Course("MA102", "线性代数", 2.5, "周二第1-2节", 55, 0));
        courses.add(new Course("MA201", "概率论与数理统计", 3.0, "周五第3-4节", 50, 0));
        courses.add(new Course("EN101", "大学英语", 2.0, "周三第1-2节", 60, 0));
        courses.add(new Course("EN201", "学术英语写作", 1.5, "周四第7-8节", 35, 0));
        courses.add(new Course("PE101", "体育", 1.0, "周五第5-6节", 60, 0));
        courses.add(new Course("HU101", "中国近现代史纲要", 2.0, "周一第7-8节", 60, 0));
        courses.add(new Course("HU102", "思想道德与法治", 2.0, "周二第7-8节", 60, 0));
        courses.add(new Course("SC101", "大学物理", 3.5, "周三第7-8节", 40, 0));
        courses.add(new Course("SC102", "物理实验", 1.5, "周六第1-2节", 30, 0));
        courses.add(new Course("AR101", "艺术鉴赏", 1.0, "周日第3-4节", 50, 0));
        return courses;
    }
}
