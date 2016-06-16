/**
 * 
 */
package com.cip.ferrari.core.common;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import org.apache.commons.io.IOUtils;

/**
 * @author yuantengkai
 * http工具类
 */
public class HttpUtil {

	/**
	 * 发送http get 请求
	 * @param url 完整的url
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String sendHttpGet(String url) throws MalformedURLException, IOException {
        HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
        connection.setConnectTimeout(3000);
        connection.setDoOutput(true);
        connection.connect();

        return IOUtils.toString(connection.getInputStream(), "UTF-8");
    }
	
	/**
	 * 发送http post请求
	 * @param destUrl 完整得url地址
	 * @param httpParams 请求参数
	 * @return
	 * @throws MalformedURLException
	 * @throws IOException
	 */
	public static String sendHttpPost(String destUrl, Map<String, String> httpParams)
            throws MalformedURLException, IOException {
        final URL url = new URL(destUrl);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setConnectTimeout(3000);
        connection.setDoOutput(true);

        // send the encoded message
        PrintWriter out = new PrintWriter(connection.getOutputStream());
        boolean isFirst = true;
        for (Map.Entry<String, String> entry : httpParams.entrySet()) {
            String key = entry.getKey();
            String value = entry.getValue();
            if (null == value) {
                continue;
            }

            if (isFirst) {
                isFirst = false;
            } else {
                out.print("&");
            }

            out.print(URLEncoder.encode(key, "UTF-8") + "=" + URLEncoder.encode(value, "UTF-8"));
        }
        out.close();

        return IOUtils.toString(connection.getInputStream(), "UTF-8");
    }
}
