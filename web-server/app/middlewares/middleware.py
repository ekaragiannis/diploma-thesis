import time
from typing import Callable
from fastapi import Request, Response
from starlette.middleware.base import BaseHTTPMiddleware
from starlette.types import ASGIApp


class ExecutionTimeMiddleware(BaseHTTPMiddleware):
    """
    Middleware to measure and report the execution time of each HTTP request.

    - Records the time at which the request starts.
    - Measures the duration after the request is processed.
    - Adds the execution time in milliseconds to the response headers under 'X-Execution-Time'.
    """

    def __init__(self, app: ASGIApp):
        super().__init__(app)

    async def dispatch(self, request: Request, call_next: Callable) -> Response:
        # Record start time
        start_time = time.time()

        # Store start time in request state for access in routes
        request.state.start_time = start_time

        # Process the request
        response = await call_next(request)

        # Calculate execution time and add to response headers (optional)
        execution_time_ms = (time.time() - start_time) * 1000
        response.headers["X-Execution-Time"] = f"{execution_time_ms:.2f}ms"

        return response


# Helper function to calculate execution time in routes
def get_execution_time(request: Request) -> float:
    """
    Helper function to retrieve the current execution time for a request.
    Can be used inside route handlers for debugging or logging.

    Args:
        request: FastAPI request object.

    Returns:
        Execution time as a float in milliseconds.
    """
    if hasattr(request.state, "start_time"):
        execution_time_ms = (time.time() - request.state.start_time) * 1000
        return round(execution_time_ms, 2)
    return 0.00
