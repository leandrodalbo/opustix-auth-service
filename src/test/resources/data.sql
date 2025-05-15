INSERT INTO users (email, password, roles, auth_provider, is_verified)
VALUES (
    'user@example.com',
    'hashedpassword123',
    'USER,ADMIN',
    'LOCAL',
    true
);
