package ch.so.agi.cccservice;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** runs back ground tasks of the ccc.service.
 * Closes inactive sessions and sends heart beats (ping) to not so active sessions.
 *
 */
public class BackgroundService implements Runnable {
    private Logger logger = LoggerFactory.getLogger(BackgroundService.class);
    private SessionPool sessionPool;
    private long maxInactivityTime;
    private long pingIntervalTime;

    public BackgroundService(SessionPool sessionPool, long maxInactivityTime,long pingIntervalTime){
        this.sessionPool = sessionPool;
        this.maxInactivityTime=maxInactivityTime;
        this.pingIntervalTime=pingIntervalTime;
    }

    @Override
    public void run() {
        try {
            logger.debug("BackgroundService.run()...maxInactivityTime "+maxInactivityTime+", pingIntervalTime "+pingIntervalTime);
            synchronized(sessionPool){
                sessionPool.sendPingToInactiveSessions(pingIntervalTime);
            }
            synchronized(sessionPool){
                sessionPool.closeInactiveSessions(maxInactivityTime);
            }

        }
        catch (Exception e){
            logger.info("Exception!! " + e);
            logger.debug("  details: ", e);
        }
    }

}
