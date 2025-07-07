from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
from typing import Dict, Any, Optional
from app.services.redis_service import redis_service
from app.services.db_service import fetch_hourlydata_last_24h, fetch_rawdata_last_24h
from app.utils.response_utils import format_data_for_response, format_rawdata_for_response
from app.middleware import get_execution_time

router = APIRouter(prefix="/sensor-data", tags=["Sensor Data"])


class SensorDataResponse(BaseModel):
    """Response model for sensor data endpoints."""
    sensor: str
    data: Dict[str, Any]
    execution_time: Optional[float] = None


class ErrorResponse(BaseModel):
    """Error response model."""
    detail: str
    execution_time: Optional[float] = None


@router.get("/{sensor}/raw", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}})
async def get_raw_sensor_data(sensor: str, request: Request):
    """
    Get raw sensor data for a specific sensor from the PostgreSQL database.

    This endpoint retrieves raw sensor data from the PostgreSQL database for the
    specified sensor for the last 24 hours. The data contains individual sensor readings
    with timestamps, energy consumption, power, voltage, and current values.

    Args:
        sensor (str): The unique identifier of the sensor (e.g., "sensor_001").

    Returns:
        SensorDataResponse: A response containing the sensor identifier and raw data.
            The data field contains a list of data points with Athens timezone timestamps.

    Raises:
        HTTPException: 404 if no data is found for the sensor in the last 24 hours.
        HTTPException: 500 if database connection fails.

    Example:
        GET /sensor-data/sensor_001/raw

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
    try:
        raw_data = fetch_rawdata_last_24h(sensor)
        if not raw_data:
            raise HTTPException(
                status_code=404, detail=f"No raw data found for sensor: {sensor} in the last 24 hours")

        formatted_data = format_data_for_response(raw_data)
        return {"sensor": sensor, "data": formatted_data, "execution_time": get_execution_time(request)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{sensor}/hourly", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}})
async def get_hourly_sensor_data_db(sensor: str, request: Request):
    """
    Get hourly sensor data for a specific sensor from the PostgreSQL database.

    This endpoint retrieves hourly energy consumption data from the PostgreSQL database for the
    specified sensor for the last 24 hours. The data contains energy consumption values for each hour
    of the day (00-23) in kilowatt-hours (kWh).

    Args:
        sensor (str): The unique identifier of the sensor (e.g., "sensor_001").

    Returns:
        SensorDataResponse: A response containing the sensor identifier and database data.
            The data field contains a dictionary where keys are hours (00-23) and
            values are energy consumption in kWh.

    Raises:
        HTTPException: 404 if no data is found for the sensor in the last 24 hours.
        HTTPException: 500 if database connection fails.

    Example:
        GET /sensor-data/sensor_001/hourly/db

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
    try:
        raw_data = fetch_hourlydata_last_24h(sensor)
        if not raw_data:
            raise HTTPException(
                status_code=404, detail=f"No data found for sensor: {sensor} in the last 24 hours")

        formatted_data = format_data_for_response(raw_data)
        return {"sensor": sensor, "data": formatted_data, "execution_time": get_execution_time(request)}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))


@router.get("/{sensor}/cached", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}, 500: {"model": ErrorResponse}})
async def get_cached_sensor_data(sensor: str, request: Request):
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
        HTTPException: 500 if Redis service is unavailable or other errors occur.

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
    try:
        # Get cached data from Redis
        cached_data = redis_service.get_sensor_data(sensor)

        if cached_data is None:
            raise HTTPException(
                status_code=404, detail=f"No cached data found for sensor: {sensor}")

        return {
            "sensor": sensor,
            "data": cached_data,
            "execution_time": get_execution_time(request)
        }
    except HTTPException:
        # Re-raise HTTP exceptions (like 404) as they are
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
