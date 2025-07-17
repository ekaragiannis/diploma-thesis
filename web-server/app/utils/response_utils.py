from typing import List
from .time_utils import convert_utc_to_athens
from app.models import SensorDataRecord, EnergyConsumption


def format_data_for_response(data: List[SensorDataRecord]) -> List[EnergyConsumption]:
    """
    Format raw sensor data records for API response.
    
    Converts SensorDataRecord objects to EnergyConsumption format suitable for API responses.
    Handles timezone conversion from UTC to Athens time and formats time periods as hour ranges.
    
    Args:
        data: List of SensorDataRecord objects from database or cache
        
    Returns:
        List of EnergyConsumption objects formatted for API response
    """
    formatted_data = []

    for row in data:
        if row.hour_bucket:
            athens_time = convert_utc_to_athens(row.hour_bucket)
            hour = athens_time.strftime("%H")
            period = f"{hour}:00-{hour}:59"
            date = athens_time.strftime("%Y-%m-%d")

            formatted_data.append(
                EnergyConsumption(
                    energy_total=row.energy_total, period=period, date=date
                )
            )

    return formatted_data
