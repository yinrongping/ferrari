/**
 * 
 */
package com.cip.ferrari.core.common;

/**
 * @author yuantengkai job常量
 */
public class JobConstants {

	// =============调度中心向job container发送的key===============
	/**
	 * 执行命令，run代表运行，terminate代表终止,log代表运行日志
	 */
	public static final String KEY_ACTION = "action_type";

	/**
	 * 每一次job运行的身份证信息
	 */
	public static final String KEY_UUID = "uuid";

	/**
	 * job名称
	 */
	public static final String KEY_JOB_NAME = "job_name";

	/**
	 * 调度中心的ip端口地址,支持多个,逗号分隔
	 */
	public static final String KEY_RESULT_URL_LIST = "return_url_list";

	/**
	 * 期望执行的类，类的全称，包含package名
	 */
	public static final String KEY_RUN_CLASS = "run_class";

	/**
	 * 期望运行的方法
	 */
	public static final String KEY_RUN_METHOD = "run_method";

	/**
	 * 方法入参，多个参数用 , 分隔
	 */
	public static final String KEY_RUN_METHOD_ARGS = "run_method_args";
	
	/**
	 * 任务描述
	 */
	public static final String KEY_JOB_DESC = "job_desc";
	
	/**
	 * 任务机器地址,格式	IP:PORT
	 */
	public static final String KEY_JOB_ADDRESS = "job_address";
	
	/**
	 * job开始触发运行的时间，long变量的string
	 */
	public static final String KEY_BEGIN_TIME = "begin_time";
	
	/**
	 * job执行结束的时间
	 */
	public static final String KEY_EXECUTE_TIME = "execute_time";

	// ===========VLAUE For KEY_ACTION=================
	public static final String VALUE_ACTION_RUN_JOB = "run";
	public static final String VALUE_ACTION_KILL_JOB = "terminate";
	public static final String VALUE_ACTION_RUN_LOG = "log";
	
	//应用方向调度中心二次反馈的key
	public static final String KEY_FEEDBACK_RESULT       = "result";

}
