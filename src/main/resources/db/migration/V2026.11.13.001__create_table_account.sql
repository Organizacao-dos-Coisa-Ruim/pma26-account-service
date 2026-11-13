CREATE TABLE account (
    id_account       VARCHAR(36)  NOT NULL,
    tx_name          VARCHAR(256) NOT NULL,
    tx_email         VARCHAR(256) NOT NULL,
    tx_password_hash VARCHAR(256) NOT NULL,
    CONSTRAINT pk_account PRIMARY KEY (id_account),
    CONSTRAINT uk_account_email UNIQUE (tx_email)
);
