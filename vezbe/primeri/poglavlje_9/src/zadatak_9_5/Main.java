package zadatak_9_5;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
        String urlVstud = "jdbc:db2://localhost:50001/vstud";
        String urlMstud = "jdbc:db2://localhost:50001/mstud";

        try (
            Connection conVstud = DriverManager.getConnection(urlVstud, "student", "abcdef");
            Connection conMstud = DriverManager.getConnection(urlMstud, "student", "abcdef");
        ) {
            conVstud.setAutoCommit(false);
            conMstud.setAutoCommit(false);

            try (Scanner ulaz = new Scanner(System.in)) {
                // Program redom:
                // Zahteva od korisnika da unese broj bodova B.

                System.out.println("Unesite broj bodova B:");
                short brojBodova = ulaz.nextShort();

                // Iz baze MSTUD izdvaja indeks, ime i prezime studenata
                // koji su polozili sve predmete koji nose vise od B bodova.

                izlistajStudenteMstud(conMstud, brojBodova);

                // Zatim, zahteva od korisnika da unese ocenu O (ceo broj od 6
                // do 10).

                System.out.println("Unesite ocenu O:");
                short ocena = ulaz.nextShort();

                // Iz baze VSTUD izlistava indeks, naziv, ocenu, godinu i oznaku
                // ispitnog roka
                // za sve studente koji nikada nisu dobili ocenu manju nego sto
                // je ocena O.

                izlistajPolaganjaVstud(conVstud, ocena);

                // Nakon ispisivanja tih podataka, u bazi MSTUD, iz tabele ISPIT
                // brise sva polaganja za studenta sa maksimalnim brojem indeksa
                // I
                // iz DOSIJE, i vraca I.

                int indeks = obrisiPolaganjaIVratiIndeksMstud(conMstud);

                // Na kraju, u bazi VSTUD, u tabeli PREDMET
                // za sve predmete koje je polozio student sa brojem indeksa I,
                // uvecava broj bodova za jedan (osim ako je broj bodova veci od
                // 10,
                // tada ostavlja nepromenjeno stanje).

                uvecajBodoveZaPredmeteVstud(conVstud, indeks);
                
                // Potvrdjivanje izmena mora da se vrsi nad obe baze!
                conVstud.commit();
                conMstud.commit();
            } catch (Exception e) {
                // Ponistavanje izmena mora da se vrsi nad obe baze!
                conVstud.rollback();
                conMstud.rollback();
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

    private static void izlistajStudenteMstud(Connection con, short brojBodova)
            throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("izlistajStudenteMstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setShort(1, brojBodova);
        ResultSet rez = stmt.executeQuery();

        System.out.println("\n\nStudenti koji su polozili sve predmete od " + brojBodova + " bodova\n");
        while (rez.next()) {
            System.out.println("Indeks: " + rez.getInt(1) + ", " + "Ime: " + rez.getString(2).trim() + ", "
                    + "Prezime: " + rez.getString(3).trim() + ", ");
        }

        rez.close();
        stmt.close();
    }

    private static void izlistajPolaganjaVstud(Connection con, short ocena) 
            throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("izlistajPolaganjaVstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setShort(1, ocena);
        ResultSet rez = stmt.executeQuery();

        System.out.println("Polozeni ispiti studenata koji nemaju ocenu manju od " + ocena);
        while (rez.next()) {
            System.out.println("Indeks: " + rez.getInt(1) + ", " + "Naziv: " + rez.getString(2).trim() + ", "
                    + "Ocena: " + rez.getInt(3) + ", " + "Godina roka: " + rez.getInt(4) + ", " + "Oznaka roka: "
                    + rez.getString(5).trim());
        }

        rez.close();
        stmt.close();
    }

    private static int obrisiPolaganjaIVratiIndeksMstud(Connection con) 
            throws Exception {
        int indeks = 0;
        Statement stmt = con.createStatement();
        ResultSet rez = stmt.executeQuery(
                "SELECT  MIN(INDEKS) " + 
                "FROM    DOSIJE");

        boolean dohvacenIndeks = rez.next();
        if (!dohvacenIndeks) {
            stmt.close();
            throw new Exception("Ne postoji nijedan indeks u bazi podataka");
        }

        indeks = rez.getInt(1);
        rez.close();

        int brojObrisanih = stmt.executeUpdate(
                "DELETE  FROM ISPIT " + 
                "WHERE   INDEKS = (SELECT MIN(INDEKS) FROM DOSIJE)");
        System.out.println("Broj obrisanih redova: " + brojObrisanih);

        stmt.close();
        return indeks;
    }

    private static void uvecajBodoveZaPredmeteVstud(Connection con, int indeks)
            throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("uvecajBodoveZaPredmeteVstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, indeks);

        int brojAzuriranih = stmt.executeUpdate();
        System.out.println("Broj azuriranih redova: " + brojAzuriranih);

        stmt.close();
    }

    private static String ucitajSqlIzDatoteke(String nazivDatoteke) 
            throws IOException {
        StringBuilder sql = new StringBuilder();
        Files.lines(Paths.get(System.getProperty("user.dir") + "/bin/zadatak_9_5/" + nazivDatoteke))
            .forEach(linija -> sql.append(linija).append("\n"));
        return sql.toString();
    }
}
