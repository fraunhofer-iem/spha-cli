#!/usr/bin/env bash
#
# Copyright (c) 2025 Fraunhofer IEM. All rights reserved.
#
# Licensed under the MIT license. See LICENSE file in the project root for details.
#
# SPDX-License-Identifier: MIT
# License-Filename: LICENSE
#

set -euo pipefail

REPO_DIR="/workspace/repo"
mkdir -p "$REPO_DIR"

# Copy repo contents into container
cp -r /input/. "$REPO_DIR"

cd "$REPO_DIR"

echo "=== Running OSV scanner ==="
osv-scanner scan - .

echo "=== Running Trufflehog ==="
#trufflehog filesystem --directory .

echo "=== Running Trivy (filesystem scan) ==="
#trivy fs --quiet .

echo "=== Running Custom Tool ==="
#spha-cli .
