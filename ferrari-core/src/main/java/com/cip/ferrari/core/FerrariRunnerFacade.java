/**
 * 
 */
package com.cip.ferrari.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.cip.ferrari.core.common.JobConstants;
import com.cip.ferrari.core.job.ContainerJobManager;
import com.cip.ferrari.core.job.DirectionType;
import com.cip.ferrari.core.job.result.FerrariFeedback;
import com.cip.ferrari.core.log.FerrariLogManager;

/**
 * @author yuantengkai Ferrari job运行对外facade
 */
public class FerrariRunnerFacade {

	private static final Logger logger = LoggerFactory
			.getLogger(FerrariRunnerFacade.class);
	
	private static final int MAX_DLT_TIME = 60*1000;

	private ContainerJobManager containerJobManager;

	public void init() {
		containerJobManager = new ContainerJobManager();
		containerJobManager.init();
	}

	public String request(Map<String, String> param) {
		// 保护性拷贝
		Map<String, String> jobParam = new HashMap<String, String>(param);
		FerrariFeedback result = new FerrariFeedback();
		result.setStatus(true);
		final String command = jobParam.remove(JobConstants.KEY_ACTION);
		try {
			//调度触发时间与接收时间比较，时差超过1分钟则忽略
			String beginTimeStr = jobParam.remove(JobConstants.KEY_BEGIN_TIME);
			if(!StringUtils.isBlank(beginTimeStr)){
				long time = Long.parseLong(beginTimeStr);
				if(Math.abs(time - System.currentTimeMillis()) > MAX_DLT_TIME){
					result.setStatus(false);
					result.setErrormsg("调度实际触发时间与接收时间差超过1分钟，忽略本次调度处理");
					return JSON.toJSONString(result);
				}
			}
			// 运行job
			if (JobConstants.VALUE_ACTION_RUN_JOB.equalsIgnoreCase(command)) {
				String uuid = jobParam.remove(JobConstants.KEY_UUID);
				result.setDirectionType(DirectionType.RUN_JOB);
				result.setUuid(uuid);
				String jobName = jobParam.remove(JobConstants.KEY_JOB_NAME);
				String returnUrls = jobParam
						.remove(JobConstants.KEY_RESULT_URL_LIST);

				List<String> returnUrllist = new ArrayList<String>();
				String[] returnUrlArray = returnUrls.split(",");
				for (String s : returnUrlArray) {
					returnUrllist.add(s);
				}
				Date beginTime = null;
				if (StringUtils.isBlank(beginTimeStr)) {
					beginTime = new Date();
				} else {
					try {
						long time = Long.parseLong(beginTimeStr);
						beginTime = new Date(time); 
					} catch (Exception e) {
						beginTime = new Date();
					}
				}
				containerJobManager.runJob(uuid, jobName, returnUrllist,
						beginTime, jobParam);
			}
			// 终止job
			else if (JobConstants.VALUE_ACTION_KILL_JOB
					.equalsIgnoreCase(command)) {
				String uuid = jobParam.remove(JobConstants.KEY_UUID);
				String runclass = jobParam.remove(JobConstants.KEY_RUN_CLASS);
				String method = jobParam.remove(JobConstants.KEY_RUN_METHOD);
				result.setDirectionType(DirectionType.KILL_JOB);
				containerJobManager.killJobById(uuid, runclass, method);
			}
			//获取执行日志  需要进行压缩 日志量大的时候 tomcat配置zip压缩
			else if(JobConstants.VALUE_ACTION_RUN_LOG.equalsIgnoreCase(command)){
				String uuid = jobParam.remove(JobConstants.KEY_UUID);
				String runclass = jobParam.remove(JobConstants.KEY_RUN_CLASS);
				String runclassFile = runclass.substring(runclass.lastIndexOf(".")+1);
				String executeTimeStr = jobParam.remove(JobConstants.KEY_EXECUTE_TIME);
				if(StringUtils.isBlank(uuid) || StringUtils.isBlank(runclass) 
						|| StringUtils.isBlank(runclassFile) || StringUtils.isBlank(executeTimeStr)){
					result.setStatus(false);
					result.setErrormsg("某些请求参数为空");
				}else{
					long time = Long.parseLong(executeTimeStr);
					String logContent = FerrariLogManager.readLog(runclassFile, uuid,new Date(time));
					result.setDirectionType(DirectionType.RUN_LOG);
					result.setContent(logContent);
				}
			}
			// 未知指令
			else {
				result.setStatus(false);
				result.setDirectionType(null);
				result.setErrormsg("Unknow command(" + command + ")!");
			}
		} catch (Throwable t) {
			logger.error("Exception when doing command:" + command + param, t);
			result.setStatus(false);
			result.setErrormsg(StringUtils.substring(t.toString(), 0, 2048));
		}
		return JSON.toJSONString(result);
	}

	public void destroy() {
		containerJobManager.dispose();
	}

}
