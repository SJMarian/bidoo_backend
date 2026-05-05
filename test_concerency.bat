@echo off
powershell -ExecutionPolicy Bypass -File "%~dp0src\main\java\com\example\bidoo_backend\concurrency-test.ps1"
pause