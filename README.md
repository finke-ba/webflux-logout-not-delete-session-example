# Webflux Logout Not Delete Session Example

## About
Showcase of spring security behaviour

## Build
`./mvnw clean install` 

## Run
`docker-compose -f docker-compose.yml up`

`./mvnw spring-boot:run`

## Steps for reproduce

1. `curl -i -X POST --data "username=admin&password=password" localhost:8080/login` - Save SESSION cookie and use it it nex step as {cookie-value-1}
2. `curl -i -X GET --cookie "SESSION={cookie-value-1}" localhost:8080/hello` - response status will be 200, body will be "HelloWorld"
3. `curl -i -X POST --cookie "SESSION={cookie-value-1}" localhost:8080/logout` -  Save SESSION cookie and use it it nex step as {cookie-value-2}
4. `curl -i -X GET --cookie "SESSION={cookie-value-2}" localhost:8080/hello` - response status will be 302, body will be empty
5. `curl -i -X GET --cookie "SESSION={cookie-value-1}" localhost:8080/hello` - response status will be 200, body will be "HelloWorld", but it should be the same as step 4

Step 5 shows that after logging out, Spring creates a new SESSION cookie, but doesn't delete the current one and we can use previous session even after logging out.
