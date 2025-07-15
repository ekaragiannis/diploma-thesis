from datetime import datetime, timezone
import pytz


def convert_utc_to_athens(utc_datetime: datetime) -> datetime:
    athens_tz = pytz.timezone("Europe/Athens")

    # If naive datetime, assume UTC
    if (
        utc_datetime.tzinfo is None
        or utc_datetime.tzinfo.utcoffset(utc_datetime) is None
    ):
        utc_datetime = utc_datetime.replace(tzinfo=timezone.utc)

    # Convert to Athens timezone
    return utc_datetime.astimezone(athens_tz)
