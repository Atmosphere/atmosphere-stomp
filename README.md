Stomp for Atmosphere.

TBD:
- Use our own parser our use an existing own like activemq
- Validate the implementation proposal for stomp transaction mechanism. If yes:
    - Attach the transaction lifecycle to the AtmosphereResource inside core framework ?
    - Test the existence of a transaction inside base implementation of Broadcaster ?
- Activate stomp if dependency added to the classpath as usual
- If stomp activated, expect by default that all messages respect STOMP protocol (configurable ?)
- Support disconnect frame ? Why/how ?
- ACK/NACK related to BroadcasterCache ?
- Receipt uses case that justify its implementation ?
- Enable heart-beart transparently when HeartbeatInterceptor activated ?