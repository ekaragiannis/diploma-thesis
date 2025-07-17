from pydantic import BaseModel, Field
from typing import List, Optional, Literal
from datetime import datetime


class SensorDataRecord(BaseModel):
    """Model representing a record retrieved from storage (redis or database)."""

    hour_bucket: datetime = Field(
        ..., description="Timestamp representing the hour bucket for the data point"
    )
    energy_total: float = Field(..., description="Total energy consumption value")


class EnergyConsumption(BaseModel):
    """Model representing formatted energy consumption data for API responses."""

    date: str = Field(..., description="Date in ISO format (YYYY-MM-DD)")
    period: str = Field(
        ..., description="Time period for the measurement (e.g., '12:00-13:00')"
    )
    energy_total: float = Field(
        ..., description="Total energy consumption for the period"
    )


class SensorDataResponse(BaseModel):
    """Response model for sensor data endpoints."""

    sensor: str = Field(
        ..., description="Identifier of the sensor that provided the data"
    )
    source: Literal["db_raw", "db_hourly", "redis"] = Field(
        ...,
        description="Data source used for retrieval (raw database data, hourly aggregated data, or Redis cache)",
    )
    data: List[EnergyConsumption] = Field(
        ..., description="List of energy consumption measurements"
    )
    execution_time: Optional[float] = Field(
        None, description="Request execution time in seconds"
    )


class SensorResponse(BaseModel):
    """Response model for sensor list endpoint."""

    sensors: List[str] = Field(..., description="List of available sensor identifiers")


class HealthResponse(BaseModel):
    """Response model for health check endpoint."""

    status: str = Field(
        ...,
        description="Overall health status of the service ('healthy' or 'unhealthy')",
    )
    database: str = Field(
        ..., description="Database connection status ('connected' or 'disconnected')"
    )
    redis: str = Field(
        ..., description="Redis connection status ('connected' or 'disconnected')"
    )


class ErrorResponse(BaseModel):
    """Error response model."""

    detail: str = Field(..., description="Error message describing what went wrong")
    execution_time: Optional[float] = Field(
        None, description="Request execution time in seconds"
    )
