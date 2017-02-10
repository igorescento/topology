package ie.nuigalway.topology.domain.dao.hibernate;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.hibernate.Criteria;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;

import ie.nuigalway.topology.domain.dao.interfaces.Filter;

public abstract class GenericHibernateDAOFilter<T,ID extends Serializable> extends GenericHibernateDAO<T, ID> implements Filter<T> {
    
    private String alias;
    
    // Represents the filter
    protected DetachedCriteria filterCriteria;
    // Keep track of the different orderings added to this filter
    protected List<Order> filterOrder = new ArrayList<Order>();

    public GenericHibernateDAOFilter(SessionFactory sessionFactory, String alias) {
        super(sessionFactory);
        
        this.alias = alias;
        this.filterCriteria = DetachedCriteria
                .forClass(getPersistentClass(), alias)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }

    @Override
    public Filter<T> orderBy(String property, ORDER order) {
        Order anOrder = null;
        switch (order) {
        case ASC:
            anOrder = Order.asc(property);
            break;
        case DESC:
            anOrder = Order.desc(property);
            break;
        }
        
        if (anOrder != null) {
            filterCriteria.addOrder(anOrder);
            filterOrder.add(anOrder);
        }
        
        return this;
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> execute() {
        List<T> result = filterCriteria.getExecutableCriteria(getSession()).list();
        
        this.reset();
        
        return new LinkedHashSet<T>(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public Set<T> execute(int nr, int offset) {
        
        /** For more info on why we use two Criteria here, see the following links:
         * 
         * http://blog.xebia.com/2008/12/11/sorting-and-pagination-with-hibernate-criteria-how-it-can-go-wrong-with-joins/
         * http://stackoverflow.com/questions/9418268/hibernate-distinct-results-with-pagination/9420509#9420509
         * 
         * We basically apply pagination to a list of entity id's that have the actual
         * filter and its ordering applied to them. Subsequently we use that list of ids
         * to get the corresponding entities.
         */
        
        filterCriteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        filterCriteria.setProjection(Projections.id());
        
        // TODO can this extra db request be prevented?
        Criteria inner = filterCriteria.getExecutableCriteria(getSession()).setFirstResult(offset).setMaxResults(nr);
        
        Criteria outer = getSession().createCriteria(getPersistentClass())
                .add(Restrictions.in(this.getIdColumnName(),  inner.list()))
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
        
        // Apply the filter's ordering to the outer criteria
        for (Order anOrder : filterOrder) {
            outer.addOrder(anOrder);
        }
        
        List<T> result = outer.list();
        
        this.reset();
        
        return new LinkedHashSet<T>(result);
    }

    @Override
    @SuppressWarnings("unchecked")
    public T executeOne() {
        T result = (T) filterCriteria.getExecutableCriteria(getSession()).uniqueResult();
        
        this.reset();
        
        return result;
    }

    @Override
    public int getCount() {
        Long count = (Long) filterCriteria.setProjection(Projections.rowCount())
                .getExecutableCriteria(getSession())
                .uniqueResult();
        
        // Remove the count projection so the filter can be executed afterwards
        filterCriteria.setProjection(null);
        
        return count.intValue();
    }

    @Override
    public void reset() {
        this.filterOrder.clear();
        this.filterCriteria = DetachedCriteria
                .forClass(getPersistentClass(), alias)
                .setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
    }
    
    /**
     * Return the name of the id column associated with entities of type {@link T}.
     * 
     * This method should be overridden by any Filters for entities whose id column name
     * is not "id".
     * 
     * @return
     */
    protected String getIdColumnName() {
        return "id";
    }

}
