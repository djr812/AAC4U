#!/usr/bin/env python3
"""
Convert ARASAAC PNG symbols to WebP format for reduced file size.
WebP typically achieves 50-70% smaller file sizes than PNG.

Usage:
    python convert_to_webp.py

Run from the project root. Converts all PNGs in
app/src/main/assets/symbols/arasaac_core/ to WebP format.
Original PNGs are kept as backups in a _png_backup folder.
"""

import os
import shutil
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("ERROR: Pillow is required. Install with: pip install Pillow")
    exit(1)

SYMBOLS_DIR = Path("app/src/main/assets/symbols/arasaac_core")
BACKUP_DIR = SYMBOLS_DIR.parent / "_png_backup"
WEBP_QUALITY = 85  # Good balance of quality and size for symbols


def convert_png_to_webp():
    if not SYMBOLS_DIR.exists():
        print(f"ERROR: Directory not found: {SYMBOLS_DIR}")
        print("Run this script from the project root directory.")
        return

    png_files = list(SYMBOLS_DIR.glob("*.png"))
    if not png_files:
        print(f"No PNG files found in {SYMBOLS_DIR}")
        return

    print(f"Found {len(png_files)} PNG files to convert")

    # Create backup directory
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    print(f"Backing up originals to {BACKUP_DIR}")

    total_png_size = 0
    total_webp_size = 0
    converted = 0
    failed = 0

    for png_file in sorted(png_files):
        try:
            # Get original size
            png_size = png_file.stat().st_size
            total_png_size += png_size

            # Convert to WebP
            webp_file = png_file.with_suffix(".webp")
            img = Image.open(png_file)

            # Convert RGBA to RGB if no transparency needed
            # Keep RGBA for symbols that use transparency
            img.save(webp_file, "WEBP", quality=WEBP_QUALITY, method=6)

            webp_size = webp_file.stat().st_size
            total_webp_size += webp_size

            # Backup original
            shutil.move(str(png_file), str(BACKUP_DIR / png_file.name))

            savings = (1 - webp_size / png_size) * 100
            converted += 1

        except Exception as e:
            print(f"  FAILED: {png_file.name} — {e}")
            failed += 1

    print(f"\nConversion complete:")
    print(f"  Converted: {converted} files")
    print(f"  Failed: {failed} files")
    print(f"  PNG total: {total_png_size / 1024:.0f} KB")
    print(f"  WebP total: {total_webp_size / 1024:.0f} KB")
    if total_png_size > 0:
        savings = (1 - total_webp_size / total_png_size) * 100
        print(f"  Savings: {savings:.1f}%")

    print(f"\nOriginal PNGs backed up to: {BACKUP_DIR}")
    print("\nIMPORTANT: After converting, you also need to update:")
    print("  1. symbol_mapping.json — change .png extensions to .webp")
    print("  2. SymbolManager.kt — update file extension references")
    print("  3. download scripts — save as .webp instead of .png")


def update_symbol_mapping():
    """Update symbol_mapping.json to reference .webp instead of .png"""
    mapping_file = Path("app/src/main/assets/symbols/symbol_mapping.json")
    if not mapping_file.exists():
        print(f"WARNING: {mapping_file} not found, skipping mapping update")
        return

    content = mapping_file.read_text()
    updated = content.replace(".png", ".webp")

    if content != updated:
        # Backup original
        shutil.copy(str(mapping_file), str(mapping_file.with_suffix(".json.bak")))
        mapping_file.write_text(updated)
        print(f"Updated {mapping_file} — .png → .webp")
        count = content.count(".png")
        print(f"  {count} references updated")
    else:
        print(f"No .png references found in {mapping_file}")


if __name__ == "__main__":
    print("=== ARASAAC PNG → WebP Converter ===\n")
    convert_png_to_webp()
    print()
    update_symbol_mapping()
    print("\nDone!")
