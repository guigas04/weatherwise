# WeatherWise — Weather Data Service (Java + JDBC)

Backend service that stores weather history per location, logs every query in an audit trail, and estimates the most likely condition for a future date from historical data.

> Group project for **Software Systems Construction** (Construção de Sistemas de Software) — BSc in Information Technology, Faculty of Sciences, University of Lisbon, phase 1.

## Architecture

The code is organised in layers and follows patterns from *Patterns of Enterprise Application Architecture* (Fowler):

```
business/      Transaction Script — one procedure per use case
dataaccess/    Row Data Gateway — one gateway per table, plain JDBC (no ORM)
dbutils/       schema creation and test data loading
```

| Use case | Class |
|---|---|
| A. Get the current forecast for a location | `ConsultarPrevisaoAtual` |
| B. Create or update a historical record | `AtualizarOuCriarPrevisaoHistorica` |
| C. Get the query audit log | `ObterAuditoria` |
| D. Estimate a future forecast (statistical mode of the last N days) | `EstimarPrevisaoFuturaUC` |

## Tech stack

Java 17 · JDBC · PostgreSQL · Maven · Docker / Docker Compose · JUnit 5 · H2 (tests)

## My contribution

Team of 3. My work:

- Designed the database schema (PostgreSQL, including the weather-condition enum) and the test data script.
- Implemented the **Row Data Gateways** for the data access layer.
- Implemented use cases **A** (current forecast) and **C** (query audit log).
- Wrote test helpers and fixed bugs before delivery.

## Getting started

```bash
docker compose up --build        # PostgreSQL + application
```

Unit and integration tests (in-memory H2 database):

```bash
./mvnw test
```

## Documentation

The [`docs/`](docs) folder has the technical report (in Portuguese), the entity-relationship diagram, the class diagram and the system sequence diagram (SSD) for use case D.
