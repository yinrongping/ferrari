/**
 * 
 */
package com.cip.ferrari.admin.core.model;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuantengkai
 * job信息实体类
 */
public class FerrariJobInfo {
	
	private int id;
	private Date addTime;
	private Date updateTime;
	private String jobGroup;	//job所在业务组
	private String jobName;		//job名称
	private String jobKey;		//jobkey--对应到quartz
	private String jobDesc;		//job描述
	private String owner;		//job负责人
	private String mailReceives;//邮件联系人，多个用,分隔
	private int failAlarmNum;	//连续失败次数报警阀值
	private int isDeleted;		//删除任务时，置为1
	
	// quartz params
	private String jobCron;
	private String jobClass;
	private String jobStatus;
	private String jobData;
		
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public Date getAddTime() {
		return addTime;
	}
	public void setAddTime(Date addTime) {
		this.addTime = addTime;
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}
	public String getJobGroup() {
		return jobGroup;
	}
	public void setJobGroup(String jobGroup) {
		this.jobGroup = jobGroup;
	}
	public String getJobName() {
		return jobName;
	}
	public void setJobName(String jobName) {
		this.jobName = jobName;
	}
	
	public String getJobKey() {
		return jobKey;
	}
	public void setJobKey(String jobKey) {
		this.jobKey = jobKey;
	}
	public String getJobDesc() {
		return jobDesc;
	}
	public void setJobDesc(String jobDesc) {
		this.jobDesc = jobDesc;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getMailReceives() {
		return mailReceives;
	}
	public void setMailReceives(String mailReceives) {
		this.mailReceives = mailReceives;
	}
	public int getFailAlarmNum() {
		return failAlarmNum;
	}
	public void setFailAlarmNum(int failAlarmNum) {
		this.failAlarmNum = failAlarmNum;
	}
	public int getIsDeleted() {
		return isDeleted;
	}
	public void setIsDeleted(int isDeleted) {
		this.isDeleted = isDeleted;
	}
	
	public String getJobCron() {
		return jobCron;
	}
	public void setJobCron(String jobCron) {
		this.jobCron = jobCron;
	}
	public String getJobClass() {
		return jobClass;
	}
	public void setJobClass(String jobClass) {
		this.jobClass = jobClass;
	}
	public String getJobStatus() {
		return jobStatus;
	}
	public void setJobStatus(String jobStatus) {
		this.jobStatus = jobStatus;
	}
	public String getJobData() {
		return jobData;
	}
	public void setJobData(String jobData) {
		this.jobData = jobData;
	}
	
	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
	
}
