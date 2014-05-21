# VoltDB Example App: Log ingestion from syslog using Kafka loader.

Use Case
--------
This application loads system logs (syslogs) forwarded to a UDP listener. The logs are pushed to configured kafka cluster.
A kafka stream based bulkloading application then pulls and loads the log data in VoltDB.
VoltDB is pupulated with event data and materialized view created for easy access.
Periodically OLD log data is deleted if specified.

Code organization
-----------------
The code is divided into projects:

- "events-db": the database project, which contains the schema, stored procedures and other configurations that are compiled into a catalog and run in a VoltDB database.  
- "ksyslogd": a java client that listens for log data on 1514 port and pushes them to specified Kafka cluster.
- "nibbler": a java client that calls VoltDB and periodically deletes old events (cruft removal) This is optional step but make sure your VoltDB cluster can handle the events data ingestion.

See below for instructions on running these applications.  For any questions, 
please contact fieldengineering@voltdb.com.

Pre-requisites
--------------
Before running these scripts you need to have VoltDB 4.3 (Enterprise or Community) or later installed, and you should add the voltdb-$(VERSION)/bin directory to your PATH environment variable, for example:

    export PATH="$PATH:$HOME/voltdb-ent-4.3/bin"


Instructions
------------

1. Start the database (in the background)

    cd events-db
    ./run.sh
     
2. Run the ksyslogd server

    cd ksyslogd
    # Get syslog4j-0.9.30.jar and copy it under lib directory
    # Make sure that machine where ksyslogd is going to run is getting syslog forwarded on port 1514
    ant jar
    ./startsyslogpush kafka-broker (host:port) [listener-ip-for-syslog-events]

4. Start kafkaloader

    kafkaloader -z kafka-zookeeper -t topic events

5. Start nibbler (If you wish to delete old event data)
    cd nibbler
    ./run.sh


6. Run various queries to get event data by connecting to VoltDB server.

Instructions for running on a cluster
-------------------------------------

Take compiled catalog and run it on a cluster by setting correct deployment. Use VoltDB enterprise manager.
