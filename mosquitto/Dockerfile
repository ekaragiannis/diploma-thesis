FROM eclipse-mosquitto:latest

# Declare build arguments
ARG MQTT_USER
ARG MQTT_PASSWORD

# Create the password file dynamically
RUN mosquitto_passwd -b -c /mosquitto/config/passwd "$MQTT_USER" "$MQTT_PASSWORD"

# Copy config file
COPY mosquitto/config/mosquitto.conf /mosquitto/config/mosquitto.conf
