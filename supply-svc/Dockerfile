FROM eclipse-temurin:17-jre

WORKDIR /app

# Copy the GPU-aware entrypoint (built from project root context)
COPY scripts/supply-entrypoint.sh /supply-entrypoint.sh
RUN chmod +x /supply-entrypoint.sh

COPY supply-svc/target/build.jar /app/build.jar

EXPOSE 8080

ENTRYPOINT ["/supply-entrypoint.sh"]
