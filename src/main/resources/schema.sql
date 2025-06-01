DROP TABLE IF EXISTS words;
DROP TABLE IF EXISTS users;

CREATE TABLE users (
    id SERIAL PRIMARY KEY,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL
);

CREATE TABLE words (
    id SERIAL PRIMARY KEY,
    original_word VARCHAR(255) NOT NULL,
    translation VARCHAR(255) NOT NULL,
    language VARCHAR(50) NOT NULL,
    difficulty_level INTEGER NOT NULL,
    proficiency_level INTEGER NOT NULL DEFAULT 1,
    user_id INTEGER NOT NULL,
    FOREIGN KEY (user_id) REFERENCES users(id)
); 