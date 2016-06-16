/**
 * 
 */
package com.cip.ferrari.core.job;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuantengkai job容器管理
 */
public class ContainerJobManager {

	private static final Logger logger = LoggerFactory
			.getLogger(ContainerJobManager.class);

	/**
	 * jobid和job实体的map key-uuid
	 */
	private final ConcurrentHashMap<String, ManagedJob> jobId2JobMap = new ConcurrentHashMap<String, ManagedJob>(
			64);

	/**
	 * job信息和jobid的map key:class_method value-uuidlist
	 */
	private final ConcurrentHashMap<String, CopyOnWriteArrayList<String>> jobInfo2JobIdMap = new ConcurrentHashMap<String, CopyOnWriteArrayList<String>>(
			64);

	/**
	 * job实体和运行线程的map
	 */
	private final ConcurrentHashMap<ManagedJob, Thread> job2ThreadMap = new ConcurrentHashMap<ManagedJob, Thread>(
			64);

	private int corePoolSize = 5;
	private int maxPoolSize = 2000;
	private long keepAliveTime = 10;

	private ThreadPoolExecutor threadPoolExecutor;

	public void init() {
		threadPoolExecutor = new ThreadPoolExecutor(corePoolSize, maxPoolSize,
				keepAliveTime, TimeUnit.SECONDS,
				new SynchronousQueue<Runnable>(), new ThreadFactory() {
					AtomicInteger index = new AtomicInteger();

					public Thread newThread(Runnable r) {
						Thread thread = new Thread(r);
						thread.setDaemon(true);
						thread.setName("Ferrari-Job-Thread#"
								+ (index.incrementAndGet()));
						return thread;
					}
				}) {

			@Override
			protected void beforeExecute(Thread t, Runnable r) {
				ContainerJobManager.this.beforeRun(t, (ManagedJob) r);
				super.beforeExecute(t, r);
			}

			@Override
			protected void afterExecute(Runnable r, Throwable t) {
				ContainerJobManager.this.afterRun((ManagedJob) r, t);
				super.afterExecute(r, t);
			}

		};
	}

	/**
	 * job运行前的预操作
	 * 
	 * @param thread
	 * @param job
	 */
	private void beforeRun(Thread thread, ManagedJob job) {
		if (jobInfo2JobIdMap.containsKey(job.getJobInfo())) {
			jobInfo2JobIdMap.get(job.getJobInfo()).add(job.getUuid());
		} else {
			CopyOnWriteArrayList<String> jobIdList = new CopyOnWriteArrayList<String>();
			jobIdList.add(job.getUuid());
			List<String> list = jobInfo2JobIdMap.putIfAbsent(job.getJobInfo(),
					jobIdList);
			if (list != null) {
				list.add(job.getUuid());
			}
		}

		job2ThreadMap.put(job, thread);// 只在这里做put
	}

	/**
	 * job运行后的尾操作
	 * 
	 * @param job
	 * @param throwable
	 */
	private void afterRun(ManagedJob job, Throwable throwable) {
		 // run方法约定不会抛出异常，
        // 在ThreadPoolExecutor自己不抛出异常的情况下，Throwable 总是为空的
		if (throwable != null) {
            logger.error("throw a Throwable when run Job:"+job.toString(),throwable);
        }
		jobId2JobMap.remove(job.getUuid());
		if(jobInfo2JobIdMap.containsKey(job.getJobInfo())){
			jobInfo2JobIdMap.get(job.getJobInfo()).remove(job.getUuid());
		}
		if (job2ThreadMap.containsKey(job)) {
			job2ThreadMap.remove(job);
			Throwable t1 = job.getRunThrowable();
			if(t1 == null && throwable != null){//job运行没抛异常，其它原因导致异常，比如线程池
				job.setRunThrowable(throwable);
			}
			FeedbackHandler.getInstance().jobFinished2Feedback(job);
		}
	}

	/**
	 * 运行 job(异步)
	 * 
	 * @param uuid
	 * @param jobName
	 * @param returnUrllist
	 * @param beginTime
	 * @param jobParam
	 */
	public void runJob(String uuid, String jobName, List<String> returnUrllist,
			Date beginTime, Map<String, String> jobParam) {
		if (jobId2JobMap.containsKey(uuid)) {
			logger.warn("job already in running,jobid=" + uuid + ",jobName="
					+ jobName);
			return;
		}
		ManagedJob job = new ManagedJob(uuid, jobName, returnUrllist,
				beginTime, jobParam);
		job.init();
		jobId2JobMap.put(job.getUuid(), job);// 只在这里做put

		threadPoolExecutor.execute(job);
	}

	/**
	 * 终止job(同步)
	 * 
	 * @param jobuuid
	 */
	public void killJob(String runClass,String method) {
		if(StringUtils.isBlank(runClass) || StringUtils.isBlank(method)){
			return;
		}
		CopyOnWriteArrayList<String> jobIdList = jobInfo2JobIdMap.get(runClass+"_"+method);
		if(!jobIdList.isEmpty()){
			for(String uuid:jobIdList){
				ManagedJob job = jobId2JobMap.remove(uuid);
				if(job == null){
					jobIdList.remove(uuid);
					continue;
				}
				Thread t = job2ThreadMap.remove(job);
				if(t == null){
					jobIdList.remove(uuid);
					continue;
				}
				t.interrupt();
				jobIdList.remove(uuid);
			}
		}
	}
	
	/**
	 * 根据job id 终止任务(同步)
	 * @param id
	 */
	public void killJobById(String uuid,String runClass,String method){
		if(StringUtils.isBlank(runClass) || StringUtils.isBlank(method) || StringUtils.isBlank(uuid)){
			return;
		}
		CopyOnWriteArrayList<String> jobIdList = jobInfo2JobIdMap.get(runClass+"_"+method);
		if(!jobIdList.isEmpty()){
			if(jobIdList.contains(uuid)){
				ManagedJob job = jobId2JobMap.remove(uuid);
				if(job == null){
					jobIdList.remove(uuid);
					return;
				}
				Thread t = job2ThreadMap.remove(job);
				if(t == null){
					jobIdList.remove(uuid);
					return;
				}
				t.interrupt();
				jobIdList.remove(uuid);
			}
		}
	}

	/**
	 * 释放资源
	 */
	public void dispose() {
		List<Runnable> list = threadPoolExecutor.shutdownNow();
		// WARNING 还没有运行的Job，在停止时，只是打Log。
		for (Runnable r : list) {
			ManagedJob job = (ManagedJob) r;
			logger.warn("Cancel Job when shutdown," + job.toString());
		}

		// 对于正在运行的Job，先不处理
	}

}
