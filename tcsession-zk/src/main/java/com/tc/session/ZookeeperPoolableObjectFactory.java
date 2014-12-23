package com.tc.session;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.ZooKeeper.States;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Zookeeper实例对象池，由于一个Zookeeper实例持有一个Socket连接，所以将Zookeeper实例池化避免实例化过程中的消耗
 *
 * @author gaofeng
 * @date Sep 13, 2013 9:42:16 AM
 * @id $Id$
 */
public class ZookeeperPoolableObjectFactory implements PoolableObjectFactory {
    
    private static final Logger log = LoggerFactory.getLogger(ZookeeperPoolableObjectFactory.class);


    public ZooKeeper makeObject() throws Exception {
        //返回一个新的zk实例
        ZookeeperConnector cw = new ZookeeperConnector();

        //连接服务端
        String servers = Configuration.SERVERS;
        int timeout = NumberUtils.toInt(Configuration.CONNECTION_TIMEOUT) * 1000;
        ZooKeeper zk = cw.connect(servers, timeout);
        if (zk != null) {
            if (log.isInfoEnabled()) {
                log.info("实例化Zookeeper客户端对象，Zookeeper.sessionId=" + zk.getSessionId());
            }
            return zk;
        } else {
            log.warn("实例化Zookeeper客户端对象失败");
            cw.close();
            throw new Exception("Error in creating connection to ZooKeeper Server: " + servers);
        }
    }


    public void destroyObject(ZooKeeper obj) throws Exception {
        if (obj != null) {
            obj.close();
            if (log.isInfoEnabled()) {
                log.info("Zookeeper客户端对象被关闭，Zookeeper.sessionId=" + obj.getSessionId());
            }
        }
    }
    @Override
	public void destroyObject(Object obj) throws Exception {
		destroyObject((ZooKeeper)obj);
	}

    public boolean validateObject(ZooKeeper obj) {

		if (obj != null && obj.getState() == States.CONNECTED) {
            if (log.isDebugEnabled()) {
                log.debug("Zookeeper客户端对象验证通过，Zookeeper.sessionId=" + obj.getSessionId());
            }
            return true;
        }else{
            if (log.isInfoEnabled()) {
                log.info("Zookeeper客户端对象验证不通过，Zookeeper.sessionId=" + obj.getSessionId());
            }
            return false;
        }
	}

	public boolean validateObject(Object obj) {
		return validateObject((ZooKeeper)obj);
	}


	@Override
	public void activateObject(Object obj) throws Exception {
	}

	@Override
	public void passivateObject(Object obj) throws Exception {
	}
}

