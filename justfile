set shell := ["bash", "-cu"]

# List all available recipes.
default:
    @just --list

# Remove Maven build output from every module.
clean:
    ./mvnw clean

# Build all modules in dependency order.
build:
    ./mvnw clean verify

# Run unit tests in every module.
test:
    ./mvnw test

# Full lifecycle: stop API, reset DB, seed DB, verify API, deploy API, test API Endpoints
lifecycle:
    #!/usr/bin/env bash
    set -euo pipefail

    cleanup() {
        just --justfile api/justfile stop
    }
    trap cleanup EXIT

    just --justfile api/justfile stop
    just --justfile database/justfile rebuild
    just --justfile api/justfile verify
    just --justfile api/justfile up
    just --justfile api/justfile wait
    just --justfile api/justfile integration

    echo ""
    echo "✓ CI lifecycle complete."