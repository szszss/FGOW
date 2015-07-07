package net.hakugyokurou.fgow.plugin;

import java.io.File;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.apache.commons.io.FileUtils;
import org.gradle.api.Action;

@SuppressWarnings("rawtypes")
abstract class ActionMD5 implements Action {
	protected char[] hexs = new char[]{'0','1','2','3','4','5','6','7','8','9','a','b','c','d','e','f'};
	protected boolean started = false;
	protected HashMap<String, String> md5s;
	protected MessageDigest md;
	
	public ActionMD5() {
		try {
			md = MessageDigest.getInstance("MD5");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	protected String getMD5(File file) {
		md.reset();
		try {
			md.update(FileUtils.readFileToByteArray(file));
		} catch (IOException e) {
			throw new RuntimeException("Failed to generate MD5 for file:"+file.getName(), e);
		}
		byte[] md5 = md.digest();
		StringBuilder sb = new StringBuilder(md5.length * 2);
		for(int i=0;i<md5.length;i++)
		{
			byte byte0 = md5[i];
            sb.append(hexs[byte0 >>> 4 & 0xf]);
            sb.append(hexs[byte0 & 0xf]);
		}
		return sb.toString();
	}
}
