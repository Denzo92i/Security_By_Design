-- Insertion d'un client avec ID 1
INSERT INTO customer (id, name, email, active, created_at, updated_at)
VALUES (1, 'Admin User', 'admin@foodndeliv.com', true, NOW(), NOW());

-- Insertion d'un restaurant avec ID 1
INSERT INTO restaurant (id, name, active, created_at, updated_at)
VALUES (1, 'Pizza Margherita Restaurant', true, NOW(), NOW()); ADD