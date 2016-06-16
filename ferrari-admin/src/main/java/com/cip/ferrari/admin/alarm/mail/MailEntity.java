/**
 * 
 */
package com.cip.ferrari.admin.alarm.mail;

import java.io.Serializable;
import java.util.Date;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author yuantengkai 邮件对象
 */
public class MailEntity implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7820492810502327894L;

	/**
	 * 编码格式
	 */
	private String charset = "utf-8";

	/**
	 * 收件人、抄送地址
	 */
	private String[] toAddress;
	private String[] ccAddress;

	/**
	 * 发送时间
	 */
	private Date gmtSend;

	/**
	 * 主题
	 */
	private String subject;

	/**
	 * 内容
	 */
	private String content;

	/**
	 * 内容类型，例如：text/html、text/plain
	 */
	private String contentType;

	
	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public String[] getToAddress() {
		return toAddress;
	}

	public void setToAddress(String[] toAddress) {
		this.toAddress = toAddress;
	}

	public String[] getCcAddress() {
		return ccAddress;
	}

	public void setCcAddress(String[] ccAddress) {
		this.ccAddress = ccAddress;
	}

	public Date getGmtSend() {
		return gmtSend;
	}

	public void setGmtSend(Date gmtSend) {
		this.gmtSend = gmtSend;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getContentType() {
		return contentType;
	}

	public void setContentType(String contentType) {
		this.contentType = contentType;
	}

	public String toString() {
		return ToStringBuilder.reflectionToString(this,
				ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
