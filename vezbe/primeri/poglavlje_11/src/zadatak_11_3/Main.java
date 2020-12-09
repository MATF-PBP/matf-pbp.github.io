package zadatak_11_3;

import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class Main {

    public static void main(String[] args) {
        System.out.println("Pocetak rada...\n");

        readSmeroviIStudenti();

        System.out.println("Zavrsetak rada.\n");
        HibernateUtil.getSessionFactory().close();
    }

    public static void readSmeroviIStudenti() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;

        try {
            TR = session.beginTransaction();

            // Kreiramo HQL upit koji izdvaja sve smerove
            String hql = "from Smer";
            Query<Smer> upit = 
                    session.createQuery(hql, Smer.class);
            // Dohvatamo rezultat u listu smerovi,
            // i proveravamo da li su svi elementi objekti klase Smer
            List<Smer> smerovi = Collections.checkedList(upit.list(), Smer.class);

            // Kreiramo HQL upit koji izdvaja sve studente
            // na smeru ciji ce identifikator biti postavljen
            // na mesto imenovane parametarske oznake "id".
            // S obzirom da ovo radimo za svaki smer,
            // dohvatanje rezultata moramo da ugnezdimo unutar petlje
            // koja prolazi smerovima.
            // Drugim recima, koristimo ugnezdjene kursore.
            hql = "from Student where id_smera = :id";
            Query<Student> upit2 = 
                    session.createQuery(hql, Student.class);

            // Spoljasnja petlja: ispisivanje smerova
            for (Smer smer : smerovi) {
                String naziv = smer.getNaziv().trim();
                int idSmera = smer.getId_smera();

                System.out.println("\n\nSMER: " + naziv);

                // Postavljanje vrednosti parametarske oznake za unutrasnju
                // petlju
                upit2.setParameter("id", idSmera);
                upit2.setMaxResults(5); // Da dohvatimo najvise 5 studenata

                // Dohvatanje studenata za tekuci smer
                List<Student> studenti = Collections.checkedList(upit2.list(), Student.class);

                // Unutrasnja petlja: ispisivanje studenata na smeru
                for (Student student : studenti) {
                    int indeks = student.getIndeks();
                    String ime = student.getIme().trim();
                    String prezime = student.getPrezime().trim();
                    double prosek = student.prosek();

                    System.out.println("Student: " + indeks + ", " + ime + ", " + prezime + ", " + prosek);
                }
            }

            TR.commit();
        } catch (Exception e) {
            System.err.println("Postoji problem sa izlistavanjem studenata na smerovima! Ponistavanje transakcije!");
            e.printStackTrace();

            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }

        // Za vezbu: razmisliti kako je moguce uraditi ovaj zadatak
        // bez koriscenja ugnezdjenih kursora
        // (tj. bez koriscenja upita koji pronalazi studente za zadati id_smera)
    }

}
