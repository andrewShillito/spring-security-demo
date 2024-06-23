CREATE SCHEMA IF NOT EXISTS demo;

-- name 'security_users' avoids conflict with h2 default schema's 'USERS' table
CREATE TABLE IF NOT EXISTS demo.security_users
(
    id bigserial not null primary key,
    username varchar(50) not null unique,
    email varchar(50) not null,
    password varchar(500) not null,
    user_type varchar(50) not null,
    user_role varchar(45) not null,
    enabled boolean not null default true,
    account_expired boolean not null default false,
    account_expired_date timestamp with time zone,
    password_expired boolean not null default false,
    password_expired_date timestamp with time zone,
    locked boolean not null default false,
    locked_date timestamp with time zone,
    created_date timestamp with time zone not null,
    last_updated_date timestamp with time zone not null,
);

CREATE TABLE IF NOT EXISTS demo.security_authorities
(
    id bigserial not null primary key,
    user_id bigserial not null,
    authority varchar(50) not null,
    constraint fk_security_authorities_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_user_id on demo.security_authorities (user_id, authority);

CREATE TABLE IF NOT EXISTS demo.accounts (
    user_id bigint not null, -- foreign key of security_users.id
    account_number bigserial not null primary key,
    account_type varchar(100) not null,
    branch_address varchar(200) not null,
    created_date timestamp with time zone default null,
    constraint fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo.account_transactions (
    transaction_id varchar(200) not null primary key,
    account_number integer not null,
    user_id bigint not null,
    transaction_date timestamp with time zone not null,
    transaction_summary varchar(200) not null,
    transaction_type varchar(100) not null,
    transaction_amount decimal not null,
    closing_balance decimal not null,
    created_date timestamp with time zone not null,
    constraint fk_account_transactions_account_number FOREIGN KEY (account_number) REFERENCES demo.accounts (account_number) ON DELETE CASCADE,
    constraint fk_account_transactions_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo.loans (
    loan_number bigserial not null primary key,
    user_id bigint not null,
    start_date timestamp with time zone not null,
    loan_type varchar(100) not null,
    total_amount decimal not null,
    amount_paid decimal not null,
    outstanding_amount decimal not null,
    created_date timestamp with time zone not null,
    constraint fk_loans_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo.cards (
    card_id bigserial not null primary key,
    card_number varchar(100) not null,
    user_id bigint not null,
    card_type varchar(100) not null,
    total_limit decimal not null,
    amount_used decimal not null,
    available_amount decimal not null,
    created_date timestamp with time zone not null,
    constraint fk_cards_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS demo.notice_details (
    notice_id bigserial not null primary key,
    notice_summary varchar(200) not null,
    notice_details varchar(500) not null,
    notice_start_date timestamp with time zone not null,
    notice_end_date timestamp with time zone not null,
    created_date timestamp with time zone not null,
    last_updated_date timestamp with time zone not null
);

CREATE TABLE IF NOT EXISTS demo.contact_messages (
    contact_id bigserial not null primary key,
    contact_name varchar(50) not null,
    contact_email varchar(100) not null,
    subject varchar(500) not null,
    message varchar(2000) not null,
    created_date timestamp with time zone not null
);