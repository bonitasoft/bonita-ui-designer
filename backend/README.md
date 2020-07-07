# Backend
 
## Development
### IDE
Before pushing your code, you have to format it and organize imports.
The former is pretty standard and the configuration used on IntelliJ for the later can be configured in `Code Style/Java/Imports`.

![Java Imports](https://cloud.githubusercontent.com/assets/4922020/5725146/c639881e-9b50-11e4-981b-80ad11a93776.png)

You should note that `*` is used only if more than 20 classes are used, and that static imports are done before the `java`
 and `javax` packages, and then the other imports.


### Jetty
To start a jetty server

    mvn jetty:run
    
For debugging purpose

    mvnDebug jetty:run
    
Default configuration launch jetty on http://localhost:8080/, source code is _watched_ every two seconds to enable hot reload of modified code

### Gulp
While editing backend js files, extra build steps are performed when building war. For development purpose you can launch jetty via gulp. This will 'watch' js files and rebuild them every time a change happens

    gulp serve

## Unit test code coverage
To run unit test code coverage, launch maven profile _coverage_
    
    mvn clean test -Pcoverage
    

###Javascript test 
#### Single run

```shell
$ yarn test
```
or
```shell
$ gulp test
```

### Watch source files and tests

```shell
$ yarn run test-watch
```

or

```shell
$ gulp test-watch
```

## Licensing 
The build will check if the license is correctly added and up to date in each files, and fail if that's not the case. 
    
If you want to add or update the license, run :
 
    mvn license:format
    
If you want to run the check manually :

    mvn license:check    

## Rest API

### Widgets
#### Resource URI
    /rest/widgets
   
#### Migrate Workspace
    GET /rest/widgets

* Response : array of widgets 

* Response code
    * 200 OK
    * 500 internal server error

#### Create a custom widget
    POST /rest/widgets
    with =>
    BODY json representation of the model of the widget

* Response : created widget 

* Response code
    * 204 OK
    * 403 not allowed to save a non custom widget
    * 500 internal server error
    
#### Save a custom widget
    PUT /rest/widgets/{widgetId}
    with =>
    BODY json representation of the model of the widget

* Response code
    * 204 OK
    * 403 not allowed to save a non custom widget
    * 500 internal server error

#### Delete a custom widget
    DELETE /rest/widgets/{widgetId}

* Response code
    * 200 OK
    * 403 not allowed to delete a non custom widget
    * 404 not found
    * 500 internal server error
    
#### Add a new property to a custom widget
    POST /rest/widgets/{widgetId}/properties
    with =>
    BODY json representation of the model of a property

* Response code
    * 200 OK
    * 403 not allowed to modify a non custom widget
    * 404 widget not found
    * 500 internal server error
    
#### Update a property of a custom widget
    PUT /rest/widgets/{widgetId}/properties/{propertyName}
    with =>
    BODY json representation of the model of a property

* Response code
    * 200 OK
    * 403 not allowed to modify a non custom widget
    * 404 widget/property not found
    * 500 internal server error
    
#### Delete a property of a custom widget
    DELETE /rest/widgets/{widgetId}/properties/{propertyName}    

* Response code
    * 200 OK
    * 403 not allowed to modify a non custom widget
    * 404 widget/property not found
    * 500 internal server error

### Page model
#### Resource URI
    /rest/pages

#### Get all page information model
    GET /rest/pages

* Response : light json representation of the model of the page (id, name)

#### Get a page model
    GET /rest/pages/{pageId}

* Response : json representation of the model of the page

* Response code
    * 200 OK
    * 404 Page {pageId} not found
    
#### Save a page model
    PUT /rest/pages/{pageId}
    with =>
    BODY json representation of the model of the page 

* Response : N/A

* Response code
    * 204 OK
    * 500 internal server error
    
#### Get page data
    GET /rest/pages/{pageId}/data

* Response : array of data

* Response code
    * 200 OK
    * 404 page {pageId} not found
    * 500 internal server error
    
#### Save page data
    PUT /rest/pages/{pageId}/data/{dataName}
    with =>
    BODY { "value": page data value, "type": page data type }

* Response : json representation of data

* Response code
    * 200 OK
    * 404 page {pageId} not found
    * 500 internal server error
    
#### Delete page data
    DELETE /rest/pages/{pageId}/data/{dataName}

* Response : json representation of data

* Response code
    * 200 OK
    * 404 page {pageId} not found or data {dataName} not found
    * 500 internal server error

### Migration model
#### Resource URI
#### Migrate all workspace

    POST /rest/migration

* Response code
    * 200 OK
    * 500 internal server error
    
#### Migrate one Page

    PUT /rest/migration/page/{pageId}

Migrate page and all children artifact (widget or fragment)

* Response : json representation of migrationReport contains one of status in:
       
       
     * none => No migration done
     * incompatible => Page is incompatible with this uid version
     * success => Migration success
     * error => Error during migration, no changes is done
     * warning => Migration success with warning for user              
   

* Response code
    * 200 OK
    * 404 Page not found
    * 500 internal server error
    
#### Migrate one Widget

    PUT /rest/migration/widget/{widgetId}

* Response : json representation of migrationReport contains one of status in:
       
       
     * none => No migration done
     * incompatible => Widget is incompatible with this uid version
     * success => Migration success
     * error => Error during migration, no changes is done
     * warning => Migration success with warning for user              
   
* Response code
    * 200 OK       
    * 404 widget not found
    * 500 internal server error


#### Get migration status from an artifact json
    POST /rest/migration/status
    With =>
    BODY json of any artifact

* Response: json object with 2 booleans, stating if the artifact is compatible (i.e. can be migrated), 
and if the artifact is to be migrated.
For instance: {"compatible": "true", "migration": "true"}

* Response code
    * 200 OK
    * 400 invalid json
    
#### Get migration status of a page
    GET /rest/migration/status/page/{pageId}

* Response: see "Get migration status from an artifact json"

* Response code
    * 200 OK
    * 404 Page not found
    
#### Get migration status of a widget
    GET /rest/migration/status/widget/{widgetId}

* Response: see "Get migration status from an artifact json"

* Response code
    * 200 OK
    * 404 Widget not found
