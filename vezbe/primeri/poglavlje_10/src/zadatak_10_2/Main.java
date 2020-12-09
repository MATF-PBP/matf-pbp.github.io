package zadatak_10_2;

import org.hibernate.Session;
import org.hibernate.Transaction;

class Main {
	
	public static void main(String[] args) {
		System.out.println("Pocetak rada...\n");

		insertIspitniRok();
		deleteIspitniRok();
		
		System.out.println("Zavrsetak rada.\n");
		HibernateUtil.getSessionFactory().close();
	}

	private static void insertIspitniRok() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		// Kreiramo praznu instancu objekta ispitnog roka 
		// koju cemo popuniti vrednostima koje treba sacuvati
		IspitniRok ir = new IspitniRok();
		
		// Kreiramo prvo identifikator, tj. slozeni kljuc,
		// a zatim i ostale podatke
		IspitniRokId id = new IspitniRokId(2019, "jun");
		ir.setId(id);
		ir.setNaziv("Jun 2019");
		ir.setPocetak("6/1/2019");
		ir.setKraj("6/22/2018");
		// Ne moramo da podesimo jer ce biti izabrana podrazumevana vrednost
		// ir.setTip("B");
		
		// Procedura za cuvanje je ista kao i do sada
		Transaction TR = null;
		try {
			TR = session.beginTransaction();
			
			session.save(ir);
			
			System.out.println("Ispitni rok je sacuvan!");
			TR.commit();
		} catch (Exception e) {
			System.err.println("Cuvanje ispitnog roka nije uspelo! Ponistavanje transakcije!");
			
			if (TR != null) {
				TR.rollback();
			}
		} finally {
			session.close();
		}
	}
	
	private static void deleteIspitniRok() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		
		IspitniRok ir = new IspitniRok();
		IspitniRokId id = new IspitniRokId(2019, "jun");
		
		Transaction TR = null;
		try {
			TR = session.beginTransaction();
			
			session.load(ir, id);
			session.delete(ir);
			
			System.out.println("Ispitni rok je obrisan!");
			TR.commit();
		} catch (Exception e) {
			System.err.println("Brisanje ispitnog roka nije uspelo! Ponistavanje transakcije!");
		
			if (TR != null) {
				TR.rollback();
			}
		} finally {
			session.close();
		}		
	}

}
