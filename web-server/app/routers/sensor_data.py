from fastapi import APIRouter, HTTPException, Request
from pydantic import BaseModel
from typing import Dict, Any, Optional, List
from app.services.redis_service import redis_service
from app.services.db_service import fetch_hourlydata_last_24h, fetch_rawdata_last_24h
from app.utils.response_utils import format_data_for_response, format_rawdata_for_response
from app.middleware import get_execution_time

router = APIRouter(prefix="/sensor-data", tags=["Sensor Data"])


class EnergyConsumption(BaseModel):
    date: str
    hour: str
    energy_total: float


class SensorDataResponse(BaseModel):
    """Response model for sensor data endpoints."""
    sensor: str
    data: List[EnergyConsumption]
    execution_time: Optional[float] = None


class ErrorResponse(BaseModel):
    """Error response model."""
    detail: str
    execution_time: Optional[float] = None


@router.get("/{sensor}/raw", response_model=SensorDataResponse, responses={404: {"model": ErrorResponse}})
async def get_raw_sensor_data(sensor: str, request: Request):
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
    try:
        # Get cached data from Redis
        cached_data = redis_service.get_sensor_data(sensor)
        if cached_data is None:
            raise HTTPException(
                status_code=404, detail=f"No cached data found for sensor: {sensor}")

        data = format_data_for_response(cached_data)
        return {
            "sensor": sensor,
            "data": data,
            "execution_time": get_execution_time(request)
        }
    except HTTPException:
        # Re-raise HTTP exceptions (like 404) as they are
        raise
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
