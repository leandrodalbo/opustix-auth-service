INSERT INTO users (email, name, password, roles, auth_providers, is_verified)
VALUES (
    'user@example.com',
    'Joe Doe',
    '$2a$10$99iX1E/3VQMHltZ3s4dzPeoPNd78p9hOwOblqgeSQ96XP3/zAaJEa',
    'USER,ADMIN',
    'LOCAL',
    true
);

INSERT INTO verify_user (token, email, expiry)
VALUES (
    'e4b7f7c4-1d8f-4c02-8d4f-3a8f2109c6fd',
    'user@example.com',
    1719076811
);
