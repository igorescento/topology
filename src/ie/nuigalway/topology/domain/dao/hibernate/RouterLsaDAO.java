package ie.nuigalway.topology.domain.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.hibernate.type.LongType;
import org.hibernate.type.StringType;

import ie.nuigalway.topology.domain.entities.RouterLsa;

public class RouterLsaDAO extends GenericHibernateDAOFilter<RouterLsa, Integer>{

	private static final String MY_ALIAS = "routerlsaa";

	public RouterLsaDAO(SessionFactory sessionFactory) {
		super(sessionFactory, MY_ALIAS);
	}

	@Override
	public Collection<RouterLsa> findAll() {
		@SuppressWarnings("unchecked")
		List<RouterLsa> list = getSession().createCriteria(getPersistentClass()).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

		return list;
	}

	public Collection<RouterLsa> findAllLinkType(String value) {
		@SuppressWarnings("unchecked")
		List<RouterLsa> list = getSession()
			.createCriteria(getPersistentClass()).add(Restrictions.eq("linktype", value)).list();

		return list;
	}

	public Collection<Long> findDistinctId() {
		@SuppressWarnings("unchecked")
		Collection<Long> list = getSession()
			.createCriteria(getPersistentClass())
			.setProjection(Projections.distinct(Projections.property("id")))
			.addOrder(Order.asc("id"))
			.list();

		return list;
	}

	/**
	 * Hibernate method to return concat interfaces for selected router
	 * using SQL query since Hibernate doesn't support vendor specific syntax
	 */
	public String getRouterInterfaces(Long routerid) {
		@SuppressWarnings("unchecked")
		
		String interfaces;
		
		SQLQuery query = getSession().createSQLQuery("SELECT group_concat(inet_ntoa(data)) as interfaces FROM mikrotik.routerlsa where linktype = 'Transit'"
				+ " and id = :routerid");
		query.setParameter("routerid", routerid);
		query.addScalar("interfaces", new StringType());
		interfaces = (String)query.uniqueResult();
		return interfaces;
	}

}
