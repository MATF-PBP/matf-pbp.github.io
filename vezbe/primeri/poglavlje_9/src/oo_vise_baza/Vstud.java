package oo_vise_baza;

import java.io.IOException;
import java.sql.*;

public class Vstud extends Database {
    public Vstud() throws SQLException {
        dbName = "VSTUD";
        url = "jdbc:db2://localhost:50001/vstud";
        username = "student";
        password = "abcdef";
        connect();
    }

    public void izlistajPolaganja(short ocena) throws SQLException, IOException {
        String sql = readSQLFromFile(System.getProperty("user.dir") + "/bin/zadatak_9_6/izlistajPolaganjaVstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setShort(1, ocena);
        ResultSet rez = stmt.executeQuery();

        System.out.println("Polozeni ispiti studenata koji nemaju ocenu manju od " + ocena);
        while (rez.next()) {
            System.out.println("Indeks: " + rez.getInt(1) + ", " + "Naziv: " + rez.getString(2).trim() + ", "
                    + "Ocena: " + rez.getInt(3) + ", " + "Godina roka: " + rez.getInt(4) + ", " + "Oznaka roka: "
                    + rez.getString(5).trim());
        }

        rez.close();
        stmt.close();
    }

    public void uvecajBodoveZaPredmete(int indeks) throws SQLException, IOException {
        String sql = readSQLFromFile(System.getProperty("user.dir") + "/bin/zadatak_9_6/uvecajBodoveZaPredmeteVstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, indeks);

        int brojAzuriranih = stmt.executeUpdate();

        System.out.println("Broj azuriranih redova: " + brojAzuriranih);

        stmt.close();
    }

}
