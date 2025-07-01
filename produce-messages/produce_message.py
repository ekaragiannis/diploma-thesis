import random
import time
import json
import os
from dotenv import load_dotenv
from paho.mqtt import client as mqtt_client

# Load environment variables from root .env file
load_dotenv('../.env')

BROKER = 'localhost'  # Change to your MQTT broker address
PORT = 1883           # Change to your MQTT broker port if needed
CLIENT_ID = f'producer-{random.randint(0, 1000)}'

# Get credentials from environment variables
MQTT_USER = os.getenv('MQTT_USER')
MQTT_PASSWORD = os.getenv('MQTT_PASSWORD')


def connect_mqtt():
    client = mqtt_client.Client(client_id=CLIENT_ID)

    # Set username and password for authentication
    if MQTT_USER and MQTT_PASSWORD:
        client.username_pw_set(MQTT_USER, MQTT_PASSWORD)

    client.connect(BROKER, PORT)
    return client


def publish(client):
    i = random.randint(1, 10)
    topic = f"/sensors/s{i}"
    energy = round(random.uniform(0.0, 50.0), 2)
    timestamp = int(time.time() * 1000)
    message = {
        'energy': energy,
        'timestamp': timestamp
    }
    result = client.publish(topic, json.dumps(message))
    status = result[0]
    if status == 0:
        print(f"Sent `{message}` to topic `{topic}`")
    else:
        print(f"Failed to send message to topic {topic}")


if __name__ == '__main__':
    client = connect_mqtt()
    publish(client)
