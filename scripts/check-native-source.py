#!/usr/bin/env python3
"""Validate GLACIA native-source independence from upstream private modules.

This is a source-level check only; it does not compile an APK.
"""
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent.parent
NATIVE = ROOT / "TMessagesProj" / "jni"
FILES = {
    "CMakeLists.txt": ["colorado/CMakeLists.txt", "security/secure_validator.cpp", "\n        colorado\n"],
    "jni.c": ['#include "colorado/colorado.h"'],
    "TgNetWrapper.cpp": [
        '#include "security/secure_validator.hpp"',
        '#include "security/skCrypter.hpp"',
        "secure_validator::",
        "skCrypt(",
    ],
}

failed = []
for path, forbidden in FILES.items():
    filename = NATIVE / path
    if not filename.is_file():
        failed.append(f"Missing native source: {filename}")
        continue
    content = filename.read_text(encoding="utf-8")
    for match in forbidden:
        if match in content:
            failed.append(f"{path} still requires removed vendor-only component: {match.strip()}")

for essential in ["voip/CMakeLists.txt", "tgnet/ConnectionsManager.cpp",
                  "prebuild/lib/arm64-v8a/libcrypto.a"]:
    if not (NATIVE / essential).is_file():
        failed.append(f"Expected native dependency missing: {essential}")

if failed:
    print("\n".join(f"ERROR: {error}" for error in failed), file=sys.stderr)
    sys.exit(1)

print("GLACIA native source dependency check passed.")
print("NOTE: This does NOT validate Android compilation or all app features.")
