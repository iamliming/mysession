
package com.tc.session.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tc.session.SessionManager;
import com.tc.session.servlet.RemotableRequestWrapper;

/**
 * 
 * session拦截器。<br />
 * 1. 加载SessionManager具体实现类 2. 替换ServletRequest的实现，完成代理模式
 * 
 * @author gaofeng
 * @date Sep 18, 2013 1:27:10 PM
 * @id $Id$
 */
public class TCSessionFilter implements Filter {
    
    private static final Logger log = LoggerFactory.getLogger(TCSessionFilter.class);
    private SessionManager sessionManager;
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    
        try {
            this.sessionManager = (SessionManager) Class.forName("com.tc.session.TCSessionManager").newInstance();
            this.sessionManager.setServletContext(filterConfig.getServletContext());
        } catch (ClassNotFoundException e) {
            log.error("过滤器初始化失败", e);
        } catch (InstantiationException e) {
            log.error("过滤器初始化失败", e);
        } catch (IllegalAccessException e) {
            log.error("过滤器初始化失败", e);
        }
        
        if (log.isInfoEnabled())
            log.info("TCSessionFilter.init completed.");
        
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
    
        RemotableRequestWrapper req = new RemotableRequestWrapper((HttpServletRequest) request, sessionManager);
        req.setResponse((HttpServletResponse)response);
        chain.doFilter(req, response);
    }
    
    @Override
    public void destroy() {
    
        if (sessionManager != null) {
            try {
                sessionManager.close();
            } catch (Exception ex) {
                log.error("关闭Session管理器时发生异常，", ex);
            }
        }
        
        if (log.isInfoEnabled()) {
            log.info("TCSessionFilter.destroy completed.");
        }
    }
    
    public static void main(String[] args) throws ServletException {
    
        TCSessionFilter f = new TCSessionFilter();
        f.init(null);
    }
}
