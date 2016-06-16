<!DOCTYPE html>
<html>
<head>
  	<title>调度中心</title>
  	<#import "/common/common.macro.ftl" as netCommon>
	<@netCommon.commonStyle />
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
			<h1>使用说明</h1>
			<!--
			<ol class="breadcrumb">
				<li><a><i class="fa fa-dashboard"></i>调度中心</a></li>
				<li class="active">使用教程</li>
			</ol>
			-->
		</section>

		<!-- Main content -->
		<section class="content">
			<div class="callout callout-info">
				<h4>简介</h4>
				<p>Ferrari是一种云调度集群管理平台，基于quartz实现定时调度。ferrari本身不执行任何任务，只是进行触发任务执行(通过http远程触发).</p>
				<p>1、简单：通过Web页面对任务进行操作，简单易上手</p>
				<p>2、动态：支持动态修改任务状态，暂停/恢复/执行任务，即时生效</p>
				<p>3、集群：任务信息持久化到db中，支持Job服务器集群(高可用)，一个任务只会在其中一台服务器上执行</p>
            </div>
            
            <!--
            <div class="callout callout-default">
				<h4>特点：</h4>
				<p>1、简单：支持通过Web页面对任务进行CRUD操作，操作简单，一分钟上手.</p>
				<p>2、动态：支持动态修改任务状态，动态暂停/恢复任务，即时生效.</p>
				<p>3、集群：任务信息持久化到db中，支持Job服务器集群(高可用)，一个任务只会在其中一台服务器上执行.</p>
            </div>
            
            <div class="callout callout-default">
				<h4>分层模型：</h4>
				<p>1、基础：基于quartz封装调度层，通过CRON自定义任务执行时间，最终执行自定义JobBean的execute方法，如需多个任务，需要开发多个JobBean实现.</p>
				<p>2、分层：上述基础调度模型存在一定局限，调度层和任务层耦合，当新任务上线势必影响任务的正常调度，因此规划将调度系统分层为：调度层 + 任务层 + 通讯层.</p>
				<p>
				 	<div class="row">
				      	<div class="col-xs-offset-1 col-xs-11">
				      		<p>》调度模块：维护任务的调度信息，负责定时/周期性的发出调度请求.</p>
							<p>》任务模块：具体的任务逻辑，负责接收调度模块的调度请求，执行任务逻辑.</p>
							<p>》通讯模块：负责调度模块和任务模块之间的通讯.</p>
							<p>(总而言之，一条完整任务由 “调度信息” 和 “任务信息” 组成.)</p>
				      	</div>      
			   		</div>
				</p>
            </div>
            -->
            
            <div class="callout callout-default">
				<h4>新增任务属性说明</h4>
				<p>1、任务组【必选】：任务分组</p>
				<p>2、任务名【必填】：任务名称，同一分组内唯一标识</p>
				<p>3、任务Cron【必填】：任务执行的时间表达式(quartz格式)</p>
				<p>4、任务描述【必填】：任务的简述</p>
				<p>5、任务机器【必填】：任务所在机器的ip地址，比如127.0.0.1:8080</p>
				<p>6、期望执行的类名【必填】：任务class名，包含package</p>
				<p>7、期望执行的方法【必填】：方法名</p>
				<p>8、期望执行的方法入参【选填】：方法入参，多个参数用,分隔</p>
				<p>9、负责人【必填】：任务负责人</p>
				<p>10、邮件联系人【必填】：任务报警邮件收件人，存在多个时用,分隔</p>
				<p>11、失败报警阀值【必填】：连续失败超过该阀值时发送报警邮件</p>
				<p>备注:
					<p style="padding-left:30px;">Quartz格式: [秒] [分] [小时] [日] [月] [周] [年] <a href="http://www.jeasyuicn.com/cron/" target="_blank" style="color:rgb(15, 163, 221);">Quartz Cron在线工具</a></p>
					<p style="padding-left:30px;">* : 所有值;</p>
					<p style="padding-left:30px;">? : 不定值,即不关心它为何值;</p>
					<p style="padding-left:30px;">- : 区间值,表示一个指定的范围;</p>
					<p style="padding-left:30px;">, : 表示附加一个可能值;</p>
					<p style="padding-left:30px;">/ : 符号前表示开始时间，符号后表示每次递增的值;</p>
					样例:
					<p style="padding-left:30px;">0 15 10 * * ? 每天10点15分触发</p>
					<p style="padding-left:30px;">0 * 14 * * ? 每天下午的 2点到2点59分每分触发</p>
					<p style="padding-left:30px;">0 0/5 14 * * ? 每天下午的 2点到2点59分(整点开始，每隔5分触发)</p>
				</p>
            </div>
            
            <div class="callout callout-default">
				<h4>调度列表属性说明</h4>
				<p>1、状态：PAUSED->暂停调度中;NORMAL->调度中</p>
				<p>2、操作[恢复]：将暂停调度中的任务恢复到调度中</p>
				<p>3、操作[暂停]：将调度中的任务暂停调度</p>
				<p>4、操作[执行]：手动触发一次任务执行</p>
				<p>5、操作[编辑]：更改任务信息</p>
				<p>6、操作[删除]：删除此任务</p>
            </div>
            
            <div class="callout callout-default">
				<h4>任务应用方接入说明</h4>
				<p>1、maven依赖
				<p>&ltdependency></p>
    			<p style="padding-left:30px;">&ltgroupId>com.dianping&lt/groupId></p>
    			<p style="padding-left:30px;">&ltartifactId>ferrari-core&lt/artifactId></p>
    			<p style="padding-left:30px;">&ltversion>1.2.4&lt/version></p>
   	 			<p>&lt/dependency></p>
   	 			</p>
				<p>2、web.xml配置servlet入口
				<p>&ltservlet><p/>
				<p style="padding-left:30px;">&ltservlet-name>FerrariServlet&lt/servlet-name></p>
				<p style="padding-left:30px;">&ltservlet-class>com.cip.ferrari.core.FerrariDirectServlet&lt/servlet-class></p>
				<p style="padding-left:30px;">&ltload-on-startup>1&lt/load-on-startup></p>
				<p>&lt/servlet><p/>

				<p>&ltservlet-mapping></p>
				<p style="padding-left:30px;"> &ltservlet-name> FerrariServlet&lt/servlet-name></p>
				<p style="padding-left:30px;"> &lturl-pattern>/ferrarijob/*&lt/url-pattern></p>
				<p>&lt/servlet-mapping></p>
				</p>
				<p>3、开始写你的任务类及方法，类名、方法、入参在新增任务时配置</p>
            </div>
            
            <div class="callout callout-default">
				<h4>调度中心查看job执行业务日志接入说明</h4>
				<p>1、log4j.xml中增加一个append配置
				<p>&ltappender name="FERRARI" class="com.cip.ferrari.core.log.FerrariFileAppender"></p>
        		<p style="padding-left:30px;">&ltparam name="filePath" value="/data/applogs/ferrari/"/></p>
        		<p style="padding-left:30px;">&ltparam name="append" value="true"/></p>
        		<p style="padding-left:30px;">&ltparam name="encoding" value="UTF-8"/></p>
        		<p style="padding-left:30px;">&ltlayout class="org.apache.log4j.PatternLayout"></p>
            	<p style="padding-left:60px;">&ltparam name="ConversionPattern" value="%-d{yyyy-MM-dd HH:mm:ss} [%c]-[%t]-[%M]-[%L]-[%p] %m%n"/></p>
        		<p style="padding-left:30px;">&lt/layout></p>
    			<p>&lt/appender></p>
   	 			</p>
   	 			<p>其中，filePath 是文件夹路径.</p>
            </div>
            
            <div class="callout callout-default">
				<h4>联系我们</h4>
				<p>zjytk05@163.com,931591021@qq.com</p>
            </div>
		</section>
		<!-- /.content -->
	</div>
	<!-- /.content-wrapper -->
	
	<!-- footer -->
	<@netCommon.commonFooter />
</div>
<@netCommon.commonScript />
</body>
</html>
