import random
import time
import json
import os
import argparse
from paho.mqtt import client as mqtt_client


BROKER = "mqtt"
PORT = 1883  # Change to your MQTT broker port if needed
CLIENT_ID = f"producer-{random.randint(0, 1000)}"

# Get credentials from environment variables
MQTT_USER = os.getenv("MQTT_USER")
MQTT_PASSWORD = os.getenv("MQTT_PASSWORD")


def connect_mqtt():
    client = mqtt_client.Client(client_id=CLIENT_ID)

    # Set username and password for authentication
    if MQTT_USER and MQTT_PASSWORD:
        client.username_pw_set(MQTT_USER, MQTT_PASSWORD)

    client.connect(BROKER, PORT)
    return client


def publish_sensor_data(sensor_name):
    """Publish data for a specific sensor continuously"""
    client = connect_mqtt()
    topic = f"/sensors/{sensor_name}"

    print(f"Starting sensor '{sensor_name}' on topic {topic}")
    print("Press Ctrl+C to stop the sensor")

    try:
        while True:
            energy = round(random.uniform(0.0, 50.0), 2)
            timestamp = int(time.time() * 1000)
            message = {
                "energy": energy,
                "timestamp": timestamp,
            }

            result = client.publish(topic, json.dumps(message))
            status = result[0]

            if status == 0:
                print(f"Sensor '{sensor_name}': Sent `{message}` to topic `{topic}`")
            else:
                print(
                    f"Sensor '{sensor_name}': Failed to send message to topic {topic}"
                )

            time.sleep(1)  # Wait 1 second before next message

    except KeyboardInterrupt:
        print(f"\nStopping sensor '{sensor_name}'...")
    except Exception as e:
        print(f"Sensor '{sensor_name}': Error publishing message: {e}")
    finally:
        client.disconnect()
        print(f"Sensor '{sensor_name}': Stopped")


def main():
    parser = argparse.ArgumentParser(description="MQTT Sensor Message Producer")
    parser.add_argument(
        "sensor_name", help="Name of the sensor (e.g., s1, s2, temperature, humidity)"
    )

    args = parser.parse_args()
    sensor_name = args.sensor_name

    publish_sensor_data(sensor_name)


if __name__ == "__main__":
    main()
