package zadatak_11_2;

import java.util.Arrays;
import java.util.List;
import java.util.Scanner;

import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

class Main {

	public static void main(String[] args) {
		System.out.println("Pocetak rada...\n");

		readStudenti();

		System.out.println("Zavrsetak rada.\n");
		HibernateUtil.getSessionFactory().close();
	}

	private static void readStudenti() {
		Session session = HibernateUtil.getSessionFactory().openSession();
		Transaction TR = null;

		try (Scanner ulaz = new Scanner(System.in)) {
			TR = session.beginTransaction();

			System.out.println("Unesite mesto rodjenja:");
			String mesto = ulaz.next();
			System.out.println("Unesite broj bodova:");
			Integer bodovi = ulaz.nextInt();

			// HQL upit za izdvajanje podataka o studentima
			// sa odredjenim mestom rodjenja i brojem bodova.
			// Kao sto se u FROM klauzi navodi naziv KLASE, a ne TABELE,
			// tako se u WHERE klauzi navode ATRIBUTI, a ne KOLONE.
			String hql = "SELECT s.ime, s.prezime, sm.Naziv " + 
						"FROM Student s INNER JOIN s.smer AS sm " +
						"WHERE s.mesto = :mesto AND " + 
						"sm.Bodovi = :bodovi";
			// Pripremanje upita
			// Zbog projekcije rezultat ce biti tipa Object[]
			Query<Object[]> upit = session.createQuery(hql, Object[].class);
			
			// Postavljanje vrednosti za imenovane parametarske oznake
			upit.setParameter("mesto", mesto);
			upit.setParameter("bodovi", bodovi);

			// Izvrsavanje upita i listanje podataka
			List<Object[]> studenti = upit.list();

			// Ispis rezultata
			if (studenti.size() != 0) {
				for (Object[] student : studenti) {
					System.out.println(Arrays.toString(student));
				}
			} else {
				System.out.println("Nema rezultata za zadate vrednosti!");
			}

			TR.commit();
		} catch (Exception e) {
			System.err.println("Postoji problem sa ispisivanjem podataka o studentima! Ponistavanje transakcije!");
			e.printStackTrace();
			if (TR != null) {
				TR.rollback();
			}
		} finally {
			session.close();
		}
	}

}
