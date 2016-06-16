/**
 * 
 */
package com.cip.ferrari.admin.alarm;

/**
 * @author yuantengkai
 *
 */
public class MonitorEntity {
	
	private long jobLogId;
	private boolean success;
	
	public MonitorEntity(long jobLogId, boolean success){
		this.jobLogId = jobLogId;
		this.success = success;
	}
	
	public long getJobLogId() {
		return jobLogId;
	}

	public boolean isSuccess() {
		return success;
	}
}
