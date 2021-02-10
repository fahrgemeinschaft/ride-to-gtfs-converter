# ride-to-gtfs-converter

Converts single rides into GTFS format.

### Run application

Define database connection and GTFS option properties in the **application.properties** file:

* spring.datasource.url
* spring.datasource.username
* spring.datasource.password
...

or use environment variables:

* DB_URL
* DB_USERNAME
* DB_PASSWORD
...

`./gradlew clean bootRun --args 'runOnce'`

to start the application and generate the GTFS once.

`./gradlew clean bootRun`

to start the application and generate the GTFS by the given cron schedule.

Default GTFS output directory is `data/output/`. When using scheduling the GTFS file will also be published to http://localhost:8080/gtfs.zip
