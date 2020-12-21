package zadatak_8_4;

import java.io.File;
import java.io.FileNotFoundException;
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
        	
            ispitiStudenteIIspite(con);
            
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

    private static void ispitiStudenteIIspite(Connection con) throws FileNotFoundException, SQLException {
        String sql = ucitajSqlIzDatoteke();
        int brojPredmeta = ucitajBrojPredmeta();

        PreparedStatement pstmt = con.prepareStatement(sql.toString());
        pstmt.setInt(1, brojPredmeta);

        ResultSet result = pstmt.executeQuery();
        int brojRedova = 0;

        int indeks = 0;
        
        while (result.next()) {
            // Ispisujemo informacije o studentu
            // samo kad naidjemo na novog studenta
            if (indeks != result.getInt(1)) {
                if (indeks != 0) {
                    System.out.print("\n\n\n");
                }

                System.out.println("Indeks: " + result.getInt(1) + "\n" + "Ime: " + result.getString(2).trim()
                        + "\n" + "Prezime: " + result.getString(3).trim() + "\n" + "Studijski program: "
                        + result.getString(4).trim() + "\n");

                brojRedova = 1;
            }

            // Ispisujemo informacije o i-tom predmetu
            System.out.println(brojRedova + ". predmet: " + result.getString(5).trim() + "\n\t" + "ocena: "
                    + result.getInt(6));

            // Uvecavamo broj reda
            ++brojRedova;
            // Pamtimo tekuci indeks za narednu iteraciju
            indeks = result.getInt(1);
        }

        pstmt.close();
    }

    private static int ucitajBrojPredmeta() {
        int brojPredmeta;
        
        System.out.println("Unesite broj predmeta:");
        try (Scanner ulaz = new Scanner(System.in)) {
            brojPredmeta = ulaz.nextInt();
        }
        return brojPredmeta;
    }

    private static String ucitajSqlIzDatoteke() throws FileNotFoundException {
        // Eclipse aplikacije se pokrecu iz direktorijuma projekta,
        // dakle, u podrazumevanom slucaju,
        // ~/IBM/rationalsdp/workspace/poglavlje_8/.
        // Mozemo koristiti sistemsko svojstvo
        // System.getProperty("user.dir")
        // da dohvatimo putanju u kojoj se pokrece aplikacija.
        // Takodje, prilikom kompiliranja .java datoteka u .class datoteke,
        // Eclipse alat ce kopirati sve druge datoteke iz src/ direktorijuma u bin/ direktorijum.
        String putanja = "./bin/zadatak_8_4/upit.sql";
        StringBuilder sql = new StringBuilder("");
        String linija = null;
        
        try (Scanner skenerFajla = new Scanner(new File(putanja), "utf-8")) {
            while (skenerFajla.hasNextLine()) {
                linija = skenerFajla.nextLine();
                sql.append(linija);
                sql.append("\n");
            }
        }

        return sql.toString();
    }
}