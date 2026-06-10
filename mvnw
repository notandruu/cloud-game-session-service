#!/usr/bin/env sh
set -eu

MVN_VERSION="${MVN_VERSION:-3.9.9}"
BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
MAVEN_DIR="$BASE_DIR/.mvn/apache-maven-$MVN_VERSION"
ARCHIVE="$BASE_DIR/.mvn/apache-maven-$MVN_VERSION-bin.tar.gz"
MAVEN_URL="https://archive.apache.org/dist/maven/maven-3/$MVN_VERSION/binaries/apache-maven-$MVN_VERSION-bin.tar.gz"

if [ ! -x "$MAVEN_DIR/bin/mvn" ]; then
  mkdir -p "$BASE_DIR/.mvn"
  if command -v curl >/dev/null 2>&1; then
    curl -fsSL "$MAVEN_URL" -o "$ARCHIVE"
  elif command -v wget >/dev/null 2>&1; then
    wget -q "$MAVEN_URL" -O "$ARCHIVE"
  else
    echo "curl or wget is required to bootstrap Maven." >&2
    exit 1
  fi
  tar -xzf "$ARCHIVE" -C "$BASE_DIR/.mvn"
fi

exec "$MAVEN_DIR/bin/mvn" "$@"
