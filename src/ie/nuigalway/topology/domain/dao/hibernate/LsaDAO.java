package ie.nuigalway.topology.domain.dao.hibernate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.StringType;

import ie.nuigalway.topology.domain.entities.Lsa;

public class LsaDAO extends GenericHibernateDAOFilter<Lsa, Integer>{

	private static final String MY_ALIAS = "lsaa";

	public LsaDAO(SessionFactory sessionFactory) {
		super(sessionFactory, MY_ALIAS);
	}

	@Override
	public Collection<Lsa> findAll() {
		@SuppressWarnings("unchecked")
		List<Lsa> list = getSession().createCriteria(getPersistentClass()).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

		return list;
	}

	/**
	 * Method to check if there is an existing entry in the table for selected attributes.
	 * @param lsa
	 * @return
	 */
	@SuppressWarnings("unchecked")
	public List<Lsa> exist(Lsa lsa){

		Map<String,Object> cols = new HashMap<>();
		cols.put("id", lsa.getIdTypePk());
		cols.put("checksum", lsa.getChecksum());

		List<Criterion> criterionList = new ArrayList<>(cols.size());
		for (Map.Entry<String, Object> entry : cols.entrySet())
			criterionList.add(Restrictions.eq(entry.getKey(), entry.getValue()));

		Criteria crit = getSession().createCriteria(getPersistentClass());
		if(criterionList != null){
			for(Criterion criterion : criterionList) 
				crit.add(criterion);
		}
		return (List<Lsa>)crit.list();
	}

	/**
	 * Method to retrieve min attribute from table.
	 * @param attribute
	 * @return
	 */
	public Integer findMinAttribute(String attribute) {
		Integer minAttr = 5000;

		Criteria criteria = getSession()
				.createCriteria(getPersistentClass())
				.setProjection(Projections.min(attribute));

		minAttr = (Integer)criteria.uniqueResult() == null ? 5000 : (Integer)criteria.uniqueResult();
		return minAttr;
	}

	public Collection<Lsa> findAllType(String value) {
		@SuppressWarnings("unchecked")
		List<Lsa> list = getSession()
		.createCriteria(getPersistentClass()).add(Restrictions.eq("IdTypePk.type", value)).list();

		return list;
	}
	
	/**
	 * Method to return all external routes based on routerid
	 */
	public String getExternalRoutes(Long routerid) {
		
		String interfaces;
		
		SQLQuery query = getSession().createSQLQuery("SELECT group_concat(inet_ntoa(id)) as external FROM mikrotik.lsa "
				+ " where type = 'as-external' and id <> :routerid");
		query.setParameter("routerid", routerid);
		query.addScalar("external", new StringType());
		interfaces = (String)query.uniqueResult();
		
		return interfaces;
	}
}
