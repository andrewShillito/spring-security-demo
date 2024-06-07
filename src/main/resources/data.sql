INSERT INTO
    security_users (id, username, email, password, user_type, user_role, enabled)
VALUES
    (1, 'user', 'user@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'standard', true),
    (2, 'admin', 'admin@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'admin', true),
    (3, 'otherUser', 'otherUser@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'standard', true),
    (4, 'otherAdmin', 'otherAdmin@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'admin', true),
    (5, 'userDisabled', 'userDisabled@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'external', 'standard', false),
    (6, 'adminDisabled', 'adminDisabled@demo.com', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', 'internal', 'admin', false)
;

INSERT INTO
    security_authorities (user_id, authority)
VALUES
    (1, 'read'),
    (2, 'admin'),
    (3, 'read'),
    (4, 'admin'),
    (4, 'read'),
    (5, 'read'),
    (6, 'admin')
;