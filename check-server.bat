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
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo '--- UPTIME ---'; uptime; echo '--- SERVICES ---'; systemctl is-active nginx 2>/dev/null || true; systemctl is-active blood-glucose-api.service 2>/dev/null || true; echo '--- RESTARTS ---'; systemctl show blood-glucose-api.service -p MainPID -p NRestarts -p Restart 2>/dev/null || true; echo '--- PORTS ---'; ss -ltnp 2>/dev/null | grep -E ':80 |:443 |:8080 ' || true; echo '--- RESOURCES ---'; free -h; df -h /; echo '--- PUBLIC HTTP ---'; curl -fsS --max-time 10 http://101.42.51.175/ -o /dev/null && echo HTTP_OK || echo HTTP_FAILED; echo '--- RECENT API LOG ---'; sudo journalctl -u blood-glucose-api.service -n 40 --no-pager 2>/dev/null || true; echo '--- NGINX ERRORS ---'; sudo tail -n 40 /var/log/nginx/error.log 2>/dev/null || true"
if errorlevel 1 goto :fail

echo.
set "FIX="
set /p "FIX=Attempt to restart Nginx and the API now? [Y/N] "
if /I not "%FIX%"=="Y" goto :done

ssh %SSH_OPTIONS% "%SSH_TARGET%" "sudo systemctl restart nginx; sudo systemctl restart blood-glucose-api.service; sleep 8; curl -fsS --max-time 10 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null && echo API_OK || echo API_CHECK_FAILED"
if errorlevel 1 goto :fail

echo [OK] Recovery commands completed. Test http://101.42.51.175/ again.
goto :done

:fail
echo.
echo [FAILED] Server check could not complete. If SSH is also unavailable, check the cloud firewall/security group and instance status.
pause
exit /b 1

:done
pause
exit /b 0
