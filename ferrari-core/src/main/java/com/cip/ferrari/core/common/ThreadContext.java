/**
 * 
 */
package com.cip.ferrari.core.common;

import java.util.HashMap;
import java.util.Map;

/**
 * @author yuantengkai
 * ThreadLocal 存储对象
 */
public class ThreadContext {

    private static final ThreadLocal<ThreadContext> localContext  = new ThreadLocal<ThreadContext>();

    private Map<String, Object>                     localCacheMap = new HashMap<String, Object>();

    private ThreadContext() {

    }

    public static void init() {
        ThreadContext ctx = localContext.get();
        if (ctx == null) {
            ctx = new ThreadContext();
            localContext.set(ctx);
        }
    }

    public static void destroy() {
        ThreadContext ctx = localContext.get();
        if (ctx != null) {
            localContext.remove();
        }
    }

    public static void put(String key, Object obj) {
        localContext.get()._put(key, obj);
    }

    private void _put(String key, Object obj) {
        localCacheMap.put(key, obj);
    }

    public static Object get(String key) {
        return localContext.get()._get(key);
    }

    private Object _get(String key) {
        return localCacheMap.get(key);
    }


}
