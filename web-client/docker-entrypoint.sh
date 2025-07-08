#!/bin/sh
# Inject runtime env vars into the static files
if [ -n "$BASE_URL" ]; then
  find /usr/share/nginx/html/assets -type f -name '*.js' -exec sed -i "s|__BASE_URL__|$BASE_URL|g" {} +
fi
exec "$@" 