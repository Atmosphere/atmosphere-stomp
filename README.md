### [STOMP](http://en.wikipedia.org/wiki/Streaming_Text_Oriented_Messaging_Protocol) implementation for the [Atmosphere Framework](https://github.com/Atmosphere/atmosphere)

#### To download Atmosphere STOMP, add the following dependency:
```xml
     <dependency>
         <groupId>org.atmosphere</groupId>
         <artifactId>atmosphere-stomp</artifactId>
         <version>0.1</version>
      </dependency>
```

### Demo

Check out our super simple [demo](https://github.com/Atmosphere/atmosphere-samples/tree/master/stomp) to get started. File issues, do pull requests to help this community. Have questions? Post them [here](https://groups.google.com/group/atmosphere-framework?pli=1)

### Quick start

Using STOMP protocol over Atmosphere is very easy.

#### Server side code

```java
@StompEndpoint
public class StompBusinessService {

    // Invoked when someone push a 'SEND' frame to the '/stomp-destination' destination
    @StompService(destination = "/stomp-destination")
    @Message(encoders = { MyEncoder.class }, decoders = {MyDecoder.class })
    public MyDto doStuff(final MyDto dto) {
        MyDto retval = ...
    
        // Broadcast the result to all '/stomp-destination' subscribers
        return retval;
    }
}
```

#### Client side code

```javascript

// Build atmosphere request object as usual
var request = { {
    url: document.location.protocol + "//" + document.location.host + '/stomp',
    ...
};

// We use Stomp.js here
var client = Stomp.over(new $.atmosphere.WebsocketApiAdapter(request));

// Bind a callback to a subscription
client.subscribe("/stomp-destination", function(e) {
    ...
});

// Send data to the destination
var myDto = { ... };
client.send("/stomp-destination", {}, myDto);

```
