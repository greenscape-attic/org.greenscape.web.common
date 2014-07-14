package org.greenscape.web.common.filter;

import java.io.IOException;
import java.util.List;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.greenscape.core.model.Organization;
import org.greenscape.core.model.OrganizationModel;
import org.greenscape.core.service.Service;
import org.greenscape.web.common.CommonConstants;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

@Component(property = { "pattern=/.*", "service.ranking=1" })
public class UrlRewriteFilter implements Filter {

	private Service service;

	@Override
	public void init(FilterConfig config) throws ServletException {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException,
	ServletException {
		HttpServletRequest httpRequest = (HttpServletRequest) request;
		HttpServletResponse httpResponse = (HttpServletResponse) response;
		// get tenant (aka organization) id
		OrganizationModel organization = (OrganizationModel) request.getAttribute(CommonConstants.ORGANIZATION);
		if (organization == null) {
			String organizationId = request.getParameter(CommonConstants.ORGANIZATION);

			if (organizationId == null) {
				// check if there is only 1 organization. in that case set it to
				// that
				List<Organization> organizationEntities = service.find(Organization.class);
				if (organizationEntities != null && organizationEntities.size() == 1) {
					request.setAttribute(CommonConstants.ORGANIZATION, organizationEntities.get(0));
				}
			} else if (organizationId != null && !organizationId.isEmpty()) {
				Organization organizationEntity = service.find(Organization.class, organizationId);
				if (organizationEntity != null) {
					request.getParameterMap().remove(CommonConstants.ORGANIZATION);
					request.setAttribute(CommonConstants.ORGANIZATION, organizationEntity);
				} else {
					httpResponse.sendRedirect("/common/pages/error/error.html");
					return;
				}
			} else {
				return;
			}
		}
		String uri = httpRequest.getRequestURI();
		if (uri.equalsIgnoreCase("/site") || uri.equals("/") || uri.equals("")) {
			httpResponse.sendRedirect("/site/");
		} else {
			chain.doFilter(request, response);
		}
	}

	@Override
	public void destroy() {

	}

	@Reference
	public void setService(Service service) {
		this.service = service;
	}

	public void unsetService(Service service) {
		this.service = null;
	}
}
