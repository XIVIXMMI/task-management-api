# Spring Boot Boilerplate
 *Spring Boot Boilerplate* is a **starter kit**. This project is a very simple and useful.
 
## Technologies 
- Spring Boot (v3.5.0)
- Spring Data JPA
- Spring Validation
- Spring Security + JWT Token
- PostgreSQL
- Mapstruct
- Lombok
- Swagger (Open API)

## Customization

- You can customize ```token information (secret key, issuer, expiry date) ``` in `src/main/resources/application.yml` file.
- You can customize ```database connection information``` in `src/main/resources/application.yml` file.
- You can customize ```swagger information``` in `src/main/resources/application.yml` file.
- You can customize ```which endpoints are accessible without token information``` in `src/main/java/com/omori/taskmanagement/configuration/SecurityConfiguration.java` file.

## Run the Application

First you need to make sure that the database is up. 
If you're using Docker, you can use ```docker compose up -d``` command. (If you have made changes locally, you should use the *local-docker-compose* file.)

If you want to build the Docker image locally, use:
```sh
docker-compose -f local-docker-compose.yml up --build
```
Otherwise, ensure the `omori/taskmanagement` image is available for the main docker-compose file.

Navigate to the root of the project. For building the project using command line, run below command :

``` mvn clean install```

Run service in command line. Navigate to *target* directory. 

``` java -jar spring-boot-boilerplate.jar ```

## Postman Collection

- [You can access the Postman collection here and you can try it after you get the project up and running.](https://www.postman.com/postmanfaruk/workspace/faruk-genc-projects/collection/11439300-3d0317df-f217-40ff-a2a6-4eaaf66e1c55?action=share&creator=11439300)

### Others

 - [For Angular]
 
### License

Apache License 2.0

   [For Angular]: <https://github.com/Genc/angular-boilerplate>
