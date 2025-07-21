# Firebase Google Sign-In Setup Instructions

## Prerequisites
1. You need a Firebase project with Google Sign-In enabled
2. You need to add your app to the Firebase project
3. You need to download the `google-services.json` file

## Setup Steps

### 1. Firebase Console Setup
1. Go to [Firebase Console](https://console.firebase.google.com/)
2. Select your project or create a new one
3. Go to **Authentication** > **Sign-in method**
4. Enable **Google** as a sign-in provider
5. Add your app's SHA-1 fingerprint:
   - For debug: Get it by running `./gradlew signingReport` in your project terminal
   - For release: Use your release keystore SHA-1

### 2. Get Web Client ID
1. In Firebase Console, go to **Project Settings** > **General**
2. In the **Your apps** section, find your Android app
3. Copy the **Web client ID** (not the Android client ID)
4. Open `app/src/main/res/values/strings.xml`
5. Replace `YOUR_WEB_CLIENT_ID_HERE` with your actual Web client ID:
   ```xml
   <string name="default_web_client_id">123456789-abcdefghijk.apps.googleusercontent.com</string>
   ```

### 3. Download google-services.json
1. In Firebase Console, go to **Project Settings** > **General**
2. In the **Your apps** section, click on your Android app
3. Click **Download google-services.json**
4. Place the file in your `app/` directory (it should already be there)

### 4. Verify Setup
1. Make sure your `google-services.json` file is in the `app/` directory
2. Make sure the Web client ID is correctly set in `strings.xml`
3. Build and run the app
4. Test Google Sign-In functionality

## Important Notes
- The Web client ID is different from the Android client ID
- Make sure your app's package name matches the one in Firebase Console
- For production, make sure to use the correct SHA-1 fingerprint from your release keystore

## Troubleshooting
1. **"Developer Error" message**: Usually means the SHA-1 fingerprint is not added or incorrect
2. **"Sign in failed"**: Check if the Web client ID is correct
3. **Build errors**: Make sure `google-services.json` is in the correct location
