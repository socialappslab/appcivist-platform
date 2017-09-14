# Notification Endpoints 

This is a base documentation for the API of all the notifications endpoints for its use as a guide in the appcivist-pb-client


HTTP Method | Base URL | Description | Path Params | Query Params | Body | Response
| ------ | ------ | ------ | ------ | ------ | ------ | ------ |
| GET | /api/user/:id/notifications/page=0&pageSize=5 | Return notifications signals for user :id | id: User id | page: page number pageSize:number of rows to return | -- | [notification list example] (#notification-list-example) |
| GET | /api/user/:id/notifications/stats  | Return  the total number of notifications reading or pending for user :id | id: User id | -- | -- | [notification-stats] (#notification-stats-example) |
| PUT |/api/user/:id/notifications/:nid/read | Update notification signal :nid to read | id: User id  nid: NotificationEventSignal id  | -- | -- | --|
| PUT | /api/user/:id/notifications/read | Update all unread notifications to read | user id | 

### Notification Status Example
```javascript
{
    "read": 5,
    "unread": 6,
    "total": 11,
    "pages": 2
}
```

### Notification List Example
```javascript
{
    "pageSize": 5,
    "page": 0,
    "total": 11,
    "list": [
        {
            "creation": "2017-09-04 22:35 PM GMT",
            "lastUpdate": "2017-09-09 19:10 PM GMT",
            "lang": "en",
            "removed": false,
            "id": 1,
            "read": true,
            "signal": {
                "creation": "2017-09-04 22:35 PM GMT",
                "lastUpdate": "2017-09-04 22:35 PM GMT",
                "lang": "en",
                "removed": false,
                "id": 32,
                "spaceType": "CAMPAIGN",
                "signalType": "REGULAR",
                "eventId": "NEW_CONTRIBUTION_IDEA",
                "title": "[AppCivist] New IDEA in Belleville",
                "data": {
                    "origin": "e1998630-4079-11e5-a151-feff819cdc9f",
                    "signaled": true,
                    "eventName": "NEW_CONTRIBUTION_IDEA",
                    "originName": "Belleville ",
                    "originType": "CAMPAIGN",
                    "resourceText": "<p>this is my idea</p>",
                    "resourceType": "IDEA",
                    "resourceUUID": "b5b85b38-7961-41c7-b059-6d7095154c15",
                    "resourceTitle": "Testing idea3",
                    "associatedUser": "Cristhian Parra",
                    "notificationDate": 1504564549501
                }
            }
        }
    ]
```

