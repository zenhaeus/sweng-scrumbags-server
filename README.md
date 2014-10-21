Scrum Tool App Engine
=====================

The backend of our ScrumToolApp which will contain all the data, such as users, projects, tasks etc. At the same time it provides a way for the application to access this data.

Setup
-----

How to setup Google App Engine in Eclipse for our project:

1. If you have already cloned the repo for the server, delete it.
2. If you haven't done so already, install the Google Cloud Eclipse Plugin:
  1. Help -> Install new Software... -> Add...
  2. Add location: https://dl.google.com/eclipse/plugin/4.2
  3. Install at least the following sources: i) Developer Tools -> Android Development Tools
    1. Google App Engine Tools for Android
    2. Google Plugin for Eclipse 4.2
    3. SDKs -> Google App Engine Java SDK 1.9.13
  
  I admit, we are not entirely sure if all of these are needed, but this configuration seems to work on my machine.

3. Clone the server repo
4. Import the project:
  1. After opening the import wizard select "General" and "Exisiting Projects into Workspace"
  2. Select the server repo as root directory
  3. Don't copy into your workspace and finish
  4. We had a weird bug where one of the libraries was not found, so when you try to build it in eclipse it will say that the classes in the datanucleus library cannot be resolved. If this is the case, Sylvain found out that you need to:
    1. Enter the server project properties
    2. Google -> App Engine and change the setting "Datanucleus JDO/JPA version:" to v1, press ok, then go back and change it back to v2.
    3. clean the project in eclipse. This should do the trick and you should be able to run the Server now.

5. If you want to work with the server you need to switch to the ServerAccess branch in our Android project, since this branch contains the libraries that are needed on the client side. Sylvain and Arno can probably explain this closer tomorrow.
