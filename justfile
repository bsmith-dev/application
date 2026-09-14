set shell := ["bash", "-cu"]

root_mvn := "./mvnw"
api_justfile := "api/justfile"
database_justfile := "database/justfile"
api_dir := "api"
database_dir := "database"

# List all available recipes.
default:
    @just --list

# Remove Maven build output from every module.
clean:
    {{root_mvn}} clean

# Build all modules in dependency order.
build:
    {{root_mvn}} clean verify

# Run unit tests in every module.
test:
    {{root_mvn}} test

# Start the local database and run the API in the foreground.
start:
    (cd {{database_dir}} && just --justfile justfile start)
    (cd {{api_dir}} && just --justfile justfile run)

# Stop the packaged API if it is running.
app-stop:
    just --justfile {{api_justfile}} stop

# Start the packaged API in the background.
app-up:
    just --justfile {{api_justfile}} up

# Wait for the packaged API to accept HTTP connections.
app-wait:
    just --justfile {{api_justfile}} wait

# Run the API in the foreground (database must already be up).
app-run:
    (cd {{api_dir}} && just --justfile justfile run)

# Run the full API verification gate.
app-verify:
    (cd {{api_dir}} && just --justfile justfile verify)

# Run the full API unit and MockMvc test suite.
test-unit:
    (cd {{api_dir}} && just --justfile justfile test-unit)

# Run a single API test class by simple name.
test-class name:
    (cd {{api_dir}} && just --justfile justfile test-class {{name}})

# Build the packaged API JAR without tests.
package:
    (cd {{api_dir}} && just --justfile justfile package)

# Open the API coverage report.
coverage:
    (cd {{api_dir}} && just --justfile justfile coverage)

# Run HTTP request tests against the local API.
test-http:
    (cd {{api_dir}} && just --justfile justfile test-http)

# Start the local database container and wait for readiness.
db-start:
    (cd {{database_dir}} && just --justfile justfile start)

# Start the local database container.
db-up:
    (cd {{database_dir}} && just --justfile justfile up)

# Wait for the local database container to accept connections.
db-wait:
    (cd {{database_dir}} && just --justfile justfile wait)

# Stop the local database container and preserve data.
db-down:
    (cd {{database_dir}} && just --justfile justfile down)

# Stop the local database container and delete its volume.
db-destroy:
    (cd {{database_dir}} && just --justfile justfile destroy)

# Follow local database logs.
db-logs:
    (cd {{database_dir}} && just --justfile justfile logs)

# Run Flyway migrations against the local database.
db-migrate:
    (cd {{database_dir}} && just --justfile justfile migrate)

# Load the local sample dataset into the database.
db-seed-sample:
    (cd {{database_dir}} && just --justfile justfile seed)

# Recreate, migrate, and seed the local database.
db-rebuild:
    (cd {{database_dir}} && just --justfile justfile rebuild)

# Open an interactive local database shell.
db-shell:
    (cd {{database_dir}} && just --justfile justfile shell)

# Full lifecycle: stop the app, rebuild the DB, verify and package the API, then run the HTTP workflow
ci:
    #!/usr/bin/env bash
    set -euo pipefail

    cleanup() {
        just --justfile {{api_justfile}} stop
    }
    trap cleanup EXIT

    just --justfile {{api_justfile}} stop
    (cd {{database_dir}} && just --justfile justfile rebuild)
    (cd {{api_dir}} && just --justfile justfile verify)
    (cd {{api_dir}} && just --justfile justfile package)
    just --justfile {{api_justfile}} up
    just --justfile {{api_justfile}} wait
    (cd {{api_dir}} && just --justfile justfile test-http)

    echo ""
    echo "✓ CI lifecycle complete."