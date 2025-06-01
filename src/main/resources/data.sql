-- Create test user (password: 'test123' encoded with BCrypt)
INSERT INTO users (email, password) 
VALUES ('test@example.com', '$2a$10$mkmwkiHbpahTFhXIm56C8udcu50.EYfXBR8ioDnqBb0HU1katknkC')
ON CONFLICT (email) DO NOTHING;

-- Insert sample words
INSERT INTO words (original_word, translation, language, difficulty_level, proficiency_level, user_id)
SELECT word.original_word, word.translation, 'english', word.difficulty, 1, u.id
FROM (
    VALUES 
    ('apple', 'jabłko', 1),
    ('book', 'książka', 1),
    ('cat', 'kot', 1),
    ('dog', 'pies', 1),
    ('elephant', 'słoń', 2),
    ('flower', 'kwiat', 1),
    ('guitar', 'gitara', 2),
    ('house', 'dom', 1),
    ('island', 'wyspa', 2),
    ('jacket', 'kurtka', 2),
    ('keyboard', 'klawiatura', 2),
    ('laptop', 'laptop', 1),
    ('mountain', 'góra', 2),
    ('notebook', 'zeszyt', 1),
    ('orange', 'pomarańcza', 1),
    ('pencil', 'ołówek', 1),
    ('queen', 'królowa', 3),
    ('rainbow', 'tęcza', 2),
    ('sunshine', 'słońce', 2),
    ('umbrella', 'parasol', 2)
) AS word(original_word, translation, difficulty)
CROSS JOIN users u
WHERE u.email = 'test@example.com'
ON CONFLICT DO NOTHING; 