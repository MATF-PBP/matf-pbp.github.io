package zadatak_12_1;

import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Order;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class Main {

    public static void main(String[] args) {
        System.out.println("Pocetak rada\n");

        System.out.println("--------------------------------");
        System.out.println("Izdvajanje podataka na 1. nacin:");
        readStudentiInfo();
        System.out.println("--------------------------------");
        System.out.println("Izdvajanje podataka na 2. nacin:");
        readStudentiInfoMultiselect();

        System.out.println("Zavrsetak rada\n");
        HibernateUtil.getSessionFactory().close();
    }

    private static void readStudentiInfo() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;
        
        try {
            TR = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();

            // Izdvojiti podatke o studentima
            CriteriaQuery<Student> criteria = cb.createQuery(Student.class);
            Root<Student> st = criteria.from(Student.class);
            criteria.select(st);
            // Bez duplikata
            criteria.distinct(true);
            // Cije ime ili prezime pocinju na slovo 'P'
            Predicate p1 = cb.or(cb.like(st.get("ime"), "P%"), 
                                 cb.like(st.get("prezime"), "P%"));
            // Zive u Beogradu ili Kragujevcu
            Predicate p2 = cb.isNotNull(st.get("mesto"));
            Predicate p3 = cb.in(st.get("mesto")).value("Beograd").value("Kragujevac");
            criteria.where(cb.and(p1, p2, p3));
            // Rezultat urediti po mestu rodjenja opadajuce
            Order o1 = cb.desc(st.get("mesto"));
            // pa po imenu i prezimenu rastuce
            Order o2 = cb.asc(st.get("ime"));
            Order o3 = cb.asc(st.get("prezime"));
            criteria.orderBy(o1, o2, o3);

            // Dobijanje podataka o studentima koji zadovoljavaju kriterijume
            List<Student> studenti = session.createQuery(criteria).getResultList();
            for (Student s : studenti) {
                System.out.println(
                        s.getMesto().trim() + " " +
                        s.getIndeks() + " " + 
                        s.getIme().trim() + " " + 
                        s.getPrezime().trim()
                );
            }

            TR.commit();
        } catch (Exception e) {
            System.err.println("Postoji problem sa ispisivanjem informacija o studentima! Ponistavanje transakcije!");
        
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }
    
    private static void readStudentiInfoMultiselect() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;
        
        try {
            TR = session.beginTransaction();

            CriteriaBuilder cb = session.getCriteriaBuilder();

            // Za svakog studenta
            CriteriaQuery<Object[]> criteria = cb.createQuery(Object[].class);
            Root<Student> st = criteria.from(Student.class);
            // Izdvojiti informacije o mestu rodjenja, imenu, prezimenu i indeksu
            criteria.multiselect(st.get("mesto"), st.get("ime"), st.get("prezime"), st.get("indeks"));
            criteria.distinct(true);
            
            // Cije ime ili prezime pocinju na slovo 'P'
            Predicate p1 = cb.or(cb.like(st.get("ime"), "P%"), 
                                 cb.like(st.get("prezime"), "P%"));
            // Zive u Beogradu ili Kragujevcu
            Predicate p2 = cb.isNotNull(st.get("mesto"));
            Predicate p3 = cb.in(st.get("mesto")).value("Beograd").value("Kragujevac");
            criteria.where(cb.and(p1, p2, p3));
            // Rezultat urediti po mestu rodjenja opadajuce
            Order o1 = cb.desc(st.get("mesto"));
            // pa po imenu i prezimenu rastuce
            Order o2 = cb.asc(st.get("ime"));
            Order o3 = cb.asc(st.get("prezime"));
            criteria.orderBy(o1, o2, o3);

            // Dobijanje podataka o studentima koji zadovoljavaju kriterijume
            List<Object[]> studenti = session.createQuery(criteria).getResultList();
            for (Object[] info : studenti) {
                System.out.println(
                        ((String)info[0]).trim() + " " +
                        ((String)info[1]).trim() + " " +
                        ((String)info[2]).trim() + " " +
                        ((Integer)info[3])
                );
            }

            TR.commit();
        } catch (Exception e) {
            System.err.println("Postoji problem sa ispisivanjem informacija o studentima! Ponistavanje transakcije!");
        
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }

}
