/**
 * 
 */
package com.cip.ferrari.core.job;

/**
 * @author yuantengkai
 * 调度中心和container的交互指令
 */
public enum DirectionType {

	/**
     * 运行Job
     */
    RUN_JOB,
    /**
     * 终止Job运行
     */
    KILL_JOB,
    /**
     * 返回Job的运行结果
     */
    RETURN_JOB_RESULT,
    /**
     * 运行日志
     */
    RUN_LOG
    
}
