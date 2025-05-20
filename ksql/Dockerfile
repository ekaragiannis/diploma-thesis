FROM alpine:latest

RUN apk add --no-cache curl

WORKDIR /workdir

COPY ksql/init-ksql.sh .
RUN chmod +x init-ksql.sh

ENTRYPOINT ["./init-ksql.sh"]
