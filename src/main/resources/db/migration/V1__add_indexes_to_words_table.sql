-- Dodanie indeksów do tabeli words dla poprawy wydajności
-- V1__add_indexes_to_words_table.sql

-- 1. Indeks na kolumnie language - często używana w getRandomWord()
-- Ten indeks znacznie przyspieszy wyszukiwanie słów po języku
CREATE INDEX IF NOT EXISTS idx_words_language ON words(language);

-- 2. Indeks na kolumnie original_word - dla findByOriginalWord()
-- Przyspieszy sprawdzanie duplikatów i wyszukiwanie po oryginalnym słowie
CREATE INDEX IF NOT EXISTS idx_words_original_word ON words(original_word);

-- 3. Indeks złożony na language + proficiency_level
-- Przyspieszy wyszukiwanie słów o określonym poziomie trudności w danym języku
-- Użyteczne dla algorytmu ważenia w getRandomWord()
CREATE INDEX IF NOT EXISTS idx_words_language_proficiency ON words(language, proficiency_level);

-- 4. Indeks na proficiency_level - dla sortowania i filtrowania
-- Przyspieszy operacje sortowania po poziomie trudności
CREATE INDEX IF NOT EXISTS idx_words_proficiency_level ON words(proficiency_level);

-- 5. Indeks częściowy na language + proficiency_level dla słów o niskim poziomie
-- Przyspieszy wyszukiwanie słów do nauki (poziom 1-2)
CREATE INDEX IF NOT EXISTS idx_words_learning_words ON words(language, proficiency_level) 
WHERE proficiency_level <= 3;

-- 6. Indeks na translation - dla wyszukiwania po tłumaczeniu
-- Przyspieszy sprawdzanie tłumaczeń w checkTranslation()
CREATE INDEX IF NOT EXISTS idx_words_translation ON words(translation);

-- 7. Indeks złożony na language + id dla efektywnej paginacji
-- Przyspieszy paginację słów w określonym języku
CREATE INDEX IF NOT EXISTS idx_words_language_id ON words(language, id);

-- 8. Indeks na kolumnie explanation (jeśli nie jest NULL)
-- Przyspieszy wyszukiwanie słów z wyjaśnieniami
CREATE INDEX IF NOT EXISTS idx_words_explanation_not_null ON words(explanation) 
WHERE explanation IS NOT NULL;

-- 9. Indeks na kolumnie example_usage (jeśli nie jest NULL)
-- Przyspieszy wyszukiwanie słów z przykładami użycia
CREATE INDEX IF NOT EXISTS idx_words_example_usage_not_null ON words(example_usage) 
WHERE example_usage IS NOT NULL;

-- 10. Indeks złożony na language + proficiency_level + id
-- Optymalny dla zapytań z sortowaniem i filtrowaniem
CREATE INDEX IF NOT EXISTS idx_words_language_proficiency_id ON words(language, proficiency_level, id);

-- Komentarz: Te indeksy są zoptymalizowane pod kątem najczęściej wykonywanych operacji:
-- - Wyszukiwanie po języku (getRandomWord)
-- - Paginacja z sortowaniem
-- - Wyszukiwanie po oryginalnym słowie
-- - Operacje bulk na określonych językach
-- - Filtrowanie po poziomie trudności
