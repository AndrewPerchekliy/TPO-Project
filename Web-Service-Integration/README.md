# TPO1 – klienci usług sieciowych

GUI (Swing) pokazuje:
- JSON z aktualną pogodą (OpenWeather),
- kurs waluty kraju wobec waluty podanej przez użytkownika (Fixer),
- kurs NBP (PLN za 1 jednostkę waluty kraju),
- stronę Wikipedii miasta w osadzonym WebView (JavaFX `WebEngine`).

## Klucze API

Aplikacja oczekuje kluczy w zmiennych środowiskowych (albo jako `-D...`):
- `OPENWEATHER_API_KEY` (albo `OPENWEATHER_KEY`)
- `FIXER_API_KEY` (albo `FIXER_KEY`)

## Uruchomienie (Maven)

Po dodaniu JDK oraz Maven:

```bash
export OPENWEATHER_API_KEY="..."
export FIXER_API_KEY="..."
mvn -q javafx:run
```

## Notatka o kursach

- `Service#getRateFor("USD")` zwraca: **ile USD** przypada na **1 jednostkę waluty kraju**.
- `Service#getNBPRate()` zwraca: **ile PLN** kosztuje **1 jednostka waluty kraju** (wg tabel NBP A/B).

