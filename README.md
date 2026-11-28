# 学生选课系统（最终考核项目）

> 对应招新标准 6.2 节「项目二：学生选课系统」。JDK 21，纯控制台，无第三方依赖。

## 一、运行

```powershell
.\run.ps1     # PowerShell
.\run.bat     # cmd / 双击；run.ps1 被执行策略拦住时用这个
```

脚本编译 `src` 到 `out`，并以项目根目录为工作目录启动 `course.Main`。
数据文件 `courses.txt`、`students.txt` 生成在**当前工作目录**，所以请始终在 `FinalProject/` 下运行。

- 首次启动：没有数据文件时使用预置的 20 门课程 + 50 名学生（学号 2024001—2024050），无选课记录。
- 正常退出（菜单选 `0` 或输入流结束）：把最新课程人数和学生选课列表写回两个文件。
- 中文显示：JDK 18+ 会跟随控制台代码页；万一乱码先 `chcp 65001`，或改用
  `java -Dstdout.encoding=UTF-8 -cp out course.Main`（见第八节）。

## 二、目录与职责

```text
FinalProject/
├── run.ps1 / run.bat                # 编译 + 运行
├── README.md                        # 本文件（提交材料的一部分）
├── courses.txt / students.txt       # 首次运行后生成的数据文件（本地数据，不提交）
└── src/course/
    ├── Main.java                    # 入口：加载 → 重建人数 → 菜单 → 正常退出保存
    ├── model/
    │   ├── Course.java              # 课程实体：学分/容量校验，人数只能由 enrollOne/dropOne 修改
    │   ├── Student.java             # 学生实体：学号 + 已选课程（LinkedHashSet，去重 + O(1) 判重）
    │   └── TimeSlot.java            # 值对象：「周一第3-4节」解析 + 冲突判断
    ├── store/
    │   ├── CourseCatalog.java       # 20 门预置课程
    │   └── DataStore.java           # 两个文本文件的读写、坏行容错、BOM 清理
    ├── service/
    │   ├── CourseService.java       # 浏览排序、选课、退课、课表、启动重算人数
    │   └── EnrollmentException.java # 选课/退课的可预期失败（受检异常）
    └── ui/
        └── ConsoleApp.java          # 登录 + 菜单 + 输入容错（EOF 安全退出）
```

分层职责：UI 只读字符串、打印；Service 只做业务规则；Model 自己守不变式（不出现非法状态）。

## 三、数据文件格式

```text
courses.txt    编号,名称,学分,时间,容量,已选人数        每行一门课程，`#` 开头为注释
students.txt   学号,课程编号1,课程编号2...              每行一名学生，没选课就只写学号
```

- 编码 UTF-8；读取时容忍 `,` `，` `、` 三种分隔符、容忍 BOM、坏行跳过并提示；
- 写入时固定用半角逗号（题目规定格式）；
- **人数是派生数据**：`students.txt` 是唯一真相，启动时按学生记录重算（见第四节）。

## 四、实现要点（答辩可讲的设计取舍）

1. **分层**：UI / Service / Model / Store 四层。加“管理员代选课”只需加一个菜单项，复用 `CourseService.enroll`。
2. **单一数据源**：`courses.txt` 里的“已选人数”只是快照，启动时 `rebuildEnrollment()` 清零后按学生记录重算，
   并清理“课程不存在 / 课程已满”的脏记录。即使文件被手改，重启后人数与课表也一致。
3. **模型守不变式**：`Course.enrolled` 没有 setter，只能通过 `enrollOne()/dropOne()`（内部判满、判负），
   `Student.courseIds` 只对外暴露只读视图，写操作必须走 `addCourse/removeCourse`。
4. **异常分层**：用户可预期的失败（课不存在、已满、重复、冲突、未选）→ 受检 `EnrollmentException`，
   界面层只打印 message；程序 bug（参数非法、状态非法）→ `IllegalArgumentException` / `IllegalStateException`。
5. **大小写不敏感**：课程编号统一经 `Course.normalizeId`（`trim` + `toUpperCase(Locale.ROOT)`），
   输入 `cs202` 能命中 `CS202`。
6. **冲突判定**：`TimeSlot.conflictsWith` = 同星期且闭区间相交
   （`a.start <= b.end && b.start <= a.end`），相邻节次（周一3-4 与 周一5-6）不冲突。
7. **排序**：浏览用 `Comparator.comparingDouble(学分).reversed().thenComparing(编号)`；
   课表用 `(dayOfWeek, startPeriod)` 升序。`Comparable` 没用，因为排序规则属于“浏览”这个用例。
8. **已知限制**：单用户控制台程序，无并发；只在正常退出时落盘，异常退出会丢本次修改；
   学号固定 `2024001—2024050`，不支持新增学生（题目未要求）。

## 五、边界自测清单

- [ ] 学号输入 `2024` / `abc` / `2024051` → 提示且不崩
- [ ] 菜单输入 `9` / 空行 → “无效选项”
- [ ] 选课输入空行 → “已取消选课”
- [ ] 课程编号 ` cs202 `（前后空格 + 小写）→ 命中 CS202
- [ ] `cs999` → “课程编号不存在”
- [ ] 重复选同一门 → “已选过”
- [ ] `EN101` 与 `CS202`（同为周三第 1-2 节）→ 冲突提示
- [ ] `CS101`（周一1-2）+ `CS201`（周一3-4）→ 不冲突
- [ ] CS304 满员（30/30）后选 → “课程已满”
- [ ] 退未选 / 退不存在的课 → 两种不同提示
- [ ] 空课表 → “本学期你还没有选课”
- [ ] 脏 `students.txt`（未知课程/未知学号/坏行/BOM）→ 警告 + 清理，不崩溃
- [ ] 重启后人数与课表一致，无“字段数量错误”警告
- [ ] 删除 `courses.txt` 重启 → 回退预置 20 门课程
