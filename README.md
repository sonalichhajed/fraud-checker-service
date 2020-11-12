# Getting Started

## About Fraud Checker Service
###Address Verification Service (AVS)
AVS is an effective security measure to detect online fraud.
When customers purchase items, they need to provide their billing
address and ZIP code. An AVS will check if this address matches with
what the card issuing bank has on file.
Part of a card-not-present (CNP) transaction, the payment gateway
can send a request for user verification to the issuing bank.
The AVS responds with a code that would help the merchant understand
if the transaction is has a full AVS match.
If they don’t match, more investigation should be carried out by
checking the CVV (Card Verification Value), email address, IP address
on the transaction or allow your payment gateway to decline the
transaction.

###Card Verification Value (CVV)
The CVV (or Card Verification Code ) is the 3 or 4-digit code that
is on every credit card. The code should never be stored on the
merchant’s database. A CVV filter acts as an added security measure,
allowing only the cardholder to use the card since it is available
only on the printed card. If an order is placed on your website and
the CVV does not match, you should allow your payment gateway to
decline the transaction.  While making a card-not-present
transaction (online, email or telephone orders), merchant gets the
required card information from the customer to verify the transaction.
Friendly fraud, is a risk associated with CNP transactions, that can
lead to a chargeback. Enabling a CVV filter helps merchants fight
fraud and reduce chargebacks.

###Device Identification
Device identification analysis the computer rather than the person
who is visiting your website. It profiles the operating system,
internet connection and browser to gauge if the online transaction
has to be approved, flagged or declined. All devices (phones,
computers, tablets, etc) have a unique device fingerprint, similar
to the fingerprints of people, that helps identify fraudulent
patterns and assess risk if any.

## Develop/Debug 
### To Start Dev Loop 
1. In one Terminal ==> ```gradle bootRun``` or to run on another port ```gradle bootRun -PjvmArgs="-Dserver.port=10001"```
    * To run a different profile at start-up, use ```gradle bootRun -Dspring.profiles.active=jenkins```.  If nothing is given, then the default, ```development``` profile is selected.
2. In the second Terminal ==> ```gradle -t test``` to run tests continuously.
3. In the IDE Terminal ==> 
    * To reload the latest classes in the JVM, use ```gradle compileJava```  
    * To reload the latest changes in static HTML files, use ```gradle reload```  

### To Debug
* Using only Intellij IDE
    * Debugging is as simple as navigating to the class with the main method, right-clicking the triangle icon, and choosing Debug.
* Using another JVM process and Intellij IDE
    1. In one Terminal ==> ```gradle bootRun -Dserver.port=10001 -Dagentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000```
        * Understanding the Java Debug Args - By default, the JVM does not enable debugging. This is because:
          * It is an additional overhead inside the JVM. 
          * It can potentially be a security concern for apps that are in public.
          Hence debugging is only done during development and never on production systems.
          
          Before attaching a debugger, we first configure the JVM to allow debugging. 
          We do this by setting a command line argument for the JVM:
          ```-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=8000```
          
          * ```-agentlib:jdwp``` - Enable the Java Debug Wire Protocol (JDWP) agent inside the JVM. This is the main command line argument that enables debugging.
          * ```transport=dt_socket``` - Use a network socket for debug connections. Other options include Unix sockets and shared memory.
          * ```server=y``` - Listen for incoming debugger connections. When set to n, the process will try to connect to a debugger instead of waiting for incoming connections. Additional arguments are required when this is set to n.
          * ```suspend=n``` - Do not wait for a debug connection at startup. The application will start and run normally until a debugger is attached. When set to y, the process will not start until a debugger is attached.
          * ```address=8000``` - The network port that the JVM will listen for debug connections. Remember, this should be a port that is not already in use.
    2. On Intellij IDE ==> 
        * Open menu Run | Edit Configurations...
        * Click the + button and Select 'Remote' from Templates

### Getting 42 crunch token
1. Using Springfox, navigate to [http://localhost:9001/swagger-ui/index.html](http://localhost:9001/swagger-ui/index.html) and click on [http://localhost:9001/v3/api-docs](http://localhost:9001/v3/api-docs) to get the JSON version of the API docs 
2. In order to generate the YAML version, go to [https://editor.swagger.io](https://editor.swagger.io) and paste the above JSON file.  Go to Edit | Convert to YAML and save the file in YAML format.
3. Go to [https://platform.42crunch.com](https://platform.42crunch.com) and sign-up/sign-in
4. Click Settings | Api Tokens | Create New Token - <<your token>>
5.  

### Reference Documentation
For further reference, please consider the following sections:

* [Official Gradle documentation](https://docs.gradle.org)
* [Spring Boot Gradle Plugin Reference Guide](https://docs.spring.io/spring-boot/docs/2.4.0-M3/gradle-plugin/reference/html/)
* [Create an OCI image](https://docs.spring.io/spring-boot/docs/2.4.0-M3/gradle-plugin/reference/html/#build-image)
* [Spring Web](https://docs.spring.io/spring-boot/docs/2.3.4.RELEASE/reference/htmlsingle/#boot-features-developing-web-applications)

### Guides
The following guides illustrate how to use some features concretely:

* [Building a RESTful Web Service](https://spring.io/guides/gs/rest-service/)
* [Serving Web Content with Spring MVC](https://spring.io/guides/gs/serving-web-content/)
* [Building REST services with Spring](https://spring.io/guides/tutorials/bookmarks/)

### Additional Links
These additional references should also help you:

* [Gradle Build Scans – insights for your project's build](https://scans.gradle.com#gradle)

