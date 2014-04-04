package org.atmosphere.stomp.protocol;

public interface StompFormat {

    Frame parse(String str);

    String format(Frame msg);
}
