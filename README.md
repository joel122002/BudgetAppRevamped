
# Build

1. Enable Firestore and Firebase authentication (With only google sign    in  enabled)

2. Add `google-services.json`(obtained from firebase) to app folder

3. Finally [use this command on windows](https://developers.google.com/android/guides/client-auth) to generate SHA1 fingerprint
```shell  
keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
```  

4. Once generate add it to project settings