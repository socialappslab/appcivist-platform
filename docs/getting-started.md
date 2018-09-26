# Getting started with AppCivist Core Platform

Installing **AppCivist** in your server entails installing each of the following components, as shown in the image below. This guide explains how to download and install the AppCivist Core Platform and the AppCivist database.  
![AppCivist alpha prototype simplified architecture](images/Prototype Implementation Architecture.png)  

The AppCivist Core Platform provides a RESTful API implemented with the full-stack [Playframework][1]. Follow these instructions to download the source code and run it. 

1. **Install activator:** install [Typesafe Activator][3] and make sure to have its binaries available in your PATH. Alternatively, you can go to step (2) and simply use the compiled **activator** binary we included in the root folder of this repository.  
2. **Download the source code:** clone the source code repository in github
3. **Prepare configuration files:** 
    * Make a copy of **conf/local.sample.conf** into **conf/local.conf** and into **conf/local.test.conf**. 
    * Make a copy of **conf/local.logback.sample.xml** into **conf/local.logback.xml**.
    * Make a copy of **conf/play-authenticate/mine.conf.sample** into **conf/play-authentica/mine.local.conf** and replace email address with and email address you own 
    * Make a copy of **conf/play-authenticate/smtp.conf.sample** into **conf/play-authentica/smpt.local.conf** and replace email and passwords with your own. 
    * In **conf/local.conf** and **conf/local.test.conf**, replace the configuration values that start with **"${?*"** with required credentials for IMGUR, Amazon S3 and MapBox API. 
    * Change the variable **application.baseUrl** to point to the url of your deployment (i.e., http://localhost:9000/). Change also the varialbe **appcivist.invitations.baseUrl** to point to the base URL of the frontend prototype to use.  
    * Change the variable **application.contributionFiles** to point to the url of your deployment (i.e., http://localhost:9000/). Change also the varialbe **appcivist.invitations.baseUrl** to point to the base URL of the frontend prototype to use. 
4. Make sure you never commit the files in previous steps into the repository.
5. **Create the database (only for testing, NOT for production):** for testing purposes, you can simply use in-memory H2 database already configured by default in the sample configuration file. If you do this, make sure to enable evolutions first (see how below). Otherwise, install **postgresql** or **mysql** and configure  the platform to use them (see below).

```
    # Enabling Evolutions
    # ~~~~~
    evolutions {
        enabled=true
        db {
            default {
                autoApply=true
            }
        }
    }
```

6. **Create the database (for production):** disable Evolutions and install **postgresql** or **mysql**. Instead of evolution scripts, use the scripts **conf/sql/database-create-XXXX.sql** to create the database( XXXX = postgres for for PostgreSQL databases, XXXX = mysql for MySQL databases). 

7. **Configure the database:** if you use H2, skipt this step. If you use something else, replace the driver, database name, username and password you use with the database. 

```
    # Example of database configuration for PostgreSQL 
    db {
        default {
            driver=org.postgresql.Driver 
            url="jdbc:postgresql://localhost:5432/appcivistcore"
            username=root
            password="12345"
        }
    }
```

8. On a console, access your local copy of appcivist-core source code: **cd <PATH_TO_YOUR_LOCAL_REPO>/appcivist-platform**
9. Run the command **activator** to enter the typsafe activator console (or if you want to enable debugging from your IDE, use **activator -jvm-debug 9999 -D**). It will automatically download all the dependencies and setup the [play framework][1].  
10. **OPTIONAL**. Follow Play Framework's [Get Started][2] guide to learn more about how the project is organized
11. Run the server using the following command **run -Dconfig.resource=local.conf -Dlogger.file=conf/local.logback.xml**
12. On your browser, go to **http://localhost:9000/api/doc** to visit the documentation of the API endpoints and have try them out with real examples. 

## Package 

This file will be packaged with your application, when using `activator dist`.
In production settings, `./activator stage` will compile the app and create an executable binary. You can then use [this sample](https://gist.github.com/cdparra/c771013842dd6cef8d5f28d8b0dd738a) to create a proper init.d script 

## Other getting started guides
1. [Getting Started with AppCivist-PB web front-end](https://github.com/socialappslab/appcivist-pb-client)
2. [Getting Started with the AppCivist Voting API](https://github.com/socialappslab/appcivist-voting-api)
3. [Set Up Etherpad-Lite](https://github.com/ether/etherpad-lite/wiki#set-up)


[1]: https://www.playframework.com/
[2]: https://www.playframework.com/documentation/2.3.x/Home
[3]: http://typesafe.com/get-started
[4]: https://www.getpostman.com/
