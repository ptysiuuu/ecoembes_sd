# ✅ PROJEKT ZOSTAŁ POMYŚLNIE ROZDZIELONY

## 📁 Struktura Projektu

Projekt został podzielony na 4 niezależne projekty w folderze:
```
C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated\
```

### Zawartość:

1. **ecoembes-server** - Główny serwer backend (Port 8081)
2. **plassb-server** - Serwer zakładu recyklingu plastiku (Port 8083)
3. **contsocket-server** - Serwer zakładu recyklingu kontenerów (Port 9090)
4. **webclient** - Aplikacja webowa (Port 8082)

## 🚀 Jak Uruchomić

### Opcja 1: W IntelliJ IDEA (ZALECANA)

1. **Otwórz IntelliJ IDEA**
2. **File → Open**
3. Wybierz folder `Ecoembes-Separated`
4. Poczekaj na synchronizację Gradle
5. **Stwórz Compound Run Configuration:**
   - Run → Edit Configurations
   - Kliknij "+" → Compound
   - Nazwij: "Run All Ecoembes Services"
   - Dodaj 4 konfiguracje:
     * `ecoembes-server [bootRun]`
     * `plassb-server [bootRun]`
     * `contsocket-server [run]`
     * `webclient [bootRun]`
   - Apply → OK
6. **Uruchom jednym kliknięciem!** ▶️

Szczegółowe instrukcje: `INTELLIJ_SETUP.md`

### Opcja 2: Ręcznie w Terminalu

Otwórz 4 osobne okna PowerShell:

**Terminal 1 - Ecoembes Server:**
```powershell
cd C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated\ecoembes-server
.\gradlew.bat bootRun
```

**Terminal 2 - PlasSB Server:**
```powershell
cd C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated\plassb-server
.\gradlew.bat bootRun
```

**Terminal 3 - ContSocket Server:**
```powershell
cd C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated\contsocket-server
.\gradlew.bat run
```

**Terminal 4 - Web Client:**
```powershell
cd C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated\webclient
.\gradlew.bat bootRun
```

### Opcja 3: Skrypt PowerShell (Jeśli utworzony)

```powershell
cd C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated
.\start-all-services.ps1
```

## 🌐 Adresy URL Po Uruchomieniu

- **Web Client (Frontend)**: http://localhost:8082
- **Ecoembes API + Swagger**: http://localhost:8081/swagger-ui.html
- **PlasSB API**: http://localhost:8083
- **ContSocket**: Socket na porcie 9090

## 🔐 Dane Logowania

- **Email**: `admin@ecomebes.com`
- **Hasło**: `admin123`

## 📝 Kolejność Uruchamiania

**WAŻNE**: Zalecana kolejność:
1. `ecoembes-server` (główny serwer)
2. `plassb-server` i `contsocket-server` (można równolegle)
3. `webclient` (na końcu)

## ✅ Co Zostało Zrobione

- ✅ Rozdzielenie na 4 niezależne projekty
- ✅ Skopiowanie całego kodu źródłowego
- ✅ Utworzenie osobnych plików `build.gradle` i `settings.gradle`
- ✅ Skopiowanie Gradle Wrapper dla każdego projektu
- ✅ Utworzenie README dla każdego projektu
- ✅ Utworzenie instrukcji IntelliJ IDEA
- ✅ Skopiowanie plików `.gitignore`

## 📚 Dokumentacja

- `README.md` - Główna dokumentacja
- `INTELLIJ_SETUP.md` - Instrukcje dla IntelliJ IDEA
- `ecoembes-server/README.md` - Dokumentacja serwera głównego
- `plassb-server/README.md` - Dokumentacja PlasSB
- `contsocket-server/README.md` - Dokumentacja ContSocket
- `webclient/README.md` - Dokumentacja klienta webowego

## 🔧 Testowanie

Każdy projekt można testować osobno:
```bash
cd <nazwa-projektu>
.\gradlew.bat test
```

## 🏗️ Budowanie

Budowanie wszystkich projektów:
```bash
cd ecoembes-server && .\gradlew.bat build
cd ..\plassb-server && .\gradlew.bat build
cd ..\contsocket-server && .\gradlew.bat build
cd ..\webclient && .\gradlew.bat build
```

## ⚠️ Uwagi

- Oryginalny projekt pozostaje niezmieniony w: `C:\Users\xadir\Documents\soft_labs\Ecoembes`
- Nowe projekty są w: `C:\Users\xadir\Documents\soft_labs\Ecoembes-Separated`
- Każdy projekt ma własne repozytorium Gradle
- Projekty są całkowicie niezależne

## 🎯 Następne Kroki

1. Otwórz projekty w IntelliJ IDEA
2. Poczekaj na zakończenie indeksowania
3. Stwórz Compound Run Configuration
4. Uruchom wszystkie serwisy
5. Otwórz http://localhost:8082 w przeglądarce
6. Zaloguj się i przetestuj funkcjonalności

## ❓ Problemy?

Sprawdź:
- `INTELLIJ_SETUP.md` - sekcja Troubleshooting
- Logi w konsoli każdego serwisu
- Czy wszystkie porty są wolne (8081, 8082, 8083, 9090)

---

**Powodzenia! 🚀**

