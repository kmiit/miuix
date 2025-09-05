# Icon SVG Generation

This directory contains tools for generating SVG files from the Miuix icon definitions.

## Quick Start

To generate SVG icons from the Kotlin ImageVector definitions:

```bash
# From the project root directory:
./docs/generateIconSvgs.sh
```

This will scan all icon definitions in `miuix/src/commonMain/kotlin/top/yukonga/miuix/kmp/icon/icons/` and generate SVG files in `build/icon-svgs/`.

## Files

- `generateIconSvgs.sh` - Main script to generate SVG icons
- `extract_icons.py` - Python script that parses Kotlin files and extracts ImageVector data
- `icon-gen/` - Original Gradle-based icon generation (requires network access)

## How it works

The Python script parses Kotlin source files and extracts:
- ImageVector definitions with their names
- Path data from the vector graphics
- Fill colors and other properties

It then generates clean SVG files with proper formatting.

## Network-Free Operation

Unlike the original Gradle-based approach, this method works without network access to external repositories. It directly parses the Kotlin source files to extract icon data.

## Generated Files

SVG files are generated in `build/icon-svgs/` with sanitized filenames based on the icon names.

Example output:
- `Play.svg`
- `Settings.svg` 
- `ArrowRight.svg`
- etc.

The SVG files are compatible with web browsers, design tools, and other applications that support SVG format.