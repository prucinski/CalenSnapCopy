# ![app icon](./app/src/main/app_icon_32x32.png) CalenSnap
Universal scheduler and event planner for Android, utilizing advanced image processing and cutting-edge cloud-based OCR technologies.

## Features
Features include:
* Read events from a single poster or gallery image
* View all scanned events at a glance
* Integration with your existing calendars
* (Business users) Map of hotspots where people scan the most events

  
## Installation
You can easily install the application on your local device by downloading the .apk file from the Releases page.

## Building the App
### Prerequisites
To build the application, you are required to have:
* [Android Studio Arctic Fox](https://developer.android.com/studio) or newer

* [Python 3.7+](https://www.python.org/downloads/)  for debugging the Flask-backend. Furthermore, an installation of [PostgreSQL](https://www.postgresql.org) is required if a local database is to be run, or the developer wishes to interact with the Heroku database directly. 

Android VM or emulator, or a device is required to use the application.

### Libraries and dependancies
For the Android application, packages and dependancies are detailed in Gradle's dependency manager and more generally in the `app/src/build.gradle` file.

The dependencies for the backend can be found in `database/requirements.txt`. To install them using Python's package manager PIP, use the following command: `pip install -r requirements.txt`. 
 
### Building the application
The application should be built via Android Studio with Gradle installed.


## Maintaining the application
### Computer Vision
The application uses [Microsoft Azure's Computer Vision](https://azure.microsoft.com/en-us/services/cognitive-services/computer-vision/) for reading the text found in the given images. Substitution for a different software/service is not recommended, but if the user wishes to do so, they must reimplement the OCRAzureREST class' functions and their return types which may be used elsewhere in the application.

If the user wishes to change ownership of Azure's services, they need to change the subscription key found in the `OCRAzureREST` class to one supplied from their own Computer Vision service registered in the Microsoft Azure portal.

### Database / Heroku Deployment
The database-backend is a fairly standard Flask CRUD API that allows the Android app to access a PostgreSQL database by sending different HTTP requests. It is hosted on Heroku. To deploy a new version, both the Heroku and the Git CLI are required. For more detailed instructions, consult the [database deployment README](https://www.postgresql.org). 
The API-endpoint is hardcoded into the Android app, and can be changed in `API.kt`.


## Team Members
Leah Hughes (PL), Piotr Rucinski(DPL), Alexandru Closca-Gheorghiu, Matey Krastev, Aleksandra Nenkova, Erik Staas, Stanislav Stoyanov 
