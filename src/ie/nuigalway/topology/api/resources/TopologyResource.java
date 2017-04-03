package ie.nuigalway.topology.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.api.model.dijkstra.Dijkstra;
import ie.nuigalway.topology.api.model.dijkstra.Edge;
import ie.nuigalway.topology.api.model.dijkstra.Graph;
import ie.nuigalway.topology.api.model.dijkstra.Node;
import ie.nuigalway.topology.domain.dao.hibernate.LsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.NetworkLsaDAO;
import ie.nuigalway.topology.domain.dao.hibernate.RouterLsaDAO;
import ie.nuigalway.topology.domain.entities.Lsa;
import ie.nuigalway.topology.domain.entities.NetworkLsa;
import ie.nuigalway.topology.domain.entities.RouterLsa;
import ie.nuigalway.topology.util.database.HibernateUtil;

@Path("topology")
public class TopologyResource {

	private SessionFactory sessionFactory;
	private RouterLsaDAO routerDAO;
	private NetworkLsaDAO netDAO;
	private LsaDAO lsaDAO;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.routerDAO = new RouterLsaDAO(sessionFactory);
		this.netDAO = new NetworkLsaDAO(sessionFactory);
		this.lsaDAO = new LsaDAO(sessionFactory);
	}

	/**
	 * Method to return full topology constructed from routers and networks tables
	 */
	@GET
	@Path("full")
	@Produces(MediaType.APPLICATION_JSON)
	public Graph getCombined(){

		try {
			Collection<RouterLsa> rlsa = new ArrayList<>();
			Collection<NetworkLsa> nlsa = new ArrayList<>();
			Collection<Lsa> external = new ArrayList<>();

			sessionFactory.getCurrentSession().getTransaction().begin();
			rlsa = routerDAO.findAllLinkType("Transit");
			nlsa = netDAO.findAll();
			external = lsaDAO.findAllType("as-external");

			HashSet<Node> routers = new HashSet<>();
			List<Edge> edges = new ArrayList<>();

			//System.out.println("Routers SIZE: " + rlsa.size() + " NETS size: " + nlsa.size());

			//loop thru routers and check which network it belongs to
			for(RouterLsa r: rlsa){
				Node rout = new Node(IPv4Converter.longToIpv4(r.getId()), IPv4Converter.longToIpv4(r.getId()), r.getType());
				routers.add(rout);
				
				for(NetworkLsa n: nlsa){
					if(r.getData() >= n.getFirstaddr() && r.getData() <= n.getLastaddr() && n.getRoutersid().contains(r.getId().toString())){

						//check if more than 2 connected
						if(n.getRoutersid().split(",").length == 2){

							for(String router : n.getRoutersid().split(",")){
								if(r.getId() != Long.parseLong(router.trim())){
									edges.add(
											new Edge(IPv4Converter.longToIpv4(n.getNetworkaddr()),
													new Node(IPv4Converter.longToIpv4(r.getId()), IPv4Converter.longToIpv4(r.getId()), r.getType()), 
													new Node(IPv4Converter.longToIpv4(Long.parseLong(router.trim())), IPv4Converter.longToIpv4(Long.parseLong(router.trim())), "router"),
													r.getMetric(),
													"internal",
													IPv4Converter.longToIpv4(n.getNetmask())
													)
											);
								}
							}
						}
						else {
							//create and add switch node in case of more that 2 routers on one network
							Node swtch = new Node(IPv4Converter.longToIpv4(n.getNetworkaddr()), IPv4Converter.longToIpv4(n.getNetworkaddr()), "switch");
							routers.add(swtch);

							int num = n.getNumrouters() - 1;
							for(String router : n.getRoutersid().split(",")){
								if(r.getId() != Long.parseLong(router.trim())){
									edges.add(
											new Edge(IPv4Converter.longToIpv4(n.getNetworkaddr()),
													new Node(IPv4Converter.longToIpv4(r.getId()), IPv4Converter.longToIpv4(r.getId()), r.getType()), 
													swtch,
													r.getMetric()/num,
													"switch",
													IPv4Converter.longToIpv4(n.getNetmask())
													)
											);
									edges.add(
											new Edge(IPv4Converter.longToIpv4(n.getNetworkaddr()),
													swtch,
													new Node(IPv4Converter.longToIpv4(r.getId()), IPv4Converter.longToIpv4(r.getId()), r.getType()),
													r.getMetric()/num,
													"switch",
													IPv4Converter.longToIpv4(n.getNetmask())
													)
											);
								}
							}
						}
					}
				}
			}
			
			//working with external routes
			List<Node> additional = new ArrayList<>();
			
			for(Node r : routers){
				//add interfaces to each router
				String interf = routerDAO.getRouterInterfaces(IPv4Converter.ipv4ToLong(r.getId()));
				r.setInterf(interf);
				
				for(Lsa e : external) {
					if(r.getId().equals(IPv4Converter.longToIpv4(e.getIdTypePk().getId())) && e.getIdTypePk().getId() != e.getOriginator()){
						//System.out.println("EXTERNAL ROUTER: " + r.getId());
						//add external routes
						String externalRoutes = ""; 
						externalRoutes = lsaDAO.getExternalRoutes(IPv4Converter.ipv4ToLong(r.getId()));
						
						//create external node with an edge
						int metric = 0;
						Node ext = new Node("EXT_" + r.getId(), "EXT_" + r.getId(), "external");
						ext.setInterf(externalRoutes);
						
						String [] text = e.getBody().split("\\r?\\n");
						for(String s : text){
							if(s.contains("metric")){
								metric = Integer.parseInt(s.split("=")[1].trim());
							}
						}
					
						Edge extE = new Edge("External_Route", r, ext, metric, "external", "");
						additional.add(ext);
						edges.add(extE);						
					}
				}
			}
			routers.addAll(additional);
			
			List<Node> l = new ArrayList<>(routers);
			Graph netGraph = new Graph(l, edges);

			sessionFactory.getCurrentSession().getTransaction().commit();
			//System.out.println("Pulling graph data");
			return netGraph;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem", "Error occured while retrieving data. " + e.getMessage());
		}
	}

	/**
	 * Method to generate a sink tree for selected node
	 */
	@GET
	@Path("sinktree/{id}")
	@Produces(MediaType.APPLICATION_JSON)
	public Graph getSyncTree(@PathParam("id") String id){

		Graph syncTree = null, g = getCombined();
		Node router = null;

		for(Node n : g.getNodes()){
			if(id.equals(n.getId())){
				router = n;
				break;
			}
		}

		Dijkstra algo = new Dijkstra(g);
		algo.run(router);
		syncTree = algo.getSyncTree(g);

		return syncTree;
	}

	/**
	 * Method to return shortest path from / to a specific node
	 */
	@GET
	@Path("shortestpath/{from}/{to}")
	@Produces(MediaType.APPLICATION_JSON)
	public List<HashMap<String, Integer>> getShortestPath(@PathParam("from") String from, @PathParam("to") String to){

		//List<String> listNodes = new ArrayList<>();
		HashMap<Node, Integer> distances = new HashMap<>();
		HashMap<String, Integer> d = new HashMap<>();
		HashMap<String, Integer> hops = new HashMap<>();
		List<HashMap<String, Integer>> result = new ArrayList<>();

		Node n = new Node(from, from, "router");
		Node n2 = new Node(to, to, "router");
		Integer hopNum = 0;

		try {
			Dijkstra algo = new Dijkstra(getCombined());
			algo.run(n);
			distances = algo.getDistance();

			for (HashMap.Entry<Node, Integer> node : distances.entrySet()) {
				d.put(node.getKey().getId(), node.getValue());

			}

			result.add(d);

			LinkedList<Node> path = algo.getPath(n2);

			/*for (Node node : path) {
				listNodes.add(node.getName());
			}*/

			for (Node node : path) {
				hops.put(node.getLabel(), hopNum);
				hopNum++;
			}

			result.add(hops);
		}
		catch(NullPointerException e){
			//e.printStackTrace();
			System.err.println("Null pointer due to overlap with database update.");
		}
		return result;
	}
	
	/**
	 * Method to generate a DEMO sink tree for selected node
	 */
	@POST
	@Path("demotree/{id}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Graph getDemoTree(@PathParam("id") String id, Graph data){
		
		Graph syncTree = null;
		Node router = null;

		for(Node n : data.getNodes()){
			if(id.equals(n.getId())){
				router = n;
				break;
			}
		}

		Dijkstra algo = new Dijkstra(data);
		algo.run(router);
		syncTree = algo.getSyncTree(data);

		return syncTree;
	}

	/**
	 * Method to return DEMO shortest path from / to a specific node
	 */
	@POST
	@Path("demopath/{from}/{to}")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public List<HashMap<String, Integer>> getDemoPath(@PathParam("from") String from, @PathParam("to") String to, Graph data){

		//List<String> listNodes = new ArrayList<>();
		HashMap<Node, Integer> distances = new HashMap<>();
		HashMap<String, Integer> d = new HashMap<>();
		HashMap<String, Integer> hops = new HashMap<>();
		List<HashMap<String, Integer>> result = new ArrayList<>();

		Node n = new Node(from, from, "router");
		Node n2 = new Node(to, to, "router");
		Integer hopNum = 0;

		try {
			Dijkstra algo = new Dijkstra(data);
			algo.run(n);
			distances = algo.getDistance();

			for (HashMap.Entry<Node, Integer> node : distances.entrySet()) {
				d.put(node.getKey().getId(), node.getValue());

			}

			result.add(d);

			LinkedList<Node> path = algo.getPath(n2);

			/*for (Node node : path) {
				listNodes.add(node.getName());
			}*/

			for (Node node : path) {
				hops.put(node.getLabel(), hopNum);
				hopNum++;
			}

			result.add(hops);
		}
		catch(NullPointerException e){
			//e.printStackTrace();
			System.err.println("Null pointer due to overlap with database update.");
		}
		return result;
	}
}