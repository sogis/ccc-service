package ch.so.agi.cccprobe;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class SimpleClient {

    public static void main(String argv[]) throws Exception {
        String sentence;
        String modifiedSentence;
        BufferedReader inFromUser = new BufferedReader(new InputStreamReader(System.in));

        Socket clientSocket = new Socket("localhost", 8080);
        DataOutputStream outToServer = new DataOutputStream(clientSocket.getOutputStream());
        BufferedReader inFromServer = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

        sentence = "{\"method\":\"appConnect\"," +
                "    \"session\":{\"sessionId\":\"{E9-TRALLALLA-UND-BLA-BLA-BLA-666}\"}," +
                "    \"clientName\":\"Axioma Mandant AfU\"," +
                "    \"apiVersion\":\"1.0\"}";
        outToServer.writeBytes(sentence + '\n');
        modifiedSentence = inFromServer.readLine();
        System.out.println(modifiedSentence);
        clientSocket.close();
    }
}