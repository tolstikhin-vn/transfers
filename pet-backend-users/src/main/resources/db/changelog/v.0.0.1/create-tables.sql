-- liquibase formatted sql
-- changeset vrtn:createInitTables
CREATE TABLE users (
                       id SERIAL PRIMARY KEY NOT NULL,
                       last_name VARCHAR(255) NOT NULL,
                       first_name VARCHAR(255) NOT NULL,
                       father_name VARCHAR(255) NOT NULL,
                       phone_number VARCHAR(11) UNIQUE NOT NULL,
                       birth_date VARCHAR(10) NOT NULL,
                       passport_number VARCHAR(10) UNIQUE NOT NULL,
                       email VARCHAR(255),
                       create_date_time TIMESTAMP DEFAULT now(),
                       is_active BOOLEAN DEFAULT true,
                       is_deleted BOOLEAN DEFAULT false
);