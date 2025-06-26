#!/usr/bin/env python3
"""Simple setup validation for Dear Diary backend.

Checks that OPENROUTER_API_KEY is defined in the .env file and that
`backend/oauth.json` exists and is readable.
"""
from __future__ import annotations

import os
from pathlib import Path
import sys

ROOT = Path(__file__).resolve().parent
ENV_PATH = ROOT / ".env"
OAUTH_PATH = ROOT / "oauth.json"

missing = []

if not ENV_PATH.exists():
    missing.append(f"Missing {ENV_PATH}")
else:
    env_vars = {}
    for line in ENV_PATH.read_text().splitlines():
        if not line.strip() or line.startswith("#"):
            continue
        if "=" in line:
            key, value = line.split("=", 1)
            env_vars[key.strip()] = value.strip()
    if not env_vars.get("OPENROUTER_API_KEY"):
        missing.append("OPENROUTER_API_KEY not set in .env")

if not OAUTH_PATH.is_file():
    missing.append(f"Missing {OAUTH_PATH}")
elif not os.access(OAUTH_PATH, os.R_OK):
    missing.append(f"{OAUTH_PATH} is not readable")

if missing:
    for m in missing:
        print(m, file=sys.stderr)
    sys.exit(1)

print("Environment looks good!")
