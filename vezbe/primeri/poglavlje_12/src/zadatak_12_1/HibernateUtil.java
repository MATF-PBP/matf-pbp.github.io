package zadatak_12_1;

import org.hibernate.SessionFactory;
import org.hibernate.boot.MetadataSources;
import org.hibernate.boot.registry.StandardServiceRegistry;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;

class HibernateUtil {
    private static SessionFactory sessionFactory = null;

    static {
        try {
            StandardServiceRegistry registry = new StandardServiceRegistryBuilder().configure().build();
            sessionFactory = new MetadataSources(registry)
                    .addAnnotatedClass(Smer.class)
                    .addAnnotatedClass(IspitniRok.class)
                    .addAnnotatedClass(Student.class)
                    .addAnnotatedClass(Ispit.class)
                    .buildMetadata().buildSessionFactory();
        } catch (Throwable e) {
            System.err.println("Session factory error");
            e.printStackTrace();

            System.exit(1);
        }
    }

    static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

}