@setlocal enabledelayedexpansion
@echo off

set DBTYPE=%2%
set OPERATION=%1%
set DIR=%CD%

REM Get Parent Directory and Parent Name
for %%I in ("%~dp0.") do for %%J in ("%%~dpI.") do set ParentPath=%%~dpnxJ
for %%I in ("%~dp0.") do for %%J in ("%%~dpI.") do set NAME=%%~nxJ
set NAME=%NAME%-severell%DBTYPE%

REM Removing All Whitespace
set NAME=%NAME: =%
for /f "usebackq delims=" %%I in (`powershell "\"%NAME%\".toLower()"`) do set "NAME=%%~I"



IF "%OPERATION%"=="start" (
	for /f "usebackq delims=" %%I in (`docker image inspect %NAME%:latest`) do set "imageExists=%%~I"
	
	if !imageExists! == [] (
		docker build -t %NAME% - < ./config/%DBTYPE%.dockerfile
	)
	
	set command=docker ps -aq -f name=%NAME%

	for /f "usebackq delims=" %%I in (`!command!`) do set "contExists=%%~I"

	if "!contExists!" == "" (

		IF "%DBTYPE%"=="postgres" (
			docker run -d --name %NAME% -p 5555:5432 %NAME%
		) ELSE IF "%DBTYPE%"=="mysql" (
			docker run -d --name %NAME% -p 5555:3306 %NAME%
		) ELSE IF "%DBTYPE%"=="redis" (
			docker run -d --name %NAME% -p 6379:6379 %NAME%
		)
	) ELSE (

		docker start %NAME%
	)

) ELSE IF "%OPERATION%"=="stop" (

	docker stop %NAME%
)

