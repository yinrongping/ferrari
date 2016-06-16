package com.cip.ferrari.admin.dao;


import java.util.Date;
import java.util.List;

import com.cip.ferrari.admin.core.model.FerrariJobLog;

public interface IFerrariJobLogDao {
	
	public int save(FerrariJobLog ferrariJobLog);
	
	public FerrariJobLog load(long id);
	
	public int updateTriggerInfo(FerrariJobLog ferrariJobLog);
	
	public int updateHandleInfo(FerrariJobLog ferrariJobLog);
	
	public List<FerrariJobLog> pageList(int offset, int pagesize,String jobGroup, String jobName, Date triggerTimeStart, Date triggerTimeEnd);
	public int pageListCount(int offset, int pagesize,String jobGroup, String jobName, Date triggerTimeStart, Date triggerTimeEnd);
	
}
