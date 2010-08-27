package com.bigcommerce.fecru.plugins.eventexecutor;

import com.atlassian.crucible.spi.services.ImpersonationService;
import com.atlassian.crucible.spi.services.ServerException;
import com.atlassian.crucible.spi.services.UserService;
import com.atlassian.fisheye.plugin.web.helpers.VelocityHelper;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class AdminServlet extends HttpServlet {
	private final ImpersonationService impersonator;
	private final UserService userService;
	private final VelocityHelper velocity;
	private final ConfigurationManager config;

	public AdminServlet(
		ConfigurationManager config,
		ImpersonationService impersonator,
		UserService userService,
		VelocityHelper velocity) {
			this.impersonator = impersonator;
			this.userService = userService;
			this.velocity = velocity;
			this.config = config;
	}

	public void doGet(HttpServletRequest request, HttpServletResponse response)
		throws IOException {
			final Map<String, Object> params = new HashMap<String, Object>();

			final String username = config.loadRunAsUser();
			params.put("username", username);

			if (!StringUtils.isEmpty(username)) {
				params.put("contextPath", request.getContextPath());
				params.put("commitHook", config.getEventCommand("commit"));
			}

			response.setContentType("text/html");
			velocity.renderVelocityTemplate("templates/admin.vm", params, response.getWriter());
	}

	@Override
	protected void doPost(final HttpServletRequest request, final HttpServletResponse response)
			throws ServletException, IOException {

				final String username = request.getParameter("username");
				config.storeRunAsUser(username);

				final String commitHook = request.getParameter("commitHook");
				config.storeEventCommand("commit", commitHook);

				response.sendRedirect("./eventexecutoradmin");
	}
}