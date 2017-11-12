# Social Login

Documentation about how use the social login from the UI

- Call the service GET /api/authenticate/providers
### Notification Status Example
```javascript
{
[
    {
        "url": "/api/authenticate/password",
        "key": "password"
    },
    {
        "url": "/api/authenticate/facebook",
        "key": "facebook"
    }
]
}
```

- For each result, create an href (with the url attribute) with an optional icon using the key attribute to determine 
which icon show