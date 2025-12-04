@echo off
REM ===================================
REM GRControl Backend - Development Run Script
REM ===================================

echo.
echo [INFO] Loading development environment variables...
echo.

REM Check if .env file exists
if not exist .env (
    echo [WARNING] .env file not found. Using default values from application.properties
    echo [INFO] To customize, copy .env.development to .env
    echo.
    goto :run
)

REM Load variables from .env file
for /f "tokens=1,2 delims==" %%a in (.env) do (
    set "line=%%a"
    REM Skip comments and empty lines
    if not "!line:~0,1!"=="#" (
        if not "%%a"=="" (
            set "%%a=%%b"
            echo [LOADED] %%a
        )
    )
)

:run
echo.
echo [INFO] Starting GRControl Backend...
echo [INFO] Server will be available at http://localhost:%SERVER_PORT%
echo.

call mvnw.cmd spring-boot:run

pause
