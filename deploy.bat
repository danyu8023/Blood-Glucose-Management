@echo off
setlocal EnableExtensions DisableDelayedExpansion

title Blood Glucose Management - Remote Deploy

rem Deployment settings. SSH key authentication is recommended.
set "SSH_USER=ubuntu"
set "SSH_HOST=101.42.51.175"
set "SSH_OPTIONS=-tt -o ConnectTimeout=15 -o ServerAliveInterval=30 -o ServerAliveCountMax=3"
set "REMOTE_DIR=/home/ubuntu/workSpace/Blood-Glucose-Management"
set "REPO_URL=https://github.com/danyu8023/Blood-Glucose-Management.git"
set "BRANCH=main"
set "DOMAIN=zlywork.site"
set "WEB_ROOT=/var/www/blood-glucose-management"
set "API_BASE=https://zlywork.site/api/v1"

echo.
echo [INFO] Checking local OpenSSH client...
where ssh >nul 2>&1
if errorlevel 1 (
    echo [ERROR] OpenSSH client was not found in PATH.
    echo         Install Windows OpenSSH Client, then run this file again.
    goto :fail
)

set "SSH_TARGET=%SSH_USER%@%SSH_HOST%"
echo [INFO] Connecting to %SSH_TARGET% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo SSH connection OK"
if errorlevel 1 goto :fail

echo [INFO] Installing missing server tools (sudo may ask for a password)...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v git >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1 || ! command -v mvn >/dev/null 2>&1 || ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1 || ! command -v nginx >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1 || ! command -v lsof >/dev/null 2>&1; then sudo apt-get update && sudo DEBIAN_FRONTEND=noninteractive apt-get install -y git openjdk-17-jdk maven nodejs npm nginx curl ca-certificates lsof; fi"
if errorlevel 1 goto :fail

ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v node >/dev/null 2>&1 || [ $(node -p 'parseInt(process.versions.node)') -lt 18 ]; then curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt-get install -y nodejs; fi"
if errorlevel 1 goto :fail

echo [INFO] Updating source code on the server...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if [ -d '%REMOTE_DIR%/.git' ]; then cd '%REMOTE_DIR%' && git config http.lowSpeedLimit 1 && git config http.lowSpeedTime 180 && git config http.version HTTP/1.1 && FETCH_OK=0; for attempt in 1 2 3; do echo Git fetch attempt $attempt/3; if GIT_TERMINAL_PROMPT=0 git fetch --prune --depth 1 --progress origin '%BRANCH%'; then FETCH_OK=1; break; fi; if [ $attempt -lt 3 ]; then sleep 5; fi; done; if [ $FETCH_OK -eq 1 ]; then git checkout '%BRANCH%' && git reset --hard 'origin/%BRANCH%'; else echo '[WARN] GitHub is unavailable after 3 attempts; continuing with the existing server checkout.'; fi; else if [ -e '%REMOTE_DIR%' ]; then rm -rf '%REMOTE_DIR%'; fi; GIT_TERMINAL_PROMPT=0 git -c http.lowSpeedLimit=1 -c http.lowSpeedTime=180 -c http.version=HTTP/1.1 clone --progress --depth 1 --branch '%BRANCH%' --single-branch '%REPO_URL%' '%REMOTE_DIR%'; fi"
if errorlevel 1 goto :fail

echo [INFO] Building frontend and publishing static files...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-vue'; npm ci; VITE_API_BASE='%API_BASE%' npm run build; test -f dist/index.html; sudo mkdir -p '%WEB_ROOT%'; sudo find '%WEB_ROOT%' -mindepth 1 -maxdepth 1 -exec rm -rf {} +; sudo cp -a dist/. '%WEB_ROOT%/'; test -f '%WEB_ROOT%/index.html'; sudo chown -R www-data:www-data '%WEB_ROOT%'"
if errorlevel 1 goto :fail

echo [INFO] Building backend and restarting the API with systemd...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-java'; if [ ! -f src/main/resources/application.yml ]; then cp src/main/resources/application.example.yml src/main/resources/application.yml; fi; mvn -DskipTests clean package; JAR=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*plain.jar' | head -n 1); test -n \"$JAR\"; sudo ln -sfn \"$JAR\" /home/ubuntu/blood-glucose-api.jar; sudo systemctl disable --now pm2-ubuntu.service blood-glucose.service 2>/dev/null || true; pm2 delete all >/dev/null 2>&1 || true; pm2 kill >/dev/null 2>&1 || true; for PID in $(sudo lsof -t -iTCP:8080 -sTCP:LISTEN 2>/dev/null); do sudo kill -9 $PID || true; done; UNIT_TMP=$(mktemp); printf '%%s\n' '[Unit]' 'Description=Blood Glucose Management API' 'After=network.target' '' '[Service]' 'Type=simple' 'User=ubuntu' 'ExecStart=/usr/bin/java -jar /home/ubuntu/blood-glucose-api.jar' 'Restart=always' 'RestartSec=5' 'SuccessExitStatus=143' '' '[Install]' 'WantedBy=multi-user.target' > \"$UNIT_TMP\"; sudo install -m 644 \"$UNIT_TMP\" /etc/systemd/system/blood-glucose-api.service; rm -f \"$UNIT_TMP\"; sudo systemctl daemon-reload; sudo systemctl enable --now blood-glucose-api.service; sleep 8; sudo systemctl is-active --quiet blood-glucose-api.service; curl -fsS --max-time 10 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null"
if errorlevel 1 goto :fail

echo [INFO] Configuring Nginx for %DOMAIN% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo 'server { listen 80; server_name %DOMAIN%; root %WEB_ROOT%; index index.html; location = /index.html { try_files /index.html =404; } location / { try_files $uri $uri/ /index.html =404; } location /api/ { proxy_pass http://127.0.0.1:8080; proxy_http_version 1.1; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; } }' | sudo tee /etc/nginx/sites-available/blood-glucose-management > /dev/null && sudo ln -sf /etc/nginx/sites-available/blood-glucose-management /etc/nginx/sites-enabled/blood-glucose-management && for file in /etc/nginx/sites-enabled/*; do if [ $file != /etc/nginx/sites-enabled/blood-glucose-management ] && [ -f $file ] && grep -q 'server_name.*%DOMAIN%' $file; then sudo rm -f $file; fi; done && sudo nginx -t && sudo systemctl enable nginx && sudo systemctl reload nginx"
if errorlevel 1 goto :fail

echo [INFO] Enabling HTTPS with Certbot (DNS must point to this server)...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v certbot >/dev/null 2>&1; then sudo apt-get update && sudo DEBIAN_FRONTEND=noninteractive apt-get install -y certbot python3-certbot-nginx; fi; sudo certbot --nginx --non-interactive --agree-tos --register-unsafely-without-email --redirect -d '%DOMAIN%' || echo '[WARN] Certbot could not issue a certificate yet; HTTP remains available.'"
if errorlevel 1 goto :fail

echo.
echo [OK] Deployment completed successfully.
echo [INFO] Open https://%DOMAIN%/ after DNS and HTTPS are configured.
echo [INFO] API endpoint: https://%DOMAIN%/api/v1
goto :done

:fail
echo.
echo [FAILED] Deployment stopped. Review the error above.
pause
exit /b 1

:done
pause
exit /b 0
