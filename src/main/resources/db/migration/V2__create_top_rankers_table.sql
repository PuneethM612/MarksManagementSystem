-- Create top_rankers table
CREATE TABLE IF NOT EXISTS top_rankers (
    id SERIAL PRIMARY KEY,
    student_name VARCHAR(100) NOT NULL,
    roll_number VARCHAR(20) NOT NULL,
    average_marks DECIMAL(5, 2) NOT NULL,
    exam_type VARCHAR(20) NOT NULL,
    rank_position INTEGER NOT NULL,
    last_updated TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);
-- Create indexes for faster queries
CREATE INDEX IF NOT EXISTS idx_top_rankers_exam_type ON top_rankers(exam_type);
CREATE INDEX IF NOT EXISTS idx_top_rankers_rank_position ON top_rankers(rank_position);
CREATE INDEX IF NOT EXISTS idx_top_rankers_roll_number ON top_rankers(roll_number);
-- Create a view for easy access to top rankers
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