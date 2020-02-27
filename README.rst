Coursera OAuth2 client
======================

This project is a JAVA library consisting of a client for interacting with Coursera's OAuth2 authorizes APIs.

Requirements
-----

Install Maven from http://maven.apache.org/install.html

Install JAVA from https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html

Clone project, open terminal, go to project directory, run in terminal ``'mvn package'``, you can find jar with dependencies in ``$project_dir/target/`` folder.

Setup
-----

Before using Coursera's OAuth2 APIs, be sure you know your client id,
client secret, and scopes you want for your application. You may create
an application at https://accounts.coursera.org/console. When creating the
application, set the Redirect URI to be http://localhost:9876/callback?client_id=<your_client_id>.

Usage
-----

Build jar file by command ``'mvn package'``, import jar with dependencies to your project and create new object:

::

    CourseraOAuth2Service service = CourseraOAuth2ServiceFactory.getInstance(CourseraOAuth2ServiceType.FILE);
    service.addClient(clientName, clientId, clientSecret, scopes); // add new client configuration
    service.generateOAuth2Tokens(clientName); // generate authentication tokens
    String accessToken = service.getAccessToken(clientName); // get access token

The Coaclient tries to open the default system browser while generating authentication tokens.
The application configuration will be saved to the local file if the request is succeeded.
You should check the data you've provided to the library during application configuration if you see any errors in the browser.

If the client was successfully added and configured, you would be able to get authentication tokens for Coursera API successfully. Otherwise, an exception will be thrown telling you
to set up your application for API access.

Documentation
-----

``public class CourseraOAuth2ServiceFactory``

Factory ``CourseraOAuth2ServiceFactory`` specifies the class for initialize ``getInstance(CourseraOAuth2ServiceType type)``
an object of CourseraOAuth2Service interface that manage client config and returns Coursera authentication tokens.


``public interface CourseraOAuth2Service``

Methods:
::

    void addClientConfig(String clientName,
                        String clientId,
                        String clientSecret,
                        Set<String> scopes) throws CreateClientAppException;

Create a new client config and save it to the local config file: ``<home.dir>/.coursera/coaclient.csv``

Parameters:
::

    clientName - Client Name
    clientId - Coursera Client ID
    clientSecret - Coursera Client Secret Key
    scopes - by default used "view_profile", for business use "access_business_api".

Throws:
``CreateClientAppException`` - if any error occurred while creating client config

::

    void deleteClientConfig(String clientName);

Delete client config from file: ``<home.dir>/.coursera/coaclient.csv``
and auth token file from ``<home.dir>/.coursera/<client_name>_oauth2.csv``

::

    void generateOAuth2Tokens(String clientName) throws TokenNotGeneratedException;

Get auth tokens from Coursera OAuth API and save to local cache token file.

Throws:
``TokenNotGeneratedException`` - if any error occurred while generating OAuth2 tokens

::

    AuthTokens getAuthTokens(String clientName);

Returns:
AuthTokens object with refresh, access tokens and expired time from auth token file:  ``<home.dir>/.coursera/<client_name>_oauth2.csv``.

::

    String getAccessToken(String clientName);

Returns:
Access token by client name from auth token file:  ``<home.dir>/.coursera/<client_name>_oauth2.csv``.

::

    List<ClientConfig> getClientConfigs();

Returns:
List of client config from local file: ``<home.dir>/.coursera/coaclient.csv``.

::

    void stopServerCallbackListener();

Stop server callback listener.

Bugs / Issues / Feature Requests
-----

Please use the Github issue tracker to document any bugs or other issues you
encounter while using this tool.