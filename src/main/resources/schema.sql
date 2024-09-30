CREATE SCHEMA IF NOT EXISTS demo;

CREATE SEQUENCE IF NOT EXISTS demo.security_users_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.security_users (
    id bigint not null primary key default nextval('demo.security_users_id_seq'),
    username varchar(100) not null unique,
    email varchar(100) not null,
    password varchar(500) not null,
    user_type varchar(100) not null,
    user_role varchar(100) not null,
    enabled boolean not null default true,
    account_expired boolean not null default false,
    account_expired_date timestamp with time zone,
    password_expired boolean not null default false,
    password_expired_date timestamp with time zone,
    failed_login_attempts int,
    num_previous_lockouts int, /* could probably go in a 1-1 user additional details table if there are other fields to put there as well */
    locked boolean not null default false,
    locked_date timestamp with time zone,
    unlock_date timestamp with time zone,
    created_date timestamp with time zone not null,
    last_updated_date timestamp with time zone not null,
    last_login_date timestamp with time zone
);

CREATE SEQUENCE IF NOT EXISTS demo.authentication_attempts_id_seq INCREMENT BY 50;

/* Some of these column lengths are very approximate - no documentation was available for expected value max lengths */
CREATE TABLE IF NOT EXISTS demo.authentication_attempts (
    id bigint not null primary key default nextval('demo.authentication_attempts_id_seq'),
    user_id bigint,
    username varchar(100),
    attempt_time timestamp with time zone not null,
    successful boolean not null,
    failure_reason varchar(50),
    requested_resource varchar(200),
    remote_address varchar(32),
    remote_host varchar(200),
    remote_user varchar(200),
    content_type varchar(100),
    user_agent_family varchar(50),
    user_agent_major varchar(50),
    user_agent_minor varchar(50),
    user_agent_patch varchar(50),
    os_family varchar(200),
    os_major varchar(50),
    os_minor varchar(50),
    os_patch varchar(50),
    os_patch_minor varchar(50),
    device_family varchar(200)
);

CREATE SEQUENCE IF NOT EXISTS demo.security_authorities_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.security_authorities
(
    id bigint not null primary key default nextval('demo.security_authorities_id_seq'),
    user_id bigint not null,
    authority varchar(100) not null,
    constraint fk_security_authorities_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id)
);

CREATE UNIQUE INDEX IF NOT EXISTS ix_auth_user_id on demo.security_authorities (user_id, authority);

CREATE SEQUENCE IF NOT EXISTS demo.accounts_account_number_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.accounts (
    account_number bigint not null primary key default nextval('demo.accounts_account_number_seq'),
    user_id bigint not null, -- foreign key of security_users.id
    account_type varchar(100) not null,
    branch_address varchar(255) not null,
    created_date timestamp with time zone not null,
    constraint fk_accounts_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS demo.account_transactions_transaction_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.account_transactions (
    transaction_id bigint not null primary key default nextval('demo.account_transactions_transaction_id_seq'),
    account_number bigint not null,
    user_id bigint not null,
    transaction_date timestamp with time zone not null,
    transaction_summary varchar(255) not null,
    transaction_type varchar(100) not null,
    transaction_amount decimal(38, 2) not null,
    closing_balance decimal(38, 2) not null,
    created_date timestamp with time zone not null,
    constraint fk_account_transactions_account_number FOREIGN KEY (account_number) REFERENCES demo.accounts (account_number) ON DELETE CASCADE,
    constraint fk_account_transactions_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS demo.loans_loan_number_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.loans (
    loan_number bigint not null primary key default nextval('demo.loans_loan_number_seq'),
    user_id bigint not null,
    start_date timestamp with time zone not null,
    loan_type varchar(100) not null,
    total_amount decimal(38, 2) not null,
    amount_paid decimal(38, 2) not null,
    outstanding_amount decimal(38, 2) not null,
    created_date timestamp with time zone not null,
    constraint fk_loans_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS demo.cards_card_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.cards (
    card_id bigint not null primary key default nextval('demo.cards_card_id_seq'),
    card_number varchar(100) not null,
    user_id bigint not null,
    card_type varchar(100) not null,
    total_limit decimal(38, 2) not null,
    amount_used decimal(38, 2) not null,
    available_amount decimal(38, 2) not null,
    created_date timestamp with time zone not null,
    constraint fk_cards_user_id FOREIGN KEY (user_id) REFERENCES demo.security_users (id) ON DELETE CASCADE
);

CREATE SEQUENCE IF NOT EXISTS demo.notice_details_notice_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.notice_details (
    notice_id bigint not null primary key default nextval('demo.notice_details_notice_id_seq'),
    notice_summary varchar(255) not null,
    notice_details varchar(500) not null,
    start_date timestamp with time zone not null,
    end_date timestamp with time zone not null,
    created_date timestamp with time zone not null,
    last_updated_date timestamp with time zone not null
);

CREATE SEQUENCE IF NOT EXISTS demo.contact_messages_contact_id_seq INCREMENT BY 50;

CREATE TABLE IF NOT EXISTS demo.contact_messages (
    contact_id bigint not null primary key default nextval('demo.contact_messages_contact_id_seq'),
    contact_name varchar(50) not null,
    contact_email varchar(100) not null,
    subject varchar(500) not null,
    message varchar(2000) not null,
    created_date timestamp with time zone not null
);
