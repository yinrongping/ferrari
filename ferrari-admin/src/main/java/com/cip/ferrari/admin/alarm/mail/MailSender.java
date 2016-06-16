/**
 * 
 */
package com.cip.ferrari.admin.alarm.mail;

import java.util.Date;
import java.util.Properties;
import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yuantengkai 邮件发送器
 */
public class MailSender {

	private static final Logger logger = LoggerFactory
			.getLogger(MailSender.class);

	private final static String DEFAULT_CONTENT_TYPE = "text/plain";

	/**
	 * SMTP服务器地址及端口
	 */
	private static String host;
	private static int port;

	/**
	 * SMTP服务器是否需要验证
	 */
	private boolean auth = true;

	/**
	 * 发送帐户的用户名和密码
	 */
	private static String userName;
	private static String password;

	/**
	 * 发件人名称
	 */
	private static String fromName;

	/**
	 * 发件人地址
	 */
	private static String fromAddress;

	/**
	 * 超时设置
	 */
	private long connectionTimeout = 60000;
	private long timeout = 60000;

	static {
		fromName = "";
		fromAddress = "xxx@16.com"; //for example: tk_yuan@126.com
		host = "smtp.126.com";
		port = 25;
		userName = "xxx@16.com";//for example: tk_yuan@126.com
		password = "1234";// mail password
	}

	/**
	 * 邮件发送入口
	 * 
	 * @param mailEntity
	 * @return
	 * @throws MessagingException
	 * @throws AddressException
	 */
	public boolean send(MailEntity mailEntity) {
		Session session = this.createSession();
		MimeMessage mimeMessage = new MimeMessage(session);
		try {
			this.setMailFrom(mimeMessage);
			this.setMailTo(mimeMessage, mailEntity);
			this.setMailCC(mimeMessage, mailEntity);
			this.setMailSubject(mimeMessage, mailEntity);
			this.setMailContent(mimeMessage, mailEntity);
			mimeMessage
					.setSentDate(mailEntity.getGmtSend() == null ? new Date()
							: mailEntity.getGmtSend());
			Transport.send(mimeMessage);
			return true;
		} catch (AddressException e) {
			logger.error("邮件发送失败AddressException," + mailEntity, e);
		} catch (MessagingException e) {
			logger.error("邮件发送失败MessagingException," + mailEntity, e);
		}
		return false;
	}

	private Session createSession() {
		Properties props = new Properties();
		props.setProperty("mail.smtp.host", host);
		props.setProperty("mail.smtp.port", Integer.toString(port));
		props.setProperty("mail.smtp.auth", Boolean.toString(auth));
		props.setProperty("mail.smtp.connectiontimeout",
				Long.toString(connectionTimeout));
		props.setProperty("mail.smtp.timeout", Long.toString(timeout));
//		props.setProperty("mail.smtp.starttls.enable", "true");

		Session session = Session.getInstance(props, new Authenticator() {

			protected PasswordAuthentication getPasswordAuthentication() {
				return new PasswordAuthentication(userName, password);
			}
		});
		return session;
	}

	/**
	 * 设置邮件发送者
	 * 
	 * @param mimeMessage
	 * @throws MessagingException
	 * @throws AddressException
	 */
	private void setMailFrom(MimeMessage mimeMessage)
			throws MessagingException, AddressException {
		String from = null;
		if (StringUtils.isBlank(fromName)) {
			from = fromAddress;
		} else {
			String name = fromName.replace("\\", "\\\\");
			name = name.replace("\"", "\\\"");
			from = "\"" + name + "\" <" + fromAddress + ">";
		}
		mimeMessage.setFrom(new InternetAddress(from));
	}

	/**
	 * 设置邮件接收者
	 * 
	 * @param mimeMessage
	 * @param mailEntity
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void setMailTo(MimeMessage mimeMessage, MailEntity mailEntity)
			throws AddressException, MessagingException {
		String[] receivers = mailEntity.getToAddress();
		if (receivers == null) {
			return;
		}

		InternetAddress[] toAddress = new InternetAddress[receivers.length];
		for (int i = 0; i < receivers.length; i++) {
			toAddress[i] = new InternetAddress(receivers[i]);
		}

		mimeMessage.setRecipients(Message.RecipientType.TO, toAddress);// 收件人邮件
	}

	/**
	 * 设置抄送者
	 * 
	 * @param mimeMessage
	 * @param mailEntity
	 * @throws AddressException
	 * @throws MessagingException
	 */
	private void setMailCC(MimeMessage mimeMessage, MailEntity mailEntity)
			throws AddressException, MessagingException {
		String[] receivers = mailEntity.getCcAddress();
		if (receivers == null) {
			return;
		}

		InternetAddress[] toAddress = new InternetAddress[receivers.length];
		for (int i = 0; i < receivers.length; i++) {
			toAddress[i] = new InternetAddress(receivers[i]);
		}

		mimeMessage.setRecipients(Message.RecipientType.CC, toAddress);// 收件人邮件
	}

	/**
	 * 设置邮件标题
	 * 
	 * @param mimeMessage
	 * @param mailEntity
	 * @throws MessagingException
	 */
	private void setMailSubject(MimeMessage mimeMessage, MailEntity mailEntity)
			throws MessagingException {
		mimeMessage
				.setSubject(mailEntity.getSubject(), mailEntity.getCharset());
	}

	/**
	 * 设置邮件内容
	 * @param mimeMessage
	 * @param mailEntity
	 * @throws MessagingException
	 */
	private void setMailContent(MimeMessage mimeMessage, MailEntity mailEntity)
			throws MessagingException {
		// mixed - 混合类型可带附件
		// 务必是MimeMultipart
		MimeMultipart multipart = new MimeMultipart("mixed");
		MimeBodyPart body = new MimeBodyPart();
		String contentType = mailEntity.getContentType();
		if (StringUtils.isBlank(contentType)) {
			contentType = DEFAULT_CONTENT_TYPE;
		}
		body.setContent(mailEntity.getContent(), contentType + ";charset="
				+ mailEntity.getCharset());
		multipart.addBodyPart(body);// 发件内容

		// ~~~~~~~~~~~~~~~~处理附件-开始~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
		// String[] attachmentsPath = mail.getAttachmentsPath();
		// if (attachmentsPath != null && attachmentsPath.length > 0) {
		// for (String path : attachmentsPath) {
		// MimeBodyPart bodyPart = createAttachment(path);
		// multipart.addBodyPart(bodyPart);
		// }
		// }
		// ~~~~~~~~~~~~~~~~~处理附件-结束~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

		mimeMessage.setContent(multipart);
	}

	public static void main(String[] args) {
		MailSender mailSender = new MailSender();
		MailEntity mail = new MailEntity();
		mail.setSubject("成单短信发送报告");
		mail.setContent("本周发送的新预约用户短信数目为:"+10);
//		mail.setContent("<html>  <head>  <meta http-equiv='Content-Type' content='text/html; charset=utf-8' />  <style>  td {  background-color:#ffffff;  }  tr {  background-color:#ffffff;  }  .title {  font-weight:bold;  background-color:#F2DBDB;  }  </style>  </head>  <body>  <table border='1' cellspacing='1' cellpadding='0' bgcolor='#ffffff' width='950px;' style='font-size:13px;'>  <tr>  <td colspan='8' height='20px;'><div align='center' style='font-size:18px;font-family:黑体; margin:10px;'>[56Eye]今日聚划算活动订单报告 - 2011-10-09 11:33:02(星期日)</div></td>  </tr>  <tr>  <td colspan='8' ><span align='left'>分析时间: 2011-10-09 11:33:02</span><span align='left' style='margin-left:20px;'>分析范围：订单创建时间：2011-10-09 00:00:00 至  2011-10-09 11:13:02</span></td>  </tr>  <tr>  <td width='12.5%' class='title'><div align='center'>报警项目 </div></td>  <td colspan='7' ><ul><li>找不到外部SPU(1500002780422)和外部SKU(30001055802)的渠道订单</li><li>内部SKU(WL2602)的可销售库存0小于淘宝该SKU(30001055802)的库存92</li></ul></td>  </tr>  <tr> <td colspan='8' style='background-color:#DDD9C3;' ><div align='center'>供货商ID： 233; 外部SPU：1500002780422; 内部SPU：2474; 外部SKU：30001055802; 内部SKU：WL2602</div></td> </tr>  <tr>  <td width='12.5%' class='title'><div align='center'>渠道订单数量 </div></td>  <td width='12.5%' class='title'><div align='center'>已转化为物流订单 </div></td>  <td width='12.5%' class='title'><div align='center'>未转化为物流订单 </div></td>  <td width='12.5%' class='title'><div align='center'>关联销售 </div></td>  <td width='12.5%' class='title'><div align='center'>已发货 </div></td>  <td width='12.5%' class='title'><div align='center'>未发货 </div></td>  <td width='12.5%' class='title'><div align='center'>淘宝网可销售库存 </div></td>  <td width='12.5%' class='title'><div align='center'>系统可销售库存 </div></td>  </tr>  <tr>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  <td width='12.5%' ><div align='center'>92</div></td>  <td width='12.5%' ><div align='center'>0</div></td>  </tr>  <tr>  <td width='12.5%' class='title'><div align='center'>转物流订单率 </div></td>  <td width='12.5%'><div align='center'>?%</div></td>  <td width='12.5%'><div align='center'>关联销售率</div></td>  <td width='12.5%'><div align='center'>?%</div></td>  <td width='12.5%' class='title''><div align='center'>发货率 </div></td>  <td width='12.5%'><div align='center'>?%</div></td>  <td width='12.5%' class='title'><div align='center'>库存预警 </div></td>  <td width='12.5%'><div align='center'><font style='color:red;'>报警</font></div></td>  </tr>  <tr>  <td width='12.5%' class='title' ><div align='center'>备注说明 </div></td>  <td colspan='7' ><div align='center'>&nbsp;</div></td>  </tr>  </table>  </body>  </html>   ");
		mail.setContentType("text/plain");
		mail.setToAddress(new String[] { "tengkai.yuan@dianping.com"});
		boolean success = mailSender.send(mail);
		System.out.println(success);
	}

}
