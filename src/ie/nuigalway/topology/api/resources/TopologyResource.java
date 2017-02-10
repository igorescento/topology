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
import ie.nuigalway.topology.api.model.TopologyModel;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;

@Path("topology")
public class TopologyResource {

	private SessionFactory sessionFactory;
	private RouterLsaDAO routerDAO;
	private NetworkLsaDAO netDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.routerDAO = new RouterLsaDAO(sessionFactory);
		this.netDAO = new NetworkLsaDAO(sessionFactory);
	}

	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<TopologyModel> getCombined(){
		List<TopologyModel> tmlist = new ArrayList<>();

		try {
			Collection<RouterLsa> rlsa = new ArrayList<>();
			Collection<NetworkLsa> nlsa = new ArrayList<>();

			sessionFactory.getCurrentSession().getTransaction().begin();
			rlsa = routerDAO.findAllLinkType("Transit");
			nlsa = netDAO.findAll();

			for(RouterLsa r: rlsa){
				
				TopologyModel tm = new TopologyModel();
				tm.setRouterid(r.getId());
				tm.setRouterinterface(r.getData());
				tm.setMetric(r.getMetric());
				
				for(NetworkLsa n: nlsa){
					if(r.getData() >= n.getFirstaddr() && r.getData() <= n.getLastaddr() && n.getRoutersid().contains(r.getId().toString())){
						tm.setRoutersid(n.getRoutersid());
						tm.setNumrouters(n.getNumrouters());
						tm.setFirstaddr(n.getFirstaddr());
						tm.setLastaddr(n.getLastaddr());
						tm.setNetworkaddr(n.getNetworkaddr());
						tm.setBroadcastaddr(n.getBroadcastaddr());
						tm.setNetmask(n.getNetmask());
					}
				}
				
				tmlist.add(tm);
			}

			sessionFactory.getCurrentSession().getTransaction().commit();
			return tmlist;
			
		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}
}
