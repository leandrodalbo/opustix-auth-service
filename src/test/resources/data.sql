INSERT INTO users (email, password, roles, auth_provider, is_verified)
VALUES (
    'user@example.com',
    'hashedpassword123',
    ARRAY['USER', 'ADMIN']::role_type[],
    'LOCAL',
    true
);
