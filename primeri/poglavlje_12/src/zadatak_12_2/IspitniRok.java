package zadatak_12_2;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "ispitni_rok")
public class IspitniRok {
    // Primarni kljuc

    @Id
    private IspitniRokId id = null;

    // Ostale kolone

    @Column(name = "naziv", nullable = false)
    private String Naziv;

    @Column(name = "pocetak_prijavljivanja", nullable = false)
    private String Pocetak;

    @Column(name = "kraj_prijavljivanja", nullable = false)
    private String Kraj;

    @Column(name = "tip", nullable = false)
    private String Tip = "B";

    // Kreiranje veze izmedju IspitniRok i Ispit.
    // S obzirom da je veza izmedju ovih klasa dvosmerna,
    // onda samo jedna klasa moze biti odgovorna za vezu,
    // sto se postize navodjenjem opcije mappedBy
    @OneToMany(mappedBy = "ispitniRok")
    List<Ispit> ispiti = new ArrayList<>();

    // Autogenerisani Get/Set metodi

    public IspitniRokId getId() {
        return id;
    }

    public void setId(IspitniRokId id) {
        this.id = id;
    }

    public String getNaziv() {
        return Naziv;
    }

    public void setNaziv(String naziv) {
        Naziv = naziv;
    }

    public String getPocetak() {
        return Pocetak;
    }

    public void setPocetak(String pocetak) {
        Pocetak = pocetak;
    }

    public String getKraj() {
        return Kraj;
    }

    public void setKraj(String kraj) {
        Kraj = kraj;
    }

    public String getTip() {
        return Tip;
    }

    public void setTip(String tip) {
        if (tip == null) {
            Tip = "B";
            return;
        }
        Tip = tip;
    }

    public List<Ispit> getIspiti() {
        return ispiti;
    }

    public void setIspiti(List<Ispit> ispiti) {
        this.ispiti = ispiti;
    }

}
