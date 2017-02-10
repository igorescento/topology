package ie.nuigalway.topology.domain.dao.interfaces;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

public interface GenericDAO<T, ID extends Serializable> {
    
    /**
     * Find an entity by its id.
     * 
     * @param id
     * @return entity or null
     */
    public T find(ID id);
    
    /**
     * Find all entities whose id is part of the given collection.
     * 
     * @param ids ids to search for
     * @return list of entities with a matching id
     */
    public List<T> find(Collection<ID> ids);
    
    /**
     * Get all entities managed by this DAO.
     * 
     * @return
     */
    public Collection<T> findAll();
    
    /**
     * Persist the given entity.
     * 
     * Note that the cascading effect depends on
     * the entity's annotations.
     * 
     * @param entity
     * @return id assigned to the entity
     */
    public ID create(T entity);
    
    /**
     * Persist changes to the given entity.
     * 
     * Note that the cascading effect depends on
     * the entity's annotations.
     * 
     * @param entity
     */
    public void update(T entity);
    
    /**
     * Remove an entity from the persistence store.
     * 
     * Note that the cascading effect depends on
     * the entity's annotations.
     * 
     * @param entity
     */
    public void remove(T entity);
    
    /**
     * Remove the entity with the given id.
     * 
     * Note that the cascading effect depends on the entity's annotations.
     * @param id
     */
    public void removeById(ID id);
    
    /**
     * @see GenericDAO#create(Object)
     * 
     * @param entities
     * @return
     */
    public List<ID> create(Collection<T> entities);

    /**
     * @see GenericDAO#update(Object)
     * 
     * @param entities
     */
    public void update(Collection<T> entities);
    
    /**
     * @see GenericDAO#remove(Object)
     * 
     * @param entities
     */
    public void remove(Collection<T> entities);
    
    /**
     * @see GenericDAO#removeById(Serializable)
     * 
     * @param ids
     */
    public void removeByIds(Collection<ID> ids);

}
