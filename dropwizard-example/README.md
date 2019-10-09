# Introduction

The Dropwizard example application was developed to, as its name implies, provide examples of some of the features
present in Dropwizard.

# Overview

Included with this application is an example of the optional DB API module. The examples provided illustrate a few of
the features available in [Hibernate](http://hibernate.org/), along with demonstrating how these are used from within
Dropwizard.

This database example is comprised of the following classes:

* The `PersonDAO` illustrates using the Data Access Object pattern with assisting of Hibernate.

* The `Person` illustrates mapping of Java classes to database tables with assisting of JPA annotations.

* All the JPQL statements for use in the `PersonDAO` are located in the `Person` class.

* `migrations.xml` illustrates the usage of `dropwizard-migrations` which can create your database prior to running
your application for the first time.

* The `PersonResource` and `PeopleResource` are the REST resource which use the PersonDAO to retrieve data from the database, note the injection
of the PersonDAO in their constructors.

As with all the modules the db example is wired up in the `initialize` function of the `HelloWorldApplication`.

# Building The Application 

* To create the example, package the application using [Apache Maven](https://maven.apache.org/) from the root dropwizard directory.

        cd dropwizard
        ./mvnw package
        cd dropwizard-example

# Running The Application

To test the example application run the following commands.

* To setup the h2 database run.

        java -jar target/dropwizard-example-$DW_VERSION.jar db migrate example.yml

* To run the server run.

        java -jar target/dropwizard-example-$DW_VERSION.jar server example.yml

# Testing The Application

* To hit the Hello World example (hit refresh a few times).

	http://localhost:8080/hello-world

* To post data into the application.

	curl -H "Content-Type: application/json" -X POST -d '{"fullName":"Other Person","jobTitle":"Other Title"}' http://localhost:8080/people
	
	open http://localhost:8080/people

# Dockerize The Application

To create, run and test Docker image of the application run following commands.

* To crate Docker image of the application build application as described in [building section](#building-the-application) and then run following command:

    docker build -t dropwizard-example .

* To find a built image.

    docker images 

* To run the application.

    docker run -it -p 8080:8080 <docker_image_id>

and in Docker console run:

    java -jar dropwizard-example.jar db migrate example.yml
    java -jar dropwizard-example.jar server example.yml
 
* To test the application check [testing section](#testing-the-application)
