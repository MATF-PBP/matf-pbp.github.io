package zadatak_11_3;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "DA.ISPIT")
public class Ispit {
    // Primarni kljuc
    // U ovom slucaju cemo koristiti drugi pristup kreiranju primarnog kljuca,
    // tj. koriscenjem @EmbeddedId anotacije.
    // Pogledati skriptu za vise detalja.
    @EmbeddedId
    private IspitId idIspita;

    // Ostale kolone

    @Column
    private Integer ocena;

    @Column
    private String status;

    // Resavanje asocijativnih veza izmedju klasa

    // Kreiranje veze izmedju Ispit i Student
    // Posto tabela Ispit sadrzi vise od jednog stranog kljuca
    // moramo da navedemo anotaciju @JoinColumn i da definisemo
    // vrednosti za sve opcije ili kombinaciju @MapsId i @JoinColumn
    // s tim da onda mozemo izostaviti opcije insertable i updatable
    @ManyToOne
    @MapsId("indeks")
    @JoinColumn(name="INDEKS", referencedColumnName="INDEKS")
    private Student student;

    // Problem je sto se u primarnom kljucu javlja "potkljuc" IspitniRokId,
    // koji ima kolone "godina" i "oznaka".
    // A u primarnom kljucu tabele "ispit",
    // te kolone se nazivaju "godina_roka" i "oznaka_roka", redom.
    // Tako da je potrebno to resiti.

    // IspitniRok se spaja sa Ispit preko IspitniRokId,
    // koji je u IspitId stavljen kao polje "id_roka"
    // Zato koristimo @MapsId anotaciju, koja prihvata naziv polja
    // u @EmbeddedId klasi IspitniRokId
    @MapsId("idRoka")
    // Sada specifikujemo kako se tacno vrsi "spajanje",
    // tj. kako se formira ogranicenje stranog kljuca.
    // Za to koristimo JoinColums, posto se spajanje vrsi po dve kolone,
    // odnosno koriscenjem dva polja iz klase
    @JoinColumns({ @JoinColumn(name = "skgodina", referencedColumnName = "skgodina"),
            @JoinColumn(name = "oznakaroka", referencedColumnName = "oznakaroka") })
    // Na kraju, specifikujemo tip veze
    @ManyToOne
    private IspitniRok ispitniRok;

    // Get/set metodi

    public IspitId getIdIspita() {
        return idIspita;
    }

    public void setId_ispita(IspitId idIspita) {
        this.idIspita = idIspita;
    }

    public Integer getOcena() {
        return ocena;
    }

    public void setOcena(Integer ocena) {
        this.ocena = ocena;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

}
