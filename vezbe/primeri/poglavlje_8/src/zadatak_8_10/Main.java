package zadatak_8_10;

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
            
            prodjiKrozSpoljasnjiKursor(con);
            
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

    private static void prodjiKrozSpoljasnjiKursor(Connection con) throws SQLException, FileNotFoundException {
        int ukupnoPredmeta = ucitajBrojPredmeta();

        PreparedStatement pstmt;

        String sql = ucitajSqlIzDatoteke("ispiti.sql");
        pstmt = con.prepareStatement(sql);

        pstmt.setInt(1, ukupnoPredmeta);
        ResultSet ispiti = pstmt.executeQuery();

        while (ispiti.next()) {
            System.out.printf("\n\nIndeks: %-10d\nIme: %-10s\nPrezime: %-20s\nNaziv smera: %-30s\n\n", ispiti.getInt(1),
                    ispiti.getString(2), ispiti.getString(3), ispiti.getString(4));

            int indeks = ispiti.getInt(1);
            prodjiKrozUnutrasnjiKursor(con, indeks);
        }

        ispiti.close();
        pstmt.close();
    }

    private static void prodjiKrozUnutrasnjiKursor(Connection con, int indeks) throws SQLException, FileNotFoundException {
        PreparedStatement pstmt;

        String sql = ucitajSqlIzDatoteke("predmeti.sql");
        pstmt = con.prepareStatement(sql);

        pstmt.setInt(1, indeks);
        ResultSet predmeti = pstmt.executeQuery();

        int redniBr = 1;

        while (predmeti.next()) {
            System.out.printf("\t%d. predmet: %s\n\t\tOcena: %d\n", redniBr, predmeti.getString(1),
                    predmeti.getShort(2));

            ++redniBr;
        }

        predmeti.close();
        pstmt.close();
    }

    private static int ucitajBrojPredmeta() {
        int godina;

        try (Scanner ulaz = new Scanner(System.in)) {
            System.out.println("Unesite broj predmeta N:");
            godina = ulaz.nextInt();
        }

        return godina;
    }
    
    private static String ucitajSqlIzDatoteke(String nazivDatoteke) throws FileNotFoundException {
        String putanja = "./bin/zadatak_8_10/" + nazivDatoteke;
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