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

* [Python 3.7+](https://www.python.org/downloads/)  for debugging the backend  built via Flask. **Erik check this.**

Android VM or emulator, or a device is required to use the application.

### Libraries and dependancies
All packages and dependancies are detailed in Gradle's dependency manager and more generally in the app/src/build.gradle file.
  
### Building the application
The application should be built via Android Studio with Gradle installed.


## Maintaining the application
### Computer Vision
The application uses [Microsoft Azure's Computer Vision](https://azure.microsoft.com/en-us/services/cognitive-services/computer-vision/) for reading the text found in the given images. Substitution for a different software/service is not recommended, but if the user wishes to do so, they must reimplement the OCRAzureREST class' functions and their return types which may be used elsewhere in the application.

If the user wishes to change ownership of Azure's services, they need to change the subscription key found in the OCRAzureREST class to one supplied from their own Computer Vision service registered in the Microsoft Azure portal.

### Database / Heroku Deployment
**See here Erik**


### Google AdMob?



## Testing
idk

## Known bugs
The app may occasionally crash while synchronizing with Heroku's database. Clearing the cache resolves this.
  
Sometimes, after premium is purchased, the app may need a reset to be completely cleared from advertisements.


## Team Members
Leah Hughes (PL), Piotr Rucinski(DPL), Alexandru Closca-Gheorghiu, Matey Krastev, Aleksandra Nenkova, Erik Staas, Stanislav Stoyanov 
