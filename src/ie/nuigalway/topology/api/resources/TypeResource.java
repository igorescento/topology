package ie.nuigalway.topology.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.api.model.NetworkLsaModel;
import ie.nuigalway.topology.api.model.RouterLsaModel;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;

@Path("type")
public class TypeResource {

	private SessionFactory sessionFactory;
	private RouterLsaDAO routerDAO;
	private NetworkLsaDAO netDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.routerDAO = new RouterLsaDAO(sessionFactory);
		this.netDAO = new NetworkLsaDAO(sessionFactory);
	}

	/**
	 * Get all LSA router instances and populate routers table.
	 */
	@GET
	@Path("/router")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<RouterLsaModel> getAllLsaRouter() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<RouterLsa> routLsaAll = new ArrayList<>(); 
			Collection<RouterLsaModel> routModel = new ArrayList<>();
			
			routLsaAll = routerDAO.findAll();
			for(RouterLsa r : routLsaAll){
				routModel.add(new RouterLsaModel(r));
			}

			sessionFactory.getCurrentSession().getTransaction().commit();
			
			return routModel;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}
	
	/**
	 * Get distinct routers.
	 */
	@GET
	@Path("/distinctrouter")
	@Produces(MediaType.APPLICATION_JSON)
	public List<String> getAllUniqueRouters() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<Long> routLsaAll = new ArrayList<>();
			List<String> routers = new ArrayList<>();
			
			routLsaAll = routerDAO.findDistinctId();
			
			for(Long l : routLsaAll){
				routers.add(IPv4Converter.longToIpv4(l));
			}
			
			sessionFactory.getCurrentSession().getTransaction().commit();
			
			return routers;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}

	/**
	 * Get all LSA instances and populate networks table.
	 */
	@GET
	@Path("/network")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<NetworkLsaModel> getAllLsaNetwork() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<NetworkLsa> netLsaAll = new ArrayList<>(); 
			Collection<NetworkLsaModel> netModel = new ArrayList<>();
			
			netLsaAll = netDAO.findAll();
			for(NetworkLsa n : netLsaAll){
				netModel.add(new NetworkLsaModel(n));
			}

			sessionFactory.getCurrentSession().getTransaction().commit();
			
			return netModel;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}
}
