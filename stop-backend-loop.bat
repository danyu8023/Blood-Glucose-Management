@echo off
setlocal EnableExtensions DisableDelayedExpansion

set "SSH_USER=ubuntu"
set "SSH_HOST=101.42.51.175"
set "SSH_TARGET=%SSH_USER%@%SSH_HOST%"

ssh -tt -o ConnectTimeout=15 "%SSH_TARGET%" "sudo systemctl stop pm2-ubuntu.service blood-glucose-api.service 2>/dev/null || true; sudo systemctl disable pm2-ubuntu.service blood-glucose-api.service 2>/dev/null || true; pm2 stop all 2>/dev/null || true; pm2 delete all 2>/dev/null || true; pm2 save --force 2>/dev/null || true; pm2 kill 2>/dev/null || true; for PID in $(sudo lsof -t -iTCP:8080 -sTCP:LISTEN 2>/dev/null); do sudo kill -9 $PID || true; done; echo '--- 8080 ---'; sudo ss -ltnp | grep 8080 || true; echo '--- SERVICES ---'; sudo systemctl list-units --type=service --all | grep -Ei 'pm2|blood|java' || true"
if errorlevel 1 (
    echo [FAILED] Could not stop the backend loop.
    pause
    exit /b 1
)

echo [OK] Backend restart loop stopped and port 8080 was cleared.
pause
