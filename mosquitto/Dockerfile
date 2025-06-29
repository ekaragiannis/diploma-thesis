FROM eclipse-mosquitto:2.0.21

# Declare build arguments
ARG MQTT_USER
ARG MQTT_PASSWORD

# Create the password file dynamically
RUN mosquitto_passwd -b -c /mosquitto/config/passwd "$MQTT_USER" "$MQTT_PASSWORD"

# Copy config file
COPY mosquitto/config/mosquitto.conf /mosquitto/config/mosquitto.conf
