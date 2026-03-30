#!/usr/bin/env python3
"""
Resize bundled ARASAAC PNG symbols from 300x300 to 150x150.
This permanently reduces bitmap memory usage by ~75%.

Usage:
    python resize_symbols.py

Requires Pillow: pip install Pillow

Original 300x300 files are backed up to _300x300_backup/
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
BACKUP_DIR = SYMBOLS_DIR.parent / "_300x300_backup"
TARGET_SIZE = (150, 150)


def resize_symbols():
    if not SYMBOLS_DIR.exists():
        print(f"ERROR: Directory not found: {SYMBOLS_DIR}")
        print("Run this script from the project root directory.")
        return

    png_files = list(SYMBOLS_DIR.glob("*.png"))
    if not png_files:
        print(f"No PNG files found in {SYMBOLS_DIR}")
        return

    print(f"Found {len(png_files)} PNG files to resize")
    print(f"Target size: {TARGET_SIZE[0]}x{TARGET_SIZE[1]}")

    # Create backup directory
    BACKUP_DIR.mkdir(parents=True, exist_ok=True)
    print(f"Backing up originals to {BACKUP_DIR}\n")

    total_before = 0
    total_after = 0
    resized = 0
    skipped = 0
    failed = 0

    for png_file in sorted(png_files):
        try:
            before_size = png_file.stat().st_size
            total_before += before_size

            img = Image.open(png_file)
            original_size = img.size

            # Skip if already 150x150 or smaller
            if original_size[0] <= TARGET_SIZE[0] and original_size[1] <= TARGET_SIZE[1]:
                total_after += before_size
                skipped += 1
                continue

            # Backup original
            backup_path = BACKUP_DIR / png_file.name
            if not backup_path.exists():
                shutil.copy2(str(png_file), str(backup_path))

            # Resize with high-quality LANCZOS resampling
            img_resized = img.resize(TARGET_SIZE, Image.Resampling.LANCZOS)

            # Save back to the same file, optimised
            img_resized.save(png_file, "PNG", optimize=True)

            after_size = png_file.stat().st_size
            total_after += after_size

            savings = (1 - after_size / before_size) * 100
            resized += 1

        except Exception as e:
            print(f"  FAILED: {png_file.name} — {e}")
            total_after += before_size
            failed += 1

    print(f"Results:")
    print(f"  Resized:  {resized} files")
    print(f"  Skipped:  {skipped} files (already ≤150x150)")
    print(f"  Failed:   {failed} files")
    print(f"  Before:   {total_before / 1024:.0f} KB ({total_before / 1024 / 1024:.1f} MB)")
    print(f"  After:    {total_after / 1024:.0f} KB ({total_after / 1024 / 1024:.1f} MB)")

    if total_before > 0:
        savings = (1 - total_after / total_before) * 100
        saved_kb = (total_before - total_after) / 1024
        print(f"  Savings:  {savings:.1f}% ({saved_kb:.0f} KB)")

    print(f"\nOriginals backed up to: {BACKUP_DIR}")
    print("You can delete the backup folder after verifying the app works correctly.")


if __name__ == "__main__":
    print("=== ARASAAC Symbol Resizer (300x300 → 150x150) ===\n")
    resize_symbols()
    print("\nDone!")
