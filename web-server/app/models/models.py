from pydantic import BaseModel
from typing import List, Optional
from datetime import datetime


class SensorDataRecord(BaseModel):
    """Model representing a record retrieved from storage (redis or database)."""

    hour_bucket: datetime
    energy_total: float


class EnergyConsumption(BaseModel):
    """Model representing formatted energy consumption data for API responses."""

    date: str
    period: str
    energy_total: float


class SensorDataResponse(BaseModel):
    """Response model for sensor data endpoints."""

    sensor: str
    data: List[EnergyConsumption]
    execution_time: Optional[float] = None


class SensorResponse(BaseModel):
    """Response model for sensor list endpoint."""

    sensors: List[str]


class HealthResponse(BaseModel):
    """Response model for health check endpoint."""

    status: str
    database: str
    redis: str


class ErrorResponse(BaseModel):
    """Error response model."""

    detail: str
    execution_time: Optional[float] = None
