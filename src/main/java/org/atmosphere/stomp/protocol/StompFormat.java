package org.atmosphere.stomp.protocol;

public interface StompFormat {

    Message parse(String str);

    String format(Message msg);
}
