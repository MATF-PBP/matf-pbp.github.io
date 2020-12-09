package zadatak_11_3;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;

// Klasa predstavlja slozeni kljuc za Ispit.
// Primetimo da ovaj put nismo anotirali klasu anotacijom @Embeddable,
// kao u prvom pristupu kreiranja slozenih kljuceva.
public class IspitId implements Serializable {

    private static final long serialVersionUID = 1L;

    // Slozeni kljuc tabele "ispit" sadrzi primarni kljuc tabele "ispitni_rok",
    // kao i kolone "indeks" i "id_predmeta"

    private IspitniRokId id_roka;

    @Column(name = "INDEKS")
    private Integer indeks;

    @Column(name = "ID_PREDMETA")
    private Integer id_predmeta;

    // Podrazumevani konstruktor za Serializable
    public IspitId() {
    }

    public IspitId(IspitniRokId id_roka, Integer indeks, Integer id_predmeta) {
        this.id_roka = id_roka;
        this.indeks = indeks;
        this.id_predmeta = id_predmeta;
    }

    // Get/set metodi

    public IspitniRokId getId_roka() {
        return id_roka;
    }

    public void setId_roka(IspitniRokId id_roka) {
        this.id_roka = id_roka;
    }

    public Integer getIndeks() {
        return indeks;
    }

    public void setIndeks(Integer indeks) {
        this.indeks = indeks;
    }

    public Integer getId_predmeta() {
        return id_predmeta;
    }

    public void setId_predmeta(Integer id_predmeta) {
        this.id_predmeta = id_predmeta;
    }

    // Prevazilazenje metoda za odredjivanje jednakosti kljuceva

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IspitId)) {
            return false;
        }

        IspitId other = (IspitId) o;

        return Objects.equals(this.getId_roka(), other.getId_roka())
                && Objects.equals(this.getIndeks(), other.getIndeks())
                && Objects.equals(this.getId_predmeta(), other.getId_predmeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getId_roka(), this.getIndeks(), this.getId_predmeta());
    }
}
