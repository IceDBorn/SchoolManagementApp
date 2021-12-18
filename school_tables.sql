create table "Users"
(
    id          integer default nextval('"User_id_seq"'::regclass) not null
        constraint user_pk
            primary key,
    name        text                                               not null,
    birthday    date                                               not null,
    password    text                                               not null,
    email       text                                               not null,
    "isAdmin"   boolean default false                              not null,
    gender      integer                                            not null,
    "isTeacher" boolean
);

create unique index user_email_uindex
    on "Users" (email);

create table "Teachers"
(
    id      integer not null
        constraint teacher_pk
            primary key
        constraint teacher_user_id_fk
            references "Users",
    subject text    not null
);

create table "Students"
(
    id   integer not null
        constraint student_pk
            primary key
        constraint student_user_id_fk
            references "Users",
    year integer not null
);

create table "Classrooms"
(
    id      integer default nextval('"Classroom_id_seq"'::regclass) not null
        constraint classroom_pk
            primary key,
    name    text,
    "limit" integer default 20                                      not null
);

create unique index classroom_name_uindex
    on "Classrooms" (name);

create table "Lessons"
(
    id      integer default nextval('"Lesson_id_seq"'::regclass) not null
        constraint lesson_pk
            primary key,
    name    text                                                 not null,
    subject text                                                 not null,
    year    integer                                              not null
);

create unique index lesson_name_uindex
    on "Lessons" (name);

create table "Courses"
(
    id            integer default nextval('"Course_id_seq"'::regclass) not null
        constraint course_pk
            primary key,
    "classroomId" integer                                              not null
        constraint course_classroom_id_fk
            references "Classrooms",
    "teacherId"   integer                                              not null
        constraint course_teacher_id_fk
            references "Teachers",
    day           text,
    time          text,
    "lessonId"    integer
        constraint courses_lessons_id_fk
            references "Lessons"
);

create table "StudentLessons"
(
    id            serial
        constraint studentlessons_pk
            primary key,
    "lessonId"    integer           not null,
    "studentId"   integer           not null
        constraint studentlessons_students_id_fk
            references "Students",
    "studentYear" integer           not null,
    grade         integer default 0 not null
);

create table "Years"
(
    id   serial
        constraint years_pk
            primary key,
    name text not null
);

create unique index years_name_uindex
    on "Years" (name);

