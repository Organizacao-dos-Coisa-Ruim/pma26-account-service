-- Papel da conta (RBAC). Contas existentes viram "user".
ALTER TABLE account ADD COLUMN tx_role VARCHAR(16) NOT NULL DEFAULT 'user';

ALTER TABLE account ADD CONSTRAINT ck_account_role CHECK (tx_role IN ('user', 'admin'));
