FROM alpine:latest

# Install dependencies
RUN apk add --no-cache \
  curl \
  jq

# Copy the schemas setup script and schema files
COPY --chmod=755 setup-schemas/init-schemas.sh /usr/local/bin/
COPY setup-schemas/schemas/ /opt/kafka/schemas/


# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/init-schemas.sh"] 