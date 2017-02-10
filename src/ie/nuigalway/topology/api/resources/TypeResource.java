package ie.nuigalway.topology.api.resources;

import java.util.ArrayList;
import java.util.Collection;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.api.model.LsaModel;
import ie.nuigalway.topology.api.model.NetworkLsaModel;
import ie.nuigalway.topology.api.model.RouterLsaModel;
import ie.nuigalway.topology.domain.dao.hibernate.LsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.Lsa;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;

@Path("type")
public class TypeResource {

	private SessionFactory sessionFactory;
	private LsaDAO lsaDAO;
	private RouterLsaDAO routerDAO;
	private NetworkLsaDAO netDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.lsaDAO = new LsaDAO(sessionFactory);
		this.routerDAO = new RouterLsaDAO(sessionFactory);
		this.netDAO = new NetworkLsaDAO(sessionFactory);
	}

	/**
	 * Get all LSA router instances and populate routerlsa table.
	 * 
	 * @return
	 */
	@GET
	@Path("/router")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<RouterLsaModel> getAllLsaRouter() {
		
		deleteTableRouter();
		
		
		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<Lsa> lsaList = lsaDAO.findAllType("router");
			Collection<LsaModel> lsaModelList = new ArrayList<LsaModel>();
			Collection<RouterLsaModel> allrouters = new ArrayList<>();
			Collection<RouterLsa> routerLsaAll = new ArrayList<>();

			for(Lsa l : lsaList) {

				lsaModelList.add(new LsaModel(l));

				for(int i = 0; i < l.getBody().split("\n").length; i++){
					RouterLsaModel rlsa = new RouterLsaModel();
					rlsa.setId(l.getIdTypePk().getId());
					rlsa.setType(l.getIdTypePk().getType());

					if(i == 0){
						//check the flag
						if(l.getBody().split("\n")[i].split("=").length > 1){

						}
					}
					//remaining information from body processed here
					else {
						for(int j = 0; j < l.getBody().split("\n")[i].trim().split(" ").length; j++){

							if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("link-type")){
								rlsa.setLinktype(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]);
							}
							if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("id")){
								try {
									rlsa.setBodyid(IPv4Converter.ipv4ToLong(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]));
								}
								catch (Exception e){
									e.printStackTrace();
								}
							}
							if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("data")){
								try {	
									rlsa.setData(IPv4Converter.ipv4ToLong(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]));
								}
								catch (Exception e){
									e.printStackTrace();
								}
							}
							if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("metric")){
								rlsa.setMetric(Integer.parseInt(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]));								
							}
						}
						allrouters.add(rlsa);
						RouterLsa r = rlsa.toEntity();
						routerLsaAll.add(r);
					}
				}
			}

			routerDAO.create(routerLsaAll);

			sessionFactory.getCurrentSession().getTransaction().commit();
			return allrouters;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}

	/**
	 * Get all LSA instances and populate networklsa table.
	 * 
	 * @return
	 */
	@GET
	@Path("/network")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<NetworkLsaModel> getAllLsaNetwork() {
		deleteTableNet();
		
		try {
			sessionFactory.getCurrentSession().getTransaction().begin();
			
			Collection<Lsa> lsaList = lsaDAO.findAllType("network");
			Collection<LsaModel> lsaModelList = new ArrayList<LsaModel>();
			Collection<NetworkLsaModel> allnetworks = new ArrayList<>();
			Collection<NetworkLsa> networkLsaAll = new ArrayList<>();

			for(Lsa l : lsaList) {
				
				lsaModelList.add(new LsaModel(l));

				int numRouters = 0;
				ArrayList<Long> routersIds = new ArrayList<>();
				NetworkLsaModel nlsa = new NetworkLsaModel();
				
				String address = IPv4Converter.longToIpv4(l.getIdTypePk().getId());
		        String mask = "";
		        
				nlsa.setId(l.getIdTypePk().getId());
				nlsa.setType(l.getIdTypePk().getType());
				nlsa.setOriginator(l.getOriginator());

				for(int i = 0; i < l.getBody().split("\n").length; i++){

					for(int j = 0; j < l.getBody().split("\n")[i].trim().split(" ").length; j++){
						if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("netmask")){
							mask = l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1];
							nlsa.setNetmask(IPv4Converter.ipv4ToLong(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]));
						}

						if(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[0].equals("routerId")){
							routersIds.add(IPv4Converter.ipv4ToLong(l.getBody().split("\n")[i].trim().split(" ")[j].split("=")[1]));
							numRouters++;
						}
					}
				}
		        SubnetUtils utils = new SubnetUtils(address, mask);
		        SubnetInfo info = utils.getInfo();
		        
		        nlsa.setNetworkAddr(IPv4Converter.ipv4ToLong(info.getNetworkAddress()));
		        nlsa.setBroadcastaddr(IPv4Converter.ipv4ToLong(info.getBroadcastAddress()));
		        nlsa.setFirstaddr(IPv4Converter.ipv4ToLong(info.getLowAddress()));
		        nlsa.setLastaddr(IPv4Converter.ipv4ToLong(info.getHighAddress()));
				nlsa.setNumrouters(numRouters);
				nlsa.setRoutersid(routersIds.toString().replace("[", "").replaceAll("]", ""));

				allnetworks.add(nlsa);
				NetworkLsa r = nlsa.toEntity();
				networkLsaAll.add(r);
			}

			netDAO.create(networkLsaAll);

			sessionFactory.getCurrentSession().getTransaction().commit();
			return allnetworks;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}
	
	public Response deleteTableNet() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			int result = netDAO.delete();
			System.out.println("DELETED ROWS: " + result);

			sessionFactory.getCurrentSession().getTransaction().commit();
			return Response.ok().build();

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem.",
					"Error while trying to retrieve data. "
							+ e.getMessage());
		}
	}
	
	public Response deleteTableRouter() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			int result = routerDAO.delete();
			System.out.println("DELETED ROWS: " + result);

			sessionFactory.getCurrentSession().getTransaction().commit();
			return Response.ok().build();

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem.",
					"Error while trying to retrieve data. "
							+ e.getMessage());
		}
	}
}
