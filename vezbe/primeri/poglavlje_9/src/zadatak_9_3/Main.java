package zadatak_9_3;

import java.io.*;
import java.nio.file.*;
import java.sql.*;
import java.util.*;

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
        String url = "jdbc:db2://localhost:50001/vstud";

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            con.setAutoCommit(false);

            try (Scanner ulaz = new Scanner(System.in)) {
                obrisiNeuspesnaPolaganja(con, ulaz);
                con.commit();
            } catch (Exception e) {
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

    private static void obrisiNeuspesnaPolaganja(Connection con, Scanner ulaz)
            throws SQLException, IOException {
        // Koristimo TYPE_SCROLL_SENSITIVE jer upit sadrzi ORDER BY klauzu.
        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        String sql = ucitajSqlIzDatoteke();
        ResultSet kursor = stmt.executeQuery(sql);

        Savepoint s = null;
        int prethodniIndeks = -1;
        int brojStudenta = 0;
        while (kursor.next()) {
            int indeks = kursor.getInt(1);
            String naziv = kursor.getString(2).trim();
            
            // Ako smo procitali podatke za novog studenta
            if (indeks != prethodniIndeks) {
                // Nemamo sta da uradimo ako obradjujemo prvog studenta
                if (0 != brojStudenta) {
                    // Ako smo vec obradili barem jednog studenta,
                    // onda treba da pitamo korisnika da li zeli da ponisti brisanja za prethodnog studenta
                    System.out.printf("Da li zelite da ponistite brisanja? [da/ne] ");
                    String odgovor = ulaz.nextLine();
                    if (odgovor.equalsIgnoreCase("da")) {
                        con.rollback(s);
                    } else {
                        con.releaseSavepoint(s);
                    }
                }
                
                // Ako je redni broj 10, izlazimo iz petlje
                if (10 == brojStudenta) {
                    break;
                }
                // Inace, pre nego sto predjemo na menjanje podataka za novog studenta,
                // moramo da postavimo novu tacku cuvanja i 
                // azuriramo informacije o prethodnom indeksu i rednom broju
                s = con.setSavepoint();
                prethodniIndeks = indeks;
                ++brojStudenta;
                System.out.println("Brisem nepolozena polaganja za " + brojStudenta + ". studenta: " + indeks);
            }

            System.out.printf("    %s\n", naziv);
            kursor.deleteRow();
        }
        kursor.close();        
        stmt.close();
    }
    
    private static String ucitajSqlIzDatoteke() throws IOException {
        StringBuilder sql = new StringBuilder();
        Files.lines(Paths.get(System.getProperty("user.dir") + "/bin/zadatak_9_3/upit.sql"))
            .forEach(linija -> sql.append(linija).append("\n"));
        return sql.toString();
    }
}