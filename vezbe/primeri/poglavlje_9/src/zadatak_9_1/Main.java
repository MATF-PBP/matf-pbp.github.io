package zadatak_9_1;

import java.sql.*;
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
        String url = "jdbc:db2://localhost:50000/stud2020";

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            // Iskljucujemo automatsko potvrdjivanje izmena
            con.setAutoCommit(false);
            // Moramo da imamo unutrasnji try-catch blok,
            // kako bismo pozvali metode commit() ili rollback()
            // tik pred zatvaranje konekcije
            try {
                Integer indeks = pronadjiNajveciIndeks(con);
                System.out.println("1. Najveci indeks u tabeli ISPIT je " + indeks);
                obrisiIspite(con, indeks);
                indeks = pronadjiNajveciIndeks(con);
                System.out.println("3. Najveci indeks u tabeli ISPIT je " + indeks);
                potvrdiIliPonistiIzmene(con);
                indeks = pronadjiNajveciIndeks(con);
                System.out.println("5. Najveci indeks u tabeli ISPIT je " + indeks);
                
                // S obzirom da je sve proslo kako treba ako se doslo do ove tacke,
                // potvrdjujemo izmene pre raskidanje konekcije
                con.commit();
            } catch (Exception e) {
                // Ako je bilo gde u kodu u try bloku doslo do gresaka,
                // ovim se osiguravamo da ce rollback() metod biti pozvan
                // pre automatskog zatvaranja konekcije nakon napustanja try-with-resources bloka
                con.rollback();
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

    private static Integer pronadjiNajveciIndeks(Connection con) throws SQLException, Exception {
        String sql = 
            "SELECT  MAX(INDEKS)" +
            "FROM    DA.ISPIT";
        Statement stmt = con.createStatement();
        ResultSet kursor = stmt.executeQuery(sql);
        
        Integer indeks = null;
        boolean imaRezultata = kursor.next();
        if (imaRezultata) {
            indeks = kursor.getInt(1);
        }
        else {
            throw new Exception("Nema ispita u bazi");
        }
        
        kursor.close();
        stmt.close();
        
        return indeks;
    }

    private static void obrisiIspite(Connection con, Integer indeks) throws SQLException {
        String sql = 
            "DELETE  FROM DA.ISPIT " +
            "WHERE   INDEKS = ?";
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, indeks);
        
        stmt.executeUpdate();
        
        System.out.println("2. Obrisani su ispiti za studenta sa indeksom " + indeks);
    }

    private static void potvrdiIliPonistiIzmene(Connection con) throws SQLException {
        System.out.println("4. Da li zelite da potvrdite izmene [y] ili da ponistite izmene [n]?");
        try (Scanner ulaz = new Scanner(System.in)) {
            String odgovor = ulaz.next();
            if (odgovor.equalsIgnoreCase("y")) {
                // Potvrdjivanje izmena
                con.commit();
                System.out.println("Izmene su potvrdjene!");
            }
            else {
                // Ponistavanje izmena
                con.rollback();
                System.out.println("Izmene su ponistene!");
            }
        }
    }
}