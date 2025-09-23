FROM ubuntu:latest AS base
ENV SPHA_CLI_VERSION=0.3.1
# Install dependencies
RUN apt-get update && apt-get install -y \
    curl git unzip wget apt-transport-https gpg \
    && wget -qO - https://packages.adoptium.net/artifactory/api/gpg/key/public | gpg --dearmor | tee /etc/apt/trusted.gpg.d/adoptium.gpg > /dev/null \
    && echo "deb https://packages.adoptium.net/artifactory/deb $(awk -F= '/^VERSION_CODENAME/{print$2}' /etc/os-release) main" | tee /etc/apt/sources.list.d/adoptium.list \
    && apt update \
    && apt install -y temurin-24-jre \
    && rm -rf /var/lib/apt/lists/*

WORKDIR /workspace

# ----------------------------
# Install OSV scanner
# ----------------------------
RUN curl -sSL https://github.com/google/osv-scanner/releases/download/v2.2.2/osv-scanner_linux_amd64 \
    -o /usr/local/bin/osv-scanner \
    && chmod +x /usr/local/bin/osv-scanner

# ----------------------------
# Install Trufflehog
# ----------------------------
RUN curl -sSfL https://raw.githubusercontent.com/trufflesecurity/trufflehog/main/scripts/install.sh | sh -s -- -b /usr/local/bin

# ----------------------------
# Install Trivy
# ----------------------------
RUN curl -sfL https://raw.githubusercontent.com/aquasecurity/trivy/main/contrib/install.sh | sh -s -- -b /usr/local/bin v0.66.0

# ----------------------------
# Install SPHA-CLI
# ----------------------------
RUN curl -sSL https://github.com/fraunhofer-iem/spha-cli/releases/download/${SPHA_CLI_VERSION}/spha-cli-${SPHA_CLI_VERSION}.zip \
    -o /tmp/spha-cli.zip \
    && unzip /tmp/spha-cli.zip -d /opt \
    && ln -s "/opt/spha-cli-${SPHA_CLI_VERSION}/bin/spha-cli" /usr/local/bin/spha-cli \
    && rm /tmp/spha-cli.zip # Start from a lightweight base

# ----------------------------
# Script to run all scanners
# ----------------------------
COPY run_scans.sh /usr/local/bin/run_scans.sh
RUN chmod +x /usr/local/bin/run_scans.sh

ENTRYPOINT ["/usr/local/bin/run_scans.sh"]
