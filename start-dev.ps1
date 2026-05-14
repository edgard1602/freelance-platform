$env:JAVA_HOME = "C:\Users\MPADASSE\AppData\Local\Programs\Eclipse Adoptium\jdk-21.0.10.7-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;" + $env:PATH
Write-Host "Java version :" -ForegroundColor Cyan
java -version
Write-Host "`nDémarrage de freelance-platform [dev]..." -ForegroundColor Green
.\mvnw spring-boot:run "-Dspring-boot.run.profiles=dev"