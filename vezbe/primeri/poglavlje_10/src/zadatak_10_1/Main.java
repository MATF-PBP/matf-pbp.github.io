package zadatak_10_1;

import org.hibernate.Session;
import org.hibernate.Transaction;

class Main {
    
    public static void main(String[] args) {
        System.out.println("Pocetak rada...\n");
        
        insertStudijskiProgram();
        readStudijskiProgram();
        updateStudijskiProgram();
        deleteStudijskiProgram();
        readStudijskiProgram();
        
        System.out.println("Zavrsetak rada.\n");
        
        // Zatvaranje fabrike sesija
        HibernateUtil.getSessionFactory().close();
    }
    
    private static void insertStudijskiProgram() {
        // Otvaranje sesije
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Kreiranje objekta klase StudijskiProgram.
        // U ovom objektu ce biti zapisane sve informacije o novom studijskom programu,
        // koje ce zatim biti skladistene u bazi podataka.
        StudijskiProgram studijskiProgram = new StudijskiProgram();
        
        // Postavljanje odgovarajucih vrednosti za studijski program
        studijskiProgram.setId(102);
        studijskiProgram.setOznaka("MATF_2020");
        studijskiProgram.setNaziv("Novi MATF studijski program u 2020. godini");
        studijskiProgram.setEspb(240);
        studijskiProgram.setNivo(1);
        studijskiProgram.setZvanje("Diplomirani informaticar");
        studijskiProgram.setOpis("Novi studijski program na Matematickom fakultetu");
        
        // Alternativno, mozemo iskoristiti konstruktor koji prima vrednosti za sva polja
        // StudijskiProgram studijskiProgram = new StudijskiProgram(102, "MATF_2020", "Novi MATF studijski program u 2020. godini", 240, 1, "Diplomirani informaticar", "Novi studijski program na Matematickom fakultetu");
        
        Transaction TR = null;
        try {
            // Zapocinjemo novu transakciju
            TR = session.beginTransaction();
            
            // Skladistimo kreirani studijski program u tabelu STUDIJSKIPROGRAM u bazi podataka
            session.save(studijskiProgram);
            // Pohranjivanje izmena i zavrsavanje transakcije
            TR.commit();
            
            System.out.println("Studijski program je sacuvan");
        } catch (Exception e) {
            // Doslo je do greske: ponistavamo izmene u transakciji
            System.out.println("Cuvanje studijskog programa nije uspelo! Transakcija se ponistava!");
            
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            // Bilo da je doslo do uspeha ili do neuspeha,
            // duzni smo da zatvorimo sesiju
            session.close();
        }
    }

    private static void readStudijskiProgram() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        
        // Ucitavanje (dohvatanje) studijskog programa na osnovu primarnog kljuca
        StudijskiProgram s = session.get(StudijskiProgram.class, 102);

        // Provera da li postoji odgovarajuci slog u tabeli
        if (s != null) {
            System.out.println(s);
        }
        else {
            System.out.println("Studijski program ne postoji!");
        }

        // Zatvaramo sesiju
        session.close();
    }
    
    private static void updateStudijskiProgram() {
        Session session = HibernateUtil.getSessionFactory().openSession();

        // Ucitavanje (dohvatanje) studijskog programa na osnovu primarnog kljuca
        StudijskiProgram s = session.get(StudijskiProgram.class, 102);

        Transaction TR = null;

        try {
            TR = session.beginTransaction();
            
            if (s != null) {
                // Azuriranje odgovarajucih polja
                s.setEspb(180);
            
                // Potvrdjivanje izmena i zavrsavanje transakcije
                TR.commit();
                System.out.println("Studijski program azuriran!");
            } else {
                System.out.println("Studijski program ne postoji!");
            }
        } catch (Exception e) {
            System.out.println("Azuriranje studijskog programa nije uspelo! Ponistavanje transakcije!");
            
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }

    private static void deleteStudijskiProgram() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        StudijskiProgram studijskiProgram = new StudijskiProgram();

        Transaction TR = null;
        try {
            TR = session.beginTransaction();

            // Ucitavanje (dohvatanje) studijskog programa na osnovu primarnog kljuca
            session.load(studijskiProgram, 102);
            // Brisanje ucitanog studijskog programa iz baze
            session.delete(studijskiProgram);

            System.out.println("Studijski program obrisan!");

            // Potvrdjivanje i zavrsavanje transakcije
            TR.commit();
        } catch (Exception e) {
            System.err.println("Brisanje studijskog programa nije uspelo! Ponistavanje transakcije!");

            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }
}
