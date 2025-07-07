import psycopg2
import os


def get_db_connection():
    """
    Get a connection to the PostgreSQL database.

    Returns:
        psycopg2.extensions.connection: A connection to the PostgreSQL database.
    """
    return psycopg2.connect(
        dbname=os.getenv('POSTGRES_DB', 'postgres'),
        user=os.getenv('POSTGRES_USER', 'postgres'),
        password=os.getenv('POSTGRES_PASSWORD', '1234'),
        host=os.getenv('POSTGRES_HOST', 'localhost'),
        port=int(os.getenv('POSTGRES_PORT', 5432))
    )


def fetch_hourlydata_last_24h(sensor):
    """
    Fetch hourly data from the database for the last 24 hours for a given sensor.

    Args:
        sensor (str): The sensor identifier.

    Returns:
        list: A list of dictionaries containing the hourly data.
    """
    conn = get_db_connection()
    cur = conn.cursor()
    query = """
        SELECT * FROM hourlydata
        WHERE sensor = %s
          AND hour_bucket >= NOW() - INTERVAL '24 hours'
        ORDER BY hour_bucket ASC;
    """
    cur.execute(query, (sensor,))
    rows = cur.fetchall()
    if not rows or cur.description is None:
        return []
    columns = [desc[0] for desc in cur.description]
    data = [dict(zip(columns, row)) for row in rows]
    cur.close()
    conn.close()
    return data


def fetch_rawdata_last_24h(sensor):
    """
    Fetch raw data from the database for the last 24 hours for a given sensor.

    Args:
        sensor (str): The sensor identifier.

    Returns:
        list: A list of dictionaries containing the raw data.
    """
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
    columns = [desc[0] for desc in cur.description]
    data = [dict(zip(columns, row)) for row in rows]
    cur.close()
    conn.close()
    return data


def fetch_sensor_names():
    """
    Fetch all sensor names from the database.

    Returns:
        list: A list of sensor names as strings.
    """
    conn = get_db_connection()
    cur = conn.cursor()
    query = "SELECT DISTINCT sensor FROM rawdata;"
    cur.execute(query)
    rows = cur.fetchall()
    print(rows)
    if not rows or cur.description is None:
        return []
    cur.close()
    conn.close()
    # Extract sensor names from the rows and return as a simple list
    sensor_names = [row[0] for row in rows]
    return sensor_names
