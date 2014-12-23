
package com.tc.session;

import java.util.NoSuchElementException;

import org.apache.commons.lang3.math.NumberUtils;
import org.apache.commons.pool.ObjectPool;
import org.apache.commons.pool.PoolableObjectFactory;
import org.apache.commons.pool.impl.StackObjectPool;
import org.apache.zookeeper.ZooKeeper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * ZK实例池管理器
 * 
 * @author gaofeng
 * @date 2013-5-24
 */
public class ZookeeperPoolManager {
    
    private static final Logger log = LoggerFactory.getLogger(ZookeeperPoolManager.class);
    
    /** 单例 */
    protected static ZookeeperPoolManager instance;
    
    private ObjectPool pool;
    
    /**
     * 构造方法
     */
    private ZookeeperPoolManager() {
    
        PoolableObjectFactory factory = new ZookeeperPoolableObjectFactory();
        
        // 初始化ZK对象池
        int maxIdle = NumberUtils.toInt(Configuration.MAX_IDLE);
        int initIdleCapacity = NumberUtils.toInt(Configuration.INIT_IDLE_CAPACITY);
        pool = new StackObjectPool(factory, maxIdle, initIdleCapacity);// 对象构建池
        // 初始化池
        for (int i = 0; i < initIdleCapacity; i++) {
            try {
                pool.addObject();
            } catch (IllegalStateException ex) {
                log.error("Zookeeper连接池初始化发生异常。", ex);
            } catch (UnsupportedOperationException ex) {
                log.error("Zookeeper连接池初始化发生异常。", ex);
            } catch (Exception ex) {
                log.error("Zookeeper连接池初始化发生异常。", ex);
            }
        }
        if (log.isInfoEnabled()) {
            log.info("Zookeeper连接池初始化完成");
        }
    }
    
    /**
     * 返回单例的对象
     * 
     * @return
     */
    public static synchronized ZookeeperPoolManager getInstance() {
    
        if (instance == null) {
            instance = new ZookeeperPoolManager();
        }
        return instance;
    }
    
    /**
     * 将ZK对象从对象池中取出
     * 
     * @return
     */
    public ZooKeeper borrowObject() {
    
        if (pool != null) {
            try {
                if(pool.getNumActive() > 40){
                    log.warn("超出连接数");
                    return null;
                }
                ZooKeeper zk = (ZooKeeper) pool.borrowObject();
                if (log.isDebugEnabled()) {
                    log.debug("从Zookeeper连接池中返回连接，zk.sessionId=" + zk.getSessionId());
                }
                return zk;
            } catch (NoSuchElementException ex) {
                log.error("从Zookeeper连接池获取连接时发生异常：", ex);
            } catch (IllegalStateException ex) {
                log.error("从Zookeeper连接池获取连接时发生异常：", ex);
            } catch (Exception e) {
                log.error("从Zookeeper连接池获取连接时发生异常：", e);
            }
        }
        return null;
    }
    
    /**
     * 将ZK实例返回对象池
     * 
     * @param zk
     */
    public void returnObject(ZooKeeper zk) {
    
        if (pool != null && zk != null) {
            try {
                pool.returnObject(zk);
                if (log.isDebugEnabled()) {
                    log.debug("将连接返回Zookeeper连接池完毕，zk.sessionId=" + zk.getSessionId());
                }
            } catch (Exception ex) {
                log.error("将连接返回Zookeeper连接池时发生异常：", ex);
            }
        }
    }
    
    /**
     * 关闭对象池
     */
    public void close() {
        if (pool != null) {
            try {
                pool.close();
                if (log.isInfoEnabled()) {
                    log.info("关闭Zookeeper连接池完成");
                }
            } catch (Exception ex) {
                log.error("关闭Zookeeper连接池时发生异常：", ex);
            }
        }
    }
}
