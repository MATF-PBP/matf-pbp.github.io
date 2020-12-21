package zadatak_8_9;

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
    	String url = "jdbc:db2://localhost:50000/stud2020";

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            
            izbrisi_nepolozene_ispite(con);

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

    private static void izbrisi_nepolozene_ispite(Connection con) throws SQLException {
        int godina = ucitaj_godinu();
        
        String selectSql = 
            "SELECT INDEKS, " + 
            "       OZNAKA_ROKA, " + 
            "       ID_PREDMETA " +
            "FROM   ISPIT " +
            "WHERE  GODINA_ROKA= ? AND " +   
            "       OCENA = 5 AND " +  
            "       STATUS_PRIJAVE = 'o'";    
        PreparedStatement stmt = con.prepareStatement(selectSql,
            ResultSet.TYPE_FORWARD_ONLY,
            // Podesavamo kursor da bude za menjanje,
            // da bi smo mogli da brisemo redove metodom deleteRow() .
            ResultSet.CONCUR_UPDATABLE);
        stmt.setInt(1, godina);
        ResultSet ispiti = stmt.executeQuery();
        
        while(ispiti.next()) {
            int indeks = ispiti.getInt(1);
            String oznaka_roka = ispiti.getString(2).trim();
            int id_predmeta = ispiti.getInt(3);
            
            ispiti.deleteRow();
            
            System.out.printf("Obrisan je ispit %-10d %-10s %-5d %-10d\n", 
                indeks, oznaka_roka, godina, id_predmeta);
        }
        
        ispiti.close();
        stmt.close();
    }
    
    private static int ucitaj_godinu() {
        int godina;
        
        try (Scanner ulaz = new Scanner(System.in)) {
            System.out.println("Unesite godinu roka za koju zelite da budu obrisani nepolozeni ispiti:");
            godina = ulaz.nextInt();
        }
        
        return godina;
    }
}