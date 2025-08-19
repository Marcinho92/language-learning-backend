# Optymalizacja bazy danych PostgreSQL

## Dodane indeksy do tabeli `words`

### 1. **Indeks podstawowy na `language`**
```sql
CREATE INDEX idx_words_language ON words(language);
```
- **Cel**: Przyspieszenie `findByLanguage()` używanej w `getRandomWord()`
- **Wpływ**: Znaczne przyspieszenie wyszukiwania słów po języku
- **Użycie**: Endpoint `/api/words/random?language=polish`

### 2. **Indeks na `original_word`**
```sql
CREATE INDEX idx_words_original_word ON words(original_word);
```
- **Cel**: Przyspieszenie `findByOriginalWord()` dla sprawdzania duplikatów
- **Wpływ**: Szybkie wyszukiwanie po oryginalnym słowie
- **Użycie**: Import CSV, sprawdzanie unikalności

### 3. **Indeks złożony na `language + proficiency_level`**
```sql
CREATE INDEX idx_words_language_proficiency ON words(language, proficiency_level);
```
- **Cel**: Optymalizacja algorytmu ważenia w `getRandomWord()`
- **Wpływ**: Przyspieszenie wyszukiwania słów o określonym poziomie w danym języku
- **Użycie**: Logika biznesowa wyboru słów do nauki

### 4. **Indeks na `proficiency_level`**
```sql
CREATE INDEX idx_words_proficiency_level ON words(proficiency_level);
```
- **Cel**: Przyspieszenie sortowania i filtrowania po poziomie trudności
- **Wpływ**: Lepsze wyniki w paginacji z sortowaniem
- **Użycie**: Endpoint `/api/words/paginated?sortBy=proficiencyLevel`

### 5. **Indeks częściowy na słowa do nauki**
```sql
CREATE INDEX idx_words_learning_words ON words(language, proficiency_level) 
WHERE proficiency_level <= 3;
```
- **Cel**: Optymalizacja dla słów o niskim poziomie (1-3)
- **Wpływ**: Szybsze wyszukiwanie słów do nauki
- **Użycie**: Algorytm wyboru słów dla początkujących

### 6. **Indeks na `translation`**
```sql
CREATE INDEX idx_words_translation ON words(translation);
```
- **Cel**: Przyspieszenie `checkTranslation()`
- **Wpływ**: Szybkie sprawdzanie poprawności tłumaczeń
- **Użycie**: Endpoint `/api/words/{id}/check`

### 7. **Indeks złożony na `language + id`**
```sql
CREATE INDEX idx_words_language_id ON words(language, id);
```
- **Cel**: Optymalizacja paginacji w określonym języku
- **Wpływ**: Lepsze wyniki dla dużych zbiorów danych
- **Użycie**: Paginacja z filtrowaniem po języku

### 8. **Indeks na `explanation` (nie-NULL)**
```sql
CREATE INDEX idx_words_explanation_not_null ON words(explanation) 
WHERE explanation IS NOT NULL;
```
- **Cel**: Przyspieszenie wyszukiwania słów z wyjaśnieniami
- **Wpływ**: Optymalizacja dla słów z dodatkowymi informacjami
- **Użycie**: Filtrowanie słów z wyjaśnieniami

### 9. **Indeks na `example_usage` (nie-NULL)**
```sql
CREATE INDEX idx_words_example_usage_not_null ON words(example_usage) 
WHERE example_usage IS NOT NULL;
```
- **Cel**: Przyspieszenie wyszukiwania słów z przykładami
- **Wpływ**: Optymalizacja dla słów z przykładami użycia
- **Użycie**: Filtrowanie słów z przykładami

### 10. **Indeks złożony na `language + proficiency_level + id`**
```sql
CREATE INDEX idx_words_language_proficiency_id ON words(language, proficiency_level, id);
```
- **Cel**: Optymalizacja złożonych zapytań z sortowaniem
- **Wpływ**: Najlepsze wyniki dla zaawansowanych operacji
- **Użycie**: Złożone zapytania z wieloma warunkami

## Oczekiwane korzyści

### **Przed dodaniem indeksów:**
- Wyszukiwanie po języku: O(n) - pełne skanowanie tabeli
- Sortowanie: O(n log n) - sortowanie w pamięci
- Filtrowanie: O(n) - sprawdzanie każdego wiersza

### **Po dodaniu indeksów:**
- Wyszukiwanie po języku: O(log n) - wyszukiwanie binarne
- Sortowanie: O(n) - wykorzystanie indeksów
- Filtrowanie: O(log n) - szybkie filtrowanie

## Jak zastosować indeksy

### **Opcja 1: Migracja SQL**
```bash
# Połącz się z bazą PostgreSQL
psql -h localhost -U username -d database_name

# Wykonaj plik migracji
\i src/main/resources/db/migration/V1__add_indexes_to_words_table.sql
```

### **Opcja 2: Automatyczna migracja**
Jeśli używasz Flyway lub Liquibase, plik migracji zostanie automatycznie wykonany przy następnym uruchomieniu aplikacji.

## Monitorowanie wydajności

### **Sprawdzenie użycia indeksów:**
```sql
-- Sprawdź statystyki użycia indeksów
SELECT 
    schemaname,
    tablename,
    indexname,
    idx_scan,
    idx_tup_read,
    idx_tup_fetch
FROM pg_stat_user_indexes 
WHERE tablename = 'words'
ORDER BY idx_scan DESC;
```

### **Analiza zapytań:**
```sql
-- Włącz analizę zapytań
SET track_io_timing = ON;
SET log_statement = 'all';

-- Sprawdź plan wykonania
EXPLAIN (ANALYZE, BUFFERS) 
SELECT * FROM words WHERE language = 'polish' AND proficiency_level <= 3;
```

## Uwagi dotyczące konserwacji

### **Regularne operacje:**
- **VACUUM ANALYZE**: Co tydzień dla aktualizacji statystyk
- **REINDEX**: Co miesiąc dla optymalizacji indeksów
- **Monitoring**: Sprawdzanie użycia indeksów co miesiąc

### **Usuwanie nieużywanych indeksów:**
```sql
-- Sprawdź nieużywane indeksy
SELECT 
    indexname,
    idx_scan,
    idx_tup_read
FROM pg_stat_user_indexes 
WHERE tablename = 'words' 
AND idx_scan = 0;
```

## Koszty indeksów

### **Zalety:**
- Znaczne przyspieszenie zapytań SELECT
- Lepsze wyniki sortowania i filtrowania
- Optymalizacja operacji JOIN

### **Wady:**
- Zwiększone zużycie dysku (ok. 20-30% więcej)
- Wolniejsze operacje INSERT/UPDATE/DELETE
- Większe zużycie pamięci

### **Rekomendacje:**
- Indeksy są szczególnie korzystne dla tabel z dużą liczbą wierszy (>1000)
- W przypadku częstych operacji INSERT, rozważ usunięcie mniej ważnych indeksów
- Monitoruj wpływ na wydajność operacji zapisu
