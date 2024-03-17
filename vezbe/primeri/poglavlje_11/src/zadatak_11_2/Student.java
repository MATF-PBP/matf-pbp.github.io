package zadatak_11_2;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "DA.DOSIJE")
public class Student {
    // Primarni kljuc
    @Id
    private Integer indeks;

    // Kolone od znacaja

    @Column(name = "IME", nullable = false)
    private String ime;

    @Column(name = "PREZIME", nullable = false)
    private String prezime;

    @Column(name = "MESTORODJENJA")
    private String mesto;


    // Kreiramo dvosmernu asocijativnu vezu izmedju klasa StudijskiProgram i Student.
    // Posto tabela Dosije sadrzi strani kljuc idprograma koji referise na StudijskiProgram
    // potrebno je da se u klasi StudijskiProgram definise vrednost za opciju mappedBy.
    // Dodatno, zbog stranog kljuca moramo dodati anotaciju @JoinColumn kako
    // bismo ogranicili koriscenje ove reference na citanje.
    @ManyToOne
    @JoinColumn(name="IDPROGRAMA", referencedColumnName="ID", insertable=false, updatable=false)
    private StudijskiProgram studijskiProgram;

    // Get/set metodi

    public Integer getIndeks() {
        return indeks;
    }

    public void setIndeks(Integer indeks) {
        this.indeks = indeks;
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

    public StudijskiProgram getStudijskiProgram() {
        return studijskiProgram;
    }

    public void setStudijskiProgram(StudijskiProgram studijskiProgram) {
        this.studijskiProgram = studijskiProgram;
    }
}
