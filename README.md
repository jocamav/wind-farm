# Wind Farm

The current solution uses different design patterns:
* Builder pattern: To build the Entities of the database. Very useful for testing and initialize the Database. Quite useful when the classes have a lot attributes.
* Factory pattern: Event it's not really needed (there's only one Service), I considered the scability of the solution in order to add more service to retrieve the information of the Database.


## Controller

There's only one controller based on Spring to retrieve the information. The API is documented using Spring Rest Docs. I see several advantages with this approach:
* It generates the HTML doc automatically.
* It enforces to write and keep the tests up to date

Even it should manage more type of exception, I'm using the code 422 for the errors (even it should manage the 404 if a farm is not found).

## Service

###Farm Service

The service get the information from the database and it maps the result to a DTO to render as JSON. The workflow of the main operation is very easy:
- Get the Farm from database
- Get the Time Zone of the Farm
- Use the Time Zone of the farm to execute the query in the database using a Timestamp (which is not dependant of the Time Zone)

###DateUtilService

It retrieves the Timestamp based on:
- LocalDate: Date with no time
- ZoneId: The ZoneId

This way, we consider always the possible scenarios with different Time Zones

## Database 

I'm using the Spring JPA Data to create the repositories. For retrieving the main information of the report I used `@Query`to create a custom Query to retrieve the information in one single query.

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)
* [Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa/)

