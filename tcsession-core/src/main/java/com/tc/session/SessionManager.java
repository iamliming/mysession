package com.tc.session;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 
 * Session管理器
 *
 * @author gaofeng
 * @date Sep 13, 2013 9:33:27 AM
 * @id $Id$
 */
public interface SessionManager {
    
    /**Cookie的过期时间，默认1年*/
    public static final int COOKIE_EXPIRY = 365 * 24 * 60 * 60;

    /**
     * 返回指定ID的HttpSession对象
     * @param id Session ID
     * @param request HTTP请求
     * @return
     */
    public HttpSession getHttpSession(String id, HttpServletRequest request);

    /**
     * 创建一个新的HttpSession对象
     * @param request HTTP请求
     * @return
     */
    public HttpSession newHttpSession(HttpServletRequest request, HttpServletResponse response);

    /**
     * 从Cookie中获取请求对象的SessionID
     * @param request HTTP请求
     * @return
     */
    public String getRequestSessionId(HttpServletRequest request);

    /**
     * 将一个HttpSession对象放入管理容器中
     * @param session HTTP Session对象
     * @param request HTTP请求
     */
    public void addHttpSession(TCSession session);

    /**
     * 删除Session
     * @param session
     */
    public void removeHttpSession(TCSession session);

    /**
     * 返回一个唯一的Session ID
     * @return
     */
    public String getNewSessionId(HttpServletRequest request);

    /**
     * 返回Servlet上下文
     * @return
     */
    public ServletContext getServletContext();

    /**
     * 设置Servlet上下文
     * @param sc
     */
    public void setServletContext(ServletContext sc);

    /**
     * 获取Session客户端接口
     *
     * @author gaofeng
     * @date Sep 12, 2013 8:23:20 PM
     *
     * @return
     */
    public SessionClient getSessionClient();
    
    /**
     * 
     * 关闭管理器，做清理
     *
     * @author gaofeng
     * @throws Exception 
     * @date Sep 17, 2013 8:05:04 PM
     *
     */
    public void close() throws Exception;
    
}
