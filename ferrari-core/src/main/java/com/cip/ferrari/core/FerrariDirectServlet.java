/**
 * 
 */
package com.cip.ferrari.core;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * @author yuantengkai
 * Ferrari 调度入口,接收调度中心的指令
 */
public class FerrariDirectServlet extends HttpServlet{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = -504259073339645587L;
	
	private static final Logger logger = LoggerFactory.getLogger(FerrariDirectServlet.class);

	private transient FerrariRunnerFacade ferrariRunner = new FerrariRunnerFacade();
	
	@Override
	public void init() throws ServletException {
		super.init();
		
		ferrariRunner.init();
		
		logger.warn("FerrariDirectServlet inited...");
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		doPost(req, resp);
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp)
			throws ServletException, IOException {
		
		Map<String, String[]> originParams = req.getParameterMap();
		
		Map<String, String> params = new HashMap<String, String>(originParams.size());
		// 如果有多个，只取第一个元素
		for (Map.Entry<String, String[]> entry : originParams.entrySet()) {
            final String paramName = entry.getKey();
            final String[] paramValues = entry.getValue();

            if (paramValues.length > 1) {
                logger.warn("multivalue for name: {}, Just use the first one!", entry.getKey());
            }
            params.put(paramName, paramValues[0]);
        }
		if(logger.isInfoEnabled()){
			logger.info("Recieve request: {}", params);
		}
		
		//执行请求
		String repsond = ferrariRunner.request(params);
		
		resp.setContentType("application/json;charset=UTF-8");
		resp.setCharacterEncoding("UTF-8");
        PrintWriter out = resp.getWriter();
        out.println(repsond);
	}

	@Override
	public void destroy() {
		
		ferrariRunner.destroy();
		logger.warn("FerrariDirectServlet destroyed...");
		super.destroy();
	}

}
