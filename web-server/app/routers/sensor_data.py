from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from typing import Dict, Any, Optional
from app.services.redis_service import redis_service

router = APIRouter(prefix="/sensor-data", tags=["Sensor Data"])


class SensorDataResponse(BaseModel):
    """Response model for sensor data endpoints."""
    sensor: str
    data: Dict[str, Any]


class ErrorResponse(BaseModel):
    """Error response model."""
    detail: str


@router.get("/{sensor}/raw", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}})
async def get_raw_sensor_data(sensor: str):
    """Get raw sensor data for a specific sensor"""
    return {"sensor": sensor, "data": "raw sensor data"}


@router.get("/{sensor}/hourly", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}})
async def get_hourly_sensor_data(sensor: str):
    """Get hourly sensor data for a specific sensor"""
    return {"sensor": sensor, "data": "hourly sensor data"}


@router.get("/{sensor}/cached", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}, 503: {"model": ErrorResponse}})
async def get_cached_sensor_data(sensor: str):
    """
    Get cached sensor data for a specific sensor from Redis.

    This endpoint retrieves cached hourly energy consumption data from Redis for the
    specified sensor. The data contains energy consumption values for each hour of
    the day (00-23) in kilowatt-hours (kWh).

    Args:
        sensor (str): The unique identifier of the sensor (e.g., "sensor_001").

    Returns:
        SensorDataResponse: A response containing the sensor identifier and cached data.
            The data field contains a dictionary where keys are hours (00-23) and
            values are energy consumption in kWh.

    Raises:
        HTTPException: 404 if no cached data is found for the sensor.
        HTTPException: 503 if Redis service is unavailable.

    Example:
        GET /sensor-data/sensor_001/cached

        Response:
        {
            "sensor": "sensor_001",
            "data": {
                "00": 1.5,
                "01": 2.1,
                "14": 3.2,
                "23": 1.8
            }
        }

    Note:
        The data structure represents hourly energy consumption where:
        - Keys are hours of the day as strings (00-23)
        - Values are energy consumption in kilowatt-hours (kWh)
        - Hours with no data will not appear in the response
    """
    # Check Redis connection
    if not redis_service.ping():
        raise HTTPException(
            status_code=503, detail="Redis service unavailable")

    # Get cached data from Redis
    cached_data = redis_service.get_sensor_data(sensor)

    if cached_data is None:
        raise HTTPException(
            status_code=404, detail=f"No cached data found for sensor: {sensor}")

    return {
        "sensor": sensor,
        "data": cached_data,
    }
