package ie.nuigalway.topology.api.resources;

import java.io.File;
import java.io.IOException;
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

import org.apache.commons.io.FileUtils;
import org.apache.commons.net.util.SubnetUtils;
import org.apache.commons.net.util.SubnetUtils.SubnetInfo;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import javax.inject.Singleton;
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

@Singleton
@Path("mikrotik")
public class MikrotikResource {

	ClassLoader loader = this.getClass().getClassLoader();

	private SessionFactory sessionFactory;
	private LsaDAO lsaDAOHibernate;
	private RouterLsaDAO routerLsaDAO;
	private NetworkLsaDAO netLsaDAO;
	private Timer timer;
	private boolean isRunning;
	private boolean sameIp;
	private ArrayList<String> address;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.lsaDAOHibernate = new LsaDAO(sessionFactory);
		this.routerLsaDAO = new RouterLsaDAO(sessionFactory);
		this.netLsaDAO = new NetworkLsaDAO(sessionFactory);
		this.timer = new Timer("Refresh");
		this.isRunning = false;
		this.sameIp = false;
		this.address = new ArrayList<>();
	}

	/**
	 * Method to retrieve LSA information from router
	 */
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveLsa(ConnDataModel data){
		//DEMO_TO_WORK_WITH_LSA_EXPORT_FROM_LIVE_ROUTER-----------------------

		if(data.getIpaddress().equals("10.8.129.6")){

			try{
				List<String> fromFile = new ArrayList<>();
				fromFile = FileUtils.readLines(new File(loader.getResource("lsa-detail.txt").getFile()), "UTF-8");
				deleteAllTables();
				updateDemoFile(fromFile);
				updateNetsTable();
				updateRoutersTable();
				return Response.ok().build();
			} catch (IOException e) {
				e.printStackTrace();
				return Response.status(500).entity(e.getMessage()).build();
			}
		}

		else {


			//DEMO_END-------------------------------------------------------------

			//keep list of logged in ip addresses to see if we are connecting to same device or accessing new device
			address.add(data.getIpaddress());
			if(isRunning){
				sameIp = data.getIpaddress().equals(address.get(address.size()-2));
			}

			//login to router and retrieve LSA table and repeat every n seconds
			try {
				ApiConnection con = ApiConnection.connect(data.getIpaddress());
				con.login(data.getUsername(), data.getPassword());

				TimerTask tt = new TimerTask() {
					public void run()
					{	
						System.out.println("Timer " + timer.toString() + " runnning. " + data.getIpaddress() + " @ " + new Date());

						//deleting old data, inserting new
						deleteAllTables();

						updateLsaTable(con);
						updateNetsTable();
						updateRoutersTable();
					}
				};

				//making sure that only one timer runs at any point in time
				synchronized (timer) {
					if (isRunning) {
						if(sameIp){
							System.out.println("Timer already running with same task.");
						}
						else {
							timer.cancel();
							timer = new Timer("Refresh");
							timer.scheduleAtFixedRate(tt, 0, 15000);
							System.out.println("Timer cancelled, new one scheduled");
						}
					} else {
						timer.scheduleAtFixedRate(tt, 0, 60000);
						isRunning = true;
						System.out.println("New timer scheduled.");
					}
				}

				return Response.ok().build();

			} catch (MikrotikApiException e) {
				e.printStackTrace();
				return Response.status(500).entity(e.getMessage()).build();
			}
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

		if(data.getIpaddress().equals("10.8.129.6")){
			return "10.99.0.148";
		}
		else {
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

	/**
	 * Create tables from lsa text file
	 */
	public void updateDemoFile(List<String> fromFile) {
		List<String> lsa = new ArrayList<>();

		String lsaObject = "";

		for(String l : fromFile){
			//break it down to meaningful object based on data from lsa export
			if(!l.equals("")){
				lsaObject += l;
			}
			else {
				lsa.add(lsaObject);
				lsaObject = "";
			}
		}

		for(String lsaRow : lsa){

			LsaModel lsaModel = new LsaModel();
			String [] dat = lsaRow.split("body=");
			String details = dat[0].trim();
			String body = dat[1].trim().replaceAll("       ", "\n");

			lsaModel.setBody(body);

			for(String l : details.split(" +")){

				String[] keyvalue = l.split("=");
				//populate LSA table
				if(keyvalue[0].equals("id")){
					lsaModel.setId(IPv4Converter.ipv4ToLong(keyvalue[1]));
				}
				if(keyvalue[0].equals("instance")){
					lsaModel.setInstance(keyvalue[1]);
				}
				if(keyvalue[0].equals("area")){
					lsaModel.setArea(keyvalue[1]);
				}
				if(keyvalue[0].equals("type")){
					lsaModel.setType(keyvalue[1]);
				}
				if(keyvalue[0].equals("originator")){
					lsaModel.setOriginator(IPv4Converter.ipv4ToLong(keyvalue[1]));
				}
				if(keyvalue[0].equals("sequence-number")){
					lsaModel.setSequence(keyvalue[1]);
				}
				if(keyvalue[0].equals("age")){
					lsaModel.setAge(Integer.parseInt(keyvalue[1]));
				}
				if(keyvalue[0].equals("checksum")){
					lsaModel.setChecksum(keyvalue[1]);
				}
				if(keyvalue[0].equals("options")){
					lsaModel.setOptions(keyvalue[1]);
				}
			}

			addLsa(lsaModel);
		}
	}
}
