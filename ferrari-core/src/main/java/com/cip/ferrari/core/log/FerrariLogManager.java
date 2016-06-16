/**
 * 
 */
package com.cip.ferrari.core.log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Layout;
import org.apache.log4j.spi.LoggingEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cip.ferrari.core.common.AntZipCompressor;



/**
 * @author yuantengkai
 * ferrari log操作管理<br/>
 * 读无需加锁，写需要加锁
 */
public class FerrariLogManager {
	
	private static final Logger logger = LoggerFactory
			.getLogger(FerrariLogManager.class);
	
	private static volatile String filePath;//ferrari log路径(文件夹)
	
	private static final String DefaultFilePath = "/data/applogs/ferrari/";
	
	private static final String DATE_FORMAT = "yyyy-MM-dd";
	
	/**
	 * 待压缩zip的log文件名 exm:TestTask
	 */
	private static final BlockingQueue<String> toZipLogFileNameQueue = new LinkedBlockingQueue<String>();
	
	private static final AtomicBoolean zipInitFlag = new AtomicBoolean(false);
	
	private static final long ZipDltTime = 5 * 24 * 60 * 60 * 1000;//5天
	
	/**
	 * 写ferrari日志, fileAppender已加同步操作，故此方法线程安全
	 * @param runclassFile
	 * @param logId
	 * @param event
	 * @param layout
	 * @throws IOException 
	 * @throws UnsupportedEncodingException 
	 */
	public static void writeLog(String runclassFile,String logId,LoggingEvent event,Layout layout) throws UnsupportedEncodingException, IOException{
		File logFile = getLogFile2Write(runclassFile, logId);
		//把日志内容写入文件
		FileOutputStream fos = null;
		try {
			fos = new FileOutputStream(logFile, true);
			fos.write(layout.format(event).getBytes("utf-8"));
			if(layout.ignoresThrowable()) {
			    String[] s = event.getThrowableStrRep();
			    if (s != null) {
					int len = s.length;
					for(int i = 0; i < len; i++) {
						fos.write(s[i].getBytes("utf-8"));
						fos.write(Layout.LINE_SEP.getBytes("utf-8"));
					}
			    }
			}
			fos.flush();
		} finally {
			if (fos != null) {
				try {
					fos.close();
				} catch (IOException e) {
				}
			}
		}
	}
	
	/**
	 * 获取日志内容
	 * @param runclassFile
	 * @param logId
	 * @param executeTime 任务执行时间
	 * @return
	 * @throws IOException 
	 */
	@SuppressWarnings("unused")
	public static String readLog(String runclassFile,String logId, Date executeTime) throws IOException{
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		File logFile = getLogFile2Read(runclassFile, logId,sdf.format(executeTime));
		if(logFile == null){
			return null;
		}
		InputStream ins = null;
		BufferedReader reader = null;
		try {
			ins = new FileInputStream(logFile);
			reader = new BufferedReader(new InputStreamReader(ins, "utf-8"));
			if (reader != null) {
				String content = null;
				StringBuilder sb = new StringBuilder();
				while ((content = reader.readLine()) != null) {
					sb.append(content).append("\n");
				}
				return sb.toString();
			}
		} finally {
			if (ins != null) {
				try {
					ins.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (reader != null) {
				try {
					reader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}
	
	/**
	 * 获取日志文件用于写
	 * @param runclassFile
	 * @param logId
	 * @return
	 */
	private static File getLogFile2Write(String runclassFile,String logId){
		initZipThread();
		if(StringUtils.isBlank(filePath)){
			filePath = DefaultFilePath;
		}
		if(!filePath.endsWith("/")){
			filePath = filePath.concat("/");
		}
		
		//创建文件目录及log文件
		File fileDir = new File(filePath);  //  /data/applogs/ferrari/
		
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		File dirA = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		dirA = new File(filePath, runclassFile); // /data/applogs/ferrari/xxxxTask
		if(dirA.exists()){
			long lastModified = dirA.lastModified();
			String lastModifiedFormat = sdf.format(new Date(lastModified));
			String nowFormat = sdf.format(new Date());
			if(!StringUtils.equalsIgnoreCase(lastModifiedFormat, nowFormat)){
				File dirALastDay = new File(filePath, runclassFile.concat("_").concat(lastModifiedFormat));
				dirA.renameTo(dirALastDay);// /data/applogs/ferrari/xxxxTask_2016-02-01
				dirA.mkdirs(); // /data/applogs/ferrari/xxxxTask
				toZipLogFileNameQueue.offer(runclassFile);
			}
		}else{
			dirA.mkdirs(); // /data/applogs/ferrari/xxxxTask
		}
		
		String logFilePath = logId.concat(".log");
		
		File logFile = new File(dirA, logFilePath);// /data/applogs/ferrari/xxxxTask/110.log
		if (!logFile.exists()) {
			try {
				logFile.createNewFile();
				return logFile;
			} catch (IOException e) {
				return null;
			}
		}
		return logFile;
	}
	
	/**
	 * 获取日志文件用于读
	 * @param runclassFile
	 * @param logId
	 * @return
	 */
	private static File getLogFile2Read(String runclassFile,String logId, String suffix){
		if(StringUtils.isBlank(filePath)){
			filePath = DefaultFilePath;
		}
		if(!filePath.endsWith("/")){
			filePath = filePath.concat("/");
		}
		
		//创建文件目录及log文件
		File fileDir = new File(filePath);  //  /data/applogs/ferrari/
		
		if (!fileDir.exists()) {
			fileDir.mkdirs();
		}
		File dirA = null;
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
		String nowFormat = sdf.format(new Date());
		if(StringUtils.equalsIgnoreCase(suffix, nowFormat)){
			dirA = new File(filePath, runclassFile); // /data/applogs/ferrari/xxxxTask
			if (!dirA.exists()){
				return null;
			}
		}else{// /data/applogs/ferrari/xxxxTask_2016-02-01
			dirA = new File(filePath, runclassFile.concat("_").concat(suffix));
			if(!dirA.exists()){
				return null;
			}
		}
		
		String logFilePath = logId.concat(".log");
		
		File logFile = new File(dirA, logFilePath);// ../xxxxTask/110.log or ../xxxxTask_2016-02-01/110.log
		if (!logFile.exists()) {
			return null;
//			try {
//				logFile.createNewFile();
//				return logFile;
//			} catch (IOException e) {
//				return null;
//			}
		}
		return logFile;
	}

	private static void initZipThread() {
		if(!zipInitFlag.compareAndSet(false, true)){
			return;
		}
		Thread t = new Thread(new Runnable() {
			
			@Override
			public void run() {
				while(true){
					try{
						final String runclassFile = toZipLogFileNameQueue.take();
						File fileDir = new File(filePath);  //  /data/applogs/ferrari/
						
						File[] historyFiles = fileDir.listFiles(new FilenameFilter() {
							
							@Override
							public boolean accept(File dir, String name) {
								if(name.startsWith(runclassFile.concat("_"))){
									if(!name.endsWith(".zip")){
										return true;
									}
								}
								return false;
							}
						});
						if(historyFiles != null && historyFiles.length > 0){
							for(File f:historyFiles){ // ../TestTask_2016-01-31
								try{
									String name = f.getName();
									String date = name.split("_")[1]; //2016-01-31
									SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
									Date fileDate = sdf.parse(date);
									if(System.currentTimeMillis() - fileDate.getTime() < ZipDltTime){
										continue;//不到5天，则不压缩
									}
									AntZipCompressor azc = new AntZipCompressor(f.getAbsolutePath().concat(".zip"));
									azc.compressExe(f.getAbsolutePath());
									//当前文件要删除
									deleteDirectory(f.getAbsolutePath());
								}catch(ParseException e){
									logger.error("zip deal error,filePathName="+f.getAbsolutePath());
									continue;
								}

								
							}
						}
						
					}catch(Exception e){
						logger.error("zip deal exception",e);
					}
				}
			}

			/**
			 * 删除目录及里面的内容
			 * @param path
			 */
			private void deleteDirectory(String path) {
				if (!path.endsWith(File.separator)) {  
					path = path + File.separator;  
			    }
				File dirFile = new File(path);  
			    //如果dir对应的文件不存在，或者不是一个目录，则退出  
			    if (!dirFile.exists() || !dirFile.isDirectory()) {  
			        return;  
			    }  
			    //删除文件夹下的所有文件(包括子目录)  
			    File[] subfiles = dirFile.listFiles();  
			    for (int i = 0; i < subfiles.length; i++) {  
			        //删除子文件  
			        if (subfiles[i].isFile()) {  
			        	subfiles[i].delete();  
			        } 
			    }
			    dirFile.delete();
			}
			
		});
		t.setDaemon(true);
		t.setName("Ferrari-Zip-Thread");
		t.start();
	}
	
	public static void setFilePath(String path) {
		filePath = path;
	}
}
