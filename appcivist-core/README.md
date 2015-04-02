# AppCivist Core Platform

Core of AppCivist services. 

## Getting Started

1. Download and install [Typesafe Activator][3]
2. Clone this repository
3. On a console, access your local copy of appcivist-core source code. 

    ```sh 
        cd <PATH_TO_YOUR_LOCAL_REPO>/appcivist-core
    ```
    
4. Run *activator* to enter the typsafe activator console. I will automatically download all the dependencies and setup the [play framework][1]. 
5. Follow Play Framework's [Get Started][2] guide to learn more about how the project is organized

## Run on server

1. Checkout the branch feature/demo-phase-1 (currently, the main composition is there)
2. Rename conf/application.conf.sample to conf/application.conf.
3. Uncomment the lines related to *Database configuration* (i.e., those starting with "db.default") and the lines related to *Ebean Configuration* (i.e., the one starting with "ebean.default=")
4. Rename also conf/play-authenticate/mine.conf.sample to conf/play-authenticate/mine.conf.sample
5. On a console, access your local copy of appcivist-core source code. 

    ```sh 
        cd <PATH_TO_YOUR_LOCAL_REPO>/appcivist-core
    ```

6. Run *activator run* 
7. When asked, apply the SQL script for the database (this can be disabled in the conf file once the database is in production).
8. After that, the server will automatically load into the DB the initial data, with the initial composition example, as defined by conf/initial-data.yml 
9. Install [Postman][4] and import the collection available in test/postman/appcivist-core-api.json.postman_collection. Now you can execute some of these calls. 
10. You can also use the current simple UI and login with the user "bob@example.com" with the password "secret". It will load the list of assemblies, with their Issues count. 

## Package 

This file will be packaged with your application, when using `activator dist`.


[1]: https://www.playframework.com/
[2]: https://www.playframework.com/documentation/2.3.x/Home
[3]: http://typesafe.com/get-started
[4]: https://www.getpostman.com/
