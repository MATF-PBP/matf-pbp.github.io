package zadatak_11_2;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name = "ISPITNI_ROK")
class IspitniRok {
    // Primarni kljuc

    @Id
    private IspitniRokId id = null;

    // Ostale kolone

    @Column(name = "NAZIV", nullable = false)
    private String Naziv;

    @Column(name = "POCETAK_PRIJAVLJIVANJA", nullable = false)
    private String Pocetak;

    @Column(name = "KRAJ_PRIJAVLJIVANJA", nullable = false)
    private String Kraj;

    @Column(name = "TIP", nullable = false)
    private String Tip = "B";

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

	@Override
	public String toString() {
		return "IspitniRok [id=" + id + ", Naziv=" + Naziv + ", Pocetak=" + Pocetak + ", Kraj=" + Kraj + ", Tip=" + Tip
				+ "]";
	}
}
