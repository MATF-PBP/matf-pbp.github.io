package zadatak_8_8;

import java.sql.*;
import java.util.ArrayList;
import java.util.Scanner;

public class Main {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private static class StatistikaPolaganja {
        public int id_predmeta;
        public int broj_polaganja;
        
        public StatistikaPolaganja(int id_predmeta, int broj_polaganja) {
            this.id_predmeta = id_predmeta;
            this.broj_polaganja = broj_polaganja;
        }
    }

    public static void main(String argv[]) {
        String url = "jdbc:db2://localhost:50001/vstud";
        ArrayList<StatistikaPolaganja> statistike = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            
            kreiraj_tabelu(con);
            sakupi_statistiku(con, statistike);
            unesi_predmete_iz_statistike(con, statistike);

        } catch (SQLException e) {
            e.printStackTrace();

            System.out.println("SQLCODE: " + e.getErrorCode() + "\n" + "SQLSTATE: " + e.getSQLState() + "\n"
                    + "PORUKA: " + e.getMessage());

            System.exit(1);
        } catch (Exception e) {
            e.printStackTrace();

            System.exit(2);
        }
    }
    
    private static void kreiraj_tabelu(Connection con) throws SQLException {
        String sql = "CREATE TABLE UNETI_PREDMETI ( "
                + "ID_PREDMETA INTEGER NOT NULL, "
                + "BROJ_POLOZENIH INTEGER NOT NULL, "
                + "PRIMARY KEY (ID_PREDMETA), "
                + "FOREIGN KEY (ID_PREDMETA) REFERENCES PREDMET "
                + ")";
        Statement stmt = con.createStatement();
        try {
            stmt.executeUpdate(sql);
        } catch (SQLException e) {
            // Ignorisemo gresku samo ako je u pitanju greska da tabela vec postoji (-601).
            // Sve ostale ispaljujemo main metodu.
            if (e.getErrorCode() != -601) {
                throw e;
            }
        }
        stmt.close();
    }

    private static void sakupi_statistiku(Connection con, ArrayList<StatistikaPolaganja> statistike) throws SQLException {
        String sql = "SELECT  ID_PREDMETA, COUNT(OCENA) " +
            "FROM    ISPIT " +
            "WHERE   OCENA > 5 AND " +
            "        STATUS_PRIJAVE = 'o' AND " +
            "        ID_PREDMETA NOT IN (SELECT ID_PREDMETA FROM UNETI_PREDMETI) " +
            "GROUP BY ID_PREDMETA";
        Statement stmt = con.createStatement();
        ResultSet kursor = stmt.executeQuery(sql);
        while (kursor.next()) {
            int id_predmeta = kursor.getInt(1);
            int broj_polaganja = kursor.getInt(2); 
            statistike.add(new StatistikaPolaganja(id_predmeta, broj_polaganja));
        }
        kursor.close();
        stmt.close();
    }
    
    private static void unesi_predmete_iz_statistike(Connection con, ArrayList<StatistikaPolaganja> statistike) throws SQLException {
        String sql = 
            "SELECT * " + 
            "FROM   UNETI_PREDMETI";
        Statement stmt = con.createStatement( 
                // Dovoljan nam je kursor koji prolazi unapred kroz redove.
                ResultSet.TYPE_FORWARD_ONLY,
                // Definisemo da je kursor azurirajuci,
                // pa su nam dostupni metodi
                // ResultSet.updateXXX i ResultSet.insertRow
                // koje cemo koristiti za konstrukciju i unos novog reda
                ResultSet.CONCUR_UPDATABLE);
        ResultSet kursor = stmt.executeQuery(sql);
        Scanner ulaz = new Scanner(System.in);
        
        for (StatistikaPolaganja statistika : statistike) {
            // Pozicioniramo kursor na "slog za unos"
            kursor.moveToInsertRow();
            // Konstruisemo "slog za unos" navodjenjem vrednost za svaku kolonu
            kursor.updateInt(1, statistika.id_predmeta);
            kursor.updateInt(2, statistika.broj_polaganja);
            // Izvrsavamo ili ponistavamo unos u zavisnosti od korisnikovog odgovora
            System.out.println("Da li zelite da unete statistiku za predmet " + statistika.id_predmeta + "? [da/ne]");
            String odgovor = ulaz.nextLine();
            if (odgovor.equalsIgnoreCase("da")) {
                kursor.insertRow();
                System.out.println("\tUneta je statistika: " + statistika.id_predmeta + " (" + statistika.broj_polaganja + ")");
            }
            else {
                System.out.println("\tPonistili ste unos!");
            }
            // Vracamo kursor na zapamceni tekuci red
            kursor.moveToCurrentRow();
        }
        
        ulaz.close();
        kursor.close();
        stmt.close();
    }
}