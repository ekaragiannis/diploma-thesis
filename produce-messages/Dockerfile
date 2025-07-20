# Use an official Python base image
FROM python:3.11-slim

# Set working directory inside container
WORKDIR /app

# Copy requirements first (better caching)
COPY requirements.txt .

# Install dependencies
RUN pip install --no-cache-dir -r requirements.txt

# Copy the rest of your project
COPY . .

# Default command (can be overridden at runtime)
ENTRYPOINT ["python", "main.py"]
