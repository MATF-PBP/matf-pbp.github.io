package zadatak_11_1;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

class Main {
	
	public static void main(String[] args) {
		System.out.println("Pocetak rada...\n");
		
		readIspitniRokovi();
		
		System.out.println("Zavrsetak rada.\n");
		HibernateUtil.getSessionFactory().close();
	}

	private static void readIspitniRokovi() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;
        
        try {
            TR = session.beginTransaction();
            
            // HQL upit za izdvajanje svih entiteta tipa IspitniRok
            String hql = "FROM IspitniRok";
            // Kreiranje objekta koji sadrzi informacije o HQL upitu.
            // Obratiti paznju da se klasa Query nalazi u paketu org.hibernate.query!!!
            // Takodje, pored samog HQL upita, 
            // metodu createQuery prosledjujemo klasu koja predstavlja entitet rezultata.
            // Drugim recima, kazemo Hibernate-u da zelimo da dohvatimo listu ispitnih rokova.
            Query<IspitniRok> upit = 
                    session.createQuery(hql, IspitniRok.class);
            // Pozivom metoda list() dohvatamo zeljeni rezultat
            List<IspitniRok> ispitniRokovi = upit.list();
            // Iteriranje kroz listu
            for(IspitniRok ir : ispitniRokovi) {
                System.out.println(ir);
            }

            TR.commit();
        } catch (Exception e) {
            System.err.println("Postoji problem sa ispisivanjem ispitnih rokova! Ponistavanje transakcije!");
            e.printStackTrace();
            
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }

}
