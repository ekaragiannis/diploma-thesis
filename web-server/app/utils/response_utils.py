from typing import Any, Dict, List
from .time_utils import convert_utc_to_athens


def format_data_for_response(raw_data: List[Dict[str, Any]]):
    formatted_data = []

    for row in raw_data:
        hour_bucket = row.get('hour_bucket')

        if hour_bucket:
            athens_time = convert_utc_to_athens(hour_bucket)
            hour = athens_time.strftime('%H')
            date = athens_time.strftime('%Y-%m-%d')
            energy_total = row.get('energy_total', 0)

            formatted_data.append({
                "energy_total": energy_total,
                "hour": hour,
                "date": date
            })

    return formatted_data


def format_rawdata_for_response(raw_data: list[Dict[str, Any]]):
    """
    Format raw data into response format with Athens timezone.

    Args:
        raw_data: List of dictionaries containing raw data from database

    Returns:
        list: List of formatted data points with Athens timestamps
    """
    formatted_data = {}
    for row in raw_data:
        timestamp = row.get('timestamp')
        if timestamp:
            # Convert to Athens timezone
            athens_time = convert_utc_to_athens(timestamp)
            hour = athens_time.strftime('%H')
            energy = row.get('energy', 0)
            formatted_data[hour] = energy
    return formatted_data
