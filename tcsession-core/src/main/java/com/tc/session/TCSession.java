
package com.tc.session;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionBindingEvent;
import javax.servlet.http.HttpSessionBindingListener;
import javax.servlet.http.HttpSessionContext;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * HttpSession的代理类。实际的操作通过远端访问完成
 * 
 * @author gaofeng
 * @date Sep 12, 2013 4:12:46 PM
 * @id $Id$
 */
@SuppressWarnings("deprecation")
public class TCSession implements HttpSession {
    
    private static final Logger log = LoggerFactory.getLogger(TCSession.class);
    
    /** Session管理器 */
    private SessionManager sessionManager;
    /** Session ID */
    private String id;
    /** Session创建时间 */
    private long createTime;
    /** Session最后一次访问时间 */
    private long lastAccessTime;
    /** Session的最大空闲时间间隔 */
    private int maxInactiveInterval;
    /** 是否是新建Session */
    private boolean isNew;
    
    private HttpServletRequest request;
    
    private static final String NULL_VALUE = "__NULL_";

    /**
     * 构造方法,指定ID
     * 
     * @param sessionManager
     * @param id
     */
    public TCSession(SessionManager sessionManager, String id, HttpServletRequest request) {
        this.sessionManager = sessionManager;
        this.createTime = System.currentTimeMillis();
        this.lastAccessTime = this.createTime;
        this.isNew = true;
        this.request = request;
        this.id = id;
    }
    
    @Override
    public long getCreationTime() {
    
        return createTime;
    }
    
    @Override
    public String getId() {
    
        return id;
    }
    
    @Override
    public long getLastAccessedTime() {
    
        return lastAccessTime;
    }
    
    @Override
    public ServletContext getServletContext() {
    
        return sessionManager.getServletContext();
    }
    
    @Override
    public void setMaxInactiveInterval(int interval) {
    
        this.maxInactiveInterval = interval;
    }
    
    @Override
    public int getMaxInactiveInterval() {
    
        return maxInactiveInterval;
    }
    
    @Override
    public HttpSessionContext getSessionContext() {
    
        return null;
    }
    
    @Override
    public Object getValue(String name) {
    
        return getAttribute(name);
    }
    
    @Override
    public void putValue(String name, Object value) {
    
        setAttribute(name, value);
    }
    
    @Override
    public void removeValue(String name) {
    
        removeAttribute(name);
    }
    
    public HttpServletRequest getRequest() {
    
        return request;
    }
    
    public void setRequest(HttpServletRequest request) {
    
        this.request = request;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public String[] getValueNames() {
    
        List<String> names = new ArrayList<String>();
        Enumeration n = getAttributeNames();
        while (n.hasMoreElements()) {
            names.add((String) n.nextElement());
        }
        return names.toArray(new String[] {});
    }
    
    @Override
    public boolean isNew() {
    
        return isNew;
    }
    
    /**
     * 被访问
     */
    public void access() {
    
        this.isNew = false;
        this.lastAccessTime = System.currentTimeMillis();
    }
    
    /**
     * 触发Session的事件
     * 
     * @param value
     */
    protected void fireHttpSessionBindEvent(String name, Object value) {
    
        // 处理Session的监听器
        if (value != null && value instanceof HttpSessionBindingListener) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            ((HttpSessionBindingListener) value).valueBound(event);
        }
    }
    
    /**
     * 触发Session的事件
     * 
     * @param value
     */
    protected void fireHttpSessionUnbindEvent(String name, Object value) {
    
        // 处理Session的监听器
        if (value != null && value instanceof HttpSessionBindingListener) {
            HttpSessionBindingEvent event = new HttpSessionBindingEvent(this, name, value);
            ((HttpSessionBindingListener) value).valueUnbound(event);
        }
    }
    
    public boolean isValid() {
    
        return lastAccessTime + maxInactiveInterval > System.currentTimeMillis();
    }
    
    @Override
    public Object getAttribute(String name) {
    
        access();
        Object v = request.getAttribute(this.id + "_" + name);
        if (v != null) {
            return NULL_VALUE.equals(v.toString()) ? null : v;
        }
        // 获取session ID
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            Object o = null;
            // 返回Session节点下的数据
            try {
                SessionClient client = sessionManager.getSessionClient();
                o = client.getAttribute(id, name);
                request.setAttribute(this.id + "_" + name, o == null ? NULL_VALUE : o);
                return o;
            } catch (NullPointerException ex) {
                if (ex.getStackTrace().length > 0 && ex.getStackTrace()[0].getClassName().equals("org.apache.catalina.connector.Request")) { // Tomcat-6.0.37版本，有时会出现这个异常，原因不明。但不影响主流程。
                    log.warn(ex.getStackTrace()[0].getClassName() + "." + ex.getStackTrace()[0].getMethodName() + " sucks here");
                } else {
                    log.error("调用getAttribute方法时发生异常，", ex);
                }
            } catch (Exception ex) {
                log.error("调用getAttribute方法时发生异常，", ex);
            }
            return o;
        }
        
        return null;
    }
    
    @SuppressWarnings("rawtypes")
    @Override
    public Enumeration getAttributeNames() {
    
        access();
        // 获取session ID
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            // 返回Session节点下的数据名字
            try {
                SessionClient client = sessionManager.getSessionClient();
                List<String> names = client.getAttributeNames(id);
                if (names != null) {
                    return Collections.enumeration(names);
                }
            } catch (Exception ex) {
                log.error("调用getAttributeNames方法时发生异常，", ex);
            }
        }
        return null;
    }
    
    @Override
    public void setAttribute(String name, Object value) {
    
        // 没有实现序列化接口的直接返回
        if (!(value instanceof Serializable)) {
            log.warn("对象[" + value + "]没有实现Serializable接口，无法保存到分布式Session中");
            return;
        }
        access();
        // 获取session ID
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            // 将数据添加到ZooKeeper服务器上
            try {
                SessionClient client = sessionManager.getSessionClient();
                client.setAttribute(id, name, (Serializable) value);
                this.request.setAttribute(this.id + "_" + name, value);
            } catch (NullPointerException ex) {
                if (ex.getStackTrace().length > 0 && ex.getStackTrace()[0].getClassName().equals("org.apache.catalina.connector.Request")) {// Tomcat-6.0.37版本，有时会出现这个异常，原因不明。但不影响主流程。
                    log.warn(ex.getStackTrace()[0].getClassName() + "." + ex.getStackTrace()[0].getMethodName() + " sucks here");
                } else {
                    log.error("调用getAttribute方法时发生异常，", ex);
                }
            } catch (Exception ex) {
                log.error("调用setAttribute方法时发生异常，", ex);
            }
        }
        // 处理Session的监听器
        fireHttpSessionBindEvent(name, value);
    }
    
    @Override
    public void removeAttribute(String name) {
    
        access();
        Object value = null;
        // 获取session ID
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            // 删除Session节点下的数据
            try {
                SessionClient client = sessionManager.getSessionClient();
                client.removeAttribute(id, name);
                this.request.setAttribute(this.id + "_" + name, NULL_VALUE);
            } catch (Exception ex) {
                log.error("调用removeAttribute方法时发生异常，", ex);
            }
        }
        // 处理Session的监听器
        fireHttpSessionUnbindEvent(name, value);
    }
    
    @Override
    public void invalidate() {
    
        // 获取session ID
        String id = getId();
        if (StringUtils.isNotBlank(id)) {
            // 删除Session节点
            try {
                SessionClient client = sessionManager.getSessionClient();
                Map<String, Object> sessionMap = client.removeSession(id);
                if (sessionMap != null && sessionMap.size() > 0) {
                    Set<String> keys = sessionMap.keySet();
                    for (String key : keys) {
                        Object value = sessionMap.get(key);
                        fireHttpSessionUnbindEvent(key, value);
                    }
                }
            } catch (Exception ex) {
                log.error("调用invalidate方法时发生异常，", ex);
            }
        }
        // 删除本地容器中的Session对象
        sessionManager.removeHttpSession(this);
    }

}
