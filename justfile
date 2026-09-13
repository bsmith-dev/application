set shell := ["bash", "-cu"]

# Build all modules in dependency order.
build:
    mvn clean verify

# Build only the database module.
database:
    just --justfile database/justfile build

# Build the API module and its required dependencies.
api:
    just --justfile api/justfile build

# Build the UI module and its required dependencies.
ui:
    just --justfile ui/justfile build

# Run tests in every module.
test:
    mvn test

# Remove Maven build output from every module.
clean:
    mvn clean

default:
    @just --list

# Full lifecycle: stop app, reset DB, seed DB, app-verify, deploy, and validate
test-lifecycle:
    #!/usr/bin/env bash
    set -euo pipefail

    cleanup() {
        just --justfile api/justfile app-stop
    }
    trap cleanup EXIT

    just --justfile api/justfile app-stop
    just --justfile database/justfile rebuild
    just --justfile api/justfile app-verify
    just --justfile api/justfile app-up
    just --justfile api/justfile app-wait
    just --justfile api/justfile test-http

    echo ""
    echo "✓ CI lifecycle complete."
