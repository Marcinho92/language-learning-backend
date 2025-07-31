# Language Learning Backend

Backend aplikacji do nauki języków obcych, napisany w Spring Boot z bazą danych PostgreSQL.

## Funkcjonalności

- 📚 Zarządzanie słownictwem (dodawanie, edycja, usuwanie)
- 🎯 Nauka słów z systemem poziomów zaawansowania
- 🔍 Wyszukiwanie słów po oryginalnym słowie lub tłumaczeniu
- 📊 Sortowanie po różnych kolumnach
- 📥 Import/Export danych w formacie CSV
- 💡 Przykłady użycia i wyjaśnienia dla słów

## Technologie

- **Spring Boot 3.x** - framework Java
- **PostgreSQL** - baza danych
- **JPA/Hibernate** - ORM
- **Maven** - zarządzanie zależnościami

## Szybki Start

### Lokalne uruchomienie

1. **Klonowanie repozytorium**
```bash
git clone https://github.com/Marcinho92/language-learning-backend.git
cd language-learning-backend
```

2. **Uruchomienie z Docker Compose**
```bash
docker-compose up --build
```

3. **Dostęp do aplikacji**
- Backend API: http://localhost:8080
- PgAdmin: http://localhost:5050

### Deploy na Railway

Szczegółowe instrukcje deployu znajdują się w pliku [DEPLOY_INSTRUCTIONS.md](DEPLOY_INSTRUCTIONS.md).

#### Szybki deploy:
```bash
npm install -g @railway/cli
railway login
railway init
railway up
```

## Struktura Projektu

```
src/
├── main/
│   ├── java/com/example/languagelearning/
│   │   ├── controller/     # Kontrolery REST API
│   │   ├── service/        # Logika biznesowa
│   │   ├── repository/     # Repozytoria danych
│   │   ├── model/          # Encje JPA
│   │   └── dto/           # Obiekty transferu danych
│   └── resources/
│       ├── application.yml # Konfiguracja aplikacji
│       └── data.sql       # Dane początkowe
└── test/                  # Testy jednostkowe
```

## API Endpoints

- `GET /api/words` - Pobieranie listy słów
- `GET /api/words/random` - Pobieranie losowego słowa
- `POST /api/words` - Dodawanie nowego słowa
- `PUT /api/words/{id}` - Aktualizacja słowa
- `DELETE /api/words/{id}` - Usuwanie słowa
- `POST /api/words/{id}/check` - Sprawdzanie tłumaczenia

## Frontend

Frontend aplikacji został przeniesiony do osobnego repozytorium: [language-learning-frontend](https://github.com/Marcinho92/language-learning-frontend)

## Licencja

MIT