package zadatak_8_3;

import java.sql.*;
import java.util.Scanner;

public class Main {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String argv[]) {
        String url = "jdbc:db2://localhost:50001/vstud";

        try (
        		Connection con = DriverManager.getConnection(url, "student", "abcdef");
        		Scanner ulaz = new Scanner(System.in);
    		) {
            // JDBC koristi iskljucivo dinamicke SQL naredbe,
            // tako da se u JDBC ne koriste maticne promenljive.
            // S obzirom da su vrednosti za bodove u klauzama SET i WHERE
            // nepoznate do faze izvrsavanja,
            // potrebno je da koristimo parametarske oznake na tim mestima.
            String updateStr = 
                "UPDATE PREDMET " + 
                "SET    BODOVI = ? " + 
                "WHERE  BODOVI = ?";
            // Zbog toga moramo da koristimo interfejs PreparedStatement,
            // jer interfejs Statement ne radi sa parametarskim oznakama.
            PreparedStatement pUpd = con.prepareStatement(updateStr);

            // Postavljamo odgovarajuce vrednosti za parametarske oznake
            // na osnovu procitanih vrednosti sa standardnog ulaza.
            int x, y;
            x = ulaz.nextInt();
            y = ulaz.nextInt();
            
            // Prvu parametarsku oznaku menjamo celim brojem y.
            pUpd.setInt(1, y);

            // Drugu parametarsku oznaku menjamo celim brojem x.
            pUpd.setInt(2, x);

            // Izvrsavamo naredbu metodom executeUpdate() 
            // koji vraca broj azuriranih redova.
            int numRows = pUpd.executeUpdate();
            System.out.println("Broj azuriranih redova: " + numRows);

            pUpd.close();
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