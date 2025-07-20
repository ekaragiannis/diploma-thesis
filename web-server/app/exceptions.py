from fastapi import Request, HTTPException
from fastapi.responses import JSONResponse
from app.models import ErrorResponse
from app.middlewares import get_execution_time
import logging

logger = logging.getLogger(__name__)


async def global_exception_handler(request: Request, exc: Exception) -> JSONResponse:
    """
    Global exception handler for unhandled exceptions.
    Converts unexpected errors to 500 Internal Server Error responses.
    """
    logger.error(f"Unhandled exception: {str(exc)}", exc_info=True)

    error_response = ErrorResponse(
        detail="Internal server error", execution_time=get_execution_time(request)
    )

    return JSONResponse(status_code=500, content=error_response.model_dump())


async def http_exception_handler_with_execution_time(
    request: Request, exc: HTTPException
) -> JSONResponse:
    """
    Custom HTTP exception handler that adds execution time to error responses.
    """
    error_response = ErrorResponse(
        detail=exc.detail, execution_time=get_execution_time(request)
    )

    return JSONResponse(
        status_code=exc.status_code,
        content=error_response.model_dump(),
        headers=getattr(exc, "headers", None),
    )
