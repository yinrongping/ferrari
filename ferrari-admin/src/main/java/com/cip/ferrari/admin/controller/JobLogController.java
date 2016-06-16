package com.cip.ferrari.admin.controller;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.cip.ferrari.admin.alarm.MonitorEntity;
import com.cip.ferrari.admin.alarm.MonitorManager;
import com.cip.ferrari.admin.common.FerrariConstantz;
import com.cip.ferrari.admin.common.JobGroupEnum;
import com.cip.ferrari.admin.core.model.FerrariJobLog;
import com.cip.ferrari.admin.core.model.ReturnT;
import com.cip.ferrari.admin.core.util.HttpUtil;
import com.cip.ferrari.admin.core.util.JacksonUtil;
import com.cip.ferrari.admin.core.util.PropertiesUtil;
import com.cip.ferrari.admin.dao.IFerrariJobInfoDao;
import com.cip.ferrari.admin.dao.IFerrariJobLogDao;
import com.cip.ferrari.core.common.JobConstants;
import com.cip.ferrari.core.job.result.FerrariFeedback;

/**
 * index controller
 * @author xuxueli 2015-12-19 16:13:16
 */
@Controller
@RequestMapping("/joblog")
public class JobLogController {
	
	private static Logger Logger = LoggerFactory.getLogger(JobLogController.class);
	
	@Resource
	public IFerrariJobLogDao ferraliJobLogDao;
	@Resource
	public IFerrariJobInfoDao ferrarijobinfodao;
	
	@RequestMapping("/save")
	@ResponseBody
	public ReturnT<String> triggerLog(long triggerLogId, String status, String msg) {
		FerrariJobLog log = ferraliJobLogDao.load(triggerLogId);
		if (log!=null) {
			log.setHandleTime(new Date());
			log.setHandleStatus(status);
			log.setHandleMsg(msg);
			
			if (log.getHandleMsg()!=null && log.getHandleMsg().length()>1900) {
				log.setHandleMsg(log.getHandleMsg().substring(0, 1850));
			}
			
			ferraliJobLogDao.updateHandleInfo(log);
			Logger.info("JobLogController save success, triggerLogId:{}, status:{}, msg:{}", triggerLogId, status, msg);
			return ReturnT.SUCCESS;
		}
		return ReturnT.FAIL;
	}
	
	@RequestMapping("/ferrarifeedback")
	@ResponseBody
	public String ferrarifeedback(String result) {
		if(Logger.isInfoEnabled()){
			Logger.info("############ferrari job feedback, result:{}", result);
		}
		if (!StringUtils.isBlank(result)) {
			FerrariFeedback feedback = JacksonUtil.readValue(result, FerrariFeedback.class);
			if (feedback != null) {
				ReturnT<String> ret = null;
				if(feedback.isStatus()){
					ret = this.triggerLog(Long.valueOf(feedback.getUuid()), HttpUtil.SUCCESS, feedback.getContent());
				}else{
					ret = this.triggerLog(Long.valueOf(feedback.getUuid()), HttpUtil.FAIL, feedback.getErrormsg());
				}
				//任务执行完后 监控报警
				MonitorManager.getInstance().put2AlarmDeal(
						new MonitorEntity(Long.valueOf(feedback.getUuid()),feedback.isStatus()));
				if (ret!=null && ret.getCode() == ReturnT.SUCCESS.getCode()) {
					return "ok";
				}
			}else{
				Logger.warn("############ferrari job feedback deal failed because of feedback is null, result:{}", result);
			}
		}
		return "fail";
	}
	
	@SuppressWarnings("unchecked")
	@RequestMapping("/ferrariLogDetail")
	@ResponseBody
	public Map<String, Object> ferrariLogDetail(long id){
		Map<String, Object> result = new HashMap<String, Object>();
		
		FerrariJobLog ferrariJobLog = ferraliJobLogDao.load(id);
		result.put("ferrariJobLog", ferrariJobLog);
		if (ferrariJobLog == null) {
			result.put("code", 500);
			result.put("msg", "参数异常");
			return result;
		}
		
		Map<String, Object> jobDataMap = JacksonUtil.readValue(ferrariJobLog.getJobData(), Map.class);
		String targetIPPort = String.valueOf(jobDataMap.get(JobConstants.KEY_JOB_ADDRESS));
		String reqURL = "http://" + targetIPPort + PropertiesUtil.getString(FerrariConstantz.ReceiveServletpath);
		
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("uuid", String.valueOf(id));
		reqMap.put("action_type", JobConstants.VALUE_ACTION_RUN_LOG);
		reqMap.put("run_class", String.valueOf(jobDataMap.get(JobConstants.KEY_RUN_CLASS)));
		Date executeTime = ferrariJobLog.getHandleTime();
		if(executeTime == null){
			executeTime = ferrariJobLog.getTriggerTime();
		}
		reqMap.put("execute_time", String.valueOf(executeTime.getTime()));
		
		String[] postResp = HttpUtil.post(reqURL, reqMap);
		
		String responseMsg = postResp[0];
		String exceptionMsg = postResp[1];
		FerrariFeedback ferrariFeedback = null;
		if (StringUtils.isNotBlank(responseMsg)) {
			try {
//				String rawRes = ZipUtil.uncompress(responseMsg);
				ferrariFeedback = JacksonUtil.readValue(responseMsg,FerrariFeedback.class);
			} catch (Exception e) {	
				Logger.error("查看业务日志，解析失败,joblogid="+ferrariFeedback.getUuid(),e);
			}
		}
		if (ferrariFeedback != null) {
			result.put("code", 200);
			result.put("ferrariFeedback", ferrariFeedback);
			return result;
		} else {
			result.put("code", 500);
			result.put("msg", "日志查询失败");
			result.put("exceptionMsg", exceptionMsg);
			return result;
		}
	}
	
	@RequestMapping("/ferrariLogDetailPage")
	public String ferrariLogDetailPage(long id, Model model){
		Map<String, Object> data = ferrariLogDetail(id);
		model.addAttribute("data", data);
		return "joblog/logdetail.page";
	}
	
	@RequestMapping
	public String index(Model model, @RequestParam(required = false, defaultValue = "-1") int jobInfoId) {
		
		// 默认filterTime
		Calendar todayz = Calendar.getInstance();
		todayz.set(Calendar.HOUR_OF_DAY, 0);
		todayz.set(Calendar.MINUTE, 0);
		todayz.set(Calendar.SECOND, 0);
		model.addAttribute("triggerTimeStart", todayz.getTime());
		model.addAttribute("triggerTimeEnd", Calendar.getInstance().getTime());
		
		model.addAttribute("jobInfo", ferrarijobinfodao.get(jobInfoId));
		model.addAttribute("groupEnum", JobGroupEnum.values());
		return "joblog/index";
	}
	
	@RequestMapping("/pageList")
	@ResponseBody
	public Map<String, Object> pageList(@RequestParam(required = false, defaultValue = "0") int start,  
			@RequestParam(required = false, defaultValue = "10") int length,
			String filterTime, String jobGroup, String jobName) {
		// parse param
		Date triggerTimeStart = null;
		Date triggerTimeEnd = null;
		if (StringUtils.isNotBlank(filterTime)) {
			String[] temp = filterTime.split(" - ");
			if (temp!=null && temp.length == 2) {
				try {
					triggerTimeStart = DateUtils.parseDate(temp[0], new String[]{"yyyy-MM-dd HH:mm:ss"});
					triggerTimeEnd = DateUtils.parseDate(temp[1], new String[]{"yyyy-MM-dd HH:mm:ss"});
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
		
		// page query
		List<FerrariJobLog> list = ferraliJobLogDao.pageList(start, length, jobGroup, jobName, triggerTimeStart, triggerTimeEnd);
		int list_count = ferraliJobLogDao.pageListCount(start, length, jobGroup, jobName, triggerTimeStart, triggerTimeEnd);
		
		// package result
		Map<String, Object> maps = new HashMap<String, Object>();
	    maps.put("recordsTotal", list_count);	// 总记录数
	    maps.put("recordsFiltered", list_count);// 过滤后的总记录数
	    maps.put("data", list);  				// 分页列表
		return maps;
	}
	
	/**
	 * 任务终止
	 * @param id
	 * @return
	 */
	@SuppressWarnings("unchecked")
	@RequestMapping("/ferrariJobKill")
	@ResponseBody
	public ReturnT<String> ferrariJobKill(int id){
		FerrariJobLog ferrariJobLog = ferraliJobLogDao.load(id);
		if (ferrariJobLog == null) {
			return new ReturnT<String>(500, "参数异常");
		}
		
		Map<String, Object> jobDataMap = JacksonUtil.readValue(ferrariJobLog.getJobData(), Map.class);
		String targetIPPort = String.valueOf(jobDataMap.get(JobConstants.KEY_JOB_ADDRESS));
		String reqURL = "http://" + targetIPPort + PropertiesUtil.getString(FerrariConstantz.ReceiveServletpath);
		
		Map<String, String> reqMap = new HashMap<String, String>();
		reqMap.put("uuid", String.valueOf(id));
		reqMap.put("action_type", JobConstants.VALUE_ACTION_KILL_JOB);
		reqMap.put("run_class", String.valueOf(jobDataMap.get(JobConstants.KEY_RUN_CLASS)));
		reqMap.put("run_method", String.valueOf(jobDataMap.get(JobConstants.KEY_RUN_METHOD)));
		
		String[] postResp = HttpUtil.post(reqURL, reqMap);
		String responseMsg = postResp[0];
		String exceptionMsg = postResp[1];
		
		FerrariFeedback ferrariFeedback = null;
		if (StringUtils.isNotBlank(responseMsg)) {
			try {
				ferrariFeedback = JacksonUtil.readValue(responseMsg, FerrariFeedback.class);
			} catch (Exception e) {	
				Logger.error("任务终止失败,joblogid:{}, error:{}", id, e);
			}
		}
		if (ferrariFeedback == null) {
			Logger.error("任务终止失败, joblogid:{}, exceptionMsg:{}", id, exceptionMsg);
			return new ReturnT<String>(500, "任务终止失败[" + exceptionMsg + "]");
		}
		if (!ferrariFeedback.isStatus()) {
			Logger.error("任务终止失败, joblogid:{}, exceptionMsg:{}, ferrariFeedback:{}", id, exceptionMsg, ferrariFeedback);
			return new ReturnT<String>(500, "任务终止失败[" + ferrariFeedback.getErrormsg() + "]");
		}
		
		ferrariJobLog.setHandleTime(new Date());
		ferrariJobLog.setHandleStatus(HttpUtil.FAIL);
		ferrariJobLog.setHandleMsg("手动终止");
		ferraliJobLogDao.updateHandleInfo(ferrariJobLog);
		return ReturnT.SUCCESS;
	}
}
