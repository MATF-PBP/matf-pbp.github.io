package zadatak_10_2;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
// Anotacija @Table je neophodna jer se ime klase i ime tabele razlikuju.
@Table(name = "DA.ISPITNIROK")
class IspitniRok {
    // Za primarni kljuc koristimo instancu klase IspitniRokId,
    // s obzirom da ova tabela ima slozeni kljuc.
    // Pogledati klasu IspitniRokId za jos informacija.

    @Id
    private IspitniRokId id = null;

    // Ostale kolone

    @Column(name = "NAZIV", nullable = false)
    private String Naziv;

    @Column(name = "DATPOCETKA", nullable = false)
    private String Pocetak;

    @Column(name = "DATKRAJA", nullable = false)
    private String Kraj;

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

}
