FROM alpine:latest

# Install dependencies
RUN apk add --no-cache \
  curl \
  jq

# Copy the connectors setup script and configuration files with executable permissions
COPY --chmod=755 setup-connectors/init-connectors.sh /usr/local/bin/
COPY setup-connectors/configs/ /opt/kafka/configs/

# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/init-connectors.sh"] 