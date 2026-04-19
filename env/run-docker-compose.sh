#!/bin/bash
set -e

# Run from repo root regardless of where the script is invoked from.
cd "$(dirname "$0")/.."

./gradlew clean assemble

docker compose -f env/docker-compose.yaml up --force-recreate --build -d