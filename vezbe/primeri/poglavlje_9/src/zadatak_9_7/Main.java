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
        public int id_predmeta;
        public String naziv;
        
        public Predmet(int id_predmeta, String naziv) {
            this.id_predmeta = id_predmeta;
            this.naziv = naziv;
        }
    }

    public static void main(String argv[]) {
        String urlVstud = "jdbc:db2://localhost:50001/vstud";
        String urlMstud = "jdbc:db2://localhost:50001/mstud";

        try (
            Connection conVstud = DriverManager.getConnection(urlVstud, "student", "abcdef");
            Connection conMstud = DriverManager.getConnection(urlMstud, "student", "abcdef");
        ) {
            conVstud.setAutoCommit(false);
            conMstud.setAutoCommit(false);

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
                        
                        ArrayList<Predmet> predmeti = a_pronadji_sve_predmete(conVstud, naziv);
                        System.out.println("Pronadjeni predmeti su: ");
                        ArrayList<Predmet> odabraniPredmeti = b_izdvoji_predmete_koji_ispunjavaju_uslov(conVstud, predmeti);
                        c_obradi_predmete(conVstud, odabraniPredmeti);
                    }
                    else if (odgovor.equalsIgnoreCase("ponistavanje")) {
                        System.out.println("Unesite godinu studija: ");
                        short godina = ulaz.nextShort();
                        ulaz.nextLine(); // '\n'
                        e_ponisti_statistike(conVstud, godina, ulaz);
                    }
                    else if (odgovor.equalsIgnoreCase("brisanje")) {                        
                        f_obrisi_statistike(conVstud);
                    }
                    else if (odgovor.equalsIgnoreCase("prikazivanje")) {                        
                        g_prikazi_statistike(conVstud);
                    }
                    else if (odgovor.equalsIgnoreCase("dalje")) {
                        break;
                    } else {
                        throw new Exception("Odabrali ste nepostojecu opciju: " + odgovor);
                    }
                }
                
                System.out.println("Unesite ocenu: ");
                short ocena = ulaz.nextShort();
                h_prikazi_statistike(conMstud, ocena);
                
                conVstud.commit();
                conMstud.commit();
            } catch (Exception e) {
                conVstud.rollback();
                conMstud.rollback();
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

    private static ArrayList<Predmet> a_pronadji_sve_predmete(Connection con, String upitZaNaziv) throws SQLException {
        ArrayList<Predmet> predmeti = new ArrayList<>();
        String sql = "SELECT ID_PREDMETA, TRIM(NAZIV) FROM PREDMET WHERE NAZIV LIKE ?";
        
        PreparedStatement pStmt = con.prepareStatement(sql);
        upitZaNaziv += "%";
        pStmt.setString(1, upitZaNaziv);
        ResultSet kursor = pStmt.executeQuery();
        
        while (kursor.next()) {
            int id_predmeta = kursor.getInt(1);
            String naziv = kursor.getString(2);
            predmeti.add(new Predmet(id_predmeta, naziv));
        }
        
        kursor.close();
        pStmt.close();
        
        return predmeti;
    }

    private static ArrayList<Predmet> b_izdvoji_predmete_koji_ispunjavaju_uslov(Connection con,
            ArrayList<Predmet> predmeti) throws SQLException, IOException {
        ArrayList<Predmet> izdvojeniPredmeti = new ArrayList<>();
        
        String sql = ucitajSqlIzDatoteke("izdvajanjePredmetaSaUslovom.sql");
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        for (Predmet predmet : predmeti) {
            pStmt.setInt(1, predmet.id_predmeta);
            ResultSet kursor = pStmt.executeQuery();
            
            boolean imaRedova = kursor.next();
            if (!imaRedova || kursor.getInt(1) == 0) {
                System.out.println("  Za predmet " + predmet.naziv + " sa identifikatorom " + predmet.id_predmeta + " nema studenata koji su ga upisali");
                continue;
            }
            
            int broj_upisanih = kursor.getInt(1);
            System.out.println("  Predmet " + predmet.naziv + " sa identifikatorom " + predmet.id_predmeta + " je upisalo " + broj_upisanih + " student/studenata");
            izdvojeniPredmeti.add(predmet);
            
            kursor.close();
        }
        
        pStmt.close();
        
        return izdvojeniPredmeti;
    }

    private static void c_obradi_predmete(Connection con, ArrayList<Predmet> predmeti) throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("statistika.sql");
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        Statement stmt = con.createStatement();
        stmt.execute("SET CURRENT LOCK TIMEOUT 5");
        
        for (Predmet predmet : predmeti) {
            pStmt.setInt(1, predmet.id_predmeta);
            ResultSet kursor = pStmt.executeQuery();
            
            while (true) {
                try {
                    // BEGIN visekorisnicko
                    boolean imaRedova = kursor.next();
                    if (!imaRedova) {
                        break;
                    }
                    
                    int id_predmeta = kursor.getInt(1);
                    short godina = kursor.getShort(2); 
                    int broj_studenata = kursor.getInt(3);
                    boolean broj_st_null = kursor.wasNull();
                    int broj_polaganja = kursor.getInt(4);
                    boolean broj_pol_null = kursor.wasNull();
                    System.out.println("  Unosim informacije: " + id_predmeta + ", " + godina + ", " + broj_studenata + ", " + broj_polaganja);
                    
                    d_unesi_novu_statistiku(con, id_predmeta, godina, broj_st_null ? null : broj_studenata, broj_pol_null ? null : broj_polaganja);
                    
                    con.commit();
                    // END visekorisnicko
                } catch (SQLException e) {
                    if (e.getErrorCode() == -911 || e.getErrorCode() == -913) {
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
    
    private static void d_unesi_novu_statistiku(Connection con, int id_predmeta, short godina, Integer broj_studenata, Integer broj_polaganja) throws SQLException {
        String sql = "INSERT INTO STATISTIKA_UPISANIH_KURSEVA VALUES (?, ?, ?, ?, 0)";
        PreparedStatement pStmt = con.prepareStatement(sql);
        
        pStmt.setInt(1, id_predmeta);
        pStmt.setShort(2, godina);
        if (null == broj_studenata) {
            pStmt.setNull(3, java.sql.Types.INTEGER);
        } else {
            pStmt.setInt(3, broj_studenata);
        }
        if (null == broj_polaganja) {
            pStmt.setNull(4, java.sql.Types.INTEGER);
        } else {
            pStmt.setInt(4, broj_polaganja);
        }
        
        pStmt.executeUpdate();
        pStmt.close();
    }
    
    private static void e_ponisti_statistike(Connection con, short godina, Scanner ulaz) throws Exception {
        String sql = "SELECT * FROM STATISTIKA_UPISANIH_KURSEVA WHERE GODINA = ?";
        PreparedStatement pStmt = con.prepareStatement(sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        pStmt.setShort(1, godina);
        ResultSet kursor = pStmt.executeQuery();
        
        while (kursor.next()) {
            Savepoint s = con.setSavepoint();
            
            int id_predmeta = kursor.getInt(1);
            kursor.updateShort(5, (short)1);
            kursor.updateRow();
            
            System.out.println("  Da li ste sigurni da zelite da ponistite podatke o predmetu sa identifikatorom " + id_predmeta + " u odabranoj godini " + godina + "? [da/ne]");
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

    private static void f_obrisi_statistike(Connection con) throws SQLException {
        String sql = "SELECT * FROM STATISTIKA_UPISANIH_KURSEVA WHERE PONISTENI = 0";
        Statement stmt = con.createStatement(ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_UPDATABLE);
        ResultSet kursor = stmt.executeQuery(sql);
        
        while (kursor.next()) {
            int id_predmeta = kursor.getInt(1);
            System.out.println("  Brisem podatke o predmetu sa identifikatorom " + id_predmeta + " u odabranoj godini");
            kursor.deleteRow();
        }
        
        kursor.close();
        stmt.close();
        
        con.commit();
    }
    
    private static void g_prikazi_statistike(Connection con) throws SQLException {
        String sql = "SELECT * FROM STATISTIKA_UPISANIH_KURSEVA ORDER BY ID_PREDMETA";
        Statement stmt = con.createStatement();
        ResultSet kursor = stmt.executeQuery(sql);
        
        System.out.println("+--------------------------------------------------------------------+");
        System.out.println("| ID_PREDMETA | GODINA | BROJ STUDENATA | BROJ POLAGANJA | PONISTENO |");
        System.out.println("|-------------+--------+----------------+----------------+-----------|");
        
        while (kursor.next()) {
            String id_predmeta = Integer.toString(kursor.getInt(1));
            String godina = Short.toString(kursor.getShort(2)); 
            String broj_studenata = Integer.toString(kursor.getInt(3));
            if(kursor.wasNull()) {
                broj_studenata = "NULL          ";
            }
            String broj_polaganja = Integer.toString(kursor.getInt(4));
            if (kursor.wasNull()) {
                broj_polaganja = "NULL          ";
            }
            String ponisteni = "FALSE    ";
            if (kursor.getShort(5) == 1) {
                ponisteni = "TRUE     ";
            }
            System.out.printf("| %s%s | %s   | %s%s | %s%s | %s |\n",
                    id_predmeta, new String(new char[11 - id_predmeta.length()]).replace("\0", " "), 
                    godina, broj_studenata, new String(new char[14 - broj_studenata.length()]).replace("\0", " "),
                    broj_polaganja, new String(new char[14 - broj_polaganja.length()]).replace("\0", " "),
                    ponisteni);
        }
        
        System.out.println("|-------------+--------+----------------+----------------+-----------|");
        System.out.println("+--------------------------------------------------------------------+");
        
        kursor.close();
        stmt.close();
    }

    private static void h_prikazi_statistike(Connection con, short ocena) throws SQLException, IOException {
        String sql = ucitajSqlIzDatoteke("statistikaMSTUD.sql");
        PreparedStatement pStmt = con.prepareStatement(sql);
        pStmt.setShort(1, ocena);
        ResultSet kursor = pStmt.executeQuery();
        
        while (kursor.next()) {
            int indeks = kursor.getInt(1);
            String ime = kursor.getString(2).trim();
            String prezime = kursor.getString(3).trim();
            double prosek = kursor.getDouble(4);
            
            System.out.println("  Student " + ime + " " + prezime + " (" + indeks + ") ima prosek" + prosek);
        }
        
        kursor.close();
        pStmt.close();
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
