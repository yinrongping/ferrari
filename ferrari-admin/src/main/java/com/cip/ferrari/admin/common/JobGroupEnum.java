/**
 * 
 */
package com.cip.ferrari.admin.common;

/**
 * @author yuantengkai
 * job组枚举
 */
public enum JobGroupEnum {
	
	DEFAULT("默认"),
	GROUP1("组1"),
	GROUP2("组2"),
	GROUP3("组3");
	
	private String desc;
	
	private JobGroupEnum(String desc){
		this.desc = desc;
	}
	public String getDesc() {
		return desc;
	}
	public void setDesc(String desc) {
		this.desc = desc;
	}
	public static JobGroupEnum match(String name){
		for (JobGroupEnum item : JobGroupEnum.values()) {
			if (item.name().equals(name)) {
				return item;
			}
		}
		return null;
	}

}
