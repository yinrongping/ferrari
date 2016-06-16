/**
 * 
 */
package com.cip.ferrari.admin.dao.impl;

import java.util.HashMap;
import java.util.List;

import javax.annotation.Resource;

import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.stereotype.Repository;
import com.cip.ferrari.admin.core.model.FerrariJobInfo;
import com.cip.ferrari.admin.dao.IFerrariJobInfoDao;

/**
 * @author yuantengkai
 *
 */
@Repository(FerrariJobInfoDaoImpl.BeanName)
public class FerrariJobInfoDaoImpl implements IFerrariJobInfoDao {
	
	public static final String BeanName = "ferrariJobInfoDao";

	@Resource
	public SqlSessionTemplate sqlSessionTemplate;
	
	@Override
	public int save(FerrariJobInfo ferrariJobInfo) {
		return sqlSessionTemplate.insert("FerrariJobInfoMapper.save", ferrariJobInfo);
	}
	
	@Override
	public List<FerrariJobInfo> pageList(int offset, int pagesize, String jobKey, String jobGroup) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("jobKey", jobKey);
		params.put("jobGroup", jobGroup);
		return sqlSessionTemplate.selectList("FerrariJobInfoMapper.pageList", params);
	}

	@Override
	public int pageListCount(int offset, int pagesize, String jobKey, String jobGroup) {
		HashMap<String, Object> params = new HashMap<String, Object>();
		params.put("offset", offset);
		params.put("pagesize", pagesize);
		params.put("jobKey", jobKey);
		params.put("jobGroup", jobGroup);
		Integer result = sqlSessionTemplate.selectOne("FerrariJobInfoMapper.pageListCount", params);
		if(result == null){
			return 0;
		}
		return result;
	}

	@Override
	public FerrariJobInfo get(int id) {
		return sqlSessionTemplate.selectOne("FerrariJobInfoMapper.get", id);
	}
	
	@Override
	public FerrariJobInfo getByKey(String jobKey) {
		return sqlSessionTemplate.selectOne("FerrariJobInfoMapper.getByKey", jobKey);
	}

	@Override
	public int removeJob(String jobKey) {
		return sqlSessionTemplate.update("FerrariJobInfoMapper.removeJob", jobKey);
	}

	@Override
	public int updateJobInfo(FerrariJobInfo jobInfo) {
		return sqlSessionTemplate.update("FerrariJobInfoMapper.updateJobInfo", jobInfo);
	}

}
