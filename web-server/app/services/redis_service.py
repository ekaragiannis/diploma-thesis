import redis
from typing import Optional, Dict


class RedisService:
    """
    A service class for interacting with Redis cache and database.

    This class provides methods to retrieve sensor data from Redis cache,
    test Redis connectivity, and manage Redis operations for the energy
    monitoring system.

    Attributes:
        redis_client (redis.Redis): The Redis client instance for database operations.
    """

    def __init__(self, host: str = "localhost", port: int = 6379, db: int = 0):
        self.redis_client = redis.Redis(
            host=host,
            port=port,
            db=db,
            decode_responses=True
        )

    def get_sensor_data(self, sensor: str) -> Optional[Dict[str, float]]:
        """
        Retrieve cached sensor data from Redis for a specific sensor.

        Args:
            sensor (str): The unique identifier of the sensor (e.g., "sensor_001").

        Returns:
            Optional[Dict[str, float]]: A dictionary containing hourly energy data
                where keys are hours (00-23 as strings) and values are energy
                consumption in kWh as floats. Returns None if no data is found
                or if an error occurs.

        """
        try:
            # Try to get data from Redis using JSON.GET command
            cached_data = self.redis_client.json().get(f"sensor:{sensor}")
            if cached_data and isinstance(cached_data, dict) and 'data' in cached_data:
                return cached_data.get('data')
            return None
        except Exception as e:
            print(f"Error retrieving data from Redis: {e}")
            return None

    def ping(self) -> bool:
        """
        Test the Redis connection by sending a PING command.

        Returns:
            bool: True if Redis is responding, False otherwise.
        """
        try:
            result = self.redis_client.ping()
            return bool(result)
        except Exception as e:
            print(f"Redis connection error: {e}")
            return False


# Create a global Redis service instance
redis_service = RedisService()
