package zadatak_8_5;

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
            
            Statement stmt = con.createStatement();
            String queryStr = 
                "SELECT IME, " + 
                "       PREZIME, " + 
                "       DATUM_RODJENJA " + 
                "FROM   DOSIJE " + 
                "WHERE  POL = 'z'";
            ResultSet rs = stmt.executeQuery(queryStr);

            System.out.printf("%-25s %-25s %-15s \n\n", "IME", "PREZIME", "DATUM");

            while (rs.next()) {
                String ime = rs.getString(1).trim();
                String prezime = rs.getString(2).trim();
                String datum = rs.getString(3);
                // Ukoliko je u tekucem redu kursora datum bio NULL,
                // onda ce rs.wasNull() biti true
                boolean datumIsNull = rs.wasNull();

                System.out.printf("%-25s %-25s %-15s \n", ime, prezime, (datumIsNull) ? "NEPOZNAT" : datum.trim());
            }

            rs.close();
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