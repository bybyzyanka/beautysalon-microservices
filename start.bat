# Clean everything first
docker-compose down -v
docker system prune -f

# Build and start
docker-compose up --build -d eureka-server

# Wait 30 seconds for Eureka to fully start
sleep 30

# Start user service
docker-compose up -d user-service

# Wait 30 seconds
sleep 30

# Start the rest
docker-compose up -d