/**
 * 
 */
package com.cip.ferrari.admin.alarm;

/**
 * @author yuantengkai
 * 报警接口
 */
public interface AlarmService {

	/**
	 * 
	 * @param toAddr 收件人邮箱
	 * @param subject 标题
	 * @param content 内容
	 */
	public void sendMail(String[] toAddr,String subject, String content);
}
