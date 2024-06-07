INSERT INTO
    demo.security_users (username, email, password, user_type, user_role, enabled)
VALUES
    ('user', 'user@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'STANDARD', true),
    ('admin', 'admin@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'ADMIN', true),
    ('otherUser', 'otherUser@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'STANDARD', true),
    ('otherAdmin', 'otherAdmin@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'ADMIN', true),
    ('userDisabled', 'userDisabled@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'STANDARD', false),
    ('adminDisabled', 'adminDisabled@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'ADMIN', false)
;

INSERT INTO
    demo.security_authorities (user_id, authority)
VALUES
    (1, 'ROLE_USER'),
    (2, 'ROLE_ADMIN'),
    (3, 'ROLE_USER'),
    (4, 'ROLE_ADMIN'),
    (4, 'ROLE_USER'),
    (5, 'ROLE_USER'),
    (6, 'ROLE_ADMIN')
;