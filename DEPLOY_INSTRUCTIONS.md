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

# Dodaj zmienne bazy danych
railway variables --set "DATABASE_URL=jdbc:postgresql://postgres.railway.internal:5432/railway"
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
```

### Problem z deployem
```bash
# Sprawdź status serwisów
railway service

# Redeploy
railway up
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

## 7. Finalne URL-e

Po udanym deployu:
- Backend: `https://language-learning-backend-production.up.railway.app`
- Frontend: `https://language-learning-frontend-production.up.railway.app`