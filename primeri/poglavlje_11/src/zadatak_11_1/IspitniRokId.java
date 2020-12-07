package zadatak_11_1;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

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

	@Override
	public String toString() {
		return "IspitniRokId [godina=" + godina + ", oznaka=" + oznaka + "]";
	}
    
    
}
