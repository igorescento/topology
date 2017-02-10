package ie.nuigalway.topology.domain.dao.hibernate;

import java.io.Serializable;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.ws.rs.core.Response;

import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Disjunction;
import org.hibernate.criterion.Restrictions;

import ie.nuigalway.topology.api.exceptions.BasicException;
import ie.nuigalway.topology.domain.dao.interfaces.GenericDAO;

/**
 * None of the methods implemented by this class return ordered results.
 *
 * @param <T>
 * @param <ID>
 */
public abstract class GenericHibernateDAO<T, ID extends Serializable> implements GenericDAO<T, ID> {

	private Class<T> entityClass;

	private SessionFactory sessionFactory;

	@SuppressWarnings("unchecked")
	public GenericHibernateDAO(SessionFactory sessionFactory) {

		// We need to do this because T is not kept at runtime
		this.entityClass = (Class<T>) ((ParameterizedType) getClass()  
				.getGenericSuperclass()).getActualTypeArguments()[0];

		this.sessionFactory = sessionFactory;
	}

	@Override
	public T find(ID id) {
		@SuppressWarnings("unchecked")
		T t = (T) getSession().get(getPersistentClass(), id);
		return t;
	}

	@Override
	public List<T> find(Collection<ID> ids) {

		Disjunction idDisjunction = Restrictions.disjunction();
		for (ID id : ids) {
			idDisjunction.add(Restrictions.idEq(id));
		}

		@SuppressWarnings("unchecked")
		List<T> list = getSession().createCriteria(getPersistentClass())
		.add(idDisjunction)
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();
		return list;
	}

	@Override
	public Collection<T> findAll() {
		@SuppressWarnings("unchecked")
		List<T> list = getSession().createCriteria(getPersistentClass())
		.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)
		.list();
		return list;
	}

	@Override
	public ID create(T entity) {
		@SuppressWarnings("unchecked")
		ID id = (ID) getSession().save(entity);
		return id;
	}

	@Override
	public void update(T entity) {
		getSession().saveOrUpdate(entity);
	}

	@Override
	public void remove(T entity) {
		getSession().delete(entity);
	}

	@Override
	public void removeById(ID id) {
		T entity = find(id);

		if (entity != null) {
			remove(entity);
		}
	}

	@Override
	public List<ID> create(Collection<T> entities) {
		List<ID> results = new ArrayList<ID>(entities.size());
		for (T entity : entities) {
			results.add(create(entity));
		}
		return results;
	}

	@Override
	public void update(Collection<T> entities) {
		for (T entity : entities) {
			update(entity);
		}
	}

	@Override
	public void remove(Collection<T> entities) {
		for (T entity : entities) {
			remove(entity);
		}
	}

	@Override
	public void removeByIds(Collection<ID> ids) {
		List<T> entities = find(ids);
		remove(entities);
	}

	/**
	 * Get the current Hibernate session to use.
	 * 
	 * @return
	 */
	protected Session getSession() {
		return sessionFactory.getCurrentSession();
	}

	/**
	 * Delete table and return total number of deleted rows.
	 * @return
	 * @throws BasicException
	 */
	public int delete() throws BasicException {
		int result = 0;
		
		try {       
			result = getSession()
			.createQuery("delete from " + getPersistentClass().getSimpleName())
			.executeUpdate();
		}
		catch (HibernateException e ) {
			throw new BasicException( Response.Status.INTERNAL_SERVER_ERROR,
					"Unable to truncate the targetted file.", e.getMessage());
		}
		return result;
	}

	/**
	 * Get the class representing the entities persisted by this DAO
	 * 
	 * @return
	 */
	protected Class<T> getPersistentClass() {
		return entityClass;
	}

}
