package zadatak_8_6;

import java.sql.*;

public class Main {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
    	String url = "jdbc:db2://localhost:50000/stud2020";

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            
            String sql = 
                "SELECT     * " + 
                "FROM       DA.ISPITNIROK " + 
                "ORDER BY   NAZIV";
            Statement stmt = con.createStatement(
                // Podesavamo da je kursor bidirekcioni i nesenzitivni,
                // sto znaci da moze da se krece kroz njega u oba smera,
                // i da izmene u bazi podataka nece biti vidljive tokom
                // prolazenja kroz kursora
                ResultSet.TYPE_SCROLL_INSENSITIVE,
                // Podesavamo da se kursor koristi samo za citanje
                ResultSet.CONCUR_READ_ONLY);
            ResultSet res = stmt.executeQuery(sql);

            System.out.printf("%-10s %-15s %-20s %-20s %-20s\n\n", 
                "GODINA", "OZNAKA", "NAZIV", "DATUM_POCETKA", "DATUM_KRAJA");

            // Pozicioniranje na kraj kursora
            res.afterLast();

            // Citanje unazad
            while (res.previous()) {
                int godina = res.getInt(1);
                String oznaka = res.getString(2).trim();
                String naziv = res.getString(3).trim();
                Date datum_pocetka = res.getDate(4);
                Date datum_kraja = res.getDate(5);

                System.out.printf("%-10d %-15s %-20s %-20s %-20s\n", 
                    godina, oznaka, naziv, datum_pocetka.toString(), datum_kraja.toString());
            }

            res.close();
            stmt.close();
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
}