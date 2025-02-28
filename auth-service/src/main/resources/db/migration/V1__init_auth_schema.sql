CREATE TABLE IF NOT EXISTS credentials (
                                           id BIGSERIAL PRIMARY KEY,
                                           username VARCHAR(50) NOT NULL UNIQUE,
                                           password_hash VARCHAR(255) NOT NULL,
                                           last_password_change TIMESTAMP WITH TIME ZONE,
                                           account_locked BOOLEAN DEFAULT FALSE,
                                           failed_login_attempts INTEGER DEFAULT 0,
                                           user_id BIGINT NOT NULL
);

CREATE INDEX idx_credentials_username ON credentials(username);
CREATE INDEX idx_credentials_user_id ON credentials(user_id);