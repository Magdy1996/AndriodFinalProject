#!/bin/sh
# Creates a tiny placeholder PNG at app/src/main/assets/welcome_meal.png
# Run from the repo root: sh app/src/main/assets/create_welcome_png.sh

OUT_DIR="$(dirname "$0")"
OUT_FILE="$OUT_DIR/welcome_meal.png"

cat > "$OUT_FILE" <<'PNGBASE64'
iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAQAAAC1HAwCAAAAC0lEQVR4nGMAAQAABQABDQottAAAAABJRU5ErkJggg==
PNGBASE64

# The heredoc above writes the base64 text; decode it to binary
base64 -d "$OUT_FILE" > "$OUT_FILE.tmp" && mv "$OUT_FILE.tmp" "$OUT_FILE"

if [ -f "$OUT_FILE" ]; then
  echo "Created $OUT_FILE"
else
  echo "Failed to create $OUT_FILE"
  exit 1
fi

