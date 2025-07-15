from fastapi import APIRouter, HTTPException, Request
from app.services.redis_service import redis_service
from app.services.db_service import fetch_hourlydata_last_24h, fetch_rawdata_last_24h
from app.utils.response_utils import format_data_for_response
from app.middlewares import get_execution_time
from app.models import SensorDataResponse, ErrorResponse

router = APIRouter(prefix="/sensor-data", tags=["Sensor Data"])


@router.get(
    "/{sensor}/raw",
    response_model=SensorDataResponse,
    responses={404: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
)
async def get_raw_sensor_data(sensor: str, request: Request):
    data = fetch_rawdata_last_24h(sensor)
    if not data:
        raise HTTPException(
            status_code=404,
            detail=f"No raw data found for sensor: {sensor} in the last 24 hours",
        )

    response_data = format_data_for_response(data)
    return SensorDataResponse(
        sensor=sensor,
        data=response_data,
        execution_time=get_execution_time(request),
    )


@router.get(
    "/{sensor}/hourly",
    response_model=SensorDataResponse,
    responses={404: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
)
async def get_hourly_sensor_data_db(sensor: str, request: Request):
    data = fetch_hourlydata_last_24h(sensor)
    if not data:
        raise HTTPException(
            status_code=404,
            detail=f"No data found for sensor: {sensor} in the last 24 hours",
        )

    response_data = format_data_for_response(data)
    return SensorDataResponse(
        sensor=sensor,
        data=response_data,
        execution_time=get_execution_time(request),
    )


@router.get(
    "/{sensor}/cached",
    response_model=SensorDataResponse,
    responses={404: {"model": ErrorResponse}, 500: {"model": ErrorResponse}},
)
async def get_cached_sensor_data(sensor: str, request: Request):
    # Get cached data from Redis
    data = redis_service.get_sensor_data(sensor)
    if data is None:
        raise HTTPException(
            status_code=404, detail=f"No cached data found for sensor: {sensor}"
        )

    response_data = format_data_for_response(data)
    return SensorDataResponse(
        sensor=sensor,
        data=response_data,
        execution_time=get_execution_time(request),
    )
