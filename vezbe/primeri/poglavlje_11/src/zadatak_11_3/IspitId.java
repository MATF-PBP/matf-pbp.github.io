package zadatak_11_3;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Column;
import javax.persistence.Embeddable;

// Klasa predstavlja slozeni kljuc za Ispit.
// Anotiralmo je anotacijom @Embeddable
@Embeddable
public class IspitId implements Serializable {

    private static final long serialVersionUID = 1L;

    // Slozeni kljuc tabele "ispit" sadrzi primarni kljuc tabele "ispitni_rok",
    // kao i kolone "indeks" i "id_predmeta"

    private IspitniRokId idRoka;

    @Column(name = "INDEKS")
    private Integer indeks;

    @Column(name = "IDPREDMETA")
    private Integer idPredmeta;

    // Podrazumevani konstruktor za Serializable
    public IspitId() {
    }

    public IspitId(IspitniRokId idRoka, Integer indeks, Integer idPredmeta) {
        this.idRoka = idRoka;
        this.indeks = indeks;
        this.idPredmeta = idPredmeta;
    }

    // Get/set metodi

    public IspitniRokId getIdRoka() {
        return idRoka;
    }

    public void setId_roka(IspitniRokId idRoka) {
        this.idRoka = idRoka;
    }

    public Integer getIndeks() {
        return indeks;
    }

    public void setIndeks(Integer indeks) {
        this.indeks = indeks;
    }

    public Integer getIdPredmeta() {
        return idPredmeta;
    }

    public void setId_predmeta(Integer idPredmeta) {
        this.idPredmeta = idPredmeta;
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

        return Objects.equals(this.getIdRoka(), other.getIdRoka())
                && Objects.equals(this.getIndeks(), other.getIndeks())
                && Objects.equals(this.getIdPredmeta(), other.getIdPredmeta());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.getIdRoka(), this.getIndeks(), this.getIdPredmeta());
    }
}
