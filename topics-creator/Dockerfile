FROM confluentinc/cp-kafka:latest

WORKDIR /workdir

COPY --chmod=755 topics-creator/create-topics.sh .

ENTRYPOINT ["/workdir/create-topics.sh"]
