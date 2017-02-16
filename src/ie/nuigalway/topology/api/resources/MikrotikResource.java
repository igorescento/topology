package ie.nuigalway.topology.api.resources;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

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
import ie.nuigalway.topology.domain.dao.hibernate.LsaDAO;
import ie.nuigalway.topology.domain.entities.Lsa;
import ie.nuigalway.topology.util.database.HibernateUtil;
import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

@Path("mikrotik")
public class MikrotikResource {

	private SessionFactory sessionFactory;
	private LsaDAO lsaDAOHibernate;

	{
		this.sessionFactory = HibernateUtil.getSessionFactory();
		this.lsaDAOHibernate = new LsaDAO(sessionFactory);
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	public Response retrieveLsa(ConnDataModel data){
		
		List<LsaModel> listLsa = new ArrayList<>();
		//int age;

		//login to router and retrieve LSA table
		try {
			System.out.println("TRYING TO CONNECT");
			ApiConnection con = ApiConnection.connect(data.getIpaddress());
			con.login(data.getUsername(), data.getPassword());
			
			//age = minAge();
			
			List<Map<String, String>> rs = con.execute("/routing/ospf/lsa/print");
			
			//if(compareAge(rs,age)){
				System.out.println("UPDATE NEEDED, DELETING PREVIOUS DATA");
				deleteTable();

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

					//System.out.println(lsa.getBody());
					addLsa(lsa);
					listLsa.add(lsa);
				}
			/*}
			else{
				System.out.println("UPDATE NOT NEEDED");
			}*/

		} catch (MikrotikApiException e) {
			e.printStackTrace();			
			return Response.status(500).entity(e.getMessage()).build();
		}
		return Response.ok().build();
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
     * 
     * @param id
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
	 * Method to delete table
	 * @return
	 */
	public Response deleteTable() {

		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			System.out.println(lsaDAOHibernate.findMinAttribute("age"));

			int result = lsaDAOHibernate.delete();
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

	public int minAge(){
		
		try {
			sessionFactory.getCurrentSession().getTransaction().begin();

			Integer minAge = lsaDAOHibernate.findMinAttribute("age");
			sessionFactory.getCurrentSession().getTransaction().commit();
			return minAge;

		} catch (HibernateException e) {
			e.printStackTrace();
			sessionFactory.getCurrentSession().getTransaction().rollback();
			throw new BasicException(Response.Status.INTERNAL_SERVER_ERROR,
					"Internal problem.",
					"Error while trying to retrieve data. " + e.getMessage());
		}
	}
	/**
	 * Neeeeeeeeeeeeeeeeeeeeeeeeeds fix, returns 2 trues, truncate all the time
	 * @param newLsa
	 * @param minAge
	 * @return
	 */
	public boolean compareAge(List<Map<String,String>> newLsa, int minAge){

		for (Map<String, String> entry : newLsa)
		{
			if(Integer.parseInt((entry.get("age"))) < minAge){
				return true;
			}
		}

		return true;
	}
}
