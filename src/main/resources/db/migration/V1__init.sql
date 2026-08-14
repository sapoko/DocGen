CREATE TABLE users (
                         id BIGSERIAL PRIMARY KEY,
                         username VARCHAR(50) UNIQUE NOT NULL,
                         password_hash VARCHAR(255) NOT NULL,
                         active boolean NOT NULL DEFAULT true
);

CREATE TABLE signers (
                       id BIGSERIAL PRIMARY KEY,
                       full_name VARCHAR(100) NOT NULL,
                       rank VARCHAR(50) NOT NULL,
                       post VARCHAR(100) NOT NULL,
                       boss_group VARCHAR(100) NOT NULL,
                       active boolean NOT NULL DEFAULT true
);

CREATE TABLE samples (
                       id BIGSERIAL PRIMARY KEY,
                       file_path VARCHAR(255) NOT NULL UNIQUE,
                       public_name VARCHAR(255) NOT NULL,
                       periodicity VARCHAR(20) NOT NULL,
                       day_of_week smallint,
                       has_hospital_table boolean NOT NULL
);

CREATE TABLE sample_fields (
                       id BIGSERIAL PRIMARY KEY,
                       sample_id BIGINT NOT NULL REFERENCES samples(id),
                       placeholder VARCHAR(50) NOT NULL,
                       form_name VARCHAR(50) NOT NULL,
                       type VARCHAR(20) NOT NULL,
                       position INT NOT NULL,
                       active boolean NOT NULL DEFAULT true,
                       UNIQUE(sample_id, placeholder)
);

CREATE TABLE documents (
                       id BIGSERIAL PRIMARY KEY,
                       sample_id BIGINT NOT NULL REFERENCES samples(id),
                       user_id BIGINT NOT NULL REFERENCES users(id),
                       created_at TIMESTAMP NOT NULL
);

CREATE TABLE inputs (
                       id BIGSERIAL PRIMARY KEY,
                       document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                       sample_field_id BIGINT NOT NULL REFERENCES sample_fields(id) ON DELETE RESTRICT,
                       value VARCHAR(300),
                       signer_id BIGINT REFERENCES signers(id)
);

CREATE TABLE hospital_rows (
                       id BIGSERIAL PRIMARY KEY,
                       document_id BIGINT NOT NULL REFERENCES documents(id) ON DELETE CASCADE,
                       position INT NOT NULL,
                       rank VARCHAR(50) NOT NULL,
                       full_name VARCHAR(100) NOT NULL,
                       platoon VARCHAR(50) NOT NULL,
                       hospital_title VARCHAR(150) NOT NULL,
                       diagnosis VARCHAR(100) NOT NULL,
                       admitted_at DATE NOT NULL
);

