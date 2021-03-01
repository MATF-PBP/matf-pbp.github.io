package zadatak_11_3;

import java.io.Serializable;
import java.util.Objects;

import javax.persistence.Embeddable;

@Embeddable
public class IspitniRokId implements Serializable {

    private static final long serialVersionUID = 1L;

    private Integer skGodina;
    private String oznakaRoka;

    public IspitniRokId() {
    }

    public IspitniRokId(Integer godina, String oznaka) {
        this.skGodina = godina;
        this.oznakaRoka = oznaka;
    }

    public Integer getSkGodina() {
        return skGodina;
    }

    public void setSkGodina(Integer godina) {
        this.skGodina = godina;
    }

    public String getOznakaRoka() {
        return oznakaRoka;
    }

    public void setOznakaRoka(String oznaka) {
        this.oznakaRoka = oznaka;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        if (!(o instanceof IspitniRokId)) {
            return false;
        }

        IspitniRokId irOther = (IspitniRokId) o;

        return Objects.equals(this.skGodina, irOther.getSkGodina()) && Objects.equals(this.oznakaRoka, irOther.getOznakaRoka());
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.skGodina, this.oznakaRoka);
    }
    
    @Override
    public String toString() {
        return "Skolska godina= " + skGodina + ", Oznaka roka:" + oznakaRoka;
    }
}
