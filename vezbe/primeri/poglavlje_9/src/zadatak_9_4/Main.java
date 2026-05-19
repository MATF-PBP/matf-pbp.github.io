package zadatak_9_4;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.*;
import java.util.ArrayList;
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
            con.setTransactionIsolation(Connection.TRANSACTION_REPEATABLE_READ);

            // Postavljanje isteka vremena za katance.
            Statement lockStmt = con.createStatement();
            lockStmt.execute("SET CURRENT LOCK TIMEOUT 5");
            
            try {                
                obradiPredmete(con);
                
                con.commit();
            } catch (Exception e) {
                con.rollback();
                throw e;
            } finally {
                con.setAutoCommit(true);
                // Vracanje podrazumevane vrednosti za istek vremena
                lockStmt.execute("SET CURRENT LOCK TIMEOUT NULL");
                lockStmt.close();
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

    private static void obradiPredmete(Connection con) throws SQLException, IOException {
        try (Scanner ulaz = new Scanner(System.in)) {
            ArrayList<Integer> obradjeniPredmeti = new ArrayList<>();
            String sql = ucitajSql();
                
            Statement stmt = con.createStatement(
                ResultSet.TYPE_FORWARD_ONLY, 
                ResultSet.CONCUR_UPDATABLE,
                // Kursor deklarisemo sa opcijom HOLD_CURSORS_OVER_COMMIT
                // da bi ostao otvoren prilikom izvrsavanja COMMIT naredbe.
                ResultSet.HOLD_CURSORS_OVER_COMMIT);
            
            ResultSet kursor = otvoriKursor(stmt, sql);
            
            // Citanje reda moze dovesti do problema zbog S ili U katanaca,
            // te moramo poziv metoda next() obraditi zasebno,
            // pa zato ide unutar petlje za obradu.
            boolean imaRedova = true;
            while(true) {
                // S ili U katanac
                try {
                    imaRedova = kursor.next();
                }
                catch (SQLException e) {
                    // Obrada katanaca
                    if (-913 <= e.getErrorCode() && e.getErrorCode() <= -911) {
                        kursor.close();
                        kursor = obradiCekanje("FETCH", con, stmt, sql);
                        continue;
                    }
                    throw e;
                }
                
                // Izlaz iz beskonacne petlje
                // ukoliko vise nema redova u kursoru
                if (!imaRedova) {
                    break;
                }
                
                // Inace, dohvatamo podatke
                int idPredmeta = kursor.getInt(1);
                String naziv = kursor.getString(2);
                short espb = kursor.getShort(3);
                
                // Preskacemo one predmete koje smo vec obradili
                if (obradjeniPredmeti.contains(idPredmeta)) {
                    continue;
                }
                
                System.out.printf("\nPredmet %s ima %d bodova\n", naziv.trim(), espb);
                System.out.println("Da li zelite da uvecate broj bodova za 1? [da/ne]");
                
                String odgovor = ulaz.next();
                if (odgovor.equalsIgnoreCase("da")) {
                    // X katanac
                    try {
                        // Ovde koristimo metode updateXXX i updateRow za azuriranje podataka.
                        // Za vezbu uraditi zadatak pozicionirajucom UPDATE naredbom.
                        kursor.updateShort(3, (short) (espb + 1));
                        kursor.updateRow();
                    }
                    catch (SQLException e) {
                        if (-913 <= e.getErrorCode() && e.getErrorCode() <= -911) {
                            kursor.close();
                            kursor = obradiCekanje("UPDATE", con, stmt, sql);
                            continue;
                        }
                        throw e;
                    }
                    
                    System.out.println("Uspesno su azurirani bodovi za tekuci predmet!");
                }
                
                // Evidentiranje obrade tekuceg predmeta
                obradjeniPredmeti.add(idPredmeta);
                
                // Zavrsavamo jednu transakciju
                con.commit();
                
                System.out.println("Da li zelite da zavrsite sa obradom? [da/ne]");
                odgovor = ulaz.next();
                
                if (odgovor.equalsIgnoreCase("da")) {
                    break;
                }
            }
            
            kursor.close();
            stmt.close();
        }
    }

    private static ResultSet otvoriKursor(Statement stmt, String sql) throws SQLException {
        ResultSet kursor = stmt.executeQuery(sql);
        return kursor;
    }
    
    private static ResultSet obradiCekanje(String codeHint, Connection con, Statement stmt, String sql) throws SQLException {
        System.out.printf("[%s] Objekat je zakljucan od strane druge transakcije!\n" +
                "Molimo sacekajte!\n", codeHint);
        
        try {
            con.rollback();
        } catch (SQLException e) {
        }
        
        return otvoriKursor(stmt, sql);
    }
    
    private static String ucitajSql() throws IOException {
        StringBuilder sql = new StringBuilder();
        Files.lines(Paths.get(System.getProperty("user.dir") + "/bin/zadatak_9_4/upit.sql"))
            .forEach(linija -> sql.append(linija).append("\n"));
        return sql.toString();
    }
}