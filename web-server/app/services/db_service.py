import psycopg2
import os
from typing import List
from app.models import SensorDataRecord


def get_db_connection():
    return psycopg2.connect(
        dbname=os.getenv("POSTGRES_DB", "postgres"),
        user=os.getenv("POSTGRES_USER", "postgres"),
        password=os.getenv("POSTGRES_PASSWORD", "1234"),
        host=os.getenv("POSTGRES_HOST", "localhost"),
        port=int(os.getenv("POSTGRES_PORT", 5432)),
    )


def fetch_hourlydata_last_24h(sensor: str) -> List[SensorDataRecord]:
    conn = get_db_connection()
    cur = conn.cursor()
    query = """
        SELECT
          energy_total,
          hour_bucket
        FROM hourlydata
        WHERE sensor = %s
          AND hour_bucket >= NOW() - INTERVAL '24 hours'
        ORDER BY hour_bucket ASC;
    """
    cur.execute(query, (sensor,))
    rows = cur.fetchall()
    if not rows or cur.description is None:
        return []

    data = [SensorDataRecord(energy_total=row[0], hour_bucket=row[1]) for row in rows]
    cur.close()
    conn.close()
    return data


def fetch_rawdata_last_24h(sensor: str) -> List[SensorDataRecord]:
    conn = get_db_connection()
    cur = conn.cursor()
    query = """
        SELECT
          SUM(energy) as energy_total,
          TIME_BUCKET('1 hour', timestamp) as hour_bucket
        FROM rawdata 
        WHERE timestamp >= DATE_TRUNC('hour', NOW()) - INTERVAL '23 hours'
          AND timestamp <= NOW()
          AND sensor = %s
        GROUP BY TIME_BUCKET('1 hour', timestamp)
    """
    cur.execute(query, (sensor,))
    rows = cur.fetchall()
    if not rows or cur.description is None:
        return []

    data = [SensorDataRecord(energy_total=row[0], hour_bucket=row[1]) for row in rows]
    cur.close()
    conn.close()
    return data


def fetch_sensor_names() -> List[str]:
    conn = get_db_connection()
    cur = conn.cursor()
    query = "SELECT DISTINCT sensor FROM rawdata;"
    cur.execute(query)
    rows = cur.fetchall()
    if not rows or cur.description is None:
        return []
    cur.close()
    conn.close()
    # Extract sensor names from query results
    sensor_names = [row[0] for row in rows]
    return sensor_names
