#!/usr/bin/env bash
set -e

# Exporter les variables d'environnement du fichier .env
if [ -f .env ]; then
  export $(grep -v '^#' .env | grep -E '^[a-zA-Z_][a-zA-Z0-9_]*=' | xargs)
fi

# Définir les identifiants GCP si le fichier est présent
if [ -f .gcloud-conf.json ]; then
  export GOOGLE_APPLICATION_CREDENTIALS="${GOOGLE_APPLICATION_CREDENTIALS:-$(pwd)/.gcloud-conf.json}"
fi

# Démarrer Quarkus en mode dev
quarkus dev
