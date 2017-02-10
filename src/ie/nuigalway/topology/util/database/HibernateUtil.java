package ie.nuigalway.topology.util.database;

import org.hibernate.SessionFactory;

public class HibernateUtil {
    
    private static SessionFactory sessionFactory;

    /**
     * Get the Hibernate session factory.
     * 
     * @return
     */
    public static SessionFactory getSessionFactory() {
        if (sessionFactory == null) {
            throw new NullPointerException("Session Factory was not set.");
        }
        return sessionFactory;
    }

    static void setSessionFactory(SessionFactory sf2) {
        if (sf2 != null) {
            sessionFactory = sf2;
        } else {
            throw new NullPointerException();
        }
    }    

}
