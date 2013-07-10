package uk.ac.cam.signups.util;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HibernateSessionRequestFilter implements Filter {

	public static final String SESSION_FACTORY = HibernateSessionRequestFilter.class
			.getName() + ".SessionFactory";

	// Create logger
	private static Logger log = LoggerFactory
			.getLogger(HibernateSessionRequestFilter.class);

	private SessionFactory sf;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
		log.debug("Initializing filter...");
		sf = configureSessionFactory();
	}

	private static SessionFactory configureSessionFactory()
			throws HibernateException {
		Configuration configuration = new Configuration();
		configuration.configure();
		ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
				.applySettings(configuration.getProperties())
				.buildServiceRegistry();
		SessionFactory sessionFactory = configuration
				.buildSessionFactory(serviceRegistry);
		return sessionFactory;
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		request.setAttribute(SESSION_FACTORY, sf);
		chain.doFilter(request, response);
	}

	@Override
	public void destroy() {
	}

	public static Session openSession(HttpServletRequest request) {
		SessionFactory factory = (SessionFactory) request
				.getAttribute(SESSION_FACTORY);
		log.debug("Opening session...");
		return factory.openSession();
	}
}