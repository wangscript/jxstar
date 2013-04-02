/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.service.note.my;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;

/**
 * 发送短信的基础类。
 * 
 * @author TonyTan
 * @version 1.0, 2012-5-21
 */
public class NoteSender {
	public NoteSender() {
		this("default", "default");
	}

	public NoteSender(String name, String pwd) {
		comName = name;
		comPwd = pwd;
		Server = "http://www.139000.com";
	}

	public String massSend(String dst, String msg, String time, String subNo,
			String txt) {
		String sUrl = null;
		try {
			sUrl = Server + "/send/gsend.asp?name=" + comName + "&pwd="
					+ comPwd + "&dst=" + dst + "&msg="
					+ URLEncoder.encode(msg, NoteProperty.getCharSet()) + "&time=" + time
					+ "&sender=" + subNo + "&txt=" + txt;// 这里必须GB2312否则发到手机乱码

		} catch (UnsupportedEncodingException uee) {
			System.out.println(uee.toString());
		}

		return getUrl(sUrl);
	}

	public String readSms() {
		String sUrl = null;
		sUrl = Server + "/send/readsms.asp?name=" + comName + "&pwd=" + comPwd;
		try {
			URLEncoder.encode(sUrl, NoteProperty.getCharSet());// linux下编码成GB18030或UTF-8
		} catch (UnsupportedEncodingException uee) {
			System.out.println(uee.toString());
		}
		return getUrl(sUrl);
	}

	public String getFee() {
		String sUrl = null;
		sUrl = Server + "/send/getfee.asp?name=" + comName + "&pwd=" + comPwd;
		return getUrl(sUrl);
	}

	public String changePwd(String newPwd) {
		String sUrl = null;
		sUrl = Server + "/send/cpwd.asp?name=" + comName + "&pwd=" + comPwd
				+ "&newpwd=" + newPwd;
		try {
			URLEncoder.encode(sUrl, NoteProperty.getCharSet());// linux下编码成GB18030或UTF-8
		} catch (UnsupportedEncodingException uee) {
			System.out.println(uee.toString());
		}
		return getUrl(sUrl);
	}

	public String getUrl(String urlString) {
		StringBuffer sb = new StringBuffer();
		try {
			URL url = new URL(urlString);
			URLConnection conn = url.openConnection();
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					conn.getInputStream()));
			for (String line = null; (line = reader.readLine()) != null;)
				sb.append(line + "\n");

			reader.close();
		} catch (IOException e) {
			System.out.println(e.toString());
		}
		return sb.toString();
	}

	private String comName;
	private String comPwd;
	private String Server;
}
