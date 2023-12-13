CREATE TABLE history
(
    uuid                  UUID           NOT NULL,
    account_number_from   VARCHAR(16)    NOT NULL,
    account_number_to     VARCHAR(16)    NOT NULL,
    amount                DECIMAL(15, 2) NOT NULL,
    cur                   VARCHAR(3)     NOT NULL,
    transaction_date_time TIMESTAMP      NOT NULL
);