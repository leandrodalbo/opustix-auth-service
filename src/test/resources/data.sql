INSERT INTO users (email, name, password, roles, auth_provider, is_verified, refresh_token)
VALUES (
    'user@example.com',
    'Joe Doe',
    '$2a$10$99iX1E/3VQMHltZ3s4dzPeoPNd78p9hOwOblqgeSQ96XP3/zAaJEa',
    'USER,ADMIN',
    'LOCAL',
    true,
    '8f2d7c4a-3a09-4f2e-9c5b-71e65d24f5b3'
);


INSERT INTO verify_user (token, email, expiry)
VALUES (
    'e4b7f7c4-1d8f-4c02-8d4f-3a8f2109c6fd',
    'user@example.com',
    1719076811
);
