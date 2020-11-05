# ride-to-gtfs-converter

Converts single rides into GTFS format.

### Run application

`./gradlew clean bootRun --args 'gtfs_data/ fc35e4a3-3959-8734-e97e-2d4a7577d886'`

where `gtfs_data/` is the given directory for saving the GTFS files and `fc35e4a3-3959-8734-e97e-2d4a7577d886` 
is the given userId for getting all relevant user rides

`./gradlew clean bootRun --args 'gtfs_data/'`

with given directory and getting all relevant rides

`./gradlew clean bootRun`

using relative default directory `data/` and getting all relevant rides
