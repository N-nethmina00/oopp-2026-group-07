-- Run once after importing university_db.sql.
-- Select your application database before running.
-- Existing sessions remain shared with batch_id = NULL.

ALTER TABLE sessions
    MODIFY COLUMN session_id INT NOT NULL AUTO_INCREMENT,
    ADD COLUMN batch_id INT NULL,
    ADD CONSTRAINT fk_sessions_batch
    FOREIGN KEY (batch_id) REFERENCES batch(batch_id);