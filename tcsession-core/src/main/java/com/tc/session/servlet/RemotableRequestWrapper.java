
package com.tc.session.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.SessionManager;

/**
 * 
 * 通过实现{@link javax.servlet.http.HttpServletRequestWrapper}完成其它调用的转发。<br />
 * 拦截getSession的两个方法
 * 
 * @author gaofeng
 * @date Sep 12, 2013 3:11:37 PM
 * @id $Id$
 */
public class RemotableRequestWrapper extends HttpServletRequestWrapper {
    
    protected Logger log = LoggerFactory.getLogger(getClass());
    private SessionManager sessionManager;
    private static final String NULL_SESSION = "__NULL_";
    private HttpServletResponse response;
    
    /**
     * 构造方法
     * 
     * @param request
     */
    public RemotableRequestWrapper(HttpServletRequest request, SessionManager sessionManager) {
    
        super(request);
        this.sessionManager = sessionManager;
    }
    
    /**
     * session拦截入口
     * <p>
     * 以Request生命周期为粒度进行缓存。更粗粒度的ThreadLocal级别缓存，在某些JavaEE应用服务器的实现上会出问题
     * 
     */
    @Override
    public HttpSession getSession(boolean create) {
    
        if (sessionManager == null) {
            throw new IllegalStateException("SessionManager not initialized...");
        }
        
        HttpServletRequest request = (HttpServletRequest) getRequest();
        Object o = request.getAttribute("tc.thisSession");
        if (!create && o != null) {
            return NULL_SESSION.equals(o.toString()) ? null : (HttpSession) o;
        }
        
        String sessionid = sessionManager.getRequestSessionId(request);
        HttpSession session = null;
        
        if (sessionid != null) {
            // 如果存在，则先从管理器中取
            session = sessionManager.getHttpSession(sessionid, request);
            if (session == null && !create) {
                request.setAttribute("tc.thisSession", NULL_SESSION);
                return null;
            }
        }
        // 否则实例化一个新的Session对象
        if (session == null && create) {
            session = sessionManager.newHttpSession(request, this.response);
            request.setAttribute("tc.tsid", session.getId());
        }
        request.setAttribute("tc.thisSession", session == null ? NULL_SESSION : session);
        
        return session;
    }
    
    @Override
    public HttpSession getSession() {
    
        return getSession(true);
    }
    
    public HttpServletResponse getResponse() {
    
        return response;
    }
    
    public void setResponse(HttpServletResponse response) {
    
        this.response = response;
    }
    
}
