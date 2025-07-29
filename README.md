# Language Learning Application

Aplikacja do nauki języków obcych z zarządzaniem słownictwem, napisana w Spring Boot (backend) i React (frontend).

## Funkcjonalności

- 📚 Zarządzanie słownictwem (dodawanie, edycja, usuwanie)
- 🎯 Nauka słów z systemem poziomów zaawansowania
- 🔍 Wyszukiwanie słów po oryginalnym słowie lub tłumaczeniu
- 📊 Sortowanie po różnych kolumnach
- 📥 Import/Export danych w formacie CSV
- 💡 Przykłady użycia i wyjaśnienia dla słów

## Technologie

### Backend
- **Spring Boot 3.x** - framework Java
- **PostgreSQL** - baza danych
- **JPA/Hibernate** - ORM
- **Maven** - zarządzanie zależnościami

### Frontend
- **React 18** - biblioteka JavaScript
- **Material-UI** - komponenty UI
- **Axios** - komunikacja z API

## Szybki Start

### Lokalne uruchomienie

1. **Klonowanie repozytorium**
```bash
git clone https://github.com/yourusername/docker-postgres.git
cd docker-postgres
```

2. **Uruchomienie z Docker Compose**
```bash
docker-compose up --build
```

3. **Dostęp do aplikacji**
- Frontend: http://localhost:3000
- Backend API: http://localhost:8080
- PgAdmin: http://localhost:5050

### Deploy na Railway

Szczegółowe instrukcje deployu znajdują się w pliku [DEPLOY_INSTRUCTIONS.md](DEPLOY_INSTRUCTIONS.md).

#### Szybki deploy:

**Backend:**
```bash
npm install -g @railway/cli
railway login
railway init
railway up
```

**Frontend:**
```bash
cd frontend
railway init
railway variables --set "REACT_APP_API_URL=https://language-learning-backend-production.up.railway.app"
railway up
```

## Struktura Projektu

```
```