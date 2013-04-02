/*
 * Copyright(c) 2012 Donghong Inc.
 */
package org.jxstar.control.login;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.jxstar.util.config.SystemVar;
import org.jxstar.util.factory.FactoryUtil;
import org.jxstar.util.factory.SystemFactory;
import org.jxstar.util.resource.JsParam;

/**
 * 单点登录打开本系统的页面时检验来源网站是否合法，并处理登录用户信息。
 *
 * @author TonyTan
 * @version 1.0, 2012-12-27
 */
public class OneLoginFilter implements Filter {
	//来源网站地址，如果有多个，可以用;分隔
	//private List<String> _lssite = FactoryUtil.newList();

	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse resp = (HttpServletResponse) response;
		
		//先判断请求头中的来源是否正确
		List<String> lssite = getSiteList();
		if (!lssite.isEmpty()) {
			String referer = req.getHeader("Referer");
			if (referer == null || referer.length() == 0) {
				resp.sendError(401, "单点访问失败：来源网站为空！");
				return;
			}
			
			boolean isvalid = false;
			for (String site : lssite) {
				if (referer.indexOf(site) >= 0) {
					isvalid = true;
					break;
				}
			}
			if (!isvalid) {
				resp.sendError(401, "单点访问失败：无效访问来源网站"+ referer +"！");
				return;
			}
		}
		
		String userCode = req.getParameter("usercode");
		
		//单点登录校验扩展类
		OneLoginProcess processLogin = null;
		String processClass = SystemVar.getValue("one.loginprocess.class");
		if (processClass.length() > 0) {
			processLogin = (OneLoginProcess) SystemFactory.createObject(processClass);
		}
		
		if (processLogin != null) {
			//对用户编码进行转换
			userCode = processLogin.getUserCode(userCode);
			//对请求进一步校验
			String validMsg = processLogin.valid(req);
			if (validMsg != null && validMsg.length() > 0 && !validMsg.equals("true")) {
				resp.sendError(401, "单点访问失败："+ validMsg +"！");
			}
		}
		
		//判断当前用户名是否已经登录，如果没有登录则创建登录信息
		Map<String,String> mpUser = OneLoginUtil.getUserMap(userCode);
		if (mpUser == null || mpUser.isEmpty()) {
			resp.sendError(401, "单点访问失败：取不到用户信息或者没有配置角色！");
			return;
		}
		
		//添加组织机构信息
		mpUser = OneLoginUtil.addOrgData(mpUser);
		
		if (processLogin != null) {
			//添加更多的用户信息
			mpUser = processLogin.getUserMap(mpUser);
		}
		
		//把当前用户信息写入会话中
		req.getSession().setAttribute(JsParam.CURRUSER, mpUser);
		
		chain.doFilter(req, resp);
	}

	//解析有效的来源站点信息
	private List<String> getSiteList() {
		List<String> lssite = FactoryUtil.newList();
		
		String src_site = SystemVar.getValue("one.srcsite");
		if (src_site.length() > 0) {
			src_site = src_site.trim();
			String[] sites = src_site.split(";");
			for (int i = 0; i < sites.length; i++) {
				String site = sites[i].trim();
				if (site.length() > 0) {
					lssite.add(site);
				}
			}
		}
		return lssite;
	}
	
	public void init(FilterConfig filterConfig) throws ServletException {}
	public void destroy() {}
}
