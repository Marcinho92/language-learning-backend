# Instrukcje Deployu na Railway

## Wymagania
- Konto na Railway (https://railway.app)
- Railway CLI zainstalowane
- Git zainstalowane

## 1. Instalacja Railway CLI

```bash
# Instalacja przez npm
npm install -g @railway/cli

# Lub przez curl (Linux/macOS)
curl -fsSL https://railway.app/install.sh | sh

# Lub przez winget (Windows)
winget install Railway.Railway
```

## 2. Przygotowanie Backendu

### Logowanie do Railway
```bash
railway login
```

### Tworzenie projektu
```bash
# W katalogu głównym projektu
railway init
```

### Dodanie bazy danych PostgreSQL
```bash
# W Railway Dashboard:
# 1. Przejdź do swojego projektu
# 2. Kliknij "New Service"
# 3. Wybierz "Database" -> "PostgreSQL"
# 4. Railway automatycznie doda zmienną DATABASE_URL
```

### Konfiguracja zmiennych środowiskowych
```bash
# Ustaw profil Spring
railway variables --set "SPRING_PROFILES_ACTIVE=railway"

# Sprawdź zmienne
railway variables
```

### Deploy backendu
```bash
# Deploy do Railway
railway up

# Lub przez Git
git add .
git commit -m "Deploy backend to Railway"
git push
```

### Sprawdzenie logów
```bash
railway logs
```

## 3. Konfiguracja bazy danych

### Sprawdzenie zmiennych bazy danych
```bash
# Przejdź do serwisu PostgreSQL
railway service Postgres

# Sprawdź zmienne
railway variables
```

### Dodanie zmiennych do serwisu aplikacji
```bash
# Przejdź do serwisu aplikacji
railway service language-learning-backend

# Railway automatycznie dodaje DATABASE_URL, ale możesz dodać dodatkowe zmienne:
railway variables --set "DB_USERNAME=postgres"
railway variables --set "DB_PASSWORD=your_password_here"
```

## 4. Sprawdzenie działania

### Backend
```bash
railway open
```

### Sprawdzenie API
```bash
# Test endpointu
curl https://language-learning-backend-production.up.railway.app/api/words
```

## 5. Troubleshooting

### Problem z połączeniem do bazy danych
```bash
# Sprawdź logi
railway logs

# Sprawdź zmienne środowiskowe
railway variables

# Upewnij się, że DATABASE_URL jest poprawnie ustawiona
# Format: postgresql://username:password@host:port/database
```

### Problem z deployem
```bash
# Sprawdź status serwisów
railway service

# Redeploy
railway up

# Sprawdź build logs
railway logs --service language-learning-backend
```

### Problem z pamięcią
```bash
# Dodaj zmienną dla zwiększenia pamięci JVM
railway variables --set "JAVA_OPTS=-Xmx512m -Xms256m"
```

## 6. Frontend

Frontend aplikacji został przeniesiony do osobnego repozytorium: [language-learning-frontend](https://github.com/Marcinho92/language-learning-frontend)

### Deploy frontendu
```bash
# W repozytorium frontendu
railway init
railway variables --set "REACT_APP_API_URL=https://language-learning-backend-production.up.railway.app"
railway up
```

## 7. Ważne uwagi

### Konfiguracja bazy danych
- Railway automatycznie tworzy zmienną `DATABASE_URL` dla serwisu PostgreSQL
- Nie musisz ręcznie ustawiać `DATABASE_URL` - Railway to robi automatycznie

### Port i healthcheck
- Aplikacja nasłuchuje na porcie określonym przez zmienną `PORT` (domyślnie 8080)
- Healthcheck jest skonfigurowany na `/api/words` w `railway.json`
- Upewnij się, że endpoint `/api/words` jest dostępny

### Migracje bazy danych
- Aplikacja używa `hibernate.ddl-auto: validate` w profilu railway
- Upewnij się, że struktura bazy danych jest zgodna z modelami JPA
- Jeśli potrzebujesz resetować bazę danych, zmień na `create` tymczasowo

## 8. Finalne URL-e

Po udanym deployu:
- Backend: `https://language-learning-backend-production.up.railway.app`
- Frontend: `https://language-learning-frontend-production.up.railway.app`

## 9. Dodatkowe komendy

```bash
# Sprawdź status wszystkich serwisów
railway status

# Sprawdź szczegóły serwisu
railway service language-learning-backend

# Sprawdź zmienne dla konkretnego serwisu
railway variables --service language-learning-backend

# Redeploy konkretnego serwisu
railway up --service language-learning-backend
```