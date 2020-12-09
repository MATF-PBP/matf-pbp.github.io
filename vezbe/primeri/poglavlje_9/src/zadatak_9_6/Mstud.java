package zadatak_9_6;

import java.io.IOException;
import java.sql.*;

public class Mstud extends Database {
    public Mstud() throws SQLException {
        dbName = "MSTUD";
        url = "jdbc:db2://localhost:50001/mstud";
        username = "student";
        password = "abcdef";
        connect();
    }

    public void izlistajStudente(short brojBodova) throws SQLException, IOException {
        String sql = readSQLFromFile(System.getProperty("user.dir") + "/bin/zadatak_9_6/izlistajStudenteMstud.sql");
        PreparedStatement stmt = con.prepareStatement(sql);

        stmt.setShort(1, brojBodova);
        ResultSet rez = stmt.executeQuery();

        System.out.println("\n\nStudenti koji su polozili sve predmete od " + brojBodova + " bodova\n");
        while (rez.next()) {
            System.out.println("Indeks: " + rez.getInt(1) + ", " + "Ime: " + rez.getString(2).trim() + ", "
                    + "Prezime: " + rez.getString(3).trim() + ", ");
        }

        rez.close();
        stmt.close();
    }

    public int obrisiPolaganjaIVratiIndeks() throws Exception {
        int indeks = 0;

        Statement stmt = con.createStatement();

        ResultSet rez = stmt.executeQuery("SELECT min(indeks) FROM dosije ");

        boolean dohvacenIndeks = rez.next();
        if (!dohvacenIndeks) {
            throw new Exception("Ne postoji nijedan indeks u bazi podataka");
        }

        indeks = rez.getInt(1);

        rez.close();

        int brojObrisanih = stmt.executeUpdate(
                "DELETE FROM ispit " + 
                "WHERE indeks = (SELECT min(indeks) FROM dosije)");

        System.out.println("Broj obrisanih redova: " + brojObrisanih);

        stmt.close();

        return indeks;
    }

}
