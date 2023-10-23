# BingoGen-Server
Server application of BingoGEN project. It exposes a simple API to get available games\sheets and to generate a task board of set size. Written using Ktor.

There are no management capabilities to API, task sheets are ingested on boot from directory pointed to by `ASSET_INGEST` environment variable or `ingest.asset_folder` property in local configuration file. Task sheets format is simple a simple csv table with set columns (examples could be found in `Asset` folder), one csv file could contain multiple sheets and sheets for multiple games or each sheet could be saved in separate folder, any subfolder structure would work.

# Build and Deployment
Server is intended to be deployed using docker. Ready to use `docker-compose.yaml` is provided (Using IntelliJ IDEAs docker service it's possible to build an app and deploy it to remote docker). Database credentials should be provided as `DB_CREDENTIALS` environment variable if an external database is to be used.

It is also possible to build jar using the `ktor.buildFatJar` gradle task and later use the file from `build/libs` to create a docker image running the app.

# Test configuration
To run tests conviniently local configuration is implemented with `src/resources/application.test.conf` file. This file as well as `src/main/resources/local configuration` folders are added to gitignore. Although those files aren't provided as they might include some credentials, logins, passwords, they would be generated with working default values on running the test.
