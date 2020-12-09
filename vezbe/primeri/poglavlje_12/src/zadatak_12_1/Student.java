package zadatak_12_1;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "dosije")
public class Student {
    // Primarni kljuc

    @Id
    @Column
    private Integer indeks;

    // Kolone od znacaja

    @Column(name = "id_smera", nullable = false)
    private Integer idSmera;

    @Column(name = "ime", nullable = false)
    private String ime;

    @Column(name = "prezime", nullable = false)
    private String prezime;
    
    @Column(name = "mesto_stanovanja", nullable = false)
    private String mestoStanovanja;

    // Da bismo izracunali prosek polozenih predmeta za studenta,
    // potrebno nam je da dohvatimo informacije o njegovim ispitima.
    // Zbog toga definisemo listu ispita,
    // i dekorisemo je anotacijom veze izmedju tabela "dosije" i "ispit".
    // S obzirom da tabela "ispit" treba da odrzava strani kljuc,
    // onda koristimo mappedBy da bismo specifikovali
    // da je ta tabela odgovorna za bidirekcionu vezu.
    @OneToMany(mappedBy = "student")
    private List<Ispit> ispiti = new ArrayList<>();

    // Get/set metodi

    public Integer getIndeks() {
        return indeks;
    }

    public void setIndeks(Integer indeks) {
        this.indeks = indeks;
    }

    public Integer getId_smera() {
        return idSmera;
    }

    public void setId_smera(Integer id_smera) {
        this.idSmera = id_smera;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public List<Ispit> getIspiti() {
        return ispiti;
    }

    public void setIspiti(List<Ispit> ispiti) {
        this.ispiti = ispiti;
    }

    public String getMestoStanovanja() {
        return mestoStanovanja;
    }

    public void setMestoStanovanja(String mestoStanovanja) {
        this.mestoStanovanja = mestoStanovanja;
    }

    // Metod koji racuna prosek studenta
    public double prosek() {
        double ukupno = 0;
        int broj_polozenih = 0;
        List<Ispit> ispiti = this.getIspiti();
        for (Ispit ispit : ispiti) {
            // Izdvajamo informacije samo o polozenim ispitima
            if (ispit.getStatus().equalsIgnoreCase("o") && ispit.getOcena() > 5) {
                ukupno += ispit.getOcena();
                broj_polozenih++;
            }
        }
        if (broj_polozenih == 0)
            return 0;
        return ukupno / broj_polozenih;
    }

}
