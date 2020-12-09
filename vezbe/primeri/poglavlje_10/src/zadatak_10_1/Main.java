package zadatak_10_1;

import org.hibernate.Session;
import org.hibernate.Transaction;

class Main {
    
    public static void main(String[] args) {
        System.out.println("Pocetak rada...\n");
        
        insertSmer();
        readSmer();
        updateSmer();
        deleteSmer();
        readSmer();
        
        System.out.println("Zavrsetak rada.\n");
        
        // Zatvaranje fabrike sesija
        HibernateUtil.getSessionFactory().close();
    }
    
    private static void insertSmer() {
        // Otvaranje sesije
        Session session = HibernateUtil.getSessionFactory().openSession();
        // Kreiranje objekta klase Smer.
        // U ovom objektu ce biti zapisane sve informacije o novom smeru,
        // koje ce zatim biti skladistene u bazi podataka.
        Smer smer = new Smer();
        
        // Postavljanje odgovarajucih vrednosti za smer
        smer.setId_smera(300);
        smer.setOznaka("MATF_2019");
        smer.setNaziv("Novi MATF smer u 2019. godini");
        smer.setSemestara(8);
        smer.setBodovi(240);
        smer.setNivo(110);
        smer.setZvanje("Diplomirani informaticar");
        smer.setOpis("Novi smer na Matematickom fakultetu");
        
        // Alternativno, mozemo iskoristiti konstruktor koji prima vrednosti za sva polja
        // Smer smer = new Smer(300, "MATF_2019", "Novi MATF smer u 2019. godini", 8, 240, 110, "Diplomirani informaticar", "Novi smer na Matematickom fakultetu");
        
        Transaction TR = null;
        try {
            // Zapocinjemo novu transakciju
            TR = session.beginTransaction();
            
            // Skladistimo kreirani smer u tabelu SMER u bazi podataka
            session.save(smer);
            // Pohranjivanje izmena i zavrsavanje transakcije
            TR.commit();
            
            System.out.println("Smer je sacuvan");
        } catch (Exception e) {
            // Doslo je do greske: ponistavamo izmene u transakciji
            System.out.println("Cuvanje smera nije uspelo! Transakcija se ponistava!");
            
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            // Bilo da je doslo do uspeha ili do neuspeha,
            // duzni smo da zatvorimo sesiju
            session.close();
        }
    }

    private static void readSmer() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
        // Ucitavanje (dohvatanje) smera na osnovu primarnog kljuca
        Smer s = session.get(Smer.class, 300);

        // Provera da li postoji odgovarajuci slog u tabeli
        if (s != null) {
            System.out.println(s);
        }
        else {
            System.out.println("Smer ne postoji!");
        }

        // Zatvaramo sesiju
        session.close();
    }
	
    private static void updateSmer() {
        Session session = HibernateUtil.getSessionFactory().openSession();

        // Ucitavanje (dohvatanje) smera na osnovu primarnog kljuca
        Smer s = session.get(Smer.class, 300);

        Transaction TR = null;

        try {
            TR = session.beginTransaction();
            
            if (s != null) {
                // Azuriranje odgovarajucih polja
                s.setBodovi(180);
                s.setSemestara(6);
            
                // Potvrdjivanje izmena i zavrsavanje transakcije
                TR.commit();
                System.out.println("Smer azuriran!");
            } else {
                System.out.println("Smer ne postoji!");
            }
        } catch (Exception e) {
            System.out.println("Azuriranje smera nije uspelo! Ponistavanje transakcije!");
            
            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }

    private static void deleteSmer() {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Smer smer = new Smer();

        Transaction TR = null;
        try {
            TR = session.beginTransaction();

            // Ucitavanje (dohvatanje) smera na osnovu primarnog kljuca
            session.load(smer, 300);
            // Brisanje ucitanog smera iz baze
            session.delete(smer);

            System.out.println("Smer obrisan!");

            // Potvrdjivanje i zavrsavanje transakcije
            TR.commit();
        } catch (Exception e) {
            System.err.println("Brisanje smera nije uspelo! Ponistavanje transakcije!");

            if (TR != null) {
                TR.rollback();
            }
        } finally {
            session.close();
        }
    }
}
