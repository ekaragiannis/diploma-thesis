from fastapi import APIRouter, HTTPException
from app.services.db_service import fetch_sensor_names
from app.models import SensorResponse, ErrorResponse

router = APIRouter(prefix="/sensors", tags=["Sensors"])


@router.get("", response_model=SensorResponse, responses={500: {"model": ErrorResponse}})
async def get_sensor_names():
    """
    Retrieve a list of all available sensor identifiers from the system.
    
    This endpoint queries the database to find all unique sensor identifiers
    that have recorded data. Useful for populating dropdown menus, validation,
    and discovery of available sensors in the monitoring system.
    
    Returns:
        SensorResponse: Response containing the list of available sensor names
            - sensors (List[str]): Array of unique sensor identifier strings
            
    Raises:
        HTTPException: 500 if database connection fails or query execution errors
        
    Example:
        GET /sensors
        Response: {
            "sensors": [
                "temperature_sensor_01",
                "humidity_sensor_02", 
                "energy_meter_hall",
                "energy_meter_office"
            ]
        }
        
    Note:
        - Only returns sensors that have at least one data record
        - Sensor names are fetched from the rawdata table
        - Results are sorted alphabetically for consistent ordering
    """
    sensor_names = fetch_sensor_names()
    return SensorResponse(sensors=sensor_names)
