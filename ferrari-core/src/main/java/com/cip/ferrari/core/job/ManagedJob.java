/**
 * 
 */
package com.cip.ferrari.core.job;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cip.ferrari.core.common.JobConstants;
import com.cip.ferrari.core.common.ThreadContext;

/**
 * @author yuantengkai job实体信息
 */
public class ManagedJob implements Runnable {
	
	private static final Logger logger = LoggerFactory.getLogger(ManagedJob.class);

	private String uuid;

	private String jobName;

	private List<String> returnUrllist;

	private Date beginTime;

	private Map<String, String> jobParam = new HashMap<String, String>();

	/**
	 * 运行的job对象
	 */
	private Object task = null;

	private Class<?> runClass;

	private Method runMethod;

	private String jobInfo;// class_method
	
	private Throwable runThrowable;
	
	private Object returnObject;//返回结果

	public ManagedJob(String uuid, String jobName, List<String> returnUrllist,
			Date beginTime, Map<String, String> param) {
		this.uuid = uuid;
		this.jobName = jobName;
		this.returnUrllist = returnUrllist;
		this.beginTime = beginTime;
		jobParam.putAll(param);
	}

	/**
	 * job实体初始化
	 */
	public void init() {
		if (StringUtils.isEmpty(uuid)) {
			throw new IllegalArgumentException("member uuid is Empty!");
		}
		if (null == returnUrllist || returnUrllist.isEmpty()) {
			throw new IllegalArgumentException(
					"member returnUrllist is null or empty!");
		}
		if (jobParam.isEmpty()) {
			throw new IllegalArgumentException("member jobParam is empty!");
		}
		String className = jobParam.get(JobConstants.KEY_RUN_CLASS);
		if (StringUtils.isBlank(className)) {
			throw new IllegalArgumentException("KEY_RUN_CLASS is null!");
		}
		String methodName = jobParam.get(JobConstants.KEY_RUN_METHOD);
		if (StringUtils.isBlank(methodName)) {
			throw new IllegalArgumentException("KEY_RUN_METHOD is null!");
		}

		// 检查 运行的类的是否存在！
		try {
			runClass = Class.forName(className);
		} catch (Exception e) {
			throw new IllegalArgumentException("Fail to find the run class("
					+ className + ") of job!");
		}
		// 检查运行的方法是否存在
		getMethod(runClass, methodName);
		if (null == runMethod) {
			throw new IllegalArgumentException("the run method(" + methodName
					+ ") of job is not existed!");
		}
		jobInfo = className + "_" + methodName;
	}

	private void getMethod(Class<?> jobClass, String methodName) {
		final Class<?>[][] paramterTypeList = new Class<?>[][] {
				// == 这个包含寻找运行方法的规则 ==

				// 1. 首先检查是否有 String[]作为参数的运行方法
				// NOTE: main方法 符合这个的签名，所以会首先使用main方法
				new Class<?>[] { String[].class },

				// 2. 再检查是否有 String[], String作为参数的运行方法
				new Class<?>[] { String[].class, String.class },

				// 3. 再检查是否有 String[], String作为参数的运行方法
				new Class<?>[] { String.class },

				// 4. 再检查是否有 空参数的运行方法
				new Class<?>[] {} };
		for (Class<?>[] paramterType : paramterTypeList) {
			try {
				runMethod = jobClass.getMethod(methodName, paramterType);
				break;
			} catch (NoSuchMethodException e) {
				runMethod = null;
			} catch (SecurityException e) {
				// ignore
			}
		}
	}

	@Override
	public void run() {
		try{
			ThreadContext.init();
			ThreadContext.put(JobConstants.KEY_RUN_CLASS, jobParam.get(JobConstants.KEY_RUN_CLASS));
			ThreadContext.put(JobConstants.KEY_UUID, uuid);
			
			String runMethodArgs = jobParam.get(JobConstants.KEY_RUN_METHOD_ARGS);
			
			if(!Modifier.isStatic(runMethod.getModifiers())){//非静态方法才调用
				task = runClass.newInstance();//保证多例
				
				Class<?>[] parameterTypes = runMethod.getParameterTypes();

		        Object[] parameters = new Object[parameterTypes.length];
		        for (int i = 0; i < parameterTypes.length; ++i) {
		            Class<?> clazz = parameterTypes[i];

		            if (clazz.equals(String[].class)) {
		                parameters[i] = parseMethodArguments(runMethodArgs);
		            } else if (clazz.equals(String.class)) {
		                parameters[i] = runMethodArgs;
		            } else {
		                // 如果method是调用getMethod得到的，则不可能出现这种情况！！
		                parameters[i] = null;
		            }
		        }
		        returnObject = runMethod.invoke(task, parameters);
			}
		}catch(Throwable t){
			logger.error("run job happens Exception,uuid="+uuid+",jobInfo="+jobInfo, t);
			runThrowable = t;
		}finally{
			ThreadContext.destroy();//线程结束销毁
		}
	}

	private String[] parseMethodArguments(String runMethodArgs) {
		if (StringUtils.isBlank(runMethodArgs)) {
            return new String[0];
        }

        String trim = runMethodArgs.trim();
        if (trim.length() == 0) {
            return new String[0];
        }
        return trim.split(",");
	}

	public String getUuid() {
		return uuid;
	}

	public String getJobInfo() {
		return jobInfo;
	}

	public String getJobName() {
		return jobName;
	}

	public List<String> getReturnUrllist() {
		return returnUrllist;
	}

	public Date getBeginTime() {
		return beginTime;
	}

	public Map<String, String> getJobParam() {
		return jobParam;
	}

	public Throwable getRunThrowable() {
		return runThrowable;
	}
	
	public void setRunThrowable(Throwable runThrowable) {
		this.runThrowable = runThrowable;
	}

	public Object getReturnObject() {
		return returnObject;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("ManagedJob[");
		sb.append("uuid=").append(uuid).append(",");
		sb.append("jobName=").append(jobName).append(",");
		sb.append("jobInfo=").append(jobInfo).append("]");
		return sb.toString();
	}

}
