# Short Description

This service segments a patient's sensitive health information using the patient's consent.

# Full Description

# Supported Source Code Tags and Current `Dockerfile` Link

[`2.2.0 (latest)`](https://github.com/bhits-dev/dss/releases/tag/2.2.0), [`2.1.0`](https://github
.com/bhits-dev/dss/releases/tag/2.1.0), [`1.16.0`](https://github.com/bhits-dev/dss/releases/tag/1.16.0)

[`Current Dockerfile`](../dss/src/main/docker/Dockerfile)

For more information about this image, the source code, and its history, please see the [GitHub repository](https://github.com/bhits-dev/dss).

# What is Document Segmentation Service API?

The Document Segmentation Service (DSS) is responsible for the segmentation of the patient's sensitive health information using the privacy settings selected in the patient's consent. The segmentation process involves the following phases:

1. Document Validation: The DSS uses the [Document-Validator](https://github.com/bhits-dev/document-validator) to verify that the original document is a valid CCD document.
2. Fact Model Extraction: The DSS extracts a fact model, which is essentially based on the coded concepts in a CCD document.
3. Value Set Lookup: For every code and code system in the fact model, the DSS uses the Value Set Service (VSS) API to lookup the value set categories. The value set categories are also stored in the fact model for future use in the *Redaction* and *Tagging* phases.
4. BRMS (Business Rules Management Service) Execution: The DSS retrieves the business rules that are stored in a *[JBoss Guvnor](http://guvnor.jboss.org/)* instance and executes them with the fact model. The business rule execution response might contain directives regarding the *Tagging* phase.
5. Redaction: The DSS redacts sensitive health information for any sensitive value set categories which the patient did not consent to share in his/her consent. *NOTE: Additional Redaction Handlers are also being configured for other clean-up purposes.*
6. Tagging: The DSS tags the document based on the business rule execution response from the *BRMS Execution* step.
7. Clean Up: The DSS removes the custom elements that were added to the document for tracing purposes.
8. Segmented Document Validation: The DSS validates the final segmented document before returning it to ensure the output of DSS is still a valid CCD document.
9. Auditing: If enabled, the DSS also audits the segmentation process using *Logback Audit* server.

For more information and related downloads for Consent2Share, please visit [Consent2Share](https://bhits-dev.github.io/consent2share/).

# How to use this image

## Start a DSS instance

Be sure to familiarize yourself with the repository's [README.md](../README.md) file before starting the instance.

`docker run  --name dss -d bhitsdev/dss:latest <additional program arguments>`

*NOTE: In order for this service to fully function as a microservice in the Consent2Share application, it is required to setup the dependency microservices and the support level infrastructure. Please refer to the Consent2Share Deployment Guide in the corresponding Consent2Share release (see [Consent2Share Releases Page](https://github.com/bhits-dev/consent2share/releases)) for instructions to setup the Consent2Share infrastructure.*


## Configure

The Spring profiles `application-default` and `docker` are activated by default when building images.

This API can run with the default configuration which is from three places: `bootstrap.yml`, `application.yml`, and the data which the [`Configuration Server`](https://github.com/bhits-dev/config-server) reads from the `Configuration Data Git Repository`. Both `bootstrap.yml` and `application.yml` files are located in the class path of the running application.

We **recommend** overriding the configuration as needed in the `Configuration Data Git Repository`, which is used by the `Configuration Server`.

Also, [Spring Boot](https://projects.spring.io/spring-boot/) supports other ways to override the default configuration to configure the API for a certain deployment environment. 

The following is an example to override the default database password:

`docker run -d bhitsdev/dss:latest --spring.datasource.password=strongpassword`

## Environment Variables

When you start the DSS image, you can edit the configuration of the DSS instance by passing one or more environment variables on the command line. 

### JAR_FILE

This environment variable is used to setup which jar file will run. you need mount the jar file to the root of container.

`docker run --name dss -e JAR_FILE="dss-latest.jar" -v "/path/on/dockerhost/dss-latest.jar:/dss-latest.jar" -d bhitsdev/dss:latest`

### JAVA_OPTS 

This environment variable is used to setup JVM argument, such as memory configuration.

`docker run --name dss -e "JAVA_OPTS=-Xms512m -Xmx700m -Xss1m" -d bhitsdev/dss:latest`

### DEFAULT_PROGRAM_ARGS 

This environment variable is used to setup an application argument. The default value is "--spring.profiles.active=application-default,docker".

`docker run --name dss -e DEFAULT_PROGRAM_ARGS="--spring.profiles.active=application-default,ssl,docker" -d bhitsdev/dss:latest`

# Supported Docker versions

This image is officially supported on Docker version 1.12.1.

Support for older versions (down to 1.6) is provided on a best-effort basis.

Please see the [Docker installation documentation](https://docs.docker.com/engine/installation/) for details on how to upgrade your Docker daemon.

# License

View [license](../LICENSE) information for the software contained in this image.

# User Feedback

## Documentation 

Documentation for this image is stored in the [bhitsdev/dss](https://github.com/bhits-dev/dss) GitHub repository. Be sure to familiarize yourself with the repository's `README.md` file before attempting a pull request.

## Issues

If you have any problems with or questions about this image, please contact us through a [GitHub issue](https://github.com/bhits-dev/dss/issues).
