
package com.tc.session.helper;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Cookie读写
 *
 * @author gaofeng
 * @date Sep 12, 2013 3:29:50 PM
 * @id $Id$
 */
public class CookieHelper {
    
    private static final String TC_SESSION_ID = "tsid";
    
    protected static Logger log = LoggerFactory.getLogger(CookieHelper.class);
    
    /**
     * 将Session ID写到客户端的Cookie中
     * 
     * @param id
     *            Session ID
     * @param response
     *            HTTP响应
     * @return
     */
    public static Cookie writeSessionIdToCookie(String id, HttpServletRequest request,
            HttpServletResponse response, int expiry) {
    
        Cookie cookie = findCookie(TC_SESSION_ID, request);
        if (cookie == null) {
            cookie = new Cookie(TC_SESSION_ID, id);
        }
        cookie.setValue(id);
        cookie.setMaxAge(expiry);
        cookie.setPath(StringUtils.isEmpty(request.getContextPath()) ? "/" : request.getContextPath());
        response.addCookie(cookie);
        return cookie;
    }
    
    /**
     * 查询指定名称的Cookie值
     * 
     * @param name
     *            cookie名称
     * @param request
     *            HTTP请求
     * @return
     */
    public static String findCookieValue(String name, HttpServletRequest request) {
    
        Cookie cookie = findCookie(name, request);
        if (cookie != null) {
            return cookie.getValue();
        }
        return null;
    }
    
    /**
     * 查询指定名称的Cookie
     * 
     * @param name
     *            cookie名称
     * @param request
     *            HTTP请求
     * @return
     */
    public static Cookie findCookie(String name, HttpServletRequest request) {
    
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        // 迭代查找
        for (int i = 0, n = cookies.length; i < n; i++) {
            if (cookies[i].getName().equalsIgnoreCase(name)) {
                return cookies[i];
            }
        }
        return null;
    }
    
    /**
     * 在Cookie中查找Session ID
     * 
     * @param request
     *            HTTP请求
     * @return
     */
    public static String findSessionId(HttpServletRequest request) {
    
        return findCookieValue(TC_SESSION_ID, request);
    }
}
