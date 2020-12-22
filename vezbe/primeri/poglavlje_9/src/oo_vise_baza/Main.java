package oo_vise_baza;

import java.sql.SQLException;
import java.util.Scanner;

public class Main {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static void main(String argv[]) {
        try (
            Vstud vstud = new Vstud(); 
            Mstud mstud = new Mstud();
        ) {
            try (Scanner ulaz = new Scanner(System.in)) {
                // Program redom:
                // Zahteva od korisnika da unese broj bodova B.

                System.out.println("Unesite broj bodova B:");
                short brojBodova = ulaz.nextShort();

                // Iz baze MSTUD izdvaja indeks, ime i prezime studenata
                // koji su polozili sve predmete koji nose vise od B bodova.

                mstud.izlistajStudente(brojBodova);

                // Zatim, zahteva od korisnika da unese ocenu O (ceo broj od 6
                // do 10).

                System.out.println("Unesite ocenu O:");
                short ocena = ulaz.nextShort();

                // Iz baze VSTUD izlistava indeks, naziv, ocenu, godinu i oznaku
                // ispitnog roka
                // za sve studente koji nikada nisu dobili ocenu manju nego sto
                // je ocena O.

                vstud.izlistajPolaganja(ocena);

                // Nakon ispisivanja tih podataka, u bazi MSTUD, iz tabele ISPIT
                // brise sva polaganja za studenta sa maksimalnim brojem indeksa
                // I
                // iz DOSIJE, i vraca I.

                int indeks = mstud.obrisiPolaganjaIVratiIndeks();

                // Na kraju, u bazi VSTUD, u tabeli PREDMET
                // za sve predmete koje je polozio student sa brojem indeksa I,
                // uvecava broj bodova za jedan (osim ako je broj bodova veci od
                // 10,
                // tada ostavlja nepromenjeno stanje).

                vstud.uvecajBodoveZaPredmete(indeks);

                // Potvrdjivanje izmena mora da se vrsi nad obe baze!
                vstud.commit();
                mstud.commit();
            } catch (Exception e) {
                // Ponistavanje izmena mora da se vrsi nad obe baze!
                vstud.rollback();
                mstud.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            System.out.println("SQLCODE: " + e.getErrorCode() + "\n" + "SQLSTATE: " + e.getSQLState() + "\n"
                    + "PORUKA: " + e.getMessage());

            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Doslo je do neke greske: " + e.getMessage());

            System.exit(2);
        }
    }

}