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
				tm.setRouterid(IPv4Converter.longToIpv4(r.getId()));
				tm.setRouterinterface(IPv4Converter.longToIpv4(r.getData()));
				tm.setMetric(r.getMetric());

				for(NetworkLsa n: nlsa){
					if(r.getData() >= n.getFirstaddr() && r.getData() <= n.getLastaddr() && n.getRoutersid().contains(r.getId().toString())){
						ArrayList<String> routIp = new ArrayList<>();
						for(String router : n.getRoutersid().split(",")){
							if(r.getId() != Long.parseLong(router.trim())){
								routIp.add(IPv4Converter.longToIpv4(Long.parseLong(router.trim())));
							}
						}
						tm.setRoutersid(routIp.toString().replace("[", "").replace("]", ""));
						tm.setNumrouters(n.getNumrouters());
						tm.setFirstaddr(IPv4Converter.longToIpv4(n.getFirstaddr()));
						tm.setLastaddr(IPv4Converter.longToIpv4(n.getLastaddr()));
						tm.setNetworkaddr(IPv4Converter.longToIpv4(n.getNetworkaddr()));
						tm.setBroadcastaddr(IPv4Converter.longToIpv4(n.getBroadcastaddr()));
						tm.setNetmask(IPv4Converter.longToIpv4(n.getNetmask()));
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
