package ie.nuigalway.topology.domain.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;

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

}
