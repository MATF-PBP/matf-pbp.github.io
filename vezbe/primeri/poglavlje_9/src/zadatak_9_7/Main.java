package zadatak_9_7;

import java.sql.*;
import java.util.*;
import java.nio.file.*;
import java.io.*;

public class Main {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
    
    private static class Predmet {
        public int idPredmeta;
        public String naziv;
        
        public Predmet(int idPredmeta, String naziv) {
            this.idPredmeta = idPredmeta;
            this.naziv = naziv;
        }
    }

    public static void main(String argv[]) {
        String url = "jdbc:db2://localhost:50000/stud2020";

        try (
            Connection con = DriverManager.getConnection(url, "student", "abcdef");
        ) {
            con.setAutoCommit(false);

            try (Scanner ulaz = new Scanner(System.in)) {
                while(true) {
                    System.out.println("Odaberite jednu od narednih opcija: ");
                    System.out.println("  1. unos");
                    System.out.println("  2. ponistavanje");
                    System.out.println("  3. brisanje");
                    System.out.println("  4. prikazivanje");
                    System.out.println("  5. dalje");
                    System.out.println("Vas unos: ");
                    String odgovor = ulaz.nextLine();
                    System.out.println("----------------------------------------");
                    
                    if (odgovor.equalsIgnoreCase("unos")) {
                        System.out.println("Unesite naziv predmeta: ");
                        String naziv = ulaz.nextLine();
                        
                        ArrayList<Predmet> predmeti = aPronadjiSvePredmete(con, naziv);
                        System.out.println("Pronadjeni predmeti su: ");
                        ArrayList<Predmet> odabraniPredmeti = bIzdvojiPredmeteKojiIspunjavajuUslov(con, predmeti);
                        cObradiPredmete(con, odabraniPredmeti);
                    }
                    else if (odgovor.equalsIgnoreCase("ponistavanje")) {
                        System.out.println("Unesite godinu studija: ");
                        short godina = ulaz.nextShort();
                        ulaz.nextLine(); // '\n'
                        ePonistiStatistike(con, godina, ulaz);
                    }
                    else if (odgovor.equalsIgnoreCase("brisanje")) {                        
                        fObrisiStatistike(con);
                    }
                    else if (odgovor.equalsIgnoreCase("prikazivanje")) {                        
                        gPrikaziStatistike(con);
                    }
                    else if (odgovor.equalsIgnoreCase("dalje")) {
                        break;
                    } else {
                        throw new Exception("Odabrali ste nepostojecu opciju: " + odgovor);
                    }
                }
                
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

    private static ArrayList<Predmet> aPronadjiSvePredmete(Connection con, String upitZaNaziv) throws SQLException {
        ArrayList<Predmet> predmeti = new ArrayList<>();
        String sql = "SELECT ID, TRIM(NAZIV) FROM DA.PREDMET WHERE NAZIV LIKE ?";
        
        PreparedStatement pStmt = con.prepareStatement(sql);
        upitZaNaziv += "%";
        pStmt.setString(1, upitZaNaziv);
        ResultSet kursor = pStmt.executeQuery();
        
        while (kursor.next()) {
            int idPredmeta = kursor.getInt(1);
            String naziv = kursor.getString(2);
            predmeti.add(new Predmet(idPredmeta, naziv));
        }
        
        kursor.close();
        pStmt.close();
        
        return predmeti;
    }

    private static ArrayList<Predmet> bIzdvojiPredmeteKojiIspunjavajuUslov(Connection con,
            ArrayList<Predmet> predmeti) throws SQLException, IOException {
        ArrayList<Predmet> izdvojeniPredmeti = new ArrayList<>();
        
        String sql = ucitajSqlIzDatoteke("izdvajanjePredmetaSaUslovom.sql");
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        for (Predmet predmet : predmeti) {
            pStmt.setInt(1, predmet.idPredmeta);
            ResultSet kursor = pStmt.executeQuery();
            
            boolean imaRedova = kursor.next();
            if (!imaRedova || kursor.getInt(1) == 0) {
                System.out.println("  Za predmet " + predmet.naziv + " sa identifikatorom " + predmet.idPredmeta + " nema studenata koji su ga upisali");
                continue;
            }
            
            int brojUpisanih = kursor.getInt(1);
            System.out.println("  Predmet " + predmet.naziv + " sa identifikatorom " + predmet.idPredmeta + " je upisalo " + brojUpisanih + " student/studenata");
            izdvojeniPredmeti.add(predmet);
            
            kursor.close();
        }
        
        pStmt.close();
        
        return izdvojeniPredmeti;
    }

    private static void cObradiPredmete(Connection con, ArrayList<Predmet> predmeti) throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("statistika.sql");
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        Statement stmt = con.createStatement();
        stmt.execute("SET CURRENT LOCK TIMEOUT 5");
        
        for (Predmet predmet : predmeti) {
            pStmt.setInt(1, predmet.idPredmeta);
            ResultSet kursor = pStmt.executeQuery();
            
            while (true) {
                try {
                    // BEGIN visekorisnicko
                    boolean imaRedova = kursor.next();
                    if (!imaRedova) {
                        break;
                    }
                    
                    int idPredmeta = kursor.getInt(1);
                    short godina = kursor.getShort(2); 
                    int brojStudenata = kursor.getInt(3);
                    boolean brojStNull = kursor.wasNull();
                    int brojPolaganja = kursor.getInt(4);
                    boolean brojPolNull = kursor.wasNull();
                    System.out.println("  Unosim informacije: " + idPredmeta + ", " + godina + ", " + brojStudenata + ", " + brojPolaganja);
                    
                    dUnesiNovuStatistiku(con, idPredmeta, godina, brojStNull ? null : brojStudenata, brojPolNull ? null : brojPolaganja);
                    
                    con.commit();
                    // END visekorisnicko
                } catch (SQLException e) {
                    if (e.getErrorCode() >= -911 && e.getErrorCode() <= -913) {
                        kursor.close();
                        kursor = obradiCekanje(con, stmt, sql);
                        continue;
                    }
                    // Vrati podrazumevanu vrednost za istek vremena, oslobodi resurse 
                    // i prosledi izuzetak main() metodi za obradu
                    stmt.execute("SET CURRENT LOCK TIMEOUT NULL");
                    
                    kursor.close();
                    stmt.close();
                    pStmt.close();
                    
                    throw e;
                }
            }
            
            kursor.close();
        }
        
        stmt.execute("SET CURRENT LOCK TIMEOUT NULL");
        
        stmt.close();
        pStmt.close();
    }
    
    private static void dUnesiNovuStatistiku(Connection con, int idPredmeta, short godina, Integer brojStudenata, Integer brojPolaganja) throws SQLException {
        String sql = "INSERT INTO DA.STATISTIKAUPISANIHKURSEVA VALUES (?, ?, ?, ?, 0)";
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        pStmt.setInt(1, idPredmeta);
        pStmt.setShort(2, godina);
        if (null == brojStudenata) {
            pStmt.setNull(3, java.sql.Types.INTEGER);
        } else {
            pStmt.setInt(3, brojStudenata);
        }
        if (null == brojPolaganja) {
            pStmt.setNull(4, java.sql.Types.INTEGER);
        } else {
            pStmt.setInt(4, brojPolaganja);
        }
        
        pStmt.executeUpdate();
        pStmt.close();
    }
    
    private static void ePonistiStatistike(Connection con, short godina, Scanner ulaz) throws Exception {
        String sql = "SELECT * FROM DA.STATISTIKAUPISANIHKURSEVA WHERE SKGODINA = ?";
        PreparedStatement pStmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        pStmt.setShort(1, godina);
        ResultSet kursor = pStmt.executeQuery();
        
        while (kursor.next()) {
            Savepoint s = con.setSavepoint();
            
            int idPredmeta = kursor.getInt(1);
            kursor.updateShort(5, (short)1);
            kursor.updateRow();
            
            System.out.println("  Da li ste sigurni da zelite da ponistite podatke o predmetu sa identifikatorom " + idPredmeta + " u odabranoj godini " + godina + "? [da/ne]");
            String odgovor = ulaz.nextLine();
            if (odgovor.equalsIgnoreCase("ne")) {
                con.rollback(s);
            } else if (odgovor.equalsIgnoreCase("da")) {
                con.releaseSavepoint(s);
            } else {
                throw new Exception("Uneli ste neispravan odgovor: " + odgovor);
            }
        }
        
        kursor.close();
        pStmt.close();
        
        con.commit();
    }

    private static void fObrisiStatistike(Connection con) throws SQLException {
        String sql = "SELECT * FROM DA.STATISTIKAUPISANIHKURSEVA WHERE PONISTENI = 0";
        Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet kursor = stmt.executeQuery(sql);
        
        while (kursor.next()) {
            int idPredmeta = kursor.getInt(1);
            System.out.println("  Brisem podatke o predmetu sa identifikatorom " + idPredmeta + " u odabranoj godini");
            kursor.deleteRow();
        }
        
        kursor.close();
        stmt.close();
        
        con.commit();
    }
    
    private static void gPrikaziStatistike(Connection con) throws SQLException {
        String sql = "SELECT * FROM DA.STATISTIKAUPISANIHKURSEVA ORDER BY IDPREDMETA";
        Statement stmt = con.createStatement();
        ResultSet kursor = stmt.executeQuery(sql);
        
        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("| ID PREDMETA | GODINA | BROJ STUDENATA | BROJ POLAGANJA | PONISTENO |");
        System.out.println("|-------------+--------+----------------+----------------+-----------|");
        
        while (kursor.next()) {
            String idPredmeta = Integer.toString(kursor.getInt(1));
            String godina = Short.toString(kursor.getShort(2)); 
            String brojStudenata = Integer.toString(kursor.getInt(3));
            if(kursor.wasNull()) {
                brojStudenata = "NULL          ";
            }
            String brojPolaganja = Integer.toString(kursor.getInt(4));
            if (kursor.wasNull()) {
                brojPolaganja = "NULL          ";
            }
            String ponisteni = "FALSE    ";
            if (kursor.getShort(5) == 1) {
                ponisteni = "TRUE     ";
            }
            System.out.printf("| %s%s | %s   | %s%s | %s%s | %s |\n",
                    idPredmeta, new String(new char[11 - idPredmeta.length()]).replace("\0", " "), 
                    godina, brojStudenata, new String(new char[14 - brojStudenata.length()]).replace("\0", " "),
                    brojPolaganja, new String(new char[14 - brojPolaganja.length()]).replace("\0", " "),
                    ponisteni);
        }
        
        System.out.println("|-------------+--------+----------------+----------------+-----------|");
        System.out.println("+--------------------------------------------------------------------+");
        
        kursor.close();
        stmt.close();
    }
    
    private static String ucitajSqlIzDatoteke(String nazivDatoteke) throws IOException {
        StringBuilder sql = new StringBuilder();
        Files.lines(Paths.get(System.getProperty("user.dir") + "/bin/zadatak_9_7/" + nazivDatoteke))
            .forEach(linija -> sql.append(linija).append("\n"));
        return sql.toString();
    }
    
    private static ResultSet obradiCekanje(Connection con, Statement stmt, String sql) throws SQLException {
        System.out.println("Objekat je zakljucan od strane druge transakcije! Molimo sacekajte...");
        
        try {
            con.rollback();
        } catch (SQLException e) {}
        
        return stmt.executeQuery(sql);
    }
}
