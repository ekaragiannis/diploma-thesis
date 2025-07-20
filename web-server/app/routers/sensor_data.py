from fastapi import APIRouter, HTTPException, Request, Query, Path
from typing import Literal
from app.services.redis_service import redis_service
from app.services.db_service import fetch_hourlydata_last_24h, fetch_rawdata_last_24h
from app.utils.response_utils import format_data_for_response
from app.middlewares import get_execution_time
from app.models import SensorDataResponse, ErrorResponse

router = APIRouter(prefix="/sensor-data", tags=["Sensor Data"])


@router.get(
    "/{sensor}",
    response_model=SensorDataResponse,
    responses={404: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
)
async def get_sensor_data(
    request: Request,
    sensor: str = Path(..., description="Unique identifier of the sensor to retrieve data for"),
    source: Literal["db_raw", "db_hourly", "redis"] = Query(
        ..., description="Data source to retrieve from"
    ),
):
    """
    Retrieve sensor data from specified data source.
    
    Fetches sensor data from one of three available sources:
    - db_raw: Raw sensor data from database (last 24 hours)
    - db_hourly: Hourly aggregated data from database (last 24 hours) 
    - redis: Cached real-time data from Redis
    
    Args:
        sensor: Sensor identifier/name
        source: Data source ("db_raw", "db_hourly", or "redis")
        
    Returns:
        SensorDataResponse containing sensor data, source info, and execution time
        
    Raises:
        HTTPException 404: No data found for the specified sensor
    """
    if source == "db_raw":
        data = fetch_rawdata_last_24h(sensor)
        error_msg = f"No raw data found for sensor: {sensor} in the last 24 hours"
    elif source == "db_hourly":
        data = fetch_hourlydata_last_24h(sensor)
        error_msg = f"No data found for sensor: {sensor} in the last 24 hours"
    elif source == "redis":
        data = redis_service.get_sensor_data(sensor)
        error_msg = f"No cached data found for sensor: {sensor}"

    if not data:
        raise HTTPException(status_code=404, detail=error_msg)

    response_data = format_data_for_response(data)
    return SensorDataResponse(
        sensor=sensor,
        source=source,
        data=response_data,
        execution_time=get_execution_time(request),
    )
