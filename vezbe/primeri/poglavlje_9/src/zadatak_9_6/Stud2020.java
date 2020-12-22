package zadatak_9_6;

import java.io.IOException;
import java.sql.*;

public class Stud2020 extends Database {

    public Stud2020() throws SQLException {
        dbName = "STUD2020";
        url = "jdbc:db2://localhost:50000/stud2020";
        username = "student";
        password = "abcdef";
        connect();
    }

    public void izlistajStudentePoStudijskimProgramima() throws SQLException, IOException {
        String sql = readSQLFromFile(System.getProperty("user.dir") + "/bin/zadatak_9_6/studijskiProgrami.sql");
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(sql);

        while (rs.next()) {
        	StudijskiProgram sp = new StudijskiProgram(rs.getInt(1), rs.getString(2), rs.getShort(3), rs.getString(4));
        	
            System.out.println("\n" + sp);
            
            izlistajStudenteStudijskogPrograma(sp.getId());
        }

        rs.close();
        stmt.close();
    }

    public void izlistajStudenteStudijskogPrograma(int idPrograma) throws SQLException, IOException {
        String sql = readSQLFromFile(System.getProperty("user.dir") + "/bin/zadatak_9_6/studentiStudijskogPrograma.sql");
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setInt(1, idPrograma);
        ResultSet rs = stmt.executeQuery();
        
        while(rs.next()) {
        	Student student = new Student(rs.getInt(1), rs.getString(2), rs.getString(3));
        	System.out.println("\t" + student);
        }

        rs.close();
        stmt.close();
    }
}
