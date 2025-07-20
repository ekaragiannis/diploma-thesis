import redis
import os
from typing import List, Optional, Any
from app.models import SensorDataRecord


class RedisService:
    def __init__(self, host: str = "localhost", port: int = 6379, db: int = 0):
        self.redis_client = redis.Redis(
            host=host, port=port, db=db, decode_responses=True
        )

    def get_sensor_data(self, sensor: str) -> Optional[List[SensorDataRecord]]:
        try:
            # Try to get data from Redis using JSON.GET command
            cached_data = self.redis_client.json().get(sensor)
            if cached_data and isinstance(cached_data, dict) and "data" in cached_data:
                data: list[dict[str, Any]] = cached_data.get("data")
                result: List[SensorDataRecord] = []
                for obj in data:
                    hour_bucket = obj.get("hour_bucket")
                    energy_total = obj.get("energy_total")
                    result.append(
                        SensorDataRecord(
                            hour_bucket=hour_bucket, energy_total=energy_total
                        )
                    )
                return result
            return None
        except Exception as e:
            print(f"Error retrieving data from Redis: {e}")
            return None

    def ping(self) -> bool:
        try:
            result = self.redis_client.ping()
            return bool(result)
        except Exception as e:
            print(f"Redis connection error: {e}")
            return False


# Global Redis service instance configured from environment variables
# This singleton pattern ensures consistent Redis configuration across the application
redis_service = RedisService(
    host=os.getenv("REDIS_HOST", "localhost"),  # Redis server hostname
    port=int(os.getenv("REDIS_PORT", 6379)),  # Redis server port
    db=int(os.getenv("REDIS_DB", 0)),  # Redis database number
)
