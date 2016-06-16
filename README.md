# Ferrari说明
#### 如果阅读完文档后，还有任何疑问，请mail to [zjytk05@163.com,931591021@qq.com]

**Ferrari**是一种云调度服务平台，基于quartz实现定时调度。ferrari本身不执行任何任务，只是进行触发任务执行（通过http远程触发）.

## ======Quick Start======

### =======调度中心设置========
调度中心的应用为ferrari-admin-web，修改如下设置后即可启动。
#### 1. 创建数据表
根据ferrari-parent中提供的tables_mysql_ferrari.sql创建数据表，
建议对表FRI_QRTZ_TriggerLog、FRI_QRTZ_JobInfo做适当的索引，对应的sql语句可以查看
```
FerrariJobInfoMapper.xml,FerrariJobLogMapper.xml
```

#### 2. 邮件报警设置
邮件报警，默认使用DefaultAlarmServiceImpl，如果要个性化，请实现报警接口:
```
com.cip.ferrari.admin.alarm.AlarmService
```

使用默认DefaultAlarmServiceImpl的话，请更改com.cip.ferrari.admin.alarm.mail.MailSender的如下初始化部分：

```
static {
		fromName = "";
		fromAddress = "xxx@16.com"; //for example: tk_yuan@126.com
		host = "smtp.126.com";
		port = 25;
		userName = "xxx@16.com";//for example: tk_yuan@126.com
		password = "1234";// mail password
	}
```
#### 3. 分组设置
随着任务的增多，为了方便管理任务分组，可以在如下枚举类中新增分组：
```
com.cip.ferrari.admin.common.JobGroupEnum
```

如果为了后续方便，也可自行改成配置的方式。

###======任务执行应用方接入配置========
任务应用方只需按照如下步骤，即可接入.
#### Step一. 依赖

```
<groupId>com.dianping</groupId>
<artifactId>ferrari-core</artifactId>
<version>1.2.4</version>
```
#### Step二. 配置web.xml

```
<servlet>
     <servlet-name>FerrariServlet</servlet-name>
     <servlet-class>com.cip.ferrari.core.FerrariDirectServlet</servlet-class>
     <load-on-startup>1</load-on-startup>
</servlet>
<servlet-mapping>
     <servlet-name>FerrariServlet</servlet-name>
     <url-pattern>/ferrarijob/*</url-pattern>
</servlet-mapping>
```

其中的 **url-pattern** 与ferrari-admin-web中的 **ferrari.properties** 中的receive_servletpath配置值一致。

#### Step三. 任务类信息配置
完成以上2步后，开始写你的任务类及方法，类名、方法、入参等信息在调度中心(ferrari-admin-web)的新增任务界面配置。

**注意点:**

应用中的任务类是 ***[多例]***

## ======调度中心查看job执行业务日志=====
如果想在调度中心查看job执行的业务日志，则需增加如下log appender配置:

```
<appender name="FERRARI" class="com.cip.ferrari.core.log.FerrariFileAppender">
        <param name="filePath" value="/data/applogs/ferrari/"/>
        <param name="append" value="true"/>
        <param name="encoding" value="UTF-8"/>
        <layout class="org.apache.log4j.PatternLayout">
            <param name="ConversionPattern" value="%-d{yyyy-MM-dd HH:mm:ss} [%c]-[%t]-[%M]-[%L]-[%p] %m%n"/>
        </layout>
    </appender>
```
其中，filePath 是文件夹路径.

ferrari调度中心的设计实现原理请查看[开源中国博客](http://my.oschina.net/tkyuan/blog/678001)

谢谢!
