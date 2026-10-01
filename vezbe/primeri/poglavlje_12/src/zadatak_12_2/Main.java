package zadatak_12_2;

import java.util.List;
import java.util.Scanner;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.hibernate.Session;
import org.hibernate.Transaction;

public class Main {

    public static void main(String[] args) {
        System.out.println("Pocetak rada\n");

        try (Scanner ulaz = new Scanner(System.in)) {
            System.out.printf("Unesite indeks studenta: ");
            Integer indeks = ulaz.nextInt();
            
            readPolozeniPredmetiCriteria(indeks);
        }

        System.out.println("Zavrsetak rada\n");
        HibernateUtil.getSessionFactory().close();
    }

    public static void readPolozeniPredmetiCriteria(Integer indeks){
        System.out.println("---------------------------------");
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;
        
        try {
            TR = session.beginTransaction();
            
            Student trazeniStudent = session.load(Student.class, indeks);
            String ime = trazeniStudent.getIme();
            String prezime = trazeniStudent.getPrezime();
            System.out.println("Student: " + indeks + " " + ime + " " + prezime);
            
            /* Postavljanje kriterijuma tako da kriterijume ispunjavaju polozeni
             * ispiti studenta sa brojem indeksa indeks */
            CriteriaBuilder cb = session.getCriteriaBuilder();
            
            CriteriaQuery<Ispit> criteria = cb.createQuery(Ispit.class);
            Root<Ispit> ispit = criteria.from(Ispit.class);
            
            Join<Ispit, Student> student = ispit.join("student");
            Join<Ispit, Predmet> predmet = ispit.join("predmet");
            
            criteria.select(ispit);
            criteria.where(cb.and(
                    cb.gt(ispit.get("ocena"), new Integer(5)), 
                    cb.like(ispit.get("status"), "o"), 
                    cb.equal(student.get("indeks"), indeks)
                    )
            );
            criteria.orderBy(cb.asc(predmet.get("naziv")));
            
            List<Ispit> results = session.createQuery(criteria).getResultList();
            results.stream()
                .map(i -> i.getPredmet().getNaziv())
                .forEach(naziv -> System.out.println("Ispit: " + naziv));
            
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
