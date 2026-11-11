-- V6 criou token_hash como CHAR(64), enquanto a entidade JPA espera VARCHAR(64).
-- Preserva dados existentes; converte a coluna antes da validacao do Hibernate.
ALTER TABLE verificacao_email_professor
    ALTER COLUMN token_hash TYPE VARCHAR(64);
