FROM python:3.11-slim

WORKDIR /app

# Install system dependencies
RUN apt-get update && apt-get install -y \
  gcc \
  curl \
  && rm -rf /var/lib/apt/lists/*

# Copy requirements and install Python dependencies
COPY web-server/requirements.txt .
RUN pip install --no-cache-dir -r requirements.txt

# Copy application code
COPY web-server/ .

# Expose port
EXPOSE 5000

# Run the application
CMD ["python", "main.py"] 