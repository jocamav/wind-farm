# Wind Farm

The current solution uses different design patterns:
* Builder pattern: To build the Entities of the database. Very useful for testing and initialize the Database. Quite useful when the classes have a lot attributes.
* Factory pattern: Event it's not really needed (there's only one Service), I considered the scability of the solution in order to add more service to retrieve the information of the Database.

To execute the application locally, execute:

```
mvn spring-bot:run
```

Example of request:

```
http://localhost:8080/api/farm/capacity/wind/1?startDate=2018-10-01&endDate=2018-10-31
```

## Controller

There's only one controller based on Spring to retrieve the information. The API is documented using Spring Rest Docs. I see several advantages with this approach:
* It generates the HTML doc automatically.
* It enforces to write and keep the tests up to date

Even it should manage more type of exception, I'm using the code 422 for the errors (even it should manage the 404 if a farm is not found).

When the application is packaged, the documentation of the API can be found in `target/generated-docs/index.html`.

## Service

###Farm Service

The service get the information from the database and it maps the result to a DTO to render as JSON. The workflow of the main operation is very easy:
- Get the Farm from database
- Get the Time Zone of the Farm
- Use the Time Zone of the farm to execute the query in the database using a Timestamp (which is not dependant of the Time Zone)

For grouping the result and calculating the total energy generated and the capacity factor, I'm using streams from Java 8. 

###DateUtilService

It retrieves the Timestamp based on:
- LocalDate: Date with no time
- ZoneId: The ZoneId

This way, we consider always the possible scenarios with different Time Zones.

Also it provides an operation to calculate the number of dives given a date and a Time Zone because some days don't have 24 hours (23 or 25 hours the days the hour is changing)

## Database 

I'm using the Spring JPA Data to create the repositories. For retrieving the main information of the report I used `@Query`to create a custom Query to retrieve the information in one single query.

Also I'm using Liquibase for managing the creation/update of the Database. To drop the database:

```
mvn liquibase:dropAll
```
The plugin updates the Database from the Changelog. This way we can manage the different updates of the Database and also the rollback. The track of the changes are stored in the table `databasechangelog`.

Some queries to test the results. 

```sql
SELECT wind_farm_id, sum(electricity_produced), sum(electricity_produced)/(count(wind_farm_id)*10)  FROM HOURLY_PRODUCTION 
where timestamp>= to_timestamp('2018-10-28', 'YYYY-MM-DD')
and timestamp < to_timestamp('2018-10-29', 'YYYY-MM-DD')
and wind_farm_id = 1
group by wind_farm_id; 

SELECT wind_farm_id, electricity_produced, timestamp FROM HOURLY_PRODUCTION 
where timestamp>= to_timestamp('2018-10-28', 'YYYY-MM-DD')
and timestamp < to_timestamp('2018-10-29', 'YYYY-MM-DD')
and wind_farm_id = 1
order by timestamp asc; 
```


