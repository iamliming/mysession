
package com.tc.session.zookeeper;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.SerializationUtils;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.KeeperException.Code;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.SessionClient;
import com.tc.session.SessionMetaData;
import com.tc.session.ZookeeperPoolManager;

/**
 * 
 * Zookeeper的session访问客户端实现
 * 
 * @author gaofeng
 * @date Sep 13, 2013 9:17:40 AM
 * @id $Id$
 */
public class ZookeeperSessionClient implements SessionClient {
    
    /** ZK组节点名称 */
    public static final String GROUP_NAME = "/TC_SESSIONS";
    
    public static final String NODE_SEP = "/";
    
    /** 日志 */
    private static final Logger log = LoggerFactory.getLogger(ZookeeperSessionClient.class);
    
    /** 单例对象 */
    private static SessionClient instance;
    
    /** ZK对象池 */
    private ZookeeperPoolManager pool;
    
    /**
     * 构造方法
     */
    private ZookeeperSessionClient() {
    
        if (pool == null) {
            pool = ZookeeperPoolManager.getInstance();
        }
    }
    
    /**
     * 返回单例方法
     * 
     * @return
     */
    public static synchronized SessionClient getInstance() {
    
        if (instance == null) {
            instance = new ZookeeperSessionClient();
        }
        return instance;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public SessionMetaData getSession(String sessionid) {
    
        log.debug(">>> Try to get sessionid: " + sessionid);
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return null;
        }
        String path = GROUP_NAME + NODE_SEP + sessionid;
        
        byte[] data;
        try {
            data = zookeeper.getData(path, false, null);
            if (data == null) {
                log.debug(">>> Failed to get sessionid: " + sessionid);
                return null;
            }
            Object obj = SerializationUtils.deserialize(data);
            if (!(obj instanceof SessionMetaData)) {
                return null;
            }
            SessionMetaData metadata = (SessionMetaData) obj;
            return metadata;
        } catch (KeeperException e) {
            if (e.code() != Code.NONODE) // session可能被定期清理了，属于正常的业务情况
                log.error("Error in fetching session from ZooKeeper Server. id: " + sessionid, e);
        } catch (InterruptedException e) {
            log.error("Error in fetching session from ZooKeeper Server. id: " + sessionid, e);
        } finally {
            pool.returnObject(zookeeper);
        }
        return null;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean updateSession(SessionMetaData metadata) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return false;
        }
        
        try {
            // //设置当前版本号
            // metadata.setVersion(stat.getVersion());
            String path = GROUP_NAME + NODE_SEP + metadata.getId();
            metadata.setLastAccessTime(System.currentTimeMillis());
            zookeeper.setData(path, SerializationUtils.serialize(metadata), -1);
            if (log.isDebugEnabled()) {
                log.debug("更新Session节点的元数据完成[" + path + "]");
            }
            return true;
        } catch (KeeperException e) {
            if (e.code() != Code.NONODE) // session可能被定期清理了，属于正常的业务情况
                log.error("Error in fetching session from ZooKeeper Server. id: " + metadata.getId(), e);
        } catch (InterruptedException e) {
            log.error("Error in fetching session from ZooKeeper Server. id: " + metadata.getId(), e);
        } finally {
            pool.returnObject(zookeeper);
        }
        return false;
        
    }
    
    /**
     * 
     * 如果metadata不为空，建立对应的session节点。如果为空，则建立根节点
     */
    @Override
    public boolean createSession(SessionMetaData metadata) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return false;
        }
        
        String createPath = null;
        try {
            String path = metadata == null ? GROUP_NAME : (GROUP_NAME + NODE_SEP + metadata.getId());
            
            // 创建组件点
            byte[] arrData = null;
            if (metadata != null) {
                arrData = SerializationUtils.serialize(metadata);
            }
            createPath = zookeeper.create(path, arrData, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException e) {
            if (e.code() != Code.NODEEXISTS)
                log.error("Error in creating session, node already exists. metadata: " + metadata, e);
            return false;
        } catch (InterruptedException e) {
            log.error("Error in creating session. metadata: " + metadata, e);
            return false;
        } finally {
            pool.returnObject(zookeeper);
        }
        if (log.isDebugEnabled()) {
            log.debug("创建节点完成:[" + createPath + "]");
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setAttribute(String sessionid, String key, Serializable value) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return false;
        }
        
        String path = GROUP_NAME + NODE_SEP + sessionid + NODE_SEP + key;
        try {
            byte[] arrData = SerializationUtils.serialize(value);
            //先检查节点是否存在
            Stat stat = zookeeper.exists(path, false);
            if(stat == null){//若节点还不存在，先创建节点——Zookeeper不允许直接对一个不存在的节点进行setData操作
                zookeeper.create(path, null, Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            }
            zookeeper.setData(path, arrData, -1);
        } catch (KeeperException e) {
            if (e.code() != Code.NODEEXISTS)
                log.error("Error in setting attribute: sessionid " + sessionid + " key: " + key + " value: " + value, e);
            return false;
        } catch (InterruptedException e) {
            log.error("Error in setting attribute: sessionid " + sessionid + " key: " + key + " value: " + value, e);
            return false;
        } finally {
            pool.returnObject(zookeeper);
        }
        if (log.isDebugEnabled()) {
            log.debug("更新数据节点数据完成[" + path + "][" + value + "]");
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Object getAttribute(String sessionid, String key) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return null;
        }
        String path = GROUP_NAME + NODE_SEP + sessionid + NODE_SEP + key;
        
        Object obj = null;
        // 获取节点数据
        byte[] data;
        try {
            data = zookeeper.getData(path, false, null);
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE) {
                log.debug("Node doesn't exist. sessionid: " + sessionid + " key: " + key);
            } else {
                log.error("Error in getting attribute. sessionid " + sessionid + " key: " + key, e);
            }
            return null;
        } catch (InterruptedException e) {
            log.error("Error in getting attribute. sessionid " + sessionid + " key: " + key, e);
            return null;
        } finally {
            pool.returnObject(zookeeper);
        }
        if (data != null) {
            obj = SerializationUtils.deserialize(data);
        }
        return obj;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean removeAttribute(String sessionid, String key) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return false;
        }
        String path = GROUP_NAME + NODE_SEP + sessionid + NODE_SEP + key;
        try {
            zookeeper.delete(path, -1);
        } catch (InterruptedException e) {
            log.error("Error in removing attribute: sessionid " + sessionid + " key: " + key, e);
            return false;
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE) {
                log.debug("Node doesn't exist. sessionid: " + sessionid + " key: " + key);
            } else {
                log.error("Error in removing attribute: sessionid " + sessionid + " key: " + key, e);
            }
            return false;
        } finally {
            pool.returnObject(zookeeper);
        }
        return true;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public List<String> getAttributeNames(String sessionid) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return null;
        }
        String path = GROUP_NAME + NODE_SEP + sessionid;
        
        try {
            SessionMetaData metadata = this.getSession(sessionid);
            if (metadata == null || !metadata.isValid()) {
                return null;
            }
            return zookeeper.getChildren(path, false);
        } catch (KeeperException e) {
            log.error("Error in getting attribute name list: sessionid " + sessionid, e);
            return null;
        } catch (InterruptedException e) {
            log.error("Error in getting attribute name list: sessionid " + sessionid, e);
            return null;
        } finally {
            pool.returnObject(zookeeper);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, Object> removeSession(String sessionid) {
    
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return null;
        }
        String path = GROUP_NAME + NODE_SEP + sessionid;
        
        HashMap<String, Object> datas = new HashMap<String, Object>();
        try {
            List<String> nodes = zookeeper.getChildren(path, false);
            if (nodes != null) {
                for (String node : nodes) {
                    String dataPath = path + NODE_SEP + node;
                    // 获取数据
                    byte[] data = zookeeper.getData(dataPath, false, null);
                    try{
                        if (data != null) {
                            Object value = SerializationUtils.deserialize(data);
                            datas.put(node, value);
                        }
                    }catch(Exception ex){
                        log.warn("Error in deserializing.");
                    }

                    zookeeper.delete(dataPath, -1);
                }
            }
            zookeeper.delete(path, -1);
        } catch (InterruptedException e) {
            log.error("Error in removing session: sessionid " + sessionid, e);
            return null;
        } catch (KeeperException e) {
            if (e.code() == Code.NONODE) {
                log.warn("Try to delete Session, but node doesn't exist. sessionid: " + sessionid);
                return null;
            }
            log.error("Error in removing session: sessionid " + sessionid, e);
            return null;
        } finally {
            pool.returnObject(zookeeper);
        }
        if (log.isDebugEnabled()) {
            log.debug("删除Session节点完成:[" + path + "]");
        }
        return datas;
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public List<String> getSessions() {
        
        ZooKeeper zookeeper = pool.borrowObject();
        if (zookeeper == null) {
            log.error("从连接池中获取连接时，发生错误");
            return null;
        }
        String path = GROUP_NAME;
        
        try {
            return zookeeper.getChildren(path, false);
        } catch (KeeperException e) {
            log.error("Error in getting session id list... ", e);
            return null;
        } catch (InterruptedException e) {
            log.error("Error in getting session id list... ", e);
            return null;
        } finally {
            pool.returnObject(zookeeper);
        }
    }

    
}
