package ie.nuigalway.topology.domain.dao.hibernate;

import java.util.Collection;
import java.util.List;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;

import ie.nuigalway.topology.domain.entities.NetworkLsa;

public class NetworkLsaDAO extends GenericHibernateDAOFilter<NetworkLsa, Integer>{

	private static final String MY_ALIAS = "networklsaa";

	public NetworkLsaDAO(SessionFactory sessionFactory) {
		super(sessionFactory, MY_ALIAS);
	}

	@Override
	public Collection<NetworkLsa> findAll() {
		@SuppressWarnings("unchecked")
		List<NetworkLsa> list = getSession().createCriteria(getPersistentClass()).setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY).list();

		return list;
	}


}
