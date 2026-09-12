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
