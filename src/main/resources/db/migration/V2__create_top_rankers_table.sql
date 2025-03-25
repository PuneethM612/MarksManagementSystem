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
-- Add comments to the table and columns
COMMENT ON TABLE top_rankers IS 'Stores information about top 3 rankers for each exam type';
COMMENT ON COLUMN top_rankers.id IS 'Unique identifier for each record';
COMMENT ON COLUMN top_rankers.student_name IS 'Name of the student';
COMMENT ON COLUMN top_rankers.roll_number IS 'Roll number of the student';
COMMENT ON COLUMN top_rankers.average_marks IS 'Average marks scored by the student';
COMMENT ON COLUMN top_rankers.exam_type IS 'Type of exam (MONTHLY, MID, ANNUAL)';
COMMENT ON COLUMN top_rankers.rank_position IS 'Position in the ranking (1, 2, or 3)';
COMMENT ON COLUMN top_rankers.last_updated IS 'Timestamp of when the record was last updated';
-- Create a function to automatically update last_updated timestamp
CREATE OR REPLACE FUNCTION update_last_updated_column() RETURNS TRIGGER AS $$ BEGIN NEW.last_updated = CURRENT_TIMESTAMP;
RETURN NEW;
END;
$$ language 'plpgsql';
-- Create a trigger to automatically update last_updated
CREATE TRIGGER update_top_rankers_last_updated BEFORE
UPDATE ON top_rankers FOR EACH ROW EXECUTE FUNCTION update_last_updated_column();