package zadatak_9_6;

public class Student {
	private int indeks;
	private String ime;
	private String prezime;
	
	public Student(int indeks, String ime, String prezime) {
		super();
		this.indeks = indeks;
		this.ime = ime;
		this.prezime = prezime;
	}

	public int getIndeks() {
		return indeks;
	}

	public void setIndeks(int indeks) {
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
	
	@Override
	public String toString() {
		return indeks + " " + ime + " " + prezime;
	}
}
