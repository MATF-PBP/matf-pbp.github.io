package zadatak_8_7;

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
            
            ispisi_i_azuriraj_predmete(con);

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

    private static void ispisi_i_azuriraj_predmete(Connection con) throws SQLException {
        int[] ucitaniBodovi = ucitaj_stare_i_nove_bodove();
        
        String sql = 
            "SELECT * " + 
            "FROM   PREDMET";
        Statement stmt = con.createStatement( 
                // Dovoljan nam je kursor koji prolazi unapred kroz redove.
                ResultSet.TYPE_FORWARD_ONLY,
                // Definisemo da je kursor azurirajuci,
                // pa su nam dostupni metodi
                // ResultSet.updateXXX i ResultSet.updateRow
                // koje cemo koristiti za azuriranje tekuceg reda
                ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
            System.out.println("Sifra: " + rs.getString(2).trim() + ", " + "Naziv:" + rs.getString(3).trim() + ", "
                    + "Bodovi: " + rs.getInt(5));

            if (rs.getInt(5) == ucitaniBodovi[0]) {
                // Azuriramo vrednost u koloni broj 5 (bodovi) iz upita iznad.
                rs.updateInt(5, ucitaniBodovi[1]);
                // Kada zavrsimo sa azuriranjem svih kolona,
                // pozivamo metod updateRow
                // da bi se sve promene odmah oslikale.
                // Ako zaboravimo da pozovemo ovaj metod,
                // izmene se nece propagirati i bice izgubljene.
                rs.updateRow();

                // Sada kada zahtevamo vrednosti u kolonama,
                // dobicemo azurirane vrednosti:
                System.out.println("------> Izvrsena je naredna promena:");
                System.out.println("        Sifra: " + rs.getString(2).trim() + ", " + "Naziv:" + rs.getString(3).trim()
                        + ", " + "Bodovi: "
                        + rs.getInt(5) /* azurirana vrednost */ + "\n");
            }
        }

        rs.close();
        stmt.close();
    }
    
    private static int[] ucitaj_stare_i_nove_bodove() {
        int[] rezultat = new int[2];
        
        try (Scanner ulaz = new Scanner(System.in)) {
            System.out.println("Unesite stare bodove:");
            rezultat[0] = ulaz.nextInt();
            System.out.println("Unesite nove bodove:");
            rezultat[1] = ulaz.nextInt();
        }
        
        return rezultat;
    }
}