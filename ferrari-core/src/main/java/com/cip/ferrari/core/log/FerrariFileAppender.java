/**
 * 
 */
package com.cip.ferrari.core.log;

import java.io.IOException;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;
import com.cip.ferrari.core.common.JobConstants;
import com.cip.ferrari.core.common.ThreadContext;

/**
 * @author yuantengkai
 * ferrari log实现版本<br/>
 * log4j配置参考:<br/>
 * 
 * log4j.rootLogger=warn,ferrari
 * 
 * log4j.appender.ferrari=com.cip.ferrari.core.log.FerrariFileAppender 
 * log4j.appender.ferrari.filePath=/data/applogs/ferrari/
 * log4j.appender.ferrari.layout=org.apache.log4j.PatternLayout
 * log4j.appender.ferrari.layout.ConversionPattern=%-d{yyyy-MM-dd HH:mm:ss} [%c]-[%t]-[%M]-[%L]-[%p] %m%n
 */
public class FerrariFileAppender extends AppenderSkeleton {
	
	private String filePath;//ferrari log路径(文件夹)
	

	/**
	 * AppenderSkeleton 已加了同步，此方法线程安全
	 */
	@Override
	protected void append(LoggingEvent event) {
		String runclass;
		String runclassFile;
		String uuid;
		try{
			runclass = (String) ThreadContext.get(JobConstants.KEY_RUN_CLASS);
			runclassFile = runclass.substring(runclass.lastIndexOf(".")+1);
			uuid = (String) ThreadContext.get(JobConstants.KEY_UUID);
		}catch(Exception e){
			//ignore 说明不是ferrari工程打日志
			return;
		}
		
		//把日志内容写入文件
		try {
			FerrariLogManager.writeLog(runclassFile, uuid, event, this.layout);
		} catch (IOException e) {
			errorHandler.error("Ferrari Log write IOException,file="+filePath+runclassFile+"/"+uuid+".log");
		} catch (Exception e) {
			errorHandler.error("Ferrari Log write UnKnowException,file="+filePath+runclassFile+"/"+uuid+".log");
		} 
	}

	@Override
	public void close() {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean requiresLayout() {
		return true;
	}

	public void setFilePath(String path) {
		filePath = path;
		FerrariLogManager.setFilePath(filePath);
	}

}
