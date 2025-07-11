FROM alpine:latest as downloader

# Install wget and unzip
RUN apk add --no-cache wget unzip

# Download and extract Lenses MQTT Source connector from GitHub
WORKDIR /tmp
RUN wget -q https://github.com/lensesio/stream-reactor/releases/download/9.0.0/kafka-connect-mqtt-9.0.0.zip && \
  mkdir -p /tmp/connectors/lenses-mqtt-source && \
  unzip kafka-connect-mqtt-9.0.0.zip -d /tmp/connectors/lenses-mqtt-source && \
  rm kafka-connect-mqtt-9.0.0.zip

# Final image
FROM confluentinc/cp-kafka-connect:8.0.0

# Install connectors from Confluent Hub
RUN confluent-hub install --no-prompt confluentinc/kafka-connect-jdbc:latest && \
  confluent-hub install --no-prompt redis/redis-kafka-connect:0.9.1 && \
  confluent-hub install --no-prompt debezium/debezium-connector-postgresql:latest

# Copy the Lenses MQTT connector from the downloader stage
COPY --from=downloader /tmp/connectors/ /kafka-connect-plugins/

# Copy the environment variables
COPY .env /secrets/properties

# Set the plugin path to include both Confluent Hub components and our custom connectors
ENV CONNECT_PLUGIN_PATH="/usr/share/java,/usr/share/confluent-hub-components,/kafka-connect-plugins"