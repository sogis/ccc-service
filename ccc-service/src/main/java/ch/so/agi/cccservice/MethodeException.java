package ch.so.agi.cccservice;

public class MethodeException extends Throwable {
    public MethodeException(String s) {
        System.out.println(s);
        //Vorläufig wird die Fehlermeldung einfach so ausgegeben. Später kann sie dann z.B. dem Gis- oder App-Client zurückgegeben werden.
    }
}
