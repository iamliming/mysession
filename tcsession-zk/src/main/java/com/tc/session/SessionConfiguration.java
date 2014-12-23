
package com.tc.session;

import java.util.Properties;

import org.apache.commons.lang3.StringUtils;

import com.tc.trinity.core.AbstractConfigurable;
import com.tc.trinity.core.ConfigContext;

/**
 * TODO 类的功能描述。
 *
 * @author kozz.gaof
 * @date Sep 16, 2014 5:19:23 PM
 * @id $Id$
 */
public class SessionConfiguration extends AbstractConfigurable {
    
    @Override
    public String getName() {
    
        return "tcsession";
    }
    
    @Override
    public String getExtensionName() {
    
        return this.getClass().getName();
    }
    
    @Override
    protected boolean doInit(ConfigContext context, Properties properties) {
    
        try {
            Class.forName("com.tc.session.Configuration", false, this.getClass().getClassLoader());
            if (StringUtils.isBlank(Configuration.SERVERS)) {
                Configuration.SERVERS = properties.getProperty("tc.session.servers");
            }
            if (StringUtils.isBlank(Configuration.MAX_IDLE)) {
                Configuration.MAX_IDLE = properties.getProperty("tc.session.max_idle");
            }
            if (StringUtils.isBlank(Configuration.INIT_IDLE_CAPACITY)) {
                Configuration.INIT_IDLE_CAPACITY = properties.getProperty("tc.session.init_idle_capacity");
            }
            if (StringUtils.isBlank(Configuration.SESSION_TIMEOUT)) {
                Configuration.SESSION_TIMEOUT = properties.getProperty("tc.session.session_timeout");
            }
            if (StringUtils.isBlank(Configuration.CONNECTION_TIMEOUT)) {
                Configuration.CONNECTION_TIMEOUT = properties.getProperty("tc.session.connection_timeout");
            }
            if (StringUtils.isBlank(Configuration.TIMEOUT_CHECK_INTERVAL)) {
                Configuration.TIMEOUT_CHECK_INTERVAL = properties.getProperty("tc.session.timeout_check_interval");
            }
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    @Override
    protected boolean doOnChange(String key, String originalValue, String value) {
    
        // TODO Auto-generated method stub
        return false;
    }
    
}
