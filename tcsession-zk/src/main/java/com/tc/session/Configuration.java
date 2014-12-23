
package com.tc.session;

import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * TODO 类的功能描述。
 * 
 * @author gaofeng
 * @date Sep 12, 2013 4:31:52 PM
 * @id $Id$
 */
public class Configuration {
    
    private static final Logger log = LoggerFactory.getLogger(Configuration.class);
    
    private static final String config_file = "session.properties";
    
    /** zk服务器地址 */
    public static String SERVERS;
    
    /** 初始连接数 */
    public static String MAX_IDLE;
    
    /** zk连接池中，最小的空闲连接数 */
    public static String INIT_IDLE_CAPACITY;
    
    /** session的生命周期 单位分钟 */
    public static String SESSION_TIMEOUT;
    
    /** 和zk服务器建立的连接的超时时间 单位秒 */
    public static String CONNECTION_TIMEOUT;
    
    /** 检查任务的启动周期 */
    public static String TIMEOUT_CHECK_INTERVAL;
    
    static {
        InputStream in = Configuration.class.getClassLoader().getResourceAsStream(config_file);
        Properties props = new Properties();
        try {
            if (in != null) {
                props.load(in);
            }
            SERVERS = props.getProperty("tc.session.servers");
            MAX_IDLE = props.getProperty("tc.session.max_idle");
            INIT_IDLE_CAPACITY = props.getProperty("tc.session.init_idle_capacity");
            SESSION_TIMEOUT = props.getProperty("tc.session.session_timeout");
            CONNECTION_TIMEOUT = props.getProperty("tc.session.connection_timeout");
            TIMEOUT_CHECK_INTERVAL = props.getProperty("tc.session.timeout_check_interval");
        } catch (Exception e) {
            log.error("读取session配置文件时出错", e);
        }
    }
    
}
