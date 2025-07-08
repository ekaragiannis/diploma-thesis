from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from app.routers.sensor_data import router as sensor_data_router
from app.routers.sensor import router as sensor_router
from app.middleware import ExecutionTimeMiddleware
from app.services.db_service import get_db_connection
from app.services.redis_service import redis_service
from dotenv import load_dotenv

# Load environment variables from .env file
load_dotenv()

app = FastAPI(title="Web Server API", version="1.0.0")

# Add CORS middleware
app.add_middleware(
    CORSMiddleware,
    # Update this if your frontend runs elsewhere
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Add execution time middleware
app.add_middleware(ExecutionTimeMiddleware)

# Add routers
app.include_router(sensor_data_router)
app.include_router(sensor_router)


@app.get("/health")
async def health_check():
    """
    Health check endpoint to verify the application and its dependencies are working.

    Returns:
        dict: Status information about the application and its dependencies.

    Raises:
        HTTPException: 503 if any dependency is not available.
    """
    try:
        # Check database connection
        db_conn = get_db_connection()
        db_conn.close()
        db_status = "healthy"
    except Exception as e:
        db_status = f"unhealthy: {str(e)}"

    try:
        # Check Redis connection
        redis_status = "healthy" if redis_service.ping() else "unhealthy"
    except Exception as e:
        redis_status = f"unhealthy: {str(e)}"

    # Overall health status
    overall_healthy = db_status == "healthy" and redis_status == "healthy"

    if not overall_healthy:
        raise HTTPException(
            status_code=503,
            detail={
                "status": "unhealthy",
                "database": db_status,
                "redis": redis_status
            }
        )

    return {
        "status": "healthy",
        "database": db_status,
        "redis": redis_status
    }

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
