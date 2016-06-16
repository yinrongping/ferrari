/**
 * 
 */
package com.cip.ferrari.admin.common;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

/**
 * @author yuantengkai
 * 
 */
public class FerrariBeanFactory implements ApplicationContextAware{

	private static ApplicationContext applicationcontext;
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		applicationcontext = applicationContext;
	}
	
	
	public static Object getBean(String beanName){
		if (applicationcontext == null || StringUtils.isBlank(beanName)) {
			return null;
		}
		try {
			return applicationcontext.getBean(beanName);
		} catch (Exception e) {
			return null;
		}
	}

}
