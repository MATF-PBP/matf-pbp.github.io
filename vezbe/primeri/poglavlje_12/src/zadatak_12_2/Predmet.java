package zadatak_12_2;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table (name = "DA.PREDMET")
public class Predmet {
    @Id
    @Column
    private Integer id;
    
    @Column
    private String naziv;
    
    @OneToMany(mappedBy="predmet")
    private List<Ispit> ispiti = new ArrayList<Ispit>();
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNaziv() {
        return naziv;
    }

    public void setNaziv(String naziv) {
        this.naziv = naziv;
    }

    public List<Ispit> getIspiti() {
        return ispiti;
    }

    public void setIspiti(List<Ispit> ispiti) {
        this.ispiti = ispiti;
    }
}
