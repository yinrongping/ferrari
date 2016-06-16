$(function() {
	// init date tables
	var jobTable = $("#job_list").dataTable({
		"deferRender": true,
		"processing" : true, 
	    "serverSide": true,
		"ajax": {
			url: base_url + "/job/pageList",
	        data : function ( d ) {
                d.jobGroup = $('#jobGroup').val();
                d.jobName = $('#jobName').val();
            }
	    },
	    "columns": [
	                { "data": 'id', "bSortable": false, "visible" : false},
	                { 
	                	"data": 'addTime', 
	                	"bSortable": false, 
	                	"visible" : false, 
	                	"render": function ( data, type, row ) {
	                		return data?moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss"):"";
	                	}
	                },
	                { 
	                	"data": 'updateTime', 
	                	"bSortable": false, 
	                	"visible" : false, 
	                	"render": function ( data, type, row ) {
	                		return data?moment(new Date(data)).format("YYYY-MM-DD HH:mm:ss"):"";
	                	}
	                },
	                { "data": 'jobGroup', "bSortable": false, "visible": false,
	                	"render": function ( data, type, row ) {
	                		return function(){
	                			var groupMenu = $("#jobGroup").find("option");
	                			for ( var index in $("#jobGroup").find("option")) {
	                				if ($(groupMenu[index]).attr('value') == data) {
										return $(groupMenu[index]).html();
									}
								}
	                			return data;
	                		};
	                	}
	                },
	                { 
	                	"data": 'jobName', 
	                	"bSortable": false, 
	                	"render": function ( data, type, row ) {
                			return data + '<br>' + moment(new Date(row.updateTime)).format("YYYY-MM-DD HH:mm:ss");
                		}
	                },
	                { "data": 'jobKey', "bSortable": false, "visible" : false},
	                { "data": 'jobDesc', "bSortable": false},
	                { "data": 'mailReceives', "bSortable": false, "visible" : false},
	                { "data": 'failAlarmNum', "bSortable": false, "visible" : false},
	                { "data": 'isDeleted', "bSortable": false, "visible" : false},
	                { "data": 'jobCron', "bSortable": false, "visible" : true},
	                { "data": 'jobClass', "bSortable": false, "visible" : false},
	                { "data": 'jobData', "bSortable": false, "visible" : true, 
	                	"render": function ( data, type, row ) {
	                		return function(){
	                			var _jobData = eval('(' + row.jobData + ')');
	                			return _jobData.job_address;
	                		};
	                	}
	                },
	                { 
	                	"data": 'jobStatus', 
	                	"bSortable": false, 
	                	"visible" : true, 
	                	"render": function ( data, type, row ) {
	                		return function(){
	                			if ('NORMAL' == data) {
	                				return '<span class="label label-success">NORMAL</span>';
	                			} else if ('PAUSED' == data){
	                				return '<span class="label label-warning">PAUSED</span>';
	                			}
	                			return data;
	                		};
	                	}
	                },
	                { "data": 'owner', "bSortable": false},
	                { "data": '操作' , "bSortable": false,
	                	"render": function ( data, type, row ) {
	                		return function(){
	                			// status
	                			var pause_resume = "";
	                			if ('NORMAL' == row.jobStatus) {
	                				pause_resume = '<button class="btn btn-info btn-xs job_operate" type="job_pause" type="button">暂停</button>  ';
								} else if ('PAUSED' == row.jobStatus){
									pause_resume = '<button class="btn btn-info btn-xs job_operate" type="job_resume" type="button">恢复</button>  ';
								}
	                			// log url
	                			var logUrl = base_url +'/joblog?jobInfoId='+ row.id;
	                			var _jobData = eval('(' + row.jobData + ')');
	                			var html = '<p jobKey="'+ row.jobKey +'" ' + 
				                			' jobGroup="'+ row.jobGroup +'" ' +
				                			' jobName="'+ row.jobName +'" ' +
			                				' cronExpression="'+ row.jobCron +'" ' +
	                						' jobDesc="'+ row.jobDesc +'" ' +
	                						' owner="'+ row.owner +'" ' +
	                						' mailReceives="'+ row.mailReceives +'" ' +
	                						' failAlarmNum="'+ row.failAlarmNum +'" ' +
	                						' job_address="'+ _jobData.job_address +'" ' +
	                						' run_class="'+ _jobData.run_class +'" ' +
	                						' run_method="'+ _jobData.run_method +'" ' +
	                						' run_method_args="'+ _jobData.run_method_args +'" ' +
	                						'>'+
	                					pause_resume +
										'<button class="btn btn-info btn-xs job_operate" type="job_trigger" type="button">执行</button>  '+
										'<button class="btn btn-info btn-xs update" type="button">编辑</button> <br> '+
									  	'<button class="btn btn-danger btn-xs job_operate" type="job_del" type="button">删除</button>  '+
									  	'<button class="btn btn-warning btn-xs" type="job_del" type="button" '+
									  		'onclick="javascript:window.open(\'' + logUrl + '\')" >调度日志</button>'+
									'</p>';
									
	                			return html;
	                		};
	                	}
	                }
	            ],
	    "searching": false,
	    "ordering": true,
		"language" : {
			"sProcessing" : "处理中...",
			"sLengthMenu" : "每页 _MENU_ 条记录",
			"sZeroRecords" : "没有匹配结果",
			"sInfo" : "第 _PAGE_ 页 ( 总共 _PAGES_ 页 )",
			"sInfoEmpty" : "无记录",
			"sInfoFiltered" : "(由 _MAX_ 项结果过滤)",
			"sInfoPostFix" : "",
			"sSearch" : "搜索:",
			"sUrl" : "",
			"sEmptyTable" : "表中数据为空",
			"sLoadingRecords" : "载入中...",
			"sInfoThousands" : ",",
			"oPaginate" : {
				"sFirst" : "首页",
				"sPrevious" : "上页",
				"sNext" : "下页",
				"sLast" : "末页"
			},
			"oAria" : {
				"sSortAscending" : ": 以升序排列此列",
				"sSortDescending" : ": 以降序排列此列"
			}
		}
	});
	$('#searchBtn').on('click', function(){
		jobTable.fnDraw();
	});
	
	// job operate
	$("#job_list").on('click', '.job_operate',function() {
		var typeName;
		var url;
		var type = $(this).attr("type");
		if ("job_pause" == type) {
			typeName = "暂停";
			url = base_url + "/job/pause";
		} else if ("job_resume" == type) {
			typeName = "恢复";
			url = base_url + "/job/resume";
		} else if ("job_del" == type) {
			typeName = "删除";
			url = base_url + "/job/remove";
		} else if ("job_trigger" == type) {
			typeName = "执行一次";
			url = base_url + "/job/trigger";
		} else {
			return;
		}
		
		var jobKey = $(this).parent('p').attr("jobKey");
		
		ComConfirm.show("确认" + typeName + "?", function(){
			$.ajax({
				type : 'POST',
				url : url,
				data : {"triggerKeyName":jobKey},
				dataType : "json",
				success : function(data){
					if (data.code == 200) {
						ComAlert.show(1, typeName + "成功", function(){
							jobTable.fnDraw();
						});
					} else {
						ComAlert.show(1, typeName + "失败");
					}
				},
			});
		});
	});
	
	// jquery.validate 自定义校验 “英文字母开头，只含有英文字母、数字和下划线”
	jQuery.validator.addMethod("myValid01", function(value, element) {
		var length = value.length;
		var valid = /^[a-zA-Z][a-zA-Z0-9_]*$/;
		return this.optional(element) || valid.test(value);
	}, "只支持英文字母开头，只含有英文字母、数字和下划线");
	
	// 新增-添加参数
	$("#addModal .addParam").on('click', function () {
		var html = '<div class="form-group newParam">'+
				'<label for="lastname" class="col-sm-2 control-label">参数&nbsp;<button class="btn btn-danger btn-xs removeParam" type="button">移除</button></label>'+
				'<div class="col-sm-4"><input type="text" class="form-control" name="key" placeholder="请输入参数key[将会强转为String]" maxlength="200" /></div>'+
				'<div class="col-sm-6"><input type="text" class="form-control" name="value" placeholder="请输入参数value[将会强转为String]" maxlength="200" /></div>'+
			'</div>';
		$(this).parents('.form-group').parent().append(html);
		
		$("#addModal .removeParam").on('click', function () {
			$(this).parents('.form-group').remove();
		});
	});
	
	// 更新
	$("#job_list").on('click', '.update',function() {
		$("#updateModal .form input[name='triggerKeyName']").val($(this).parent('p').attr("jobKey"));
		$("#updateModal .form input[name='jobGroup']").val($(this).parent('p').attr("jobGroup"));
		$("#updateModal .form input[name='jobName']").val($(this).parent('p').attr("jobName"));
		$("#updateModal .form input[name='cronExpression']").val($(this).parent('p').attr("cronExpression"));
		$("#updateModal .form input[name='job_address']").val($(this).parent('p').attr("job_address"));
		$("#updateModal .form input[name='run_class']").val($(this).parent('p').attr("run_class"));
		$("#updateModal .form input[name='run_method']").val($(this).parent('p').attr("run_method"));
		$("#updateModal .form input[name='run_method_args']").val($(this).parent('p').attr("run_method_args"));
		$("#updateModal .form input[name='job_desc']").val($(this).parent('p').attr("jobDesc"));
		$("#updateModal .form input[name='owner']").val($(this).parent('p').attr("owner"));
		$("#updateModal .form input[name='mailReceives']").val($(this).parent('p').attr("mailReceives"));
		$("#updateModal .form input[name='failAlarmNum']").val($(this).parent('p').attr("failAlarmNum"));
		$('#updateModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var updateModalValidate = $("#updateModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,  
        rules : {  
        	triggerKeyName : {  
        		required : true ,
                minlength: 4,
                maxlength: 100
            },  
            cronExpression : {  
            	required : true ,
                maxlength: 100
            },  
            job_desc : {  
            	required : true ,
                maxlength: 200
            },
            job_address : {
            	required : true ,
                maxlength: 200
            },
            run_class : {
            	required : true ,
                maxlength: 200
            },
            run_method : {
            	required : true ,
                maxlength: 200
            },
            run_method_args : {
            	required : false ,
                maxlength: 200
            },
            owner : {
            	required : true ,
                maxlength: 200
            },
            mailReceives : {
            	required : true ,
                maxlength: 200
            },
            failAlarmNum : {
            	required : true ,
            	digits:true
            }
        }, 
        messages : {  
        	triggerKeyName : {  
        		required :"请输入“任务Key”."  ,
                minlength:"“任务Key”不应低于4位",
                maxlength:"“任务Key”不应超过100位"
            },  
            cronExpression : {
            	required :"请输入“任务Cron”."  ,
                maxlength:"“任务Cron”不应超过100位"
            },  
            job_desc : {
            	required :"请输入“任务描述”."  ,
                maxlength:"长度不应超过200位"
            },  
            job_address : {
            	required :"请输入“任务机器”."  ,
                maxlength:"长度不应超过200位"
            },
            run_class : {
            	required : "请输入“期望执行的类”."  ,
                maxlength: "长度不应超过200位"
            },
            run_method : {
            	required : "请输入“期望执行的方法”."  ,
                maxlength: "长度不应超过200位"
            },
            run_method_args : {
            	required : "请输入“方法入参”."  ,
                maxlength: "长度不应超过200位"
            },
            owner : {
            	required : "请输入“负责人”." ,
                maxlength: "长度不应超过200位"
            },
            mailReceives : {
            	required : "请输入“邮件联系人”." ,
                maxlength: "长度不应超过200位"
            },
            failAlarmNum : {
            	required : "请输入“连续报警阀值”." ,
            	digits:"阀值应该为整数."
            }
        }, 
		highlight : function(element) {  
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
        },
        submitHandler : function(form) {
    		$.post(base_url + "/job/reschedule", $("#updateModal .form").serialize(), function(data, status) {
    			if (data.code == "200") {
    				ComAlert.show(1, "更新成功", function(){
    					$('#updateModal').modal({backdrop: false, keyboard: false}).modal('hide');
    					jobTable.fnDraw();
    				});
    			} else {
    				if (data.msg) {
    					ComAlert.show(2, data.msg);
					} else {
						ComAlert.show(2, "更新失败");
					}
    			}
    		});
		}
	});
	$("#updateModal").on('hide.bs.modal', function () {
		$("#updateModal .form")[0].reset()
	});
	
	// 新增
	$(".addFerrari").click(function(){
		$('#addFerrariModal').modal({backdrop: false, keyboard: false}).modal('show');
	});
	var addModalValidate = $("#addFerrariModal .form").validate({
		errorElement : 'span',  
        errorClass : 'help-block',
        focusInvalid : true,  
        rules : {  
        	jobName : {  
        		required : true ,
                minlength: 4,
                maxlength: 100,
                myValid01:true
            },  
            cronExpression : {  
            	required : true ,
                maxlength: 100
            },  
            job_desc : {  
            	required : true ,
                maxlength: 200
            },
            job_address : {
            	required : true ,
                maxlength: 200
            },
            run_class : {
            	required : true ,
                maxlength: 200
            },
            run_method : {
            	required : true ,
                maxlength: 200
            },
            run_method_args : {
            	required : false ,
                maxlength: 200
            },
            owner : {
            	required : true ,
                maxlength: 200
            },
            mailReceives : {
            	required : true ,
                maxlength: 200
            },
            failAlarmNum : {
            	required : true ,
            	digits:true
            }
        }, 
        messages : {  
        	jobName : {  
        		required :"请输入“任务名”"  ,
                minlength:"长度不应低于4位",
                maxlength:"长度不应超过100位"
            },  
            cronExpression : {
            	required :"请输入“任务Cron”"  ,
                maxlength:"长度不应超过100位"
            },  
            job_desc : {
            	required :"请输入“任务描述”"  ,
                maxlength:"长度不应超过200位"
            },  
            job_address : {
            	required :"请输入“任务机器”"  ,
                maxlength:"长度不应超过200位"
            },
            run_class : {
            	required : "请输入“期望执行的类”"  ,
                maxlength: "长度不应超过200位"
            },
            run_method : {
            	required : "请输入“期望执行的方法”"  ,
                maxlength: "长度不应超过200位"
            },
            run_method_args : {
            	required : "请输入“方法入参”"  ,
                maxlength: "长度不应超过200位"
            },
            owner : {
            	required : "请输入“负责人”" ,
                maxlength: "长度不应超过200位"
            },
            mailReceives : {
            	required : "请输入“邮件联系人”" ,
                maxlength: "长度不应超过200位"
            },
            failAlarmNum : {
            	required : "请输入“连续报警阀值”" ,
            	digits:"阀值应该为整数"
            }
        }, 
		highlight : function(element) {  
            $(element).closest('.form-group').addClass('has-error');  
        },
        success : function(label) {  
            label.closest('.form-group').removeClass('has-error');  
            label.remove();  
        },
        errorPlacement : function(error, element) {  
            element.parent('div').append(error);  
        },
        submitHandler : function(form) {
    		$.post(base_url + "/job/addFerrari", $("#addFerrariModal .form").serialize(), function(data, status) {
    			if (data.code == "200") {
    				ComAlert.show(1, "新增调度任务成功", function(){
    					window.location.reload();
    				});
    			} else {
    				if (data.msg) {
    					ComAlert.show(2, data.msg);
    				} else {
    					ComAlert.show(2, "新增失败");
    				}
    			}
    		});
    		
		}
	});
	$("#addFerrariModal").on('hide.bs.modal', function () {
		$("#addFerrariModal .form .form-group").removeClass("has-error");
		addModalValidate.resetForm();
	});
});
