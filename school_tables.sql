create table "Users"
(
    id          serial
        constraint user_pk
            primary key,
    name        text                  not null,
    password    text                  not null,
    email       text                  not null,
    gender      integer               not null,
    birthday    date                  not null,
    "isTeacher" boolean default false not null,
    "isAdmin"   boolean default false not null
);

create unique index user_email_uindex
    on "Users" (email);

create table "Classrooms"
(
    id      serial
        constraint classroom_pk
            primary key,
    name    text,
    "limit" integer not null
);

create unique index classroom_name_uindex
    on "Classrooms" (name);

create table "Years"
(
    id   serial
        constraint years_pk
            primary key,
    name text not null
);

create unique index years_name_uindex
    on "Years" (name);

create table "Students"
(
    id       integer not null
        constraint student_pk
            primary key
        constraint student_user_id_fk
            references "Users",
    "yearId" integer not null
        constraint students_years_id_fk
            references "Years"
);

create table "StudentLessons"
(
    id          serial
        constraint studentlessons_pk
            primary key,
    "courseId"  integer                      not null,
    "studentId" integer                      not null
        constraint studentlessons_students_id_fk
            references "Students",
    grade       double precision default 0.0 not null
);

create table "Professions"
(
    id   serial
        constraint professions_pk
            primary key,
    name text not null
);

create unique index professions_name_uindex
    on "Professions" (name);

create table "Lessons"
(
    id             serial
        constraint lesson_pk
            primary key,
    name           text    not null,
    "professionId" integer not null
        constraint lessons_professions_id_fk
            references "Professions",
    "yearId"       integer not null
        constraint lessons_years_id_fk
            references "Years"
);

create unique index lesson_name_uindex
    on "Lessons" (name);

create table "Teachers"
(
    id             integer not null
        constraint teacher_pk
            primary key
        constraint teachers_users_id_fk
            references "Users",
    "professionId" integer not null
        constraint teachers_professions_id_fk
            references "Professions"
);

create table "Courses"
(
    id            serial
        constraint course_pk
            primary key,
    "lessonId"    integer not null
        constraint courses_lessons_id_fk
            references "Lessons",
    "teacherId"   integer not null
        constraint courses_teachers_id_fk
            references "Teachers",
    "classroomId" integer not null
        constraint courses_classrooms_id_fk
            references "Classrooms",
    day           text    not null,
    "startTime"   text    not null,
    "endTime"     text
);

