CREATE SCHEMA IF NOT EXISTS demo;

-- name 'security_users' avoids conflict with h2 default schema's 'USERS' table
create table IF NOT EXISTS demo.security_users
(
    id bigserial not null primary key,
    username varchar(50) not null unique,
    email varchar(50) not null,
    password varchar(500) not null,
    user_type varchar(50) not null,
    user_role varchar(45) not null,
    enabled boolean not null default true,
    account_expired boolean not null default false,
    account_expired_date timestamp,
    password_expired boolean not null default false,
    password_expired_date timestamp,
    locked boolean not null default false,
    locked_date timestamp
);

create table IF NOT EXISTS demo.security_authorities
(
    id bigserial not null primary key,
    user_id bigserial not null,
    authority varchar(50) not null,
    constraint fk_security_authorities_user_id foreign key (user_id) references demo.security_users (id)
);

create unique index IF NOT EXISTS ix_auth_user_id on demo.security_authorities (user_id, authority);
