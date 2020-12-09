package zadatak_10_2;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

// Tabela ISPITNI_ROK ima primarni kljuc koji se sastoji od dve kolone. 
// Ovakav primarni kljuc se naziva slozeni kljuc.
// Za slozeni kljuc je potrebno da se kreira posebna klasa 
// koja mora da implementira interfejs java.io.Serializable. 
// Stoga moraju biti definisana i naredna dva metoda: equals() i hashCode(). 
// Takodje, neophodno je da ima definisan i podrazumevani konstruktor.

// S obzirom da se ova klasa koristi kao primarni kljuc za drugu klasu,
// onda je ne anotiramo pomocu @Entity,
// vec koristimo anotaciju @Embeddable
@Embeddable
class IspitniRokId implements Serializable {

    // Podrazumevani serijski ID verzije
    private static final long serialVersionUID = 1L;

    // Kolone koje ulaze u primarni kljuc
    private Integer godina;
    private String oznaka;

    // Podrazumevani konstruktor
    public IspitniRokId() {
    }

    public IspitniRokId(Integer godina, String oznaka) {
        this.godina = godina;
        this.oznaka = oznaka;
    }

    // Autogenerisani get/set metodi
    public Integer getGodina() {
        return godina;
    }

    public void setGodina(Integer godina) {
        this.godina = godina;
    }

    public String getOznaka() {
        return oznaka;
    }

    public void setOznaka(String oznaka) {
        this.oznaka = oznaka;
    }

    // Prevazilazenje metoda radi testiranja kolizije primarnih kljuceva

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IspitniRokId)) {
            return false;
        }

        IspitniRokId irOther = (IspitniRokId) o;

        return Objects.equals(this.godina, irOther.getGodina()) && Objects.equals(this.oznaka, irOther.getOznaka());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.godina, this.oznaka);
    }
}
