# Message format send to RabbitMQ

The messages are sent following the following configurations defined in the local.conf file:
```
appcivist {
    rabbitmq {
        host = "localhost"
        port = 5672
        user = "guest"
        password = "guest"
    }
  }
```

A queue is created for each user subscribed to the notification, with the user uuid as the name of the queue. 
The structure of the sent message is:

```json
{
  "resourceSpaceUUID":"e199839c-4079-11e5-a151-feff819cdc9f",
  "text":"DISCUSSION of ASSEMBLY {2} was updated by Jeff",
  "title":"[AppCivist] Updated DISCUSSION in Assemblèe Belleville"
}
```