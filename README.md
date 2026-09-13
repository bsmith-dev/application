# application

A Maven multi-module project with `database`, `api`, and `ui` modules.

## Dependency direction

```text
database <- api <- ui
```

- `database`: persistence-oriented shared module placeholder.
- `api`: depends on `database`.
- `ui`: depends on `api`.

Each module includes a local `justfile`. The root `justfile` builds the full Maven reactor or delegates to an individual module.

## Prerequisites

- JDK 21+
- Maven 3.9+
- [just](https://just.systems/) (optional)

## Root commands

```bash
just db-api-ui-build      # Build database, api, and ui
just database   # Build database only
just api        # Build api and database
just ui         # Build ui, api, and database
just test       # Test all modules
just clean      # Clean all modules
```

## Direct Maven equivalents

```bash
mvn clean verify
mvn -pl database -am clean verify
mvn -pl api -am clean verify
mvn -pl ui -am clean verify
```
