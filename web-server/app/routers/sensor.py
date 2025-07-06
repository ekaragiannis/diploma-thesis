from fastapi import APIRouter, HTTPException
from pydantic import BaseModel
from app.services.db_service import fetch_sensor_names

router = APIRouter(prefix="/sensors", tags=["Sensors"])


class SensorResponse(BaseModel):
    sensors: list[str]


class ErrorResponse(BaseModel):
    detail: str


@router.get("", response_model=SensorResponse, responses={500: {"model": ErrorResponse}})
async def get_sensor_names():
    """
    Get all available sensor names from the database.

    Returns:
        SensorResponse: A response containing a list of sensor names.
            {
                "sensors": ["sensor1", "sensor2", "sensor3", ...]
            }

    Raises:
        HTTPException: If there's an error fetching sensor names from the database.
    """
    try:
        sensor_names = fetch_sensor_names()
        return {"sensors": sensor_names}
    except Exception as e:
        raise HTTPException(status_code=500, detail=str(e))
