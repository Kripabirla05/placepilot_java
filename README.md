# PlacePilot - Intelligent Campus Placement & Internship Management System
Pure terminal project (Core Java + JDBC + MySQL). No GUI, no website, no Maven, no IDE needed.
45 Java classes in 11 packages.

## 1. One-time setup

1. Install **JDK 11 or newer** (check: `javac -version`) and **MySQL Server** (check: `mysql --version`).
2. Download **MySQL Connector/J** from https://dev.mysql.com/downloads/connector/j/ (Platform Independent ZIP).
   Copy the jar into `lib/` and rename it `mysql-connector-j.jar`.
3. Open `src/database/DBConnection.java` and change `PASSWORD` to your MySQL root password.
4. Create database and tables (run from the project folder, VS Code terminal):
   ```
   mysql -u root -p < sql/schema.sql
   mysql -u root -p placepilot_db < sql/sample_data.sql
   ```

## 2. Compile and run (VS Code terminal, project folder)

Windows (cmd or PowerShell)
```
compile.bat
run.bat
```
Mac / Linux
```
./compile.sh
./run.sh
```
Manual commands (what the scripts do):
```
javac -d out @sources.txt
java -cp "out;lib\mysql-connector-j.jar" main.Main        (Windows)
java -cp "out:lib/mysql-connector-j.jar" main.Main        (Mac/Linux)
```
Always run from the **PlacePilot root folder** (the program reads `data/questions.csv` and writes `reports/`).

**How Connector/J connects:** `javac` does not need the jar (JDBC interfaces are inside the JDK: `java.sql`).
At run time the jar must be on the classpath (`-cp`), because `Class.forName("com.mysql.cj.jdbc.Driver")`
loads the driver from it. Then `DriverManager.getConnection(url, user, password)` returns a `Connection`.

## 3. Demo logins (from sample_data.sql)

| Role | Login | Password |
|---|---|---|
| Student | aman@gmail.com (CSE 8.5) / neha@gmail.com (CSE 9.1) / karan@gmail.com (1 backlog) | pass123 |
| Recruiter | hr@tcs.com / hr@infosys.com / hr@wipro.com | tcs123 / infy123 / wipro123 |
| Recruiter (unverified) | hr@startupx.com | start123 |
| Admin | admin | admin123 |

## 4. Folder structure

```
PlacePilot/
  src/main          Main, StudentMenu, CompanyMenu, AdminMenu                                   (4)
  src/model         Displayable, User, Student, Recruiter, Company, Opportunity, Job,
                    Internship, Application, TestResult, Notification, PlacementRecord          (12)
  src/database      DBConnection                                                                (1)
  src/dao           StudentDAO (+test results), CompanyDAO (+admin login), JobDAO (+internships),
                    ApplicationDAO (+placements/PPO), NotificationDAO                           (5)
  src/service       AuthService (login, registration, student profile),
                    JobService (company, verification, jobs, internships),
                    ApplicationService, EligibilityService, JobFilter                           (5)
  src/interview     QuestionBank (has nested Question class), MockInterview, McqTest           (3)
  src/analytics     SkillAnalyzer (skill gap + skill demand),
                    PlacementAnalytics (analytics + readiness score + company comparison)       (2)
  src/notification  NotificationService                                                         (1)
  src/concurrency   NotificationProcessor, ReportGeneratorThread, TestTimer, ThreadStateDemo   (4)
  src/utility       InputHelper, Validator, TablePrinter, FileUtil                              (4)
  src/exception     InvalidInputException, LoginFailedException,
                    DuplicateRecordException, NotEligibleException                              (4)
  sql/              schema.sql, sample_data.sql
  data/             questions.csv        reports/   generated files        lib/   MySQL jar
```
Flow: `Menu -> Service -> DAO -> MySQL`. Menus only talk to services, services only talk to DAOs.

## 5. Database tables (placepilot_db)

admin, student, company, recruiter(company_id FK), job(company_id FK), internship(company_id FK),
application(student_id FK, job_id FK or internship_id FK), test_result(student_id FK),
notification(student_id FK), placement(student_id FK, job_id FK, internship_id FK).
Skills are stored as text like `Java,SQL,Python` and split in Java (Set operations).

## 6. Where each syllabus topic is used

| Topic | Where |
|---|---|
| Classes, constructors, this/static | every model class; `NotificationService.getInstance()` (static singleton) |
| Inheritance, abstraction, interface, polymorphism | `User`->`Student`/`Recruiter`; `Opportunity`->`Job`/`Internship` (`getMaxBacklogs()` differs); `Displayable` |
| 2D arrays | `McqTest` answer sheet; branch-wise table in `PlacementAnalytics` |
| String methods, StringBuilder, wrappers | `Validator`, `TablePrinter`, `JobService.securityInfo`, `InputHelper` (Integer/Double parse) |
| Checked / unchecked / custom exceptions, multi-catch, finally, throws | `exception` package; all menus; `NotificationProcessor` (finally) |
| ArrayList, LinkedHashSet, TreeSet, HashSet | `Validator.toSkillSet`, `SkillAnalyzer`, `PlacementAnalytics` |
| HashMap, TreeMap | `SkillAnalyzer` (demand), `PlacementAnalytics` (best LPA per company) |
| Queue / PriorityQueue, Iterator | `NotificationService` (priority queue, iterator in inbox) |
| Comparable / Comparator / Collections | `Job`, `Student`, `Notification`; `JobFilter`; `Collections.sort/shuffle` |
| Thread, Runnable, start, sleep, join, interrupt | `ReportGeneratorThread`, `NotificationProcessor`, `TestTimer` |
| Thread states, daemon, priority, race condition, synchronized | `ThreadStateDemo` (Admin option 5) |
| wait / notify, producer-consumer | `NotificationService` (producer: send, consumer: take) |
| JDBC, PreparedStatement, CRUD, ResultSet | `DBConnection` + all DAOs |
| Lambda, Stream API, method reference | `JobFilter`, `SkillAnalyzer`, `PlacementAnalytics` |
| Generics | `JobFilter.filter<T>`, `FileUtil.exportList<T>` |
| File handling (FileReader/Writer, byte streams, CSV) | `FileUtil`, `QuestionBank`, Admin export/backup |

## 7. Four-member division

| Member | Work |
|---|---|
| 1 | Student registration/login, profile, academic records, skills: `User`, `Student`, `StudentDAO`, `AuthService`, `Validator`, student profile part of `StudentMenu`, readiness score (in `PlacementAnalytics`) |
| 2 | Company, recruiter, jobs, internships, filters: `Company`, `Recruiter`, `Opportunity`, `Job`, `Internship`, `CompanyDAO`, `JobDAO`, `JobService`, `JobFilter`, company comparison (in `PlacementAnalytics`), `CompanyMenu` |
| 3 | Applications, eligibility, skill gap/demand, mock interview: `Application`, `ApplicationDAO` (application part), `ApplicationService`, `EligibilityService`, `SkillAnalyzer`, `QuestionBank`, `MockInterview`, `McqTest` |
| 4 | MySQL/JDBC, main menu, notifications, threads, analytics: `DBConnection`, `Main`, `AdminMenu`, `Notification`, `NotificationDAO`, `NotificationService`, all `concurrency` classes, `PlacementRecord`, placement part of `ApplicationDAO`, `PlacementAnalytics`, `InputHelper`, `TablePrinter`, `FileUtil`, exceptions |

Everyone: testing and documentation.

## 8. Test checklist (run in this order)

1. **DB**: start program -> "Database connected successfully."
2. **Admin**: login admin/admin123 -> option 2 shows 4 companies -> option 1 verify StartupX (id 4).
3. **Student**: register a new student (try bad email, CGPA 15, duplicate email to see exceptions) -> login -> view/update profile -> add skills.
4. **Jobs**: option 4 try every filter (min LPA 6, company TCS, location Pune, role Engineer).
5. **Recruiter** (hr@tcs.com): post job, update, view, delete; post paid and unpaid internship.
6. **Apply**: login as Karan (6.9 CGPA, 1 backlog) and apply to a job -> "Not eligible" message. Login as Neha and apply -> success; apply again -> duplicate message.
7. **Recruiter**: view applications -> shortlist -> select -> student inbox shows notifications (HIGH first).
8. **Internship -> PPO**: apply for internship, recruiter selects, option 11 "PPO Offered", student option 17.
9. **Smart features**: skill gap (9), skill demand (10), comparison (11), readiness (12), mock interview (13), MCQ test (14), test results (18).
10. **Admin**: analytics (3), background report (4, see `reports/placement_report.txt`), thread demo (5), export (6), backup (7), announcement (8, then check a student inbox).

## 9. Common errors

| Message | Fix |
|---|---|
| `MySQL driver not found` | jar missing in `lib/` or wrong name, or `-cp` not used |
| `Access denied for user 'root'` | wrong password in `DBConnection.java` |
| `Unknown database 'placepilot_db'` | run `schema.sql` first |
| `Could not find or load main class` | run `compile.bat` first; run from project root |
| questions file error | run from project root so `data/questions.csv` is found |

## 10. Note on security
Passwords are stored as plain text to keep the code simple for CA-3. In real projects they must be hashed (CA-4 / future scope).
