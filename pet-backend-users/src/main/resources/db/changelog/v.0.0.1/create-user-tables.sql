CREATE TABLE users
(
    id               SERIAL PRIMARY KEY NOT NULL,
    last_name        VARCHAR(255)       NOT NULL,
    first_name       VARCHAR(255)       NOT NULL,
    father_name      VARCHAR(255)       NOT NULL,
    phone_number     VARCHAR(11) UNIQUE NOT NULL,
    birth_date       VARCHAR(10)        NOT NULL,
    passport_number  VARCHAR(10) UNIQUE NOT NULL,
    email            VARCHAR(255),
    create_date_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    is_active        BOOLEAN   DEFAULT TRUE,
    is_deleted       BOOLEAN   DEFAULT FALSE
);