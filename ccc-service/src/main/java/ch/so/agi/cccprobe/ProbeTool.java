package ch.so.agi.cccprobe;

import ch.so.agi.cccservice.Service;
import ch.so.agi.cccservice.SessionId;
import ch.so.agi.cccservice.messages.ConnectAppMessage;
import ch.so.agi.cccservice.messages.ConnectGisMessage;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;

/**
 * Main class of the tool for testing the liveness of the ccc-service.
 * The liveness is checked, by executing a basic happy flow of the ccc protocol.
 */
public class ProbeTool {

    public static final String GIS_CLIENT_NAME = "ProbeTool GIS";
    public static final String CCC_PROTOCOL_VERSION = "1.0";
    public static final String APP_CLIENT_NAME = "ProbeTool APP";
    private static final String DEFAULT_ENDPOINT = "ws://localhost:8080/ccc-service";
    Logger logger = LoggerFactory.getLogger(ProbeTool.class);
    private AppClientHandler appSessionHandler;
    private AppClientHandler gisSessionHandler;

    public static void main(String[] args) throws Exception {
        
        int exitCode=new ProbeTool().mymain(args);
        System.exit(exitCode);
    }
    private static void alternativeMainToTestConcurrentUseCases(String[] args) throws Exception {
        
        for(int i=0;i<10;i++) {
            Thread thread=new Thread(new Runnable() {
                Logger logger = LoggerFactory.getLogger(ProbeTool.class);
                public int nr=0;

                @Override
                public void run() {
                    try {
                        int id=Integer.parseInt(Thread.currentThread().getName().substring("ProbeTool-".length()));
                        ProbeTool tool=new ProbeTool();
                        tool.mymain(new String[] {});
                        TimeUnit.SECONDS.sleep(2+60);
                        if(id<9) {
                            TimeUnit.SECONDS.sleep(60);
                        }
                        tool.close();
                    } catch (Exception e) {
                        logger.error("ProbeTool failed",e);
                    }
                }
                
            },"ProbeTool-"+i);
            thread.start();
        }
    }
    public int mymain(String[] args) throws Exception {
        final SessionId sessionId = new SessionId("{"+UUID.randomUUID().toString()+"}");
        StandardWebSocketClient appClient = new StandardWebSocketClient();
        appSessionHandler = new AppClientHandler(APP_CLIENT_NAME);
        String endpoint=DEFAULT_ENDPOINT;
        
        if(args.length==1) {
            endpoint=args[0];
        }
        appClient.doHandshake(appSessionHandler,endpoint);

        ConnectAppMessage appConnectMessage = new ConnectAppMessage();
        appConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        appConnectMessage.setSession(sessionId);
        appConnectMessage.setClientName(APP_CLIENT_NAME);

        Thread.sleep(2000);
        if (appSessionHandler.isConnected()) {
            appSessionHandler.sendMessage(appConnectMessage);
        }
        else {
            logger.error("Session not open. Could not send "+appConnectMessage.getMethod());
        }


        StandardWebSocketClient gisClient = new StandardWebSocketClient();
        gisSessionHandler = new AppClientHandler(GIS_CLIENT_NAME);

        gisClient.doHandshake(gisSessionHandler,endpoint);

        ConnectGisMessage gisConnectMessage = new ConnectGisMessage();
        gisConnectMessage.setApiVersion(CCC_PROTOCOL_VERSION);
        gisConnectMessage.setSession(sessionId);
        gisConnectMessage.setClientName(GIS_CLIENT_NAME);

        Thread.sleep(2000);
        if (gisSessionHandler.isConnected()) {
            gisSessionHandler.sendMessage(gisConnectMessage);
        }
        else {
            logger.error("Session not open. Could not send "+gisConnectMessage.getMethod());
        }

        Thread.sleep(2000);
        if (gisSessionHandler.getAppReady() != null && gisSessionHandler.getAppReady() == true && appSessionHandler.getAppReady() != null && appSessionHandler.getAppReady() == true) {
            return 0;
        }

        return 1;

    }
    private void close() throws Exception {
        if(appSessionHandler!=null) {
            appSessionHandler.close();
        }
        if(gisSessionHandler!=null) {
            gisSessionHandler.close();
        }
    }
}