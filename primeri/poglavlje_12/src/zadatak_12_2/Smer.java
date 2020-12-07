package zadatak_12_2;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "SMER")
public class Smer {
    @Id
    private int id_smera;

    @Column(name = "oznaka", nullable = false)
    private String Oznaka;

    @Column(name = "naziv", nullable = false)
    private String Naziv;

    @Column(name = "semestara", nullable = false)
    private Integer Semestara;

    @Column(name = "bodovi", nullable = false)
    private Integer Bodovi;

    @Column(name = "id_nivoa", nullable = false)
    private Integer Nivo;

    @Column(name = "zvanje", nullable = false)
    private String Zvanje;

    @Column(name = "opis", nullable = true)
    private String Opis;

    // Kreiramo asocijativnu vezu izmedju klasa Smer i Student.
    // Primetimo da u klasi Student nemamo referencu na klasu Smer,
    // sto ovu vezu cini jednosmernom.
    @OneToMany
    @JoinColumn(name = "id_smera")
    private List<Student> studentiNaSmeru = new ArrayList<>();

    public int getId_smera() {
        return id_smera;
    }

    public void setId_smera(int id_smera) {
        this.id_smera = id_smera;
    }

    public String getOznaka() {
        return Oznaka;
    }

    public void setOznaka(String oznaka) {
        Oznaka = oznaka;
    }

    public String getNaziv() {
        return Naziv;
    }

    public void setNaziv(String naziv) {
        Naziv = naziv;
    }

    public Integer getSemestara() {
        return Semestara;
    }

    public void setSemestara(Integer semestara) {
        Semestara = semestara;
    }

    public Integer getBodovi() {
        return Bodovi;
    }

    public void setBodovi(Integer bodovi) {
        Bodovi = bodovi;
    }

    public Integer getNivo() {
        return Nivo;
    }

    public void setNivo(Integer nivo) {
        Nivo = nivo;
    }

    public String getZvanje() {
        return Zvanje;
    }

    public void setZvanje(String zvanje) {
        Zvanje = zvanje;
    }

    public String getOpis() {
        return Opis;
    }

    public void setOpis(String opis) {
        Opis = opis;
    }

    public List<Student> getStudentiNaSmeru() {
        return studentiNaSmeru;
    }

    public void setStudentiNaSmeru(List<Student> studentiNaSmeru) {
        this.studentiNaSmeru = studentiNaSmeru;
    }

}
