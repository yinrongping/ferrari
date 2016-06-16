/**
 * 
 */
package com.cip.ferrari.core.common;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * @author yuantengkai 文本数据压缩解压工具类<br/>
 * 1、使用ISO-8859-1作为中介编码，可以保证准确还原数据<br/>
 * 2、字符编码确定时，可以在uncompress方法最后一句中显式指定编码
 */
public class ZipUtil {

	/**
	 * 压缩
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String compress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gzip = new GZIPOutputStream(out);
		gzip.write(str.getBytes());
		gzip.close();
		return out.toString("ISO-8859-1");
	}

	/**
	 * 解压
	 * @param str
	 * @return
	 * @throws IOException
	 */
	public static String uncompress(String str) throws IOException {
		if (str == null || str.length() == 0) {
			return str;
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ByteArrayInputStream in = new ByteArrayInputStream(
				str.getBytes("ISO-8859-1"));
		GZIPInputStream gunzip = new GZIPInputStream(in);
		byte[] buffer = new byte[256];
		int n;
		while ((n = gunzip.read(buffer)) >= 0) {
			out.write(buffer, 0, n);
		}
		// toString()使用平台默认编码，也可以显式的指定如toString("UTF-8")
		return out.toString("UTF-8");
	}

	// 测试方法
	public static void main(String[] args) throws IOException {
		String compress = ZipUtil.compress("中国China中国China中国China中国China中国China");
		System.out.println(compress);
		System.out.println(ZipUtil.uncompress(compress));
		System.out.println(System.currentTimeMillis());
	}
}
