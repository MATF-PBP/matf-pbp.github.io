package zadatak_12_1;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "ispit")
public class Ispit {
    // Primarni kljuc
    // U ovom slucaju cemo koristiti drugi pristup kreiranju primarnog kljuca,
    // tj. koriscenjem @EmbeddedId anotacije.
    // Pogledati skriptu za vise detalja.
    @EmbeddedId
    private IspitId id_ispita;

    // Ostale kolone

    @Column
    private Integer ocena;

    @Column(name = "status_prijave")
    private String status;

    // Resavanje asocijativnih veza izmedju klasa

    // Kreiranje veze izmedju Ispit i Student
    @ManyToOne
    @MapsId("indeks")
    @JoinColumn(name="indeks", referencedColumnName="indeks")
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
    @MapsId("id_roka")
    // Sada specifikujemo kako se tacno vrsi "spajanje",
    // tj. kako se formira ogranicenje stranog kljuca.
    // Za to koristimo JoinColums, posto se spajanje vrsi po dve kolone,
    // odnosno koriscenjem dva polja iz klase
    @JoinColumns({ @JoinColumn(name = "godina_roka", referencedColumnName = "godina"),
            @JoinColumn(name = "oznaka_roka", referencedColumnName = "oznaka") })
    // Na kraju, specifikujemo tip veze
    @ManyToOne
    private IspitniRok ispitniRok;

    // Get/set metodi

    public IspitId getId_ispita() {
        return id_ispita;
    }

    public void setId_ispita(IspitId id_ispita) {
        this.id_ispita = id_ispita;
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
