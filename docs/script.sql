create table admins
(
    admin_id   int auto_increment comment '관리자 id'
        primary key,
    admin_name varchar(50)                         not null comment '관리자 이름',
    level      tinyint                             not null comment '권한 레벨',
    status     tinyint   default 0                 null,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at timestamp                           null comment '수정일',
    deleted_at timestamp                           null comment '삭제일',
    email      varchar(100)                        not null comment '이메일',
    password   varchar(255)                        not null comment '비밀번호'
)
    comment '관리자';

create table attendances
(
    attendance_id     int auto_increment comment '출결 id'
        primary key,
    student_id        int                                 not null comment '학생 id',
    attendance_status tinyint                             not null comment '출결 상태',
    created_at        timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at        timestamp                           null comment '수정일',
    deleted_at        timestamp                           null comment '삭제일',
    checkin_time      timestamp                           null comment '입실 시간',
    checkout_time     timestamp                           null comment '퇴실 시간',
    attendance_date   date                                not null comment '출결일',
    status            tinyint   default 0                 null
)
    comment '출결관리';

create table classes
(
    class_id   int auto_increment comment '수업 id'
        primary key,
    class_name varchar(100)                        not null comment '수업 이름',
    class_time varchar(100)                        not null comment '수업 시간',
    capacity   int                                 not null comment '수업 인원',
    price      decimal(10, 2)                      not null comment '수업 가격',
    status     tinyint   default 0                 null,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at timestamp                           null comment '수정일',
    deleted_at timestamp                           null comment '삭제일'
)
    comment '수업';

create table enrollments
(
    enrollment_id int auto_increment comment '수강신청 id'
        primary key,
    student_id    int                                 not null comment '학생 id',
    class_id      int                                 not null comment '수업 id',
    status        tinyint   default 0                 null,
    created_at    timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at    timestamp                           null comment '수정일',
    deleted_at    timestamp                           null comment '삭제일'
)
    comment '수강신청';

create table instructor_class
(
    instructor_id int not null comment '강사 id',
    class_id      int not null comment '수업 id',
    primary key (instructor_id, class_id)
)
    comment '강사-수업';

create table instructors
(
    instructor_id   int auto_increment comment '강사 id'
        primary key,
    instructor_name varchar(50)                         not null comment '강사 이름',
    phone           varchar(50)                         not null comment '전화번호',
    status          tinyint   default 0                 null,
    created_at      timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at      timestamp                           null comment '수정일',
    deleted_at      timestamp                           null comment '삭제일',
    email           varchar(100)                        not null comment '이메일',
    password        varchar(255)                        not null comment '비밀번호'
)
    comment '강사';

create table notices
(
    notice_id  int auto_increment comment '공지사항 id'
        primary key,
    admin_id   int                                 not null comment '관리자 id',
    title      varchar(100)                        not null comment '제목',
    content    text                                not null comment '내용',
    status     tinyint   default 0                 null,
    created_at timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at timestamp                           null comment '수정일',
    deleted_at timestamp                           null comment '삭제일'
)
    comment '공지사항';

create table payments
(
    payment_id           int auto_increment comment '결제 id'
        primary key,
    amount               decimal(10, 2)                      not null comment '결제 금액',
    status               tinyint   default 0                 null,
    created_at           timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at           timestamp                           null comment '수정일',
    deleted_at           timestamp                           null comment '삭제일',
    enrollment_id        int                                 not null comment '수강신청 id',
    active_enrollment_id int as ((case when (`status` = 0) then `enrollment_id` else NULL end)) stored,
    constraint uq_payments_active
        unique (active_enrollment_id)
)
    comment '결제';

create table reviews
(
    review_id            int auto_increment comment '리뷰 id'
        primary key,
    rating               tinyint                             not null comment '별점',
    content              text                                null comment '리뷰내용',
    status               tinyint   default 0                 null,
    created_at           timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at           timestamp                           null comment '수정일',
    deleted_at           timestamp                           null comment '삭제일',
    enrollment_id        int                                 not null comment '수강신청 id',
    active_enrollment_id int as ((case when (`status` = 0) then `enrollment_id` else NULL end)) stored,
    constraint uq_reviews_active
        unique (active_enrollment_id)
)
    comment '리뷰';

create table students
(
    student_id   int auto_increment comment '학생 id'
        primary key,
    student_name varchar(50)                         not null comment '학생 이름',
    birth_date   date                                not null comment '생년월일',
    gender       tinyint                             not null comment '성별',
    phone        varchar(50)                         not null comment '핸드폰 번호',
    address      varchar(255)                        not null comment '주소',
    status       tinyint   default 0                 null,
    created_at   timestamp default CURRENT_TIMESTAMP not null comment '등록일',
    updated_at   timestamp                           null comment '수정일',
    deleted_at   timestamp                           null comment '삭제일',
    email        varchar(100)                        not null comment '이메일',
    password     varchar(255)                        not null comment '비밀번호'
)
    comment '학생';


