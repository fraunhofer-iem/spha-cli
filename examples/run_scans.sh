#!/usr/bin/env bash
#
# Copyright (c) 2025 Fraunhofer IEM. All rights reserved.
#
# Licensed under the MIT license. See LICENSE file in the project root for details.
#
# SPDX-License-Identifier: MIT
# License-Filename: LICENSE
#

set -uo pipefail  # Removed -e to allow continuing after non-zero exits

REPO_DIR="/input"
RESULT_DIR="/result"
mkdir -p "$REPO_DIR"
mkdir -p "$RESULT_DIR"

# Copy repo contents into container

cd "$REPO_DIR" || exit

# Track overall success/failure
OVERALL_SUCCESS=0

echo "=== Running OSV scanner ==="
if ! osv-scanner scan --format json -r . > "$RESULT_DIR"/osv.json; then
    echo "OSV scanner completed with findings or errors (exit code: $?)"
    OVERALL_SUCCESS=1
fi

if ! osv-scanner scan --format cyclonedx-1-5 --all-packages . > "$RESULT_DIR"/sbom.json; then
    echo "OSV scanner completed with findings or errors (exit code: $?)"
    OVERALL_SUCCESS=1
fi

echo "=== Running Trufflehog ==="
if ! trufflehog filesystem --json --directory . > "$RESULT_DIR"/trufflehog.json; then
    echo "Trufflehog completed with findings or errors (exit code: $?)"
    OVERALL_SUCCESS=1
fi

echo "=== Running Trivy (filesystem scan) ==="
if ! trivy fs --output "$RESULT_DIR"/trivy.json .; then
    echo "Trivy completed with findings or errors (exit code: $?)"
    OVERALL_SUCCESS=1
fi

echo "=== Running Custom Tool ==="
ls "$RESULT_DIR"

# analyze --output test.json --toolResultDir /Users/struewer/git/spha/spha-cli/examples/toolResults --repoUrl https://github.com/fraunhofer-iem/spha
spha-cli analyze --output "$REPO_DIR"/kpis.json --toolResultDir "$RESULT_DIR" --repoUrl "$REPO_URL"

# Exit with error if any tool had issues, but only after all tools ran
if [ $OVERALL_SUCCESS -ne 0 ]; then
    echo "One or more tools completed with findings or errors"
    exit 1
fi

echo "All tools completed successfully"