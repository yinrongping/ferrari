<!DOCTYPE html>
<html>
<head>
  	<title>调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
	<!-- DataTables -->
  	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.css">
  	<!-- daterangepicker -->
  	<link rel="stylesheet" href="${request.contextPath}/static/adminlte/plugins/daterangepicker/daterangepicker-bs3.css">
</head>
<body class="hold-transition skin-blue sidebar-mini">
<div class="wrapper">
	<!-- header -->
	<@netCommon.commonHeader />
	<!-- left -->
	<@netCommon.commonLeft />
	
	<!-- Content Wrapper. Contains page content -->
	<div class="content-wrapper">
		<!-- Content Header (Page header) -->
		<section class="content-header">
			<h1>调度日志</h1>
		</section>
		
		<!-- Main content -->
	    <section class="content">
	    	<div class="row">
	            <div class="col-xs-4">
              		<div class="input-group">
                		<span class="input-group-addon">时间</span>
	                	<input type="text" class="form-control" id="filterTime" readonly 
	                		value="<#if triggerTimeStart?exists && triggerTimeEnd?exists >${triggerTimeStart?if_exists?string('yyyy-MM-dd HH:mm:ss')} - ${triggerTimeEnd?if_exists?string('yyyy-MM-dd HH:mm:ss')}</#if>"  >
	              	</div>
	            </div>
	            <div class="col-xs-3">
	              	<div class="input-group">
	                	<span class="input-group-addon">任务组</span>
                		<select class="form-control" id="jobGroup" >
                			<#list groupEnum as group>
                				<option value="${group}" <#if jobInfo?exists && group == jobInfo.jobGroup>selected</#if> >${group.desc}</option>
                			</#list>
	                  	</select>
	              	</div>
	            </div>
	            <div class="col-xs-3">
	              	<div class="input-group">
	                	<span class="input-group-addon">任务名</span>
	                	<input type="text" class="form-control" id="jobName" value="<#if jobInfo?exists>${jobInfo.jobName}</#if>" autocomplete="on" >
	              	</div>
	            </div>
	            <div class="col-xs-2">
	            	<button class="btn btn-block btn-info" id="searchBtn">搜索</button>
	            </div>
          	</div>
			
			<div class="row">
				<div class="col-xs-12">
					<div class="box">
					<!--
			            <div class="box-header"><h3 class="box-title">调度日志</h3></div>
			            -->
			            <div class="box-body">
			              	<table id="joblog_list" class="table table-bordered table-striped display" width="100%" >
				                <thead>
					            	<tr>
					                	<th>id</th>
					                  	<th class="jobGroup" >任务组</th>
					                  	<th class="jobName" >任务名称</th>
					                  	<th class="jobCron" >任务Cron</th>
					                  	<th class="jobClass" >任务类型</th>
					                  	<th class="jobData" >任务数据</th>
					                  	<th class="triggerTime" >调度时间</th>
					                  	<th class="triggerStatus" >调度状态</th>
					                  	<th class="triggerMsg" >调度结果</th>
					                  	<th class="handleTime" >执行时间</th>
					                  	<th class="handleStatus" >执行状态</th>
					                  	<th class="handleMsg" >执行结果</th>
					                  	<th class="handleMsg" >操作</th>
					                </tr>
				                </thead>
				                <tbody></tbody>
							</table>
						</div>
					</div>
				</div>
			</div>
	    </section>
	</div>
	
	<!-- footer -->
	<@netCommon.commonFooter />
</div>

<@netCommon.commonScript />
<@netCommon.comAlert />
<!-- DataTables -->
<script src="${request.contextPath}/static/adminlte/plugins/datatables/jquery.dataTables.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/datatables/dataTables.bootstrap.min.js"></script>
<!-- daterangepicker -->
<script src="${request.contextPath}/static/adminlte/plugins/daterangepicker/moment.min.js"></script>
<script src="${request.contextPath}/static/adminlte/plugins/daterangepicker/daterangepicker.js"></script>
<script>var base_url = '${request.contextPath}';</script>
<script src="${request.contextPath}/static/js/joblog.index.1.js"></script>
</body>
</html>
