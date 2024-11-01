
# Build

1. Enable Firestore and Firebase authentication (With only google sign in enabled)

2. Add the following variables to `local.properties` file
```shell
ROOM_NAME="<room name>"
```

3. Add `google-services.json`(obtained from firebase) to app folder

4. Build the project

5. Run the following command to generate the SHA1 key
```shell
gradlew signingReport # Run in root folder
```

6. Once generate add it to project settings. For release generate a similar release key and add it to firebase