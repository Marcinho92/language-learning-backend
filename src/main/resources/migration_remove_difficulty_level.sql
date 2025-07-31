-- Migration script to remove difficulty_level column from words table
-- Run this script if you want to manually remove the column instead of relying on Hibernate auto-update

-- Remove difficulty_level column
-- ALTER TABLE words DROP COLUMN IF EXISTS difficulty_level; 