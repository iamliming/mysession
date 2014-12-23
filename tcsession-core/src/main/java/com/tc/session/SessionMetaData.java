
package com.tc.session;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * Session存储
 * 
 * @author gaofeng
 * @date Sep 12, 2013 8:17:54 PM
 * @id $Id$
 */
public class SessionMetaData implements Serializable {
    
    private static final long serialVersionUID = -6446174402446690125L;
    
    private String id;
    /** session的创建时间 */
    private Long createTime;
    /** session的最大空闲时间 */
    private Long maxIdle;
    /** session的最后一次访问时间 */
    private Long lastAccessTime;
    /** 当前版本 */
    private int version = 0;
    
    /**
     * 构造方法
     */
    public SessionMetaData() {
        this.createTime = System.currentTimeMillis();
        this.lastAccessTime = this.createTime;
    }
    
    public Long getCreateTime() {
    
        return createTime;
    }
    
    public void setCreateTime(Long createTime) {
    
        this.createTime = createTime;
    }
    
    /**
     * @return Returns the maxIdle.
     */
    public Long getMaxIdle() {
    
        return maxIdle;
    }
    
    /**
     * @param maxIdle
     *            The maxIdle to set.
     */
    public void setMaxIdle(Long maxIdle) {
    
        this.maxIdle = maxIdle;
    }
    
    public Long getLastAccessTime() {
    
        return lastAccessTime;
    }
    
    public void setLastAccessTime(Long lastAccessTime) {
    
        this.lastAccessTime = lastAccessTime;
    }

    public Boolean isValid() {
        return (this.getLastAccessTime() + this.getMaxIdle()) > System.currentTimeMillis();
    }

    /**
     * @return Returns the id.
     */
    public String getId() {
    
        return id;
    }
    
    /**
     * @param id
     *            The id to set.
     */
    public void setId(String id) {
    
        this.id = id;
    }
    
    /**
     * Getter method for property <tt>version</tt>.
     * 
     * @return property value of version
     */
    public int getVersion() {
    
        return version;
    }
    
    /**
     * Setter method for property <tt>version</tt>.
     * 
     * @param version
     *            value to be assigned to property version
     */
    public void setVersion(int version) {
    
        this.version = version;
    }

    @Override
    public String toString() {
        return "SessionMetaData [id=" + id + ", createTime=" + new Date(createTime) + ", maxIdle=" + maxIdle + ", lastAccessTime=" + new Date(lastAccessTime) + ", version=" + version + "]";
    }

}
