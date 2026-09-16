package zadatak_9_2;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
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
        String url = "jdbc:db2://localhost:50000/stud2020";

        try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {
            con.setAutoCommit(false);
            con.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (Scanner ulaz = new Scanner(System.in)) {
                izdvoji10NajuspesnijihStudenata(con, ulaz);
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

    private static void izdvoji10NajuspesnijihStudenata(Connection con, Scanner ulaz)
            throws SQLException, IOException {
        Statement stmt = con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
        String sql = 
                "SELECT  * " + 
                "FROM    DA.UKUPNIBODOVI " +
                "FETCH   FIRST 10 ROWS ONLY";
        ResultSet kursor = stmt.executeQuery(sql);

        System.out.println("Obradjujem 10 najuspesnijih studenata: \n");
        int i = 1;
        while (kursor.next()) {
            int indeks = kursor.getInt(1);
            String ime = kursor.getString(2).trim();
            String prezime = kursor.getString(3).trim();
            int espb = kursor.getInt(4);

            System.out.println(i + ". " + ime + " " + prezime + "(" + indeks + ") ima osvojenih " + espb + " ESPB.");
            System.out.println("Da li zelite da dodelite 10 pocasnih bodova? [d/n]");
            String odgovor = ulaz.nextLine();
            if (odgovor.equalsIgnoreCase("d")) {
                kursor.updateInt(4, espb + 10);
                kursor.updateRow();
                System.out.println("Uspesno ste dodeliti pocasne poene!");
            }
            System.out.println();
            ++i;
        }
        kursor.close();
        
        System.out.println("Unesite \"I\", pa zatim ENTER za prikaz izvestaja:");
        ulaz.nextLine();
        
        kursor = stmt.executeQuery(sql);
        i = 1;
        while (kursor.next()) {
            int indeks = kursor.getInt(1);
            String ime = kursor.getString(2);
            String prezime = kursor.getString(3);
            int bodovi = kursor.getInt(4);

            System.out.println(i + ". " + ime + " " + prezime + "(" + indeks + ") ima osvojenih " + bodovi + " ESPB.");
            ++i;
        }
        kursor.close();
        
        // Zavrsavam transakciju
        con.commit();
        
        stmt.close();
    }
}