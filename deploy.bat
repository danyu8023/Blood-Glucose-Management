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
set "DOMAIN=101.42.51.175"
set "WEB_ROOT=/var/www/blood-glucose-management"
set "API_BASE=http://101.42.51.175/api/v1"

rem Use the atomic deployment flow below. The legacy flow remains for reference.
goto :stable_deploy

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
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-java'; if [ ! -f src/main/resources/application.yml ]; then cp src/main/resources/application.example.yml src/main/resources/application.yml; fi; mvn -DskipTests clean package; JAR='/home/ubuntu/workSpace/Blood-Glucose-Management/Blood Glucose Management-java/target/blood-glucose-management-api-1.0.0.jar'; test -f \"$JAR\"; sudo ln -sfn \"$JAR\" /home/ubuntu/blood-glucose-api.jar; sudo systemctl disable --now pm2-ubuntu.service blood-glucose.service 2>/dev/null || true; sudo systemctl mask pm2-ubuntu.service blood-glucose.service 2>/dev/null || true; pm2 delete all >/dev/null 2>&1 || true; pm2 kill >/dev/null 2>&1 || true; for PID in $(sudo lsof -t -iTCP:8080 -sTCP:LISTEN 2>/dev/null); do sudo kill -9 $PID || true; done; UNIT_TMP=$(mktemp); printf '%%s\n' '[Unit]' 'Description=Blood Glucose Management API' 'After=network.target' '' '[Service]' 'Type=simple' 'User=ubuntu' 'ExecStart=/usr/bin/java -jar /home/ubuntu/blood-glucose-api.jar' 'Restart=on-failure' 'RestartSec=10' 'SuccessExitStatus=143' '' '[Install]' 'WantedBy=multi-user.target' > \"$UNIT_TMP\"; sudo install -m 644 \"$UNIT_TMP\" /etc/systemd/system/blood-glucose-api.service; rm -f \"$UNIT_TMP\"; sudo mkdir -p /etc/systemd/system/blood-glucose-api.service.d; printf '%%s\n' '[Unit]' 'StartLimitIntervalSec=60' 'StartLimitBurst=3' '' '[Service]' 'ExecStartPre=/usr/bin/test -r /home/ubuntu/blood-glucose-api.jar' > \"$UNIT_TMP\"; sudo install -m 644 \"$UNIT_TMP\" /etc/systemd/system/blood-glucose-api.service.d/stability.conf; rm -f \"$UNIT_TMP\"; sudo systemctl daemon-reload; sudo systemctl enable --now blood-glucose-api.service; sleep 8; sudo systemctl is-active --quiet blood-glucose-api.service; curl -fsS --max-time 10 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null"
if errorlevel 1 goto :fail

echo [INFO] Configuring Nginx for %DOMAIN% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo 'server { listen 80 default_server; listen [::]:80 default_server; server_name %DOMAIN% _; root %WEB_ROOT%; index index.html; location = /index.html { try_files /index.html =404; } location / { try_files $uri $uri/ /index.html; } location /api/ { proxy_pass http://127.0.0.1:8080; proxy_http_version 1.1; proxy_set_header Host $host; proxy_set_header X-Real-IP $remote_addr; proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for; proxy_set_header X-Forwarded-Proto $scheme; proxy_read_timeout 60s; } }' | sudo tee /etc/nginx/sites-available/blood-glucose-management > /dev/null && sudo ln -sf /etc/nginx/sites-available/blood-glucose-management /etc/nginx/sites-enabled/blood-glucose-management && sudo rm -f /etc/nginx/sites-enabled/default && sudo nginx -t && sudo systemctl enable nginx && sudo systemctl reload nginx"
if errorlevel 1 goto :fail

echo.
echo [OK] Deployment completed successfully.
echo [INFO] Open http://%DOMAIN%/
echo [INFO] API endpoint: http://%DOMAIN%/api/v1
goto :done

:stable_deploy
echo.
echo [INFO] Checking local OpenSSH client...
where ssh >nul 2>&1
if errorlevel 1 goto :stable_fail
set "SSH_TARGET=%SSH_USER%@%SSH_HOST%"
echo [INFO] Connecting to %SSH_TARGET% ...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "echo SSH connection OK"
if errorlevel 1 goto :stable_fail

echo [INFO] Ensuring required server tools are installed...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v git >/dev/null 2>&1 || ! command -v java >/dev/null 2>&1 || ! command -v mvn >/dev/null 2>&1 || ! command -v node >/dev/null 2>&1 || ! command -v npm >/dev/null 2>&1 || ! command -v nginx >/dev/null 2>&1 || ! command -v curl >/dev/null 2>&1; then sudo apt-get update && sudo DEBIAN_FRONTEND=noninteractive apt-get install -y git openjdk-17-jdk maven nodejs npm nginx curl ca-certificates; fi"
if errorlevel 1 goto :stable_fail
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if ! command -v node >/dev/null 2>&1 || [ $(node -p 'parseInt(process.versions.node)') -lt 18 ]; then curl -fsSL https://deb.nodesource.com/setup_20.x | sudo -E bash - && sudo apt-get install -y nodejs; fi"
if errorlevel 1 goto :stable_fail

echo [INFO] Updating source code (three attempts, then use existing checkout)...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "if [ -d '%REMOTE_DIR%/.git' ]; then cd '%REMOTE_DIR%' && git config http.lowSpeedLimit 1 && git config http.lowSpeedTime 180 && git config http.version HTTP/1.1 && FETCH_OK=0; for attempt in 1 2 3; do echo Git fetch attempt $attempt/3; if GIT_TERMINAL_PROMPT=0 git fetch --prune --depth 1 --progress origin '%BRANCH%'; then FETCH_OK=1; break; fi; if [ $attempt -lt 3 ]; then sleep 5; fi; done; if [ $FETCH_OK -eq 1 ]; then git checkout '%BRANCH%' && git reset --hard 'origin/%BRANCH%'; else echo '[WARN] GitHub unavailable; using existing checkout.'; fi; else GIT_TERMINAL_PROMPT=0 git -c http.lowSpeedLimit=1 -c http.lowSpeedTime=180 -c http.version=HTTP/1.1 clone --progress --depth 1 --branch '%BRANCH%' --single-branch '%REPO_URL%' '%REMOTE_DIR%'; fi"
if errorlevel 1 goto :stable_fail

echo [INFO] Building and publishing frontend...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-vue'; npm ci; VITE_API_BASE='%API_BASE%' npm run build; test -s dist/index.html; STAGE=$(mktemp -d /tmp/bgm-web.XXXXXX); cp -a dist/. \"$STAGE/\"; sudo mkdir -p '%WEB_ROOT%'; sudo find '%WEB_ROOT%' -mindepth 1 -maxdepth 1 -exec rm -rf {} +; sudo cp -a \"$STAGE/.\" '%WEB_ROOT%/'; rm -rf \"$STAGE\"; test -s '%WEB_ROOT%/index.html'; sudo chown -R www-data:www-data '%WEB_ROOT%'"
if errorlevel 1 goto :stable_fail

echo [INFO] Building backend and atomically replacing the stable JAR...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; cd '%REMOTE_DIR%/Blood Glucose Management-java'; if [ ! -f src/main/resources/application.yml ]; then cp src/main/resources/application.example.yml src/main/resources/application.yml; fi; mvn -DskipTests clean package; BUILT_JAR='%REMOTE_DIR%/Blood Glucose Management-java/target/blood-glucose-management-api-1.0.0.jar'; test -s \"$BUILT_JAR\"; sudo install -o ubuntu -g ubuntu -m 0644 \"$BUILT_JAR\" /home/ubuntu/blood-glucose-api.jar.new; sudo mv -f /home/ubuntu/blood-glucose-api.jar.new /home/ubuntu/blood-glucose-api.jar; sudo systemctl disable --now pm2-ubuntu.service blood-glucose.service 2>/dev/null || true; sudo systemctl mask pm2-ubuntu.service blood-glucose.service 2>/dev/null || true; pm2 delete all >/dev/null 2>&1 || true; pm2 kill >/dev/null 2>&1 || true; UNIT_TMP=$(mktemp); printf '%%s\n' '[Unit]' 'Description=Blood Glucose Management API' 'After=network-online.target' 'Wants=network-online.target' 'StartLimitIntervalSec=60' 'StartLimitBurst=3' '' '[Service]' 'Type=simple' 'User=ubuntu' 'WorkingDirectory=%REMOTE_DIR%/Blood Glucose Management-java' 'ExecStartPre=/usr/bin/test -s /home/ubuntu/blood-glucose-api.jar' 'ExecStart=/usr/bin/java -jar /home/ubuntu/blood-glucose-api.jar' 'Restart=on-failure' 'RestartSec=10' 'TimeoutStartSec=90' 'TimeoutStopSec=30' 'SuccessExitStatus=143' '' '[Install]' 'WantedBy=multi-user.target' > \"$UNIT_TMP\"; sudo install -m 644 \"$UNIT_TMP\" /etc/systemd/system/blood-glucose-api.service; rm -f \"$UNIT_TMP\"; sudo rm -f /etc/systemd/system/blood-glucose-api.service.d/stability.conf; sudo systemctl daemon-reload; sudo systemctl enable blood-glucose-api.service; sudo systemctl restart blood-glucose-api.service; READY=0; for i in 1 2 3 4 5 6 7 8 9 10 11 12; do if curl -fsS --max-time 5 'http://127.0.0.1:8080/api/v1/public/articles?page=1&pageSize=1' >/dev/null; then READY=1; break; fi; sleep 2; done; test \"$READY\" = 1; sudo systemctl is-active --quiet blood-glucose-api.service"
if errorlevel 1 goto :stable_fail

echo [INFO] Validating Nginx and HTTP...
ssh %SSH_OPTIONS% "%SSH_TARGET%" "set -e; sudo nginx -t; sudo systemctl enable nginx; sudo systemctl reload nginx; curl -fsS --max-time 15 http://%DOMAIN%/ >/dev/null"
if errorlevel 1 goto :stable_fail
echo.
echo [OK] Deployment completed successfully with an atomic backend release.
echo [INFO] Open http://%DOMAIN%/
goto :done

:stable_fail
echo.
echo [FAILED] Deployment stopped. Review the error above.
pause
exit /b 1

:fail
echo.
echo [FAILED] Deployment stopped. Review the error above.
pause
exit /b 1

:done
pause
exit /b 0
