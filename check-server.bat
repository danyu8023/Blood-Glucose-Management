@echo off
setlocal EnableExtensions DisableDelayedExpansion

title Blood Glucose Management - Server Check

set "SSH_USER=ubuntu"
set "SSH_HOST=101.42.51.175"
set "SSH_OPTIONS=-tt -o ConnectTimeout=15 -o ServerAliveInterval=30 -o ServerAliveCountMax=3"
set "SSH_TARGET=%SSH_USER%@%SSH_HOST%"

where ssh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] OpenSSH client was not found in PATH.
    goto :fail
)

echo [INFO] Checking server health on %SSH_TARGET% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo '--- UPTIME ---'; uptime; echo '--- SERVICES ---'; systemctl is-active nginx 2>/dev/null || true; pm2 status 2>/dev/null || true; echo '--- PORTS ---'; ss -ltnp 2>/dev/null | grep -E ':80 |:443 |:8080 ' || true; echo '--- RESOURCES ---'; free -h; df -h /; echo '--- RECENT API LOG ---'; pm2 logs blood-glucose-api --lines 40 --nostream 2>/dev/null || true; echo '--- NGINX ERRORS ---'; sudo tail -n 40 /var/log/nginx/error.log 2>/dev/null || true"
if errorlevel 1 goto :fail

echo.
set "FIX="
set /p "FIX=Attempt to restart Nginx and the API now? [Y/N] "
if /I not "%FIX%"=="Y" goto :done

ssh %SSH_OPTIONS% "%SSH_TARGET%" "sudo systemctl restart nginx; cd '/home/ubuntu/workSpace/Blood-Glucose-Management/Blood Glucose Management-java'; JAR=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*plain.jar' | head -n 1); pm2 delete blood-glucose-api 2>/dev/null || true; for PID in $(sudo lsof -t -iTCP:8080 -sTCP:LISTEN 2>/dev/null); do sudo kill $PID || true; done; pm2 start java --name blood-glucose-api --interpreter none --time --restart-delay 5000 --max-memory-restart 512M -- -jar \"$JAR\"; pm2 save; sleep 5; curl -fsS --max-time 10 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null && echo API_OK || echo API_CHECK_FAILED"
if errorlevel 1 goto :fail

echo [OK] Recovery commands completed. Test https://zlywork.site/ again.
goto :done

:fail
echo.
echo [FAILED] Server check could not complete. If SSH is also unavailable, check the cloud firewall/security group and instance status.
pause
exit /b 1

:done
pause
exit /b 0
