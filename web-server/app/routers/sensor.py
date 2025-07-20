from fastapi import APIRouter
from app.services.db_service import fetch_sensor_names
from app.models import SensorResponse, ErrorResponse

router = APIRouter(prefix="/sensors", tags=["Sensors"])


@router.get(
    "", response_model=SensorResponse, responses={500: {"model": ErrorResponse}}
)
async def get_sensor_names():
    """
    Retrieve list of all available sensors.

    Fetches the complete list of sensor identifiers that are available
    in the system for data retrieval.

    Returns:
        SensorResponse containing list of all sensor identifiers

    Raises:
        HTTPException 500: Database connection or query error
    """
    sensor_names = fetch_sensor_names()
    return SensorResponse(sensors=sensor_names)
