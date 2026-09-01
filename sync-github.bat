@echo off
setlocal EnableExtensions DisableDelayedExpansion

title Blood Glucose Management - Sync to GitHub

set "BRANCH=main"
set "EXPECTED_REMOTE=https://github.com/danyu8023/Blood-Glucose-Management.git"
set "SCRIPT_DIR=%~dp0"
if "%SCRIPT_DIR:~-1%"=="\" set "SCRIPT_DIR=%SCRIPT_DIR:~0,-1%"

echo [INFO] Checking Git repository...
where git >nul 2>&1
if errorlevel 1 (
    echo [ERROR] Git was not found in PATH.
    echo         Install Git for Windows and run this script again.
    goto :fail
)

if not exist "%SCRIPT_DIR%\.git" (
    echo [ERROR] This script must be stored in the repository root.
    echo         Expected: "%SCRIPT_DIR%\.git"
    goto :fail
)

pushd "%SCRIPT_DIR%"
git rev-parse --is-inside-work-tree >nul 2>&1
if errorlevel 1 (
    echo [ERROR] The script directory is not a Git work tree.
    popd
    goto :fail
)

for /f "delims=" %%R in ('git remote get-url origin 2^>nul') do set "REMOTE_URL=%%R"
if not defined REMOTE_URL (
    echo [ERROR] Git remote "origin" is not configured.
    echo         Add it with: git remote add origin %EXPECTED_REMOTE%
    popd
    goto :fail
)
echo [INFO] Remote: %REMOTE_URL%
echo [INFO] Branch: %BRANCH%

set "CURRENT_BRANCH="
for /f "delims=" %%B in ('git branch --show-current') do set "CURRENT_BRANCH=%%B"
if /I not "%CURRENT_BRANCH%"=="%BRANCH%" (
    echo [ERROR] The current branch is not "%BRANCH%".
    echo         Switch branches before syncing, or edit BRANCH in this file.
    popd
    goto :fail
)

echo.
echo [INFO] Local changes to be reviewed:
git status --short
git status --porcelain | findstr . >nul
if errorlevel 1 (
    echo [INFO] No local changes to sync.
    popd
    goto :done
)

echo.
set "CONFIRM="
set /p "CONFIRM=Stage and commit all listed changes, then push to origin/%BRANCH%? [Y/N] "
if /I not "%CONFIRM%"=="Y" (
    echo [INFO] Sync cancelled.
    popd
    goto :done
)

set "COMMIT_MESSAGE=%~1"
if not defined COMMIT_MESSAGE set /p "COMMIT_MESSAGE=Commit message: "
if not defined COMMIT_MESSAGE (
    echo [ERROR] Commit message cannot be empty.
    popd
    goto :fail
)

echo.
echo [INFO] Staging changes...
git add -A
if errorlevel 1 (
    popd
    goto :fail
)

git diff --cached --quiet
if not errorlevel 1 (
    echo [INFO] Nothing staged after applying ignore rules.
    popd
    goto :done
)

echo [INFO] Files in this commit:
git diff --cached --name-status
echo.
echo [INFO] Creating commit...
git commit -m "%COMMIT_MESSAGE%"
if errorlevel 1 (
    popd
    goto :fail
)

echo [INFO] Pushing to origin/%BRANCH%...
git push origin "%BRANCH%"
if errorlevel 1 (
    echo [ERROR] Push failed. The remote branch may contain commits not in this checkout.
    echo         Review the remote changes, run git pull --rebase, then retry this script.
    popd
    goto :fail
)

echo.
echo [OK] Local changes were committed and pushed successfully.
popd
goto :done

:fail
echo.
echo [FAILED] GitHub sync stopped. Review the error above.
pause
exit /b 1

:done
pause
exit /b 0
