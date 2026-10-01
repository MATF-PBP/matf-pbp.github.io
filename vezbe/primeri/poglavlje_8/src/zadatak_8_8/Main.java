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
        public int idPredmeta;
        public int brojPolaganja;
        
        public StatistikaPolaganja(int idPredmeta, int brojPolaganja) {
            this.idPredmeta = idPredmeta;
            this.brojPolaganja = brojPolaganja;
        }
    }

    public static void main(String argv[]) {
        String url = "jdbc:db2://localhost:50000/stud2020";
        ArrayList<StatistikaPolaganja> statistike = new ArrayList<>();

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            
            kreirajTabelu(con);
            sakupiStatistiku(con, statistike);
            unesiPredmeteIzStatistike(con, statistike);

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
    
    private static void kreirajTabelu(Connection con) throws SQLException {
        String sql = "CREATE TABLE DA.UNETIPREDMETI ( "
                + "IDPREDMETA INTEGER NOT NULL, "
                + "BROJPOLOZENIH INTEGER NOT NULL, "
                + "PRIMARY KEY (IDPREDMETA), "
                + "FOREIGN KEY (IDPREDMETA) REFERENCES DA.PREDMET "
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

    private static void sakupiStatistiku(Connection con, ArrayList<StatistikaPolaganja> statistike) throws SQLException {
        String sql = "SELECT  IDPREDMETA, COUNT(OCENA) " +
            "FROM    DA.ISPIT " +
            "WHERE   OCENA > 5 AND " +
            "        STATUS = 'o' AND " +
            "        IDPREDMETA NOT IN (SELECT IDPREDMETA FROM DA.UNETIPREDMETI) " +
            "GROUP BY IDPREDMETA";
        Statement stmt = con.createStatement();
        ResultSet kursor = stmt.executeQuery(sql);
        while (kursor.next()) {
            int idPredmeta = kursor.getInt(1);
            int brojPolaganja = kursor.getInt(2); 
            statistike.add(new StatistikaPolaganja(idPredmeta, brojPolaganja));
        }
        kursor.close();
        stmt.close();
    }
    
    private static void unesiPredmeteIzStatistike(Connection con, ArrayList<StatistikaPolaganja> statistike) throws SQLException {
        String sql = 
            "SELECT * " + 
            "FROM   DA.UNETIPREDMETI";
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
            kursor.updateInt(1, statistika.idPredmeta);
            kursor.updateInt(2, statistika.brojPolaganja);
            // Izvrsavamo ili ponistavamo unos u zavisnosti od korisnikovog odgovora
            System.out.println("Da li zelite da unete statistiku za predmet " + statistika.idPredmeta + "? [da/ne]");
            String odgovor = ulaz.nextLine();
            if (odgovor.equalsIgnoreCase("da")) {
                kursor.insertRow();
                System.out.println("\tUneta je statistika: " + statistika.idPredmeta + " (" + statistika.brojPolaganja + ")");
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