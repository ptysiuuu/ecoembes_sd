# 🚀 QUICK START GUIDE

## ⚡ Super Szybki Start (5 minut)

### Krok 1: Otwórz Projekt w IntelliJ IDEA
1. Uruchom **IntelliJ IDEA**
2. **File → Open**
3. Wybierz folder: `C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated`
4. Kliknij **OK**
5. ☕ Poczekaj na zakończenie Gradle sync (może potrwać 2-3 minuty)

### Krok 2: Utwórz Compound Run Configuration
1. **Run → Edit Configurations**
2. Kliknij **"+"** → wybierz **Compound**
3. Nazwij: `Run All Ecoembes Services`
4. Kliknij **"+"** w sekcji "Run configurations to execute"
5. Dodaj po kolei (jeśli nie ma, najpierw utwórz je jako Gradle tasks):
   - **ecoembes-server** [bootRun]
   - **plassb-server** [bootRun]
   - **contsocket-server** [run]
   - **webclient** [bootRun]
6. Kliknij **Apply** → **OK**

### Krok 3: Uruchom Wszystko!
1. Wybierz `Run All Ecoembes Services` z listy konfiguracji (na górze IDE)
2. Kliknij **zielony przycisk Run** ▶️
3. Poczekaj ~30-60 sekund aż wszystkie serwisy się uruchomią

### Krok 4: Testuj Aplikację
1. Otwórz przeglądarkę: **http://localhost:8082**
2. Zaloguj się:
   - Email: `admin@ecomebes.com`
   - Hasło: `admin123`
3. **Gotowe!** Możesz korzystać z aplikacji

---

## 🎯 Dostępne Adresy

| Serwis | URL | Port |
|--------|-----|------|
| **Web Client** (Frontend) | http://localhost:8082 | 8082 |
| **Ecoembes API** (Backend) | http://localhost:8081 | 8081 |
| **Swagger UI** | http://localhost:8081/swagger-ui.html | 8081 |
| **PlasSB Server** | http://localhost:8083 | 8083 |
| **ContSocket Server** | Socket connection | 9090 |

---

## ❓ Często Zadawane Pytania

### Jak zatrzymać wszystkie serwisy?
W IntelliJ: **Run → Stop All** lub naciśnij **Ctrl+F2**

### Port już zajęty?
Zatrzymaj wszystkie serwisy i uruchom ponownie. Jeśli problem persystuje, zrestartuj komputer.

### Gradle sync nie działa?
1. **File → Invalidate Caches / Restart**
2. Po restarcie: prawy klik na projekt → **Gradle → Reload Gradle Project**

### Nie mogę się zalogować?
Sprawdź czy `ecoembes-server` się uruchomił (w zakładce Run powinno być "Started EcoembesApplication").

---

## 📖 Więcej Informacji

- **Pełna instrukcja**: [START_HERE.md](START_HERE.md)
- **Konfiguracja IntelliJ**: [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md)
- **Główna dokumentacja**: [README.md](README.md)

---

## 🐛 Problemy?

1. Sprawdź logi w zakładce **Run** (Alt+4)
2. Upewnij się że używasz **Java 21**
3. Sprawdź czy wszystkie porty są wolne: 8081, 8082, 8083, 9090
4. Zobacz [INTELLIJ_SETUP.md](INTELLIJ_SETUP.md) → sekcja "Troubleshooting"

---

**Powodzenia! 🎉**

