@echo off
echo Getting SHA-1 fingerprint for Firebase...
echo.
cd /d "%~dp0"
call gradlew signingReport
echo.
echo Look for "SHA1:" in the output above
echo Copy the SHA-1 value and add it to Firebase Console
echo.
pause
