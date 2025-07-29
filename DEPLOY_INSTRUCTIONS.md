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

## 3. Przygotowanie Frontendu

### Tworzenie projektu frontend
```bash
# W katalogu frontend
cd frontend
railway init
```

### Konfiguracja zmiennych środowiskowych
```bash
# Ustaw URL API backendu
railway variables --set "REACT_APP_API_URL=https://language-learning-backend-production.up.railway.app"
```

### Deploy frontendu
```bash
# Deploy do Railway
railway up

# Lub przez Git
git add .
git commit -m "Deploy frontend to Railway"
git push
```

## 4. Sprawdzenie działania

### Backend
```bash
railway open
```

### Frontend
```bash
cd frontend
railway open
```

## 5. Troubleshooting

### Sprawdzenie logów
```bash
railway logs
```

### Sprawdzenie zmiennych środowiskowych
```bash
railway variables
```

### Restart aplikacji
```bash
railway service restart
```

## 6. Aktualizacje

### Backend
```bash
git add .
git commit -m "Update backend"
railway up
```

### Frontend
```bash
cd frontend
git add .
git commit -m "Update frontend"
railway up
```

## 7. Backup bazy danych

```bash
# Railway automatycznie tworzy backupy
# Możesz je pobrać z Railway Dashboard
```

## 8. Monitoring

```bash
# Sprawdzenie statusu
railway status

# Sprawdzenie użycia zasobów
railway service
```

## 9. Domeny

Railway automatycznie przypisuje domeny:
- Backend: `https://language-learning-backend-production.up.railway.app`
- Frontend: `https://language-learning-frontend-production.up.railway.app`

Możesz dodać własną domenę w Railway Dashboard.

## 10. Zalety Railway vs Heroku

✅ **Darmowy tier** - 500 godzin/miesiąc  
✅ **Automatyczne deployy** z Git  
✅ **Wbudowana baza PostgreSQL**  
✅ **Lepsze wsparcie Docker**  
✅ **Szybsze deployy**  
✅ **Prostsza konfiguracja**  
✅ **Automatyczne SSL**  
✅ **Monitoring wbudowany**