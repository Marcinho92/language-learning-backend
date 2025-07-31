-- Migration script to add exampleUsage and explanation columns to words table
-- Run this script if you want to manually add the columns instead of relying on Hibernate auto-update

-- Add exampleUsage column
ALTER TABLE words ADD COLUMN IF NOT EXISTS example_usage TEXT;

-- Add explanation column  
ALTER TABLE words ADD COLUMN IF NOT EXISTS explanation TEXT;

-- Add comments to the columns for documentation
COMMENT ON COLUMN words.example_usage IS 'Example sentence showing how to use the word';
COMMENT ON COLUMN words.explanation IS 'Detailed explanation of the word meaning and usage'; 