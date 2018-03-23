package ch.so.agi.cccservice;

import java.io.IOException;

public class MissingArgumentException extends IOException {
    public MissingArgumentException(String s) {
        System.out.println(s);
        //Vorläufig wird die Fehlermeldung einfach so ausgegeben. Später kann sie dann z.B. dem Gis- oder App-Client zurückgegeben werden.
    }
}
