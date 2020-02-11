Coursera OAuth2 client
======================

This project is a JAVA library consisting of a client for interacting with Coursera's OAuth2 authorizes APIs.

Requirements
-----

Install Maven from http://maven.apache.org/install.html
Install JAVA from https://www.oracle.com/technetwork/java/javase/downloads/jre8-downloads-2133155.html
Clone project, open terminal, go to project directory, run in terminal 'mvn package',
you can find jar in $project_dir/target/ folder.

Setup
-----

Before using Coursera's OAuth2 APIs, be sure you know your client id,
client secret, and scopes you want for your application. You may create
an application at https://accounts.coursera.org/console. When creating the
application, set the
Redirect URI to be http://localhost:9876/callback?client_id=<your_client_id>.

Usage
Build jar file by command ``'mvn package'``, import jar to your project and create new object:

::

    CourseraOAuth2Service service = new CourseraOAuth2ServiceImpl();
    service.addClient(clientName, clientId, clientSecret, scopes); ``// add new client configuration``
    service.generateOAuth2Tokens(clientName); ``// generate authentication tokens``
    String accessToken = service.getAccessToken(clientName); ``// get access token``

The Coaclient tries to open the default system browser (If this step fails, the Coaclient suggests to open a link in the browser manually).
The application configuration will be saved to the local file if the request is succeeded.
You should check the data you've provided to the library during application configuration if you see any errors in the browser.

If client was successfully added and configured, you will be able to
successfully get authentication tokens for Coursera API. Otherwise, an exception will be thrown telling you
to set up your application for API access.

Documentation
-----

``public interface CourseraOAuth2Service``

Interface ``CourseraOAuth2Service`` specifies the interface for an object that manage client config and returns Coursera authentication tokens.

Implementations: ``public class CourseraOAuth2ServiceImpl``

Methods:
::

    void addClient(String clientName,
                        String clientId,
                        String clientSecret,
                        String scope) throws CreateClientAppException;

Create a new client config and save it to the local config file: ``<home.dir>/.coursera/coaclient.csv``

Parameters:
::

    clientName - Client Name
    clientId - Coursera Client ID
    clientSecret - Coursera Client Secret Key
    scope - by default used "view_profile", for business use "access_business_api".

Throws:
``CreateClientAppException`` - if any error occured while creating client config

::

    void deleteClient(String clientName);

Delete client config from file: ``<home.dir>/.coursera/coaclient.csv``
and auth token file from ``<home.dir>/.coursera/<client_name>_oauth2.csv``

::

    void generateOAuth2Tokens(String clientName) throws TokenNotGeneratedException;

By default starting server callback listener and get auth tokens from Coursera OAuth API.
Throws:
``TokenNotGeneratedException`` - if any error occured while generating OAuth2 tokens

::

    Map<String, String> getAuthTokens(String clientName);

Returns:
Map with refresh, access tokens and expired time from auth token file:  ``<home.dir>/.coursera/<client_name>_oauth2.csv``.

::

    String getAccessToken(String clientName);

Returns:
Access token by client name from auth token file:  ``<home.dir>/.coursera/<client_name>_oauth2.csv``.

::

    List<CourseraOAuth2Config> getClients();

Returns:
List of client config from local file: ``<home.dir>/.coursera/coaclient.csv``.

::

    void startServerCallbackListener() throws TokenNotGeneratedException;

Start server on default port '9876' for callback listener
Throws:
``TokenNotGeneratedException`` - if any error occured while starting server listener

::

    void stopServerCallbackListener();


Bugs / Issues / Feature Requests
-----

Please use the Github issue tracker to document any bugs or other issues you
encounter while using this tool.