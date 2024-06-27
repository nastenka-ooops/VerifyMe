create table role
(
    id        bigint generated always as identity
        constraint role_pk
            primary key,
    authority integer not null
);
create table app_user
(
    id         bigint generated always as identity
        constraint app_user_pk
            primary key,
    email      varchar(255)          not null
        constraint app_user_email_pk
            unique,
    login      varchar(255)          not null,
    password   varchar(255)          not null,
    is_confirm boolean default false not null
);
create table user_role_junction
(
    user_id integer not null,
    role_id integer not null,
    constraint user_role_junction_pk
        primary key (user_id, role_id)
);