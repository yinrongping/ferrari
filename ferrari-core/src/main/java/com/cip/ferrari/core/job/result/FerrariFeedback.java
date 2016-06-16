/**
 * 
 */
package com.cip.ferrari.core.job.result;

import com.cip.ferrari.core.job.DirectionType;

/**
 * @author yuantengkai
 * 返回结果类
 */
public class FerrariFeedback {
	
	//返回的Job运行的身份证信息  
    private String             uuid;
	
	//应用端运行Job是否成功
    private boolean            status;

    //表示应用端接收的指令类型
    private DirectionType      directionType;

    //错误信息
    private String             errormsg;

    //返回的内容
    private String             content;
    
	public String getUuid() {
		return uuid;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public boolean isStatus() {
		return status;
	}

	public void setStatus(boolean status) {
		this.status = status;
	}

	public DirectionType getDirectionType() {
		return directionType;
	}

	public void setDirectionType(DirectionType directionType) {
		this.directionType = directionType;
	}

	public String getErrormsg() {
		return errormsg;
	}

	public void setErrormsg(String errormsg) {
		this.errormsg = errormsg;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
