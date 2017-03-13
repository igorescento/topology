package ie.nuigalway.topology.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.api.model.ConnDataModel;
import ie.nuigalway.topology.api.model.LsaModel;
import ie.nuigalway.topology.api.model.LsaModelConverted;
import ie.nuigalway.topology.api.model.NetworkLsaModel;
import ie.nuigalway.topology.api.model.RouterLsaModel;
import ie.nuigalway.topology.domain.dao.hibernate.LsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.Lsa;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

@Path("mikrotik")
public class MikrotikResource {

	private SessionFactory sessionFactory;
	private LsaDAO lsaDAOHibernate;
	private RouterLsaDAO routerLsaDAO;
	private NetworkLsaDAO netLsaDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.lsaDAOHibernate = new LsaDAO(sessionFactory);
		this.routerLsaDAO = new RouterLsaDAO(sessionFactory);
		this.netLsaDAO = new NetworkLsaDAO(sessionFactory);
	}

	/**
	 * Method to retrieve LSA information from router
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveLsa(ConnDataModel data){
		
		Timer timer = new Timer();
		//timer utility to execute the code after successful login and repeatedly fetch all the data
		System.out.println("TIMER RUNNNING " + timer.toString());
		
		//login to router and retrieve LSA table
		try {
			System.out.println("Connecting to router.");
			ApiConnection con = ApiConnection.connect(data.getIpaddress());
			con.login(data.getUsername(), data.getPassword());		

			timer.scheduleAtFixedRate(new TimerTask() {
				public void run()
				{
					System.out.println("Timer runnning. " + data.getIpaddress() + " @ " + new Date());
					
					//deleting all tables
					System.out.println("Deleting old table. New data inserted.");
					deleteAllTables();
					
					updateLsaTable(con);
					updateNetsTable();
					updateRoutersTable();
				}
			}, 0, 300000);
			
			return Response.ok().build();

		} catch (MikrotikApiException e) {
			e.printStackTrace();
			timer.cancel();
			return Response.status(500).entity(e.getMessage()).build();
		}
	}

	/**
	 * Method to retrieve router id when establishing connection
	 */
	@POST
	@Path("details")
	@Consumes(MediaType.APPLICATION_JSON)
	public String retrieveRouterId(ConnDataModel data){

		String routerId = null;

		try {
			ApiConnection con = ApiConnection.connect(data.getIpaddress());
			con.login(data.getUsername(), data.getPassword());

			List<Map<String, String>> rs = con.execute("/ip/address/print");

			for (Map<String,String> r : rs) {
				if(r.get("interface").equals("loopback")){
					routerId = r.get("address").split("/")[0];
				}
			}

			return routerId;

		} catch (MikrotikApiException e) {
			e.printStackTrace();
			return routerId;
		}
	}
	/**
	 * Get all LSA instances.
	 * 
	 * @return
	 */
	@GET
	@Path("lsa")
	@Produces(MediaType.APPLICATION_JSON)
	public Collection<LsaModelConverted> getAllLsa() {
		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<Lsa> lsaList = lsaDAOHibernate.findAll();
			Collection<LsaModelConverted> lsaModelList = new ArrayList<LsaModelConverted>();

			for(Lsa l : lsaList) {
				lsaModelList.add(new LsaModelConverted(l));
			}

			sessionFactory.getCurrentSession().getTransaction().commit();
			return lsaModelList;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error on trying to retrieve data: "
							+ e.getMessage());
		}

	}

	@GET
	@Path("{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Lsa getById(@PathParam("id") Integer id){
		sessionFactory.getCurrentSession().getTransaction().begin();

		try{
			Lsa l = lsaDAOHibernate.find(id);
			if(l == null) {
				throw new NotFoundException("LSA entry doesn't exist.");
			}
			sessionFactory.getCurrentSession().getTransaction().commit();
			return l;

		} catch (Exception e) {
			e.printStackTrace();

			return null;
		}
	}

	/**
	 * Post method to persist new LSA.
	 */
	public Response addLsa(LsaModel data) {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();
			Lsa l = data.toEntity();
			lsaDAOHibernate.create(l);
			sessionFactory.getCurrentSession().getTransaction().commit();
			return Response.ok().build();


		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error while saving data: "
							+ e.getMessage());
		}
	}


	/**
	 * Remove LSA.
	 */
	@DELETE
	@Path("{id}")
	public void delete(@PathParam("id") Integer id) {
		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			lsaDAOHibernate.removeById(id);

			sessionFactory.getCurrentSession().getTransaction().commit();

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error on trying to delete resource: "
							+ e.getMessage());
		}
	}

	/**
	 * Method to pull lsa data from router on successful connection
	 */
	public void updateLsaTable(ApiConnection con) {
		List<LsaModel> listLsa = new ArrayList<>();

		try {

			List<Map<String, String>> rs = con.execute("/routing/ospf/lsa/print");

			for (Map<String,String> r : rs) {
				LsaModel lsa = new LsaModel();

				try {
					lsa.setId(IPv4Converter.ipv4ToLong(r.get("id")));
					lsa.setOriginator(IPv4Converter.ipv4ToLong(r.get("originator")));
				}
				catch (Exception e){
					e.printStackTrace();
				}

				lsa.setType(r.get("type"));
				lsa.setInstance(r.get("instance"));
				lsa.setArea(r.get("area"));
				lsa.setSequence(r.get("sequence-number"));
				lsa.setAge(Integer.parseInt(r.get("age")));
				lsa.setChecksum(r.get("checksum"));
				lsa.setOptions(r.get("options"));
				lsa.setBody(r.get("body"));

				addLsa(lsa);
				listLsa.add(lsa);
			}
		}
		catch(MikrotikApiException e){
			e.printStackTrace();
		}
	}

	/**
	 * Method to update routers table from lsa data available from router
	 */
	public void updateRoutersTable() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<Lsa> lsaList = lsaDAOHibernate.findAllType("router");
			Collection<LsaModel> lsaModelList = new ArrayList<LsaModel>();
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
						RouterLsa r = rlsa.toEntity();
						routerLsaAll.add(r);
					}
				}
			}

			routerLsaDAO.create(routerLsaAll);

			sessionFactory.getCurrentSession().getTransaction().commit();

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}
	
	/**
	 * Method to update networks table
	 */
	public void updateNetsTable() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Collection<Lsa> lsaList = lsaDAOHibernate.findAllType("network");
			Collection<LsaModel> lsaModelList = new ArrayList<LsaModel>();
			Collection<NetworkLsa> networkLsaAll = new ArrayList<>();
			
			//processing data from LSA table to extract network details
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

				nlsa.setNetworkaddr(IPv4Converter.ipv4ToLong(info.getNetworkAddress()));
				nlsa.setBroadcastaddr(IPv4Converter.ipv4ToLong(info.getBroadcastAddress()));
				nlsa.setFirstaddr(IPv4Converter.ipv4ToLong(info.getLowAddress()));
				nlsa.setLastaddr(IPv4Converter.ipv4ToLong(info.getHighAddress()));
				nlsa.setNumrouters(numRouters);
				nlsa.setRoutersid(routersIds.toString().replace("[", "").replaceAll("]", ""));

				NetworkLsa r = nlsa.toEntity();
				networkLsaAll.add(r);
			}

			netLsaDAO.create(networkLsaAll);

			sessionFactory.getCurrentSession().getTransaction().commit();

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}


	/**
	 * Truncating all tables
	 * @return
	 */
	public Response deleteAllTables() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			int resLsa = lsaDAOHibernate.delete();
			int resRout = routerLsaDAO.delete();
			int resNet = netLsaDAO.delete();

			System.out.println("Number of deleted rows:\nLSA Table - "   + resLsa + 
					"\nRouters Table - " + resRout + "\nNetworks Table - " + resNet);

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
