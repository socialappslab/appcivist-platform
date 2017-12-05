# How to create Swagger based documentation on a Static HTML file

All you need to generate the static HMTL is the JSON file created by Swagger


## 1. Swaager-UI

I preffer using the pre-built docker image directly from Dockerhub:

```
docker pull swaggerapi/swagger-ui
```

The next step will run the docker image for production,

The base URL of the web application can be changed by specifying the `BASE_URL` environment variable:

```
docker run -d -p 80:8080 -e BASE_URL=/api/ -e SWAGGER_JSON=/foo/doc.json -v /bar:/foo swaggerapi/swagger-ui
```

> This will serve Swagger UI at ´/api´ . 

More info about Swagger-UI [here](https://github.com/swagger-api/swagger-ui).

## 2. Spectacles

Simply install Spectacle from `npm`  typing:

``` 
npm install -g spectacle-docs
```

Next you need to pass the JSON file to generate the HTML typing:

```
spectacle -d doc.json
```

The documentation will be located in the public directory by default.

More info about this package [here](https://github.com/sourcey/spectacle).


## 3. Bootprint

Install the package usin `npm`typing:

```
npm install -g bootprint
npm install -g bootprint-openapi
```

Now you can use the command 

```
bootprint openapi https://platform.appcivist.org/api/doc.json target
```

To create a single file we can use the `npm` package `html-inline` describing the usage next.



More info about this package [here](https://www.npmjs.com/package/bootprint-swagger).

### HTML-Inline

Install the package:

```
npm -g install html-inline
```

Now we type the next command to create

```
html-inline target/index.htm  >> api-docs-bootprint.html
```
