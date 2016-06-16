package com.cip.ferrari.admin.dao;

import java.util.List;

import com.cip.ferrari.admin.core.model.FerrariJobInfo;

/**
 * @author yuantengkai
 *
 */
public interface IFerrariJobInfoDao {

	public int save(FerrariJobInfo ferrariJobInfo);

	// for page list
	public List<FerrariJobInfo> pageList(int offset, int pagesize, String jobKey, String jobGroup);
	public int pageListCount(int offset, int pagesize, String jobKey, String jobGroup);

	public FerrariJobInfo get(int id);
	public FerrariJobInfo getByKey(String triggerKeyName);

	public int removeJob(String jobKey);

	public int updateJobInfo(FerrariJobInfo jobInfo);
	
	
}
