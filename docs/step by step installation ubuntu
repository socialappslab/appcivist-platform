# How To Install and Configure AppCivist Platform on Ubuntu 16.04

## Introduction
This guide explains step by step how to download and install a production isntance with AppCivist Core Platform and the AppCivist database on a Ubuntu 16.04 server.

The AppCivist Core Platform provides a RESTful API implemented with the full-stack Playframework. Follow these instructions to download the source code and run it.

## Installation

First we update all the system.

```
sudo apt-get update
sudo apt-get -y upgrade
sudo apt -y full-upgrade
```

Install Java Developer Kit

```
sudo apt-get install -y default-jdk
```

> Optional but recommended, next step will install essentials tools for developers.


```
sudo apt-get install -y build-essential
```

> Optional but recommended, next step will install **node.js V6**.


```
curl -sL https://deb.nodesource.com/setup_6.x | sudo -E bash -
sudo apt-get install -y nodejs
```


Now we install PostgreSQL

```
sudo apt-get install -y postgresql postgresql-contrib
systemctl start postgresql
systemctl enable postgresql
```

We have to modify the file **pg_hba.conf** to support password login

> The directory **will differ** depending on **psql version** installed


```
vim /etc/postgresql/9.5/main/pg_hba.conf
```

Search for the line with, and change 

```
local   all             all                                     peer
```

To

```
local   all             all                                    md5
```

Then restart psql server

```
sudo service postgresql restart
```

Now we can clone the repository in the home directory

```
cd
git clone https://github.com/socialappslab/appcivist-platform.git
cd appcivist-platform/
```

Copy the sql script into postgres home directory

```
cp conf/sql/database-create-postgres.sql /var/lib/postgresql/
```

Login into postgres 

```
sudo -i -u postgres
```

Login psql console

```
psql
```

Create the database with default name

```
create database appcivistcore;
```

Create password for database user **postgres**

```
\password
```

Exit psql console

```
\q
```

Run the sql script into appcivistcore database

```
psql -d appcivistcore -f database-create-postgres.sql
```

Logout from postgres

```
logout
```

Install Activator from compiled source code

> Next steps may take a long time to complete. This will install all dependecies required by Play-Framework


Make sure you are on the home directory of your user.

```
sudo ./activator
```

Once finished you will see

```
[appcivist-core] $ 
```

Exit TypeSafe Activator console typing: 

```
exit
```

Now we have installed TypeSafe Activator and Play-Framework dependencies, so we need to copy the next files 

```
cp conf/local.sample.conf conf/local.conf
cp conf/local.sample.conf conf/local.test.conf
cp conf/local.logback.sample.xml conf/local.logback.xml
cp conf/play-authenticate/mine.conf.sample conf/play-authenticate/mine.local.conf
cp conf/play-authenticate/smtp.conf.sample conf/play-authenticate/smtp.local.conf
```

### Configuring setup files

Main configuration file of the project **local.conf**.

```
vim conf/local.conf
```

Make sure that **Evolutions are disabled** (for production)

```
...
evolutions {
        enabled=false
        db {
            default {
                autoApply=false
            }
        }
    }
...
```
Make sure that the database is configured like this, we use the default PostgreSQL user and the password we configure before

```
...
db {
    default {
        driver=org.postgresql.Driver
        url="jdbc:postgresql://localhost:5432/appcivistcore"
        username="postgres"
        password="postgres"
    }
}
...
```

In **application.baseUrl** we can setup our domain name or `localhost`
```
...
application {
    baseUrl="http://www.example.com:9000/"
...
```

Change **appcivist.invitations.baseUrl** to point to the base URL of the frontend prototype to use.
```
...
invitations {
        baseUrl = "http://www.example.com/"
    }
...
```

Replace the configuration values that start with `"${?*"` with required credentials for IMGUR, Amazon S3 and MapBox API.

We need to get Google credentials to use reCaptcha service.

* ReCaptcha credentials: get them here https://www.google.com/recaptcha/

Add credentials in the file

```
recaptcha {
           secret = "secret code for recaptcha service"
           serverURL = "https://www.google.com/recaptcha/api/siteverify"
        }
```


**Save all the changes** on local.conf and exit.

Open **mine.local.conf**

```
vim conf/play-authenticate/mine.local.conf
```

Search for `email="youremail@example.com"` and change it with your own

We need to get Google and Facebook credentials to use their services.

* Google credentials: get them here https://code.google.com/apis/console
* Facebook credentials: get them here https://developers.facebook.com/apps

Then we add those credentials in the file
```
clientId=""
clientSecret=""
```

**Save the changes and exit**

Open **smtp.local.conf**

```
vim conf/play-authenticate/smtp.local.conf
``` 

Search for

`user="youremail@gmail.com"` and replace it with your own.

`password="yourpassword@gmail.com"` and replace it with your own.

`email="you@gmail.com"` and replace it with your own.

`name="Your Name"` and replace it with your own.


## Running the server

Run the server using the following commands

```
cd
cd appcivist-platform/
./activator
run -Dconfig.resource=local.conf -Dlogger.file=conf/local.logback.xml
```
On your browser, go to 

http://www.example.com:9000/api/doc 

To visit the documentation of the API endpoints and have try them out with real examples.
