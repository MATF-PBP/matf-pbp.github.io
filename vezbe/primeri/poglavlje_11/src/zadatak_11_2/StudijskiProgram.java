package zadatak_11_2;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "DA.STUDIJSKIPROGRAM")
class StudijskiProgram {
    @Id
    private int id;

    @Column(name = "OZNAKA", nullable = false)
    private String Oznaka;

    @Column(name = "NAZIV", nullable = false)
    private String Naziv;

    @Column(name = "OBIMESPB", nullable = false)
    private Integer Espb;

    @Column(name = "IDNIVOA", nullable = false)
    private Integer Nivo;

    @Column(name = "ZVANJE", nullable = false)
    private String Zvanje;

    @Column(name = "OPIS", nullable = true)
    private String Opis;
    
    // Kreiramo dvosmernu asocijativnu vezu izmedju klasa StudijskiProgram i Student.
    // Posto tabela Dosije sadrzi strani kljuc id_smera koji referise na StudijskiProgram
    // potrebno je da se u klasi StudijskiProgram postavljamo opciju mappedBy na naziv polja
    // tipa StudijskiProgram u klasi Student.
    @OneToMany(mappedBy="studijskiProgram")
    private List<Student> studenti;


    public StudijskiProgram() {
    }
    
    public StudijskiProgram(int id, String oznaka, String naziv, Integer espb, Integer nivo,
            String zvanje, String opis) {
        this.id = id;
        Oznaka = oznaka;
        Naziv = naziv;
        Espb = espb;
        Nivo = nivo;
        Zvanje = zvanje;
        Opis = opis;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    public Integer getEspb() {
        return Espb;
    }

    public void setEspb(Integer espb) {
        Espb = espb;
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

    public List<Student> getStudenti() {
        return studenti;
    }

    public void setStudenti(List<Student> studenti) {
        this.studenti = studenti;
    }

    @Override
    public String toString() {
        return "Studijski program [id=" + id + ", Oznaka=" + Oznaka + ", Naziv=" + Naziv
                + ", Espb=" + Espb + ", Nivo=" + Nivo + ", Zvanje=" + Zvanje + ", Opis=" + Opis + "]";
    }
}
