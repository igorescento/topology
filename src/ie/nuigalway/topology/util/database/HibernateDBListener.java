package ie.nuigalway.topology.util.database;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

/**
 * Application Lifecycle Listener implementation class DBListener.
 * 
 * When the application's context is initialized, a {@link SessionFactory}
 * instance will be made accessible to {@link HibernateUtil}. Calling
 * {@link HibernateUtil#getSessionFactory()} after execution of
 * {@link #contextInitialized(ServletContextEvent)} will allow you to get
 * the SessionFactory.
 *
 */
@WebListener
public class HibernateDBListener implements ServletContextListener {

	private SessionFactory sessionFactory;

	/**
	 * @see ServletContextListener#contextInitialized(ServletContextEvent)
	 */
	public void contextInitialized(ServletContextEvent sce)  {

		Configuration configuration = new Configuration().configure("hibernate.cfg.xml");
		ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
		sessionFactory = configuration.buildSessionFactory(serviceRegistry);
		HibernateUtil.setSessionFactory(sessionFactory);
	}

	/**
	 * @see ServletContextListener#contextDestroyed(ServletContextEvent)
	 */
	public void contextDestroyed(ServletContextEvent sce)  {
		if (sessionFactory != null) {
			sessionFactory.close();
		}
	}
}
