CREATE TABLE top_rankers (
    id BIGSERIAL PRIMARY KEY,
    student_name VARCHAR(255) NOT NULL,
    roll_number VARCHAR(50) NOT NULL,
    average_marks DOUBLE PRECISION NOT NULL,
    exam_type VARCHAR(20) NOT NULL,
    rank_position INTEGER NOT NULL,
    last_updated TIMESTAMP NOT NULL
);
-- Create index for faster queries
CREATE INDEX idx_top_rankers_exam_type ON top_rankers(exam_type);
CREATE INDEX idx_top_rankers_rank_position ON top_rankers(rank_position);
-- Create view for easy access to top rankers
CREATE OR REPLACE VIEW v_top_rankers AS
SELECT student_name,
    roll_number,
    average_marks,
    exam_type,
    rank_position,
    last_updated
FROM top_rankers
ORDER BY exam_type,
    rank_position;