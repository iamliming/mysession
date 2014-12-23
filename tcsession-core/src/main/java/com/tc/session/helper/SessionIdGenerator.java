
package com.tc.session.helper;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * SessionId生成器
 * 
 * @author gaofeng
 * @date Sep 18, 2013 2:04:12 PM
 * @id $Id$
 */
public class SessionIdGenerator {
    
    private static SessionIdGenerator instance;
    
    private final static String __NEW_SESSION_ID = "tc.sessionid";
    
    protected final static String SESSION_ID_RANDOM_ALGORITHM = "SHA1PRNG";
    
    protected final static String SESSION_ID_RANDOM_ALGORITHM_ALT = "IBMSecureRandom";
    
    private Logger log = LoggerFactory.getLogger(getClass());
    
    protected Random random;
    
    private boolean weakRandom;
    
    private SessionIdGenerator() {
    
        if (random == null) {
            try {
                random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM);
            } catch (NoSuchAlgorithmException e) {
                try {
                    random = SecureRandom.getInstance(SESSION_ID_RANDOM_ALGORITHM_ALT);
                    weakRandom = false;
                } catch (NoSuchAlgorithmException e_alt) {
                    log.warn("获取随机数生成器时出错", e);
                    random = new Random();
                    weakRandom = true;
                }
            }
        }
        random.setSeed(random.nextLong() ^ System.currentTimeMillis()
                ^ hashCode() ^ Runtime.getRuntime().freeMemory());
    }
    
    public static synchronized SessionIdGenerator getInstance() {
    
        if (instance == null) {
            instance = new SessionIdGenerator();
        }
        return instance;
    }
    
    public synchronized String newSessionId(HttpServletRequest request) {
    
        // A requested session ID can only be used if it is in use already.
        String requestedId = request.getRequestedSessionId();
        
        if (requestedId != null) {
            return requestedId;
        }
        
        // Else reuse any new session ID already defined for this request.
        String newId = (String) request.getAttribute(__NEW_SESSION_ID);
        if (newId != null) {
            return newId;
        }
        
        // pick a new unique ID!
        String id = null;
        while (id == null || id.length() == 0) {
            long r = weakRandom ? (hashCode()
                    ^ Runtime.getRuntime().freeMemory() ^ random.nextInt() ^ (((long) request
                    .hashCode()) << 32)) : random.nextLong();
            r ^= System.currentTimeMillis();
            if (request != null && request.getRemoteAddr() != null)
                r ^= request.getRemoteAddr().hashCode();
            if (r < 0)
                r = -r;
            id = Long.toString(r, 36);
        }
        
        request.setAttribute(__NEW_SESSION_ID, id);
        return id;
    }
}
