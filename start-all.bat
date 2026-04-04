@echo off
echo ============================================
echo  Microservices Assignment - Startup Script
echo ============================================
echo.
echo STEP 1: Starting Config Server (port 8888)...
start "Config Server" cmd /k "cd /d C:\Microservice-Assignment2\config-server && mvn spring-boot:run"
echo Waiting 15 seconds for Config Server...
timeout /t 15 /nobreak

echo.
echo STEP 2: Starting Discovery Server / Eureka (port 8761)...
start "Discovery Server" cmd /k "cd /d C:\Microservice-Assignment2\discovery-server && mvn spring-boot:run"
echo Waiting 20 seconds for Eureka...
timeout /t 20 /nobreak

echo.
echo STEP 3: Starting Product Service (port 8081) - Service B...
start "Product Service" cmd /k "cd /d C:\Microservice-Assignment2\product-service && mvn spring-boot:run"
echo Waiting 15 seconds...
timeout /t 15 /nobreak

echo.
echo STEP 4: Starting Order Service (port 8082) - Service A...
start "Order Service" cmd /k "cd /d C:\Microservice-Assignment2\order-service && mvn spring-boot:run"
echo Waiting 15 seconds...
timeout /t 15 /nobreak

echo.
echo STEP 5: Starting API Gateway (port 8080)...
start "API Gateway" cmd /k "cd /d C:\Microservice-Assignment2\api-gateway && mvn spring-boot:run"

echo.
echo ============================================
echo  All services starting!
echo.
echo  URLs:
echo    Config Server:    http://localhost:8888
echo    Eureka Dashboard: http://localhost:8761
echo    API Gateway:      http://localhost:8080
echo    Product Service:  http://localhost:8081
echo    Order Service:    http://localhost:8082
echo.
echo  Login: POST http://localhost:8080/auth/login
echo    Body: {"username":"admin","password":"password"}
echo ============================================
pause
