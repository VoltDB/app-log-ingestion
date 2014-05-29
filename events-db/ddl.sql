-- events
CREATE TABLE events
(
  event_date timestamp NOT NULL
, event_level integer NOT NULL
, event_facility integer
, event_host VARCHAR(256) NOT NULL
, event_message VARCHAR(1024)
);

PARTITION TABLE events ON COLUMN event_host;

CREATE INDEX event_ts_index ON events (event_date);

-- Agg views
CREATE VIEW events_by_second
(
  event_host,
  event_level,
  second_ts,
  count_values
)
AS SELECT event_host, event_level, TRUNCATE(SECOND, event_date), COUNT(*)
   FROM events
   GROUP BY event_host, event_level, TRUNCATE(SECOND, event_date);

CREATE VIEW events_by_minute
(
  event_host,
  event_level,
  minute_ts,
  count_values
)
AS SELECT event_host, event_level, TRUNCATE(MINUTE, event_date), COUNT(*)
   FROM events
   GROUP BY event_host, event_level, TRUNCATE(MINUTE, event_date);

CREATE VIEW events_by_hour
(
  event_host,
  event_level,
  hour_ts,
  count_values
)
AS SELECT event_host, event_level, TRUNCATE(HOUR, event_date), COUNT(*)
   FROM events
   GROUP BY event_host, event_level, TRUNCATE(HOUR, event_date);

-- stored procedures
CREATE PROCEDURE FROM CLASS events.DeleteAfterDate;
PARTITION PROCEDURE DeleteAfterDate ON TABLE events COLUMN event_host;

CREATE PROCEDURE FROM CLASS events.DeleteOldestToTarget;
PARTITION PROCEDURE DeleteOldestToTarget ON TABLE events COLUMN event_host;
