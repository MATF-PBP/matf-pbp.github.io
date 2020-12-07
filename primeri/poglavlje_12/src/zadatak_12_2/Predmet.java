package zadatak_12_2;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.*;

@Entity
@Table (name = "predmet")
public class Predmet {
	@Id
	@Column
	private Integer id_predmeta;
	
	@Column
	private String naziv;
	
	@OneToMany(mappedBy="predmet")
	private List<Ispit> ispiti = new ArrayList<Ispit>();
	
	public Integer getId_predmeta() {
		return id_predmeta;
	}

	public void setId_predmeta(Integer id_predmeta) {
		this.id_predmeta = id_predmeta;
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
