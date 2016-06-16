package com.cip.ferrari.admin.core.util;

import java.util.Date;
import java.util.HashSet;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.quartz.CronScheduleBuilder;
import org.quartz.CronTrigger;
import org.quartz.Job;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.JobKey;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.Trigger;
import org.quartz.Trigger.TriggerState;
import org.quartz.TriggerBuilder;
import org.quartz.TriggerKey;
import org.quartz.impl.triggers.CronTriggerImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cip.ferrari.admin.common.FerrariConstantz;
import com.cip.ferrari.admin.core.model.FerrariJobInfo;

/**
 * base quartz scheduler util
 * @author xuxueli 2015-12-19 16:13:53
 */
public final class DynamicSchedulerUtil {
    private static final Logger logger = LoggerFactory.getLogger(DynamicSchedulerUtil.class);
    
    
    private static Scheduler scheduler;
    
    public static void setScheduler(Scheduler scheduler) {
		DynamicSchedulerUtil.scheduler = scheduler;
	}
    
	// fill job info
	public static void fillJobInfo(FerrariJobInfo jobInfo) {
		// TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(jobInfo.getJobKey(), Scheduler.DEFAULT_GROUP);
        JobKey jobKey = new JobKey(jobInfo.getJobKey(), Scheduler.DEFAULT_GROUP);
        try {
			Trigger trigger = scheduler.getTrigger(triggerKey);
			JobDetail jobDetail = scheduler.getJobDetail(jobKey);
			TriggerState triggerState = scheduler.getTriggerState(triggerKey);
			
			// parse quartz params
			if (trigger!=null && trigger instanceof CronTriggerImpl) {
				String cronExpression = ((CronTriggerImpl) trigger).getCronExpression();
				jobInfo.setJobCron(cronExpression);
			}
			if (jobDetail!=null) {
				String jobClass = jobDetail.getJobClass().getName();
				jobInfo.setJobClass(jobClass);
			}
			if (triggerState!=null) {
				jobInfo.setJobStatus(triggerState.name());
			}
			jobInfo.setJobData(JacksonUtil.writeValueAsString(jobDetail.getJobDataMap()));
			
		} catch (SchedulerException e) {
			e.printStackTrace();
		}
	}

	// generate quartz jobkey
	public static String generateTriggerKey(String jobGroup, String jobName){
		if (StringUtils.isBlank(jobGroup) || StringUtils.isBlank(jobName)) {
			throw new IllegalArgumentException("任务参数非法");
		}
		return jobGroup.concat(FerrariConstantz.job_group_name_split).concat(jobName);
	}
	
	/**
	 * 新增一个job
	 * @param triggerKeyName
	 * @param cronExpression
	 * @param jobClass
	 * @param jobData
	 * @return
	 * @throws SchedulerException
	 */
    public static boolean addJob(String triggerKeyName, String cronExpression, Class<? extends Job> jobClass, Map<String, Object> jobData) throws SchedulerException {
    	// TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, Scheduler.DEFAULT_GROUP);
        
        // TriggerKey valid if_exists
        if (scheduler.checkExists(triggerKey)) {
//            Trigger trigger = scheduler.getTrigger(triggerKey);
            logger.warn("###### Already exist trigger with key [" + triggerKey + "] in quartz scheduler");
            return false;
        }
        
        // CronTrigger : TriggerKey + cronExpression + MISFIRE_INSTRUCTION_DO_NOTHING
        CronScheduleBuilder cronScheduleBuilder = CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing();
        CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(cronScheduleBuilder).build();
        // JobDetail : jobClass
        JobDetail jobDetail = JobBuilder.newJob(jobClass).withIdentity(triggerKeyName, Scheduler.DEFAULT_GROUP).build();
        
        if (jobData !=null && jobData.size() > 0) {
        	JobDataMap jobDataMap = jobDetail.getJobDataMap();
        	jobDataMap.putAll(jobData);	// JobExecutionContext context.getMergedJobDataMap().get("mailGuid");
		}
        
        // schedule : jobDetail + cronTrigger
        Date date = scheduler.scheduleJob(jobDetail, cronTrigger);

        if(logger.isInfoEnabled()){
        	logger.info("###### addJob success, jobDetail:{}, cronTrigger:{}, date:{}", jobDetail, cronTrigger, date);
        }
        
        return true;
    }
    
    /** reschedule 重置cron
     * 
     * @param triggerKeyName
     * @param cronExpression
     * @return
     * @throws SchedulerException
     */
    public static boolean rescheduleJob(String triggerKeyName, String cronExpression, Map<String, Object> jobData) throws SchedulerException {
        // TriggerKey : name + group
    	String group = Scheduler.DEFAULT_GROUP;
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, group);
        
        if (scheduler.checkExists(triggerKey)) {
            // CronTrigger : TriggerKey + cronExpression + MISFIRE_INSTRUCTION_DO_NOTHING
            CronTrigger cronTrigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).
            		withSchedule(CronScheduleBuilder.cronSchedule(cronExpression).withMisfireHandlingInstructionDoNothing()).build();
            
            // JobDetail-JobDataMap fresh
            JobDetail jobDetail = scheduler.getJobDetail(new JobKey(triggerKeyName, group));
        	JobDataMap jobDataMap = jobDetail.getJobDataMap();
        	jobDataMap.clear();
        	jobDataMap.putAll(jobData);
        	
        	// Trigger fresh
        	HashSet<Trigger> triggerSet = new HashSet<Trigger>();
        	triggerSet.add(cronTrigger);
            
            scheduler.scheduleJob(jobDetail, triggerSet, true);
            return true;
        } else {
        	logger.warn("######scheduler.rescheduleJob, triggerKey not exist,key="+triggerKeyName);
        }
        return false;
    }
    
    /**unscheduleJob 删除
     * 
     * @param triggerKeyName
     * @return
     * @throws SchedulerException
     */
    public static boolean removeJob(String triggerKeyName) throws SchedulerException {
    	// TriggerKey : name + group
    	String group = Scheduler.DEFAULT_GROUP;
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, group);
        
        boolean result = true;
        if (scheduler.checkExists(triggerKey)) {
            result = scheduler.unscheduleJob(triggerKey);
        }
        return result;
    }

    /** Pause 暂停
     * 
     * @param triggerKeyName
     * @return
     * @throws SchedulerException
     */
    public static boolean pauseJob(String triggerKeyName) throws SchedulerException {
    	// TriggerKey : name + group
    	String group = Scheduler.DEFAULT_GROUP;
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, group);
        
        boolean result = true;
        if (scheduler.checkExists(triggerKey)) {
            scheduler.pauseTrigger(triggerKey);
            return true;
        }else{
        	logger.warn("######scheduler.pauseTrigger, triggerKey not exist,key="+triggerKeyName);
        }
        return result;
    }
    
    /** resume 重启 
     * 
     * @param triggerKeyName
     * @return
     * @throws SchedulerException
     */
    public static boolean resumeJob(String triggerKeyName) throws SchedulerException {
        // TriggerKey : name + group
    	String group = Scheduler.DEFAULT_GROUP;
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, group);
        
        if (scheduler.checkExists(triggerKey)) {
            scheduler.resumeTrigger(triggerKey);
            return true;
        } else {
        	logger.warn("######scheduler.resumeTrigger, triggerKey not exist,key="+triggerKeyName);
        }
        return false;
    }
    
    /** run 执行一次 
     * 
     * @param triggerKeyName
     * @return
     * @throws SchedulerException
     */
    public static boolean triggerJob(String triggerKeyName) throws SchedulerException {
        // TriggerKey : name + group
    	String group = Scheduler.DEFAULT_GROUP;
        JobKey jobKey = JobKey.jobKey(triggerKeyName, group);
        
        if (scheduler.checkExists(jobKey)) {
            scheduler.triggerJob(jobKey);
            return true;
        } else {
        	logger.warn("######scheduler.triggerJob, triggerKey not exist,key="+triggerKeyName);
        }
        return false;
    }
    
    public static boolean hasExistsJob(String triggerKeyName) throws SchedulerException{
    	// TriggerKey : name + group
        TriggerKey triggerKey = TriggerKey.triggerKey(triggerKeyName, Scheduler.DEFAULT_GROUP);
        
        // TriggerKey valid if_exists
		if (scheduler.checkExists(triggerKey)) {
		    return true;
		}
        return false;
    }

}