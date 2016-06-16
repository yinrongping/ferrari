/**
 * 
 */
package com.cip.ferrari.core.job;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;
import com.cip.ferrari.core.common.HttpUtil;
import com.cip.ferrari.core.common.JobConstants;
import com.cip.ferrari.core.job.result.FerrariFeedback;

/**
 * @author yuantengkai job执行完后的反馈处理handler
 */
public class FeedbackHandler {

	private static final Logger logger = LoggerFactory
			.getLogger(FeedbackHandler.class);

	private final String RESPONSE_OK = "ok";

	private static final FeedbackHandler instance = new FeedbackHandler();

	private final BlockingQueue<ManagedJob> feedbackQueue = new LinkedBlockingQueue<ManagedJob>(
			10000);

	private final BlockingQueue<RetrySendJob> resendQueue = new LinkedBlockingQueue<RetrySendJob>(
			20000);

	private final long[] resendTimePoints = { 1000 * 5, 1000 * 30, 1000 * 120,
			1000 * 240 };

	private FeedbackHandler() {
		this.init();
	}

	private void init() {
		Thread t = new Thread(new Runnable() {

			@Override
			public void run() {
				FeedbackHandler.this.dealSendBack();
			}
		});
		t.setName("Ferrari-Feedback-Thread");
		t.setDaemon(true);
		t.start();

		Thread retry = new Thread(new Runnable() {

			@Override
			public void run() {
				FeedbackHandler.this.retrySendBack();
			}
		});
		retry.setName("Ferrari-RetryFeedback-Thread");
		retry.setDaemon(true);
		retry.start();
	}

	public static FeedbackHandler getInstance() {
		return instance;
	}

	/**
	 * job执行完后的反馈
	 * 
	 * @param job
	 * @return
	 */
	public boolean jobFinished2Feedback(ManagedJob job) {
		boolean success = feedbackQueue.offer(job);
		if (success) {
			return true;
		} else {
			try {
				return feedbackQueue.offer(job, 200, TimeUnit.MILLISECONDS);
			} catch (InterruptedException e) {
				logger.error("job finished to offer feedbackQueue failed,"
						+ job.toString(), e);
				return false;
			}
		}
	}

	/**
	 * 反馈处理
	 */
	private void dealSendBack() {
		while (true) {
			ManagedJob job = null;
			try {
				job = feedbackQueue.take();
				String result = getFeedbackJson(job);
				Map<String, String> httpParams = new HashMap<String, String>();
				httpParams.put(JobConstants.KEY_FEEDBACK_RESULT, result);

				if (logger.isInfoEnabled()) {
					logger.info("start send job finish feedback:" + result);
				}

				// http发送
				boolean sendSuccess = doSend(job, httpParams,
						job.getReturnUrllist());

				if (!sendSuccess) {// 反馈失败
					throw new RuntimeException("send feedback failed,"
							+ job.toString());
				}
			} catch (Throwable t) {
				logger.error("job finished, feedback deal happens exception.",
						t);
				if (job != null) {// 重试发送
					resendQueue.offer(new RetrySendJob(job));
				}
			}
		}
	}

	/**
	 * 重试反馈处理
	 */
	private void retrySendBack() {
		while (true) {
			RetrySendJob retryJob = null;
			try {
				retryJob = resendQueue.take();
				if (System.currentTimeMillis() >= retryJob.getResendTime()) {
					if (retryJob.getRetryTimes() >= resendTimePoints.length) {
						logger.error("Fail to resend Job in many times,"
								+ retryJob);
						continue;
					}
					ManagedJob job = retryJob.getJob();
					String result = getFeedbackJson(job);
					Map<String, String> httpParams = new HashMap<String, String>();
					httpParams.put(JobConstants.KEY_FEEDBACK_RESULT, result);

					if (logger.isInfoEnabled()) {
						logger.info("start resend job feedback:" + retryJob);
					}
					// http发送
					boolean sendSuccess = doSend(job, httpParams,
							job.getReturnUrllist());
					if (!sendSuccess) {// 反馈失败
						retryJob.addRetryTimesAndSetSendTime();
						resendQueue.offer(retryJob);
					}
				} else {// 还没到发送时间
					resendQueue.offer(retryJob);
				}
			} catch (Throwable t) {
				logger.error("job feedback retrySend happens exception."
						+ retryJob.toString(), t);
				if (retryJob != null) {// 重试发送
					retryJob.addRetryTimesAndSetSendTime();
					resendQueue.offer(retryJob);
				}
			} finally {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					// ignore it
				}
			}
		}
	}

	private boolean doSend(ManagedJob job, Map<String, String> httpParams,
			List<String> urlList) {
		for (String url : urlList) {
			try {
				String urlTmp = "http://" + url + "/joblog/ferrarifeedback";
				String resp = HttpUtil.sendHttpPost(urlTmp, httpParams);
				if (RESPONSE_OK.equalsIgnoreCase(resp)) {
					if (logger.isInfoEnabled()) {
						logger.info("send feedback{} success with job{}.",
								urlTmp, job.toString());
					}
					return true;
				}
				logger.warn("send feedback{} failed with job{},response{}",
						new Object[] { urlTmp, job.toString(), resp });
			} catch (Exception e) {
				logger.error("send feedback happens exception,url=" + url
						+ ",job:" + job.toString(), e);
			}
		}
		return false;
	}

	private String getFeedbackJson(ManagedJob job) {
		FerrariFeedback feedback = new FerrariFeedback();
		feedback.setStatus(true);
		feedback.setUuid(job.getUuid());
		feedback.setDirectionType(DirectionType.RETURN_JOB_RESULT);
		if (job.getRunThrowable() != null) {
			feedback.setStatus(false);
			// 使用Exception更详细的说明信息（包含了cause exception的信息）
			StringBuilder builder = new StringBuilder(512);
			for (Throwable t = job.getRunThrowable(); null != t; t = t
					.getCause()) {
				builder.append(t.toString()).append("\n");
			}
			feedback.setErrormsg(StringUtils.substring(builder.toString(), 0,
					2048));
		}

		// 当run method 的返回值类型是 void时，result是null
		if (job.getReturnObject() != null) {
			feedback.setContent(job.getReturnObject().toString());
		}
		return JSON.toJSONString(feedback);
	}

	/**
	 * 反馈重发job类
	 * 
	 * @author yuantengkai
	 *
	 */
	private class RetrySendJob {

		private int retryTimes;
		private long resendTime;
		private ManagedJob job;

		public RetrySendJob(ManagedJob job) {
			this.retryTimes = 0;
			this.resendTime = System.currentTimeMillis()
					+ resendTimePoints[retryTimes];
			this.job = job;
		}

		public int getRetryTimes() {
			return retryTimes;
		}

		public long getResendTime() {
			return resendTime;
		}

		public ManagedJob getJob() {
			return job;
		}

		public void addRetryTimesAndSetSendTime() {
			this.retryTimes++;
			if (retryTimes >= resendTimePoints.length) {
				this.resendTime = System.currentTimeMillis()
						+ resendTimePoints[resendTimePoints.length - 1];
			} else {
				this.resendTime = System.currentTimeMillis()
						+ resendTimePoints[retryTimes];
			}
		}

		@Override
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("RetrySendJob[");
			sb.append("retryTimes=").append(retryTimes).append(",");
			sb.append("resendTime=").append(new Date(resendTime)).append(",");
			sb.append("job=").append(job.toString()).append("]");
			return sb.toString();
		}

	}

}
