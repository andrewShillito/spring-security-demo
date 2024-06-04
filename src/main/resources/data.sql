INSERT INTO
    users (username, password, enabled)
VALUES
    ('user', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', true),
    ('admin', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', true),
    ('otherUser', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', true),
    ('otherAdmin', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', true),
    ('userDisabled', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', false),
    ('adminDisabled', '{bcrypt}$2a$10$8BVum5sbIjSE9tx7KY24ZerJyKkm8zyIS8p.XEB4PwHeOP6K3lzpa', false)
;

INSERT INTO
    authorities (username, authority)
VALUES
    ('user', 'read'),
    ('admin', 'admin'),
    ('otherUser', 'read'),
    ('otherAdmin', 'admin'),
    ('otherAdmin', 'read'),
    ('userDisabled', 'read'),
    ('adminDisabled', 'admin')
;