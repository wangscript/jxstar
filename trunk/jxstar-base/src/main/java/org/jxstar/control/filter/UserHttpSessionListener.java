package org.jxstar.control.filter;

import java.util.Map;

import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.jxstar.service.studio.LoginLogBO;
import org.jxstar.util.resource.JsParam;

/**
 * 在线用户会话监听处理器，会话失效时删除在线用户记录，在系统退出时也会删除在线用户记录。
 *
 * @author TonyTan
 * @version 1.0, 2011-12-6
 */
public class UserHttpSessionListener implements HttpSessionListener {

	public void sessionCreated(HttpSessionEvent se) {
		
	}

	@SuppressWarnings("unchecked")
	public void sessionDestroyed(HttpSessionEvent se) {
		HttpSession session = se.getSession();
		Map<String,String> mpUser = (Map<String,String>) session.getAttribute(JsParam.CURRUSER);
		
		if (mpUser != null && !mpUser.isEmpty()) {
			String userId = mpUser.get("user_id");
			String sessionId = session.getId();
			
			LoginLogBO login = new LoginLogBO();
			login.logout(userId, sessionId);
		}
	}

}
