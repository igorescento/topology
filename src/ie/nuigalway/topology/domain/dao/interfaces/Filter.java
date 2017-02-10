package ie.nuigalway.topology.domain.dao.interfaces;

import java.util.Set;

/**
 * Interface that can be used to filter a repository of objects of type T
 * on its properties. Note that to use this interface, it should be extended
 * and methods should be implemented to allow a complex query based on T's
 * properties to be built.
 * <p>
 * When the filter's query has been built up, it can be executed with
 * {@link #execute()}, {@link #execute(int, int)} or {@link #executeOne()}.
 * <p>
 * Calling any of the execute* methods will reset the query afterwards.
 *
 * @param <T>
 */
public interface Filter<T> {
    
    /** Order the results should be returned in. */
    public enum ORDER { ASC, DESC }
    
    /** Return the filter's results in a specific order. */
    public Filter<T> orderBy(String property, ORDER order);
    
    /** Execute the filter's query and get all results. */
    public Set<T> execute();
    
    /** Execute the filter's query and get n results starting at offset. */
    public Set<T> execute(int nr, int offset);
    
    /** Execute the filter's query and get one result. */
    public T executeOne();
    
    /** Execute the filter's query and get the number of results. Note that this doesn't reset the filter. */
    public int getCount();
    
    /** Manually reset the filter's query. */
    public void reset();

}