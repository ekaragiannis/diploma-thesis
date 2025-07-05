from typing import Any, Dict
from .time_utils import convert_utc_to_athens


def format_data_for_response(raw_data: list[Dict[str, Any]]):
    """
    Format raw hourlydata into response format with Athens timezone.

    Args:
        raw_data: List of dictionaries containing hourly data from database

    Returns:
        dict: Formatted data with hour keys (00-23) and energy values
    """
    # Sort raw_data by hour_bucket before processing, so that the hours are in order
    sorted_data = sorted(raw_data, key=lambda x: x.get('hour_bucket', ''))
    formatted_data = {}
    for row in sorted_data:
        hour_bucket = row.get('hour_bucket')
        if hour_bucket:
            # Convert to Athens timezone
            athens_time = convert_utc_to_athens(hour_bucket)
            hour = athens_time.strftime('%H')
            value = row.get('energy_total', 0)
            formatted_data[hour] = value
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
