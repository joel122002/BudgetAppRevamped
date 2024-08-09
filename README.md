
# Build

1. Enable Firestore and Firebase authentication (With only google sign    in  enabled)

2. [Use this command on windows](https://developers.google.com/android/guides/client-auth) to generate SHA1 fingerprint
```shell  
keytool -list -v -alias androiddebugkey -keystore %USERPROFILE%\.android\debug.keystore
```  

3. Add `google-services.json`(obtained from firebase) to app folder

4. Once generate add it to project settings. For release generate a similar release key and add it to firebase

5. Add the following variables to `local.properties` file
```shell
ROOM_NAME="<room name>"
```