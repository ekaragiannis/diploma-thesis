from datetime import timezone
import pytz


def convert_utc_to_athens(utc_datetime):
    """
    Convert UTC datetime to Athens timezone.

    Args:
        utc_datetime: datetime object (naive or UTC timezone-aware)

    Returns:
        datetime: Athens timezone datetime
    """
    athens_tz = pytz.timezone('Europe/Athens')

    # If naive datetime, assume UTC
    if hasattr(utc_datetime, 'tzinfo') and utc_datetime.tzinfo is None:
        utc_datetime = utc_datetime.replace(tzinfo=timezone.utc)

    return utc_datetime.astimezone(athens_tz)
