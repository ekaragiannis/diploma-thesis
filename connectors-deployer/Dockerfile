FROM alpine:latest

RUN apk add --no-cache curl bash

WORKDIR /workdir

# Copy script from the `connectors-deployer/` subdir (relative to Dockerfile location)
COPY --chmod=755 connectors-deployer/deploy-connectors.sh .

# Copy configs from the subdir
COPY connectors-deployer/configs ./configs/

ENTRYPOINT ["./deploy-connectors.sh"]
