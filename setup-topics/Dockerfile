FROM confluentinc/cp-kafka:8.0.0

# Copy the topics setup script with executable permissions
COPY --chmod=755 setup-topics/init-topics.sh /usr/local/bin/

# Set the entrypoint
ENTRYPOINT ["/usr/local/bin/init-topics.sh"] 