package zadatak_9_6;

public class StudijskiProgram {
	private int id;
	private String naziv;
	private short obimespb;
	private  String zvanje;
	
	public StudijskiProgram(int id, String naziv, short obimespb, String zvanje) {
		super();
		this.id = id;
		this.naziv = naziv;
		this.obimespb = obimespb;
		this.zvanje = zvanje;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getNaziv() {
		return naziv;
	}

	public void setNaziv(String naziv) {
		this.naziv = naziv;
	}

	public short getObimespb() {
		return obimespb;
	}

	public void setObimespb(short obimespb) {
		this.obimespb = obimespb;
	}

	public String getZvanje() {
		return zvanje;
	}

	public void setZvanje(String zvanje) {
		this.zvanje = zvanje;
	}
	
	@Override
	public String toString() {
		return "Naziv: " + naziv + ", obim: " + obimespb + ", zvanje: " + zvanje;
	}
	
}
