# Web-layer (Controller) Tests
  
There are 3 testing approaches in Spring: 
* Using MockMVC in standalone mode 
  * Without any filters and/or advices
  * Manually setting-up filters and/or advices
* Using MockMVC with WebApplicationContext, but no Web-Server 
* Using SpringBootTest with WebApplicationContext and Web-Server.

### Using MockMVC in standalone mode 
This is as close as it gets to a "REAL" Unit Test as we don't load any context, none, not even 
Spring Web-Application Context. Also, no Web-Server is running.  These tests run really fast! 
If at all any filters and advices (interceptors) are required, then they have to be set-up manually.
This has to be done manually because no Spring ApplicationContext is loaded; in its presence the 
filters and interceptors would have been injected automatically. 
  * Without any filters and/or advices
  * Manually setting-up filters and/or advices

### Using MockMVC with WebApplicationContext, but no Web-Server 
For this we use either the annotation @WebMvcTest or @SpringBootTest(webEnvironment = WebEnvironment.MOCK)
Here we get the WebApplicationContext injected and hence all filters and advices would
be automatically available.  However, no Web-Server loads when these tests execute.
The down-side is that we lose fine-grained control over what we want to configure. However,
it can also be an up-side when we really don't need such a fine-grained control, and we
want to set-up our test-context close to reality.  So in this case, when we run the tests, all
the dependencies, filters and advices are available - by default.  

### Using @SpringBootTest (Loads WebApplicationContext and Web-Server)
Spring loads WebApplicationContext and Web-Server when test annotated with@SpringBootTest(webEnvironment = WebEnvironment.RANDOM_PORT) 
or @SpringBootTest(webEnvironment = WebEnvironment.DEFINED_PORT) is found.
The test needs to use RestTemplate or TestRestTemplate to hit the Web-Server which in-turn
delegates the request to the controller.  We still can inject mocks using @MockBean and is very 
useful for running In-Process Component-Tests.