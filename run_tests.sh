#!/usr/bin/env bash
# Compila ed esegue i test automatici di ArrowClock in una cartella temporanea.
# Uso:  ./run_tests.sh      (richiede solo un JDK 17+ nel PATH)
set -e
DIR="$(cd "$(dirname "$0")" && pwd)"
OUT="$(mktemp -d)"
trap 'rm -rf "$OUT"' EXIT

javac -d "$OUT" "$DIR"/Codes/*.java "$DIR"/Tests/*.java
( cd "$OUT" && java EsecutoreTest )
