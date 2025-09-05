#!/bin/bash

# Generate icon SVGs using the Python extraction script
# This bypasses the need for network access to external repositories

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
PROJECT_ROOT="$(cd "$SCRIPT_DIR/.." && pwd)"

SOURCE_DIR="$PROJECT_ROOT/miuix/src/commonMain/kotlin/top/yukonga/miuix/kmp/icon/icons"
OUTPUT_DIR="$PROJECT_ROOT/build/icon-svgs"

echo "Generating SVGs from package: top.yukonga.miuix.kmp.icon.icons -> $OUTPUT_DIR"

# Run the Python extraction script
python3 "$SCRIPT_DIR/extract_icons.py" "$SOURCE_DIR" "$OUTPUT_DIR"

echo "SVG generation complete. Output: $OUTPUT_DIR"