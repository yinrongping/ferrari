/**
 * 
 */
package com.cip.ferrari.admin.alarm;

import javax.annotation.Resource;

import com.cip.ferrari.admin.alarm.mail.MailEntity;
import com.cip.ferrari.admin.alarm.mail.MailSender;

/**
 * @author yuantengkai
 *
 */
public class DefaultAlarmServiceImpl implements AlarmService{
	
	public static final String BeanName = "defaultAlarmService";
	
	@Resource
	private MailSender mailSender;

	@Override
	public void sendMail(String[] toAddr, String subject, String content) {
		if(toAddr == null){
			return;
		}
		MailEntity mail = new MailEntity();
		mail.setToAddress(toAddr);
		mail.setSubject(subject);
		mail.setContent(content);
		mail.setContentType("text/plain");
		mailSender.send(mail);
	}

}
