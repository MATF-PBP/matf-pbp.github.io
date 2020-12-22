package zadatak_11_3;

import java.util.Collections;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

public class Main {

    public static void main(String[] args) {
        System.out.println("Pocetak rada...\n");

        readStudijskiProgramoviIStudenti();

        System.out.println("Zavrsetak rada.\n");
        HibernateUtil.getSessionFactory().close();
    }

    public static void readStudijskiProgramoviIStudenti() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction TR = null;

        try {
            TR = session.beginTransaction();

            // Kreiramo HQL upit koji izdvaja sve studijske programe
            String hql = "from StudijskiProgram";
            Query<StudijskiProgram> upit = 
                    session.createQuery(hql, StudijskiProgram.class);
            // Dohvatamo rezultat u listu studijskiProgrami,
            // i proveravamo da li su svi elementi objekti klase StudijskiProgram
            List<StudijskiProgram> studijskiProgrami = Collections.checkedList(upit.list(), StudijskiProgram.class);

            // Kreiramo HQL upit koji izdvaja sve studente
            // na studijskom programuu ciji ce identifikator biti postavljen
            // na mesto imenovane parametarske oznake "id".
            // S obzirom da ovo radimo za svaki studijski program,
            // dohvatanje rezultata moramo da ugnezdimo unutar petlje
            // koja prolazi studijskim programima.
            // Drugim recima, koristimo ugnezdjene kursore.
            hql = "from Student where idprograma = :id";
            Query<Student> upit2 = 
                    session.createQuery(hql, Student.class);

            // Spoljasnja petlja: ispisivanje studijskih programa
            for (StudijskiProgram studijskiProgram : studijskiProgrami) {
                String naziv = studijskiProgram.getNaziv().trim();
                int id = studijskiProgram.getId();

                System.out.println("\n\nSTUDISJKI PROGRAM: " + naziv);

                // Postavljanje vrednosti parametarske oznake za unutrasnju
                // petlju
                upit2.setParameter("id", id);
                upit2.setMaxResults(5); // Da dohvatimo najvise 5 studenata

                // Dohvatanje studenata za tekuci studijski program
                List<Student> studenti = Collections.checkedList(upit2.list(), Student.class);

                // Unutrasnja petlja: ispisivanje studenata na studijskom programu
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
            System.err.println("Postoji problem sa izlistavanjem studenata na studijskim programima! Ponistavanje transakcije!");
            e.printStackTrace();

            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }

        // Za vezbu: razmisliti kako je moguce uraditi ovaj zadatak
        // bez koriscenja ugnezdjenih kursora
        // (tj. bez koriscenja upita koji pronalazi studente za zadati id studijskog programa)
    }

}
