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
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v git >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1 || ! command -v mvn >/dev/null 2>&1 || ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1 || ! command -v nginx >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1; then sudo apt-get update && sudo DEBIAN_FRONTEND=noninteractive apt-get install -y git openjdk-17-jdk maven nodejs npm nginx curl ca-certificates; fi"
if errorlevel 1 goto :fail

ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v node >/dev/null 2>&1 || [ $(node -p 'parseInt(process.versions.node)') -lt 18 ]; then curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt-get install -y nodejs; fi"
if errorlevel 1 goto :fail

echo [INFO] Updating source code on the server...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if [ -d '%REMOTE_DIR%/.git' ]; then cd '%REMOTE_DIR%' && git config http.lowSpeedLimit 1 && git config http.lowSpeedTime 180 && git config http.version HTTP/1.1 && (for attempt in 1 2 3; do echo Git fetch attempt $attempt/3; GIT_TERMINAL_PROMPT=0 git fetch --prune --depth 1 --progress origin '%BRANCH%' && break; if [ $attempt -eq 3 ]; then exit 1; fi; sleep 5; done) && git checkout '%BRANCH%' && git reset --hard 'origin/%BRANCH%'; else if [ -e '%REMOTE_DIR%' ]; then rm -rf '%REMOTE_DIR%'; fi; GIT_TERMINAL_PROMPT=0 git -c http.lowSpeedLimit=1 -c http.lowSpeedTime=180 -c http.version=HTTP/1.1 clone --progress --depth 1 --branch '%BRANCH%' --single-branch '%REPO_URL%' '%REMOTE_DIR%'; fi"
if errorlevel 1 goto :fail

echo [INFO] Building frontend and publishing static files...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-vue'; npm ci; VITE_API_BASE='%API_BASE%' npm run build; sudo mkdir -p '%WEB_ROOT%'; sudo find '%WEB_ROOT%' -mindepth 1 -maxdepth 1 -exec rm -rf {} +; sudo cp -a dist/. '%WEB_ROOT%/'; sudo chown -R www-data:www-data '%WEB_ROOT%'"
if errorlevel 1 goto :fail

echo [INFO] Building backend and restarting the API with PM2...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-java'; if [ ! -f src/main/resources/application.yml ]; then cp src/main/resources/application.example.yml src/main/resources/application.yml; fi; mvn -DskipTests clean package; JAR=$(find target -maxdepth 1 -type f -name '*.jar' ! -name '*plain.jar' | head -n 1); test -n $JAR; if ! command -v pm2 >/dev/null 2>&1; then sudo npm install -g pm2; fi; pm2 delete blood-glucose-api >/dev/null 2>&1 || true; pm2 start $JAR --name blood-glucose-api --interpreter none --time --restart-delay 5000 --max-memory-restart 512M; if [ ! -f /etc/systemd/system/pm2-ubuntu.service ]; then sudo env PATH=$PATH:/usr/bin pm2 startup systemd -u ubuntu --hp /home/ubuntu; fi; pm2 save; sleep 5; curl -fsS --max-time 10 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null"
if errorlevel 1 goto :fail

echo [INFO] Configuring Nginx for %DOMAIN% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo 'server { listen 80; server_name %DOMAIN%; root %WEB_ROOT%; index index.html; location / { try_files $uri $uri/ /index.html; } location /api/ { proxy_pass http://127.0.0.1:8080; proxy_http_version 1.1; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; } }' | sudo tee /etc/nginx/sites-available/blood-glucose-management > /dev/null && sudo ln -sf /etc/nginx/sites-available/blood-glucose-management /etc/nginx/sites-enabled/blood-glucose-management && for file in /etc/nginx/sites-enabled/*; do if [ $file != /etc/nginx/sites-enabled/blood-glucose-management ] && [ -f $file ] && grep -q 'server_name.*%DOMAIN%' $file; then sudo rm -f $file; fi; done && sudo nginx -t && sudo systemctl enable nginx && sudo systemctl reload nginx"
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
