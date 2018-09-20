package com.scj.pic_server.util;

import java.security.MessageDigest;
import java.util.Random;

public class Md5Util {

	
	/**
	 * @Title: thirtyTwo
	 * @Description: 32位MD5加密
	 * @param content
	 * @return String
	 */
	public static String toMd5(String content) {
		try {
			MessageDigest md = MessageDigest.getInstance("MD5");
			byte[] resultByte = md.digest(content.getBytes("UTF-8"));
			StringBuffer buf = new StringBuffer("");
			int i;
			for (int offset = 0; offset < resultByte.length; offset++) {
				i = resultByte[offset];
				i = i & 0xff;
				if (i < 16)
					buf.append("0");
				buf.append(Integer.toHexString(i));
			}
			return buf.toString();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}
	
	
	/**
	 * 生成盐值	
	 * @return
	 */
	public static  String getSalt() {
		   Random r = new Random();  
	        StringBuilder sb = new StringBuilder(8);  
	        sb.append(r.nextInt(99999999));//.append(r.nextInt(99999999));  
	        int len = sb.length();  
	        if (len < 8) {  
	            for (int i = 0; i < 8 - len; i++) {  
	                sb.append("0");  
	            }  
	        }  
	        String salt = sb.toString();  
	        return salt;
	}
}
