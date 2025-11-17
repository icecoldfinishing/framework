-- Création de la base
DROP DATABASE IF EXISTS pg11;
CREATE DATABASE pg11;

\c pg11;

-- Table employe avec 3 champs seulement
CREATE TABLE employe (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    salaire NUMERIC(10,2)
);

-- Données de test
INSERT INTO employe (nom, salaire) VALUES
('Rakoto', 1200.00),
('Randria', 950.50),
('Rabe', 800.00);