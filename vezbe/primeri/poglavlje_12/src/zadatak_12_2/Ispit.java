package zadatak_12_2;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.ManyToOne;
import javax.persistence.MapsId;
import javax.persistence.Table;

@Entity
@Table(name = "DA.ISPIT")
public class Ispit {
    // Primarni kljuc
    @EmbeddedId
    private IspitId idIspita;

    // Ostale kolone

    @Column
    private Integer ocena;

    @Column
    private String status;
    
    @ManyToOne
    @MapsId("indeks")
    @JoinColumn(name="INDEKS", referencedColumnName="INDEKS")
    private Student student;

    @MapsId("idRoka")
    @JoinColumns({ @JoinColumn(name = "skgodina", referencedColumnName = "skgodina"),
            @JoinColumn(name = "oznakaroka", referencedColumnName = "oznakaroka") })
    @ManyToOne
    private IspitniRok ispitniRok;
    
    @ManyToOne
    @JoinColumn(name="idPredmeta",referencedColumnName="id", insertable=false, updatable=false)
    private Predmet predmet;

    // Get/set metodi

    public IspitId getIdIspita() {
        return idIspita;
    }

    public void setIdIspita(IspitId idIspita) {
        this.idIspita = idIspita;
    }

    public Integer getOcena() {
        return ocena;
    }

    public void setOcena(Integer ocena) {
        this.ocena = ocena;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Student getStudent() {
        return student;
    }

    public void setStudent(Student student) {
        this.student = student;
    }

    public Predmet getPredmet() {
        return predmet;
    }

    public void setPredmet(Predmet predmet) {
        this.predmet = predmet;
    }

}
