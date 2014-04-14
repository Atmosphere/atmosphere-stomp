Stomp for Atmosphere.

TBD:
- Validate the implementation proposal for stomp transaction mechanism. If yes:
    - Attach the transaction lifecycle to the AtmosphereResource inside core framework ?
        JFA => +1. The protocol info should be removed so the resource acts like a normal request (without stomp)
    - Test the existence of a transaction inside base implementation of Broadcaster ?
- Activate stomp if dependency added to the classpath as usual
- If stomp activated, expect by default that all messages respect STOMP protocol (configurable ?)
   JFA => +1 for configurable
- Support disconnect frame ? Why/how ?
  JFA => yes we should support it. For that we will add a new Interceptor just for the Stomp Protocol
- ACK/NACK related to BroadcasterCache ?
  JFA => Agree it will need to be transparent.
- Receipt uses case that justify its implementation ?
  JFA => I think we should wait for that.
- Enable heart-beart transparently when HeartbeatInterceptor activated ?
  JFA => +1 for configurable
