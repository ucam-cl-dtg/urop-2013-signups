package uk.ac.cam.signups.util;

import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.DefaultComponentSafeNamingStrategy;
import org.hibernate.service.ServiceRegistry;
import org.hibernate.service.ServiceRegistryBuilder;

public class HibernateUtil {
    private static SessionFactory sf = configureSessionFactory();
    
    private static SessionFactory configureSessionFactory()
            throws HibernateException {
        Configuration configuration = new Configuration();
        configuration.configure();
configuration.setNamingStrategy(new DefaultComponentSafeNamingStrategy());
        ServiceRegistry serviceRegistry = new ServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .buildServiceRegistry();
        SessionFactory sessionFactory = configuration
                .buildSessionFactory(serviceRegistry);
        return sessionFactory;
    }
    
    public static SessionFactory getSF() {
        return sf;
    }
    
    public static Session getSession() {
     Session session = sf.getCurrentSession();
    
     if (session.isOpen()) {
     session = sf.openSession();
     }
    
        return session;
    }

    public static Session getTransaction() {
        Session session = getSession();
        session.beginTransaction();
        return session;
    }
}