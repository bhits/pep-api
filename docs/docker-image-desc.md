# Short Description
The PEP API is a RESTful web service for the Access Control Service entry point.

# Full Description

# Supported Source Code Tags and Current `Dockerfile` Link

[`1.10.0 (latest)`](https://github.com/bhits/pep-api/releases/tag/1.10.0), [`1.7.0`](https://github.com/bhits/pep-api/releases/tag/1.7.0)

[`Current Dockerfile`](https://github.com/bhits/pep-api/blob/master/pep/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits/pep-api).

# What is PEP?

The Policy Enforcement Point (PEP) API is a RESTful web service for the Access Control Service (ACS) entry point. PEP delegates the access decision to the Context Handler API, and it utilizes the Document Segmentation Service ([DSS](https://github.com/bhits/dss-api)) for segmenting CCD documents according to a patient's granular consent. PEP gives the same response for both "No applicable consents" and "No documents found" cases to avoid exposing the existence of a patient's consent.

For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits.github.io/consent2share/).
# How to use this image


## Start a PEP instance

Be sure to familiarize yourself with the repository's [README.md](https://github.com/bhits/pep-api) file before starting the instance.

`docker run  --name pep -d bhits/pep:latest <additional program arguments>`

*NOTE: In order for this API to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and support level infrastructure. Please refer to the [Consent2Share Deployment Guide](https://github.com/bhits/consent2share/releases/download/2.1.0/c2s-deployment-guide.pdf) for instructions to setup the Consent2Share infrastructure.*


## Configure

This API runs with a [default configuration](https://github.com/bhits/pep-api/blob/master/pep/src/main/resources/application.yml) that is primarily targeted for the development environment.  The Spring profile `docker` is actived by default when building images. [Spring Boot](https://projects.spring.io/spring-boot/) supports several methods to override the default configuration to configure the API for a certain deployment environment. 

Here is example to override default database password:

`docker run -d bhits/pep:latest --spring.datasource.password=strongpassword`

## Using a custom configuration file

To use custom `application.yml`, mount the file to the docker host and set the environment variable `spring.config.location`.

`docker run -v "/path/on/dockerhost/C2S_PROPS/pep/application.yml:/java/C2S_PROPS/pep/application.yml" -d bhits/pep:tag --spring.config.location="file:/java/C2S_PROPS/pep/"`

## Environment Variables

When you start the PEP image, you can edit the configuration of the PEP instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name pep -e JAR_FILE="pep-latest.jar" -v "/path/on/dockerhost/pep-latest.jar:/pep-latest.jar" -d bhits/pep:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name pep -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhits/pep:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup application arugument. The default value of is "--spring.profiles.active=docker".

`docker run --name pep -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=ssl,docker" -d bhits/pep:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.12.1.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](https://github.com/bhits/pep-api/blob/master/LICENSE) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhits/pep-api](https://github.com/bhits/pep-api) GitHub repository. Be sure to familiarize yourself with the repository's README.md file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits/pep-api/issues).