#!/usr/bin/env bash
# Install packages required for running the test suite.
set -e
pip install -r backend/requirements.txt
pip install pytest pytest-asyncio
