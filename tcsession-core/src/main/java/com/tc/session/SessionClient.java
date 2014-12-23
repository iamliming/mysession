package com.tc.session;

import java.io.Serializable;
import java.util.List;
import java.util.Map;



/**
 * session操作接口
 *
 * @author gaofeng
 * @date Sep 12, 2013 8:23:04 PM
 * @id $Id$
 */
public interface SessionClient {
   
    /**
     * 
     * 获取所有的sessionid列表
     *
     * @author gaofeng
     * @date Oct 30, 2013 10:11:11 AM
     *
     * @return
     */
    public List<String> getSessions(); 

   /**
    *
    *获取session对象
    *
    * @author gaofeng
    * @date Sep 13, 2013 10:21:44 AM
    *
    * @param sessionid
    * @param create
    * @return
    */
   public SessionMetaData getSession(String sessionid);
   
   /**
    * 
    * 更新session访问日期
    *
    * @author gaofeng
    * @date Sep 23, 2013 4:41:53 PM
    *
    * @param sessionid
    * @return
    */
   public boolean updateSession(SessionMetaData metadata);
   
   /**
    * 
    * 创建一个新的session节点
    *
    * @author gaofeng
    * @date Sep 17, 2013 1:55:10 PM
    *
    * @param metadata
    * @return
    */
   public boolean createSession(SessionMetaData metadata);
   
   /**
    * 
    * 增加session的新属性值对
    *
    * @author gaofeng
    * @date Sep 17, 2013 3:43:16 PM
    *
    * @param sessionid
    * @param key
    * @param value
    * @return
    */
   public boolean setAttribute(String sessionid, String key, Serializable value);
   
   /**
    * 
    * 获取session的属性值
    *
    * @author gaofeng
    * @date Sep 17, 2013 3:41:57 PM
    *
    * @return
    */
   public Object getAttribute(String sessionid, String key);
   
   /**
    * 
    * 删除session的指定key
    *
    * @author gaofeng
    * @date Sep 17, 2013 3:41:38 PM
    *
    * @param sessionid
    * @param key
    * @return
    */
   public boolean removeAttribute(String sessionid, String key);
   
   /**
    * 
    * 获取session的属性名称列表
    *
    * @author gaofeng
    * @date Sep 17, 2013 4:51:25 PM
    *
    * @param sessionid
    * @return
    */
   public List<String> getAttributeNames(String sessionid);
   
   /**
    * 
    * 移除session
    *
    * @author gaofeng
    * @date Sep 17, 2013 5:12:46 PM
    *
    * @param sessionid
    * @return session包含的键值对
    */
   public Map<String, Object> removeSession(String sessionid);
}
