CREATE DATABASE gestion_presence;
USE gestion_presence;

CREATE TABLE employees (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    matricule VARCHAR(50) UNIQUE NOT NULL,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL,
    poste VARCHAR(100),
    salaire_base DECIMAL(10,2),
    date_embauche DATE,
    qr_code LONGBLOB,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE presences (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    date_heure_pointage DATETIME NOT NULL,
    type_pointage ENUM('ENTREE', 'SORTIE') NOT NULL,
    retard BOOLEAN DEFAULT FALSE,
    minutes_retard INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

CREATE TABLE bulletins_salaire (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    employee_id BIGINT NOT NULL,
    periode_debut DATE NOT NULL,
    periode_fin DATE NOT NULL,
    salaire_base DECIMAL(10,2) NOT NULL,
    jours_travailles INT NOT NULL,
    jours_absence INT NOT NULL,
    total_retards_minutes INT NOT NULL,
    deduction_absences DECIMAL(10,2) NOT NULL,
    deduction_retards DECIMAL(10,2) NOT NULL,
    salaire_net DECIMAL(10,2) NOT NULL,
    envoye BOOLEAN DEFAULT FALSE,
    date_envoi DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (employee_id) REFERENCES employees(id) ON DELETE CASCADE
);

-- Index pour optimiser les requêtes
CREATE INDEX idx_presences_employee_date ON presences(employee_id, date_heure_pointage);
CREATE INDEX idx_presences_date ON presences(date_heure_pointage);
CREATE INDEX idx_bulletins_periode ON bulletins_salaire(periode_debut, periode_fin);