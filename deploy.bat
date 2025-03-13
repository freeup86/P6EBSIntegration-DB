@echo off
echo Deploying P6-EBS Integration Tool...

if not exist "config" mkdir config
if not exist "config\database.properties" (
    copy "resources\config\database.properties.template" "config\database.properties"
    echo Please edit config\database.properties with your database connection details.
)

echo Deployment complete. Run the application using run.bat