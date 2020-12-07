package zadatak_11_3;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "DOSIJE")
public class Student {
    // Primarni kljuc

    @Id
    private Integer indeks;

    // Kolone od znacaja

    @Column(name = "IME", nullable = false)
    private String ime;

    @Column(name = "PREZIME", nullable = false)
    private String prezime;

    @Column(name = "MESTO_RODJENJA")
    private String mesto;


    // Kreiramo dvosmernu asocijativnu vezu izmedju klasa Smer i Student.
    // Posto tabela Dosije sadrzi strani kljuc id_smera koji referise na Smer
    // potrebno je da se u klasi Smer definise vrednost za opciju mappedBy.
    // Dodatno, zbog stranog kljuca moramo dodati anotaciju @JoinColumn kako
    // bismo ogranicili koriscenje ove reference na citanje.
    @ManyToOne
    @JoinColumn(name="ID_SMERA", referencedColumnName="ID_SMERA", insertable=false, updatable=false)
    private Smer smer;

    // Da bismo izracunali prosek polozenih predmeta za studenta,
    // potrebno nam je da dohvatimo informacije o njegovim ispitima.
    // Zbog toga definisemo listu ispita,
    // i dekorisemo je anotacijom veze izmedju tabela "dosije" i "ispit".
    // S obzirom da tabela "ispit" treba da odrzava strani kljuc,
    // onda koristimo mappedBy da bismo specifikovali
    // da je ta tabela odgovorna za bidirekcionu vezu.
    @OneToMany(mappedBy = "student")
    private List<Ispit> ispiti = new ArrayList<>();

    // Get/set metodi

    public Integer getIndeks() {
        return indeks;
    }

    public void setIndeks(Integer indeks) {
        this.indeks = indeks;
    }

    public String getIme() {
        return ime;
    }

    public void setIme(String ime) {
        this.ime = ime;
    }

    public String getPrezime() {
        return prezime;
    }

    public void setPrezime(String prezime) {
        this.prezime = prezime;
    }

    public Smer getSmer() {
		return smer;
	}

	public void setSmer(Smer smer) {
		this.smer = smer;
	}

	public List<Ispit> getIspiti() {
		return ispiti;
	}

	public void setIspiti(List<Ispit> ispiti) {
		this.ispiti = ispiti;
	}

	// Metod koji racuna prosek studenta
    public double prosek() {
        double ukupno = 0;
        int broj_polozenih = 0;
        // Pozivom getIspiti() vrsi se citanje podataka o ispitima 
        // iz baze i njihovo smestanje u listu.
        List<Ispit> ispiti = this.getIspiti();
        for (Ispit ispit : ispiti) {
            // Izdvajamo informacije samo o polozenim ispitima
            if (ispit.getStatus().equalsIgnoreCase("o") && ispit.getOcena() > 5) {
                ukupno += ispit.getOcena();
                broj_polozenih++;
            }
        }
        if (broj_polozenih == 0)
            return 0;
        return ukupno / broj_polozenih;
    }

}
