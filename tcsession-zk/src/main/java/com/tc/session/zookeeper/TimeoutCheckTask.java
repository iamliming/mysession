
package com.tc.session.zookeeper;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.Configuration;
import com.tc.session.SessionClient;
import com.tc.session.SessionMetaData;

/**
 * 
 * session timeout检查任务
 * 
 * @author gaofeng
 * @date Sep 18, 2013 11:10:14 AM
 * @id $Id$
 */
public class TimeoutCheckTask implements Callable<Boolean> {
    
    private static final Logger log = LoggerFactory.getLogger(TimeoutCheckTask.class);
    
    private static long SLEEP_TIMEOUT;
    private SessionClient client;
    
    public TimeoutCheckTask() {
    
        SLEEP_TIMEOUT = NumberUtils.toInt(Configuration.TIMEOUT_CHECK_INTERVAL);
        client = ZookeeperSessionClient.getInstance();
    }
    
    @Override
    public Boolean call() throws Exception {
    
        while (true) {
            try {
                List<String> sessionIds = client.getSessions();
                if(sessionIds == null){
                    continue;
                }
                for(String sessionId : sessionIds){
                    SessionMetaData metadata = client.getSession(sessionId);
                    if(metadata == null)continue;
                    if (!metadata.isValid()) {
                        log.debug("Session invalid! " + metadata);
                        client.removeSession(sessionId);
                    }
                }
            } catch (Exception ex) {
                log.error("Session超时定时任务发生异常，", ex);
            }finally{
                TimeUnit.SECONDS.sleep(SLEEP_TIMEOUT);
            }
        }
    }
}
