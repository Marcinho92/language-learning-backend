# Language Learning Application

A web application for learning and managing vocabulary words with PostgreSQL database.

## Features

- Add, view, and delete vocabulary words
- Support for multiple languages (English, Polish, Spanish, German)
- Proficiency tracking (1-5 scale)
- **New**: Optional example usage sentences
- **New**: Optional detailed explanations
- Export/Import functionality via CSV
- Modern React frontend with Material-UI

## Word Model

Each word includes:
- **Original Word** (required): The word in its original language
- **Translation** (required): The translation of the word
- **Language** (required): The language of the word
- **Proficiency Level** (required): 1-5 scale indicating learning progress
- **Example Usage** (optional): Example sentence showing how to use the word
- **Explanation** (optional): Detailed explanation of the word's meaning and usage

## Database Migration

If you're updating from a previous version, the new columns will be automatically added by Hibernate. Alternatively, you can run the manual migration script:

```sql
-- Run the migration script in src/main/resources/migration_add_example_usage_explanation.sql
```

## Running the Application

1. Start the application using Docker Compose:
   ```bash
   docker-compose up
   ```

2. Access the application at `http://localhost:3000`

3. The backend API is available at `http://localhost:8080`