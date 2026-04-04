@echo off
echo Starting Order Service Instance 2 on port 8083...
echo (Demonstrates multiple instances registered in Eureka)
start "Order Service Instance 2" cmd /k "cd /d C:\Microservice-Assignment2\order-service && mvn spring-boot:run -Dspring-boot.run.arguments=--server.port=8083"
pause
