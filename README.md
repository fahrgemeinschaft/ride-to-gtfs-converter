# ride-to-gtfs-converter

Converts single rides into GTFS format.

### Run application

Define database connection and GTFS option properties in the **application.properties** file:

* spring.datasource.url
* spring.datasource.username
* spring.datasource.password
* custom.gtfs.trips.use-time-period

or use environment variables:

* DB_URL
* DB_USERNAME
* DB_PASSWORD
* GTFS_TRIPS_USE_TIME_PERIOD

`./gradlew clean bootRun --args 'gtfs_data/ fc35e4a3-3959-8734-e97e-2d4a7577d886'`

where `gtfs_data/` is the given directory for saving the GTFS files and `fc35e4a3-3959-8734-e97e-2d4a7577d886` 
is the given userId for getting all relevant user rides

`./gradlew clean bootRun --args 'gtfs_data/'`

with given directory and getting all relevant rides

`./gradlew clean bootRun`

using relative default directory `data/` and getting all relevant rides
