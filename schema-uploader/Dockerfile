FROM alpine:latest

RUN apk add --no-cache curl bash jq

WORKDIR /workdir

# Copy script from the `schema-uploader/` subdir
COPY --chmod=755 schema-uploader/upload-schemas.sh .

# Copy schemas from the subdir
COPY schema-uploader/schemas ./schemas/

ENTRYPOINT ["./upload-schemas.sh"]