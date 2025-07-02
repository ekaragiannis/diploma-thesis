from fastapi import FastAPI
from app.routers.sensor_data import router as sensor_data_router

app = FastAPI(title="Web Server API", version="1.0.0")

app.include_router(sensor_data_router)

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=5000)
