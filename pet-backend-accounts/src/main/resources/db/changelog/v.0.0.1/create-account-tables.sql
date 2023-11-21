CREATE TABLE accounts (
                         id SERIAL PRIMARY KEY NOT NULL,
                         account_number VARCHAR(16) UNIQUE NOT NULL,
                         client_id INT NOT NULL,
                         cur VARCHAR(3) NOT NULL,
                         balance DECIMAL(15,2) DEFAULT 0,
                         create_date_time TIMESTAMP,
                         is_main BOOLEAN DEFAULT true,
                         is_closed BOOLEAN DEFAULT false
);