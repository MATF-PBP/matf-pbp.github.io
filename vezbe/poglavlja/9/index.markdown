---
layout: page
title: 9. Napredne tehnike razvijanja Java/SQL aplikacija
under_construction: true
---

U ovom poglavlju ćemo demonstrirati rad sa transakcijama, rad u konkurentnom okruženju,
kao i povezivanje na više baza podataka u JDBC aplikacijama.

## 9.1 Transakcioni rad

JDBC podr\v zava naredne koncepte za upravljanje transakcijama:

- Re\v zim automatskog potvr\dj ivanja izmena
- Pode\v savanje nivoa izolovanosti transakcija
- Ta\v cke \v cuvanja

### 9.1.1 Re\v zim automatskog potvr\dj ivanja izmena

Prilikom povezivanja na bazu podataka, JDBC drajver podrazumevano postavlja svojstvo
objekta interfejsa `Connection` automatskog potvr\dj ivanja izmena na `true`. Ako je
konekcija pode\v sena u re\v zimu automatskog potvr\dj ivanja izmena, onda \'ce svaka
SQL naredba koja se izvr\v si nad bazom podataka biti u isto vreme i potvr\dj ena (u
slu\v caju uspe\v snog izvr\v senja te naredbe) ili poni\v stena (u slu\v caju 
neuspe\v snog izvr\v senja te naredbe).

Ovo pona\v sanje nam \v cesto nije po\v zeljno, s obzirom da je neophodno da sami
defini\v semo koje sve SQL naredbe predstavljaju deo transakcija u aplikacijama. Zbog
toga, potrebno je isklju\v citi re\v zim automatskog potvr\dj ivanja izmena. Ovo se
izvr\v sava pozivom metoda `Connection.setAutoCommit(boolean autoCommit)` i 
prosle\d jivanjem vrednosti `false`, nakon \v sto se konekcija ka bazi podataka uspe\v sno
uspostavi.

```java
// Dohvatanje konekcije
Connection con = DriverManager.getConnection(dbURL, userId, password);

// Isklju\v civanje re\v zima automatskog potvr\dj ivanja izmena
con.setAutoCommit(false);
```

Ako se metod `setAutoCommit` poziva da bi se promenio re\v zim automatskog potvr\dj ivanja 
izmena u toku izvr\v savanja neke transakcije, ta transakcija \'ce biti potvr\dj ena u tom
trenutku. Zbog toga \v sto ovakvo pona\v sanje mo\v ze dovesti do neo\v cekivanih pona\v sanja
u odnosu na poslovnu logiku aplikacije, obi\v cno se re\v zim automatskog potvr\dj ivanja 
izmena postavlja odmah nakon povezivanja na bazu podataka, kao u primeru koda iznad.

#### Potvr\dj ivanje i poni\v stavanje izmena u transakcijama

Jednog kada je re\v zim automatskog potvr\dj ivanja izmena isklju\v cen, na raspolaganju
su nam metodi `Connection.commit()` i `Connection.rollback()` kojima se upravlja du\v zina
trajanja i uspe\v snost transakcija.

Uobi\v cajeni kod u JDBC aplikacijama koji izvr\v sava transakciju nad bazom podataka je
dat u nastavku:

```java
try (
    // Get a connection
    Connection con = DriverManager.getConnection(dbURL, userId, password);
) {
    // Set the auto-commit off
    con.setAutoCommit(false);

    // Perform database transaction activities here
    
    // Successful scenario:
    con.commit();
}
catch (SQLException e) {
    System.out.println("An error occured: " + e.getMessage());
    System.out.println("Rolling back the transaction");

    try {
        // Unsuccessful scenario:
        con.rollback();
    }
    catch (SQLException e) {}

    System.exit(2);
}
catch (Exception e) {
    // ...
}
```

{% include lab/exercise.html broj="9.1" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji redom:

1. Pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.
2. Briše studenta sa pronađenim indeksom iz tabele `ISPIT` i ispisuje poruku korisniku o uspešnosti brisanja.
3. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.
4. Pita korisnika da li želi da potvrdi ili poništi izmene. U zavisnosti od korisnikovog odgovora, aplikacija potvrđuje ili poništava izmene uz ispisivanje poruke korisniku.
5. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`." %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_1/Main.java, java)

### 9.1.2 Nivoi izolacije transakcija

ANSI SQL-92 standard defini\v se \v cetiri nivoa izolacije transakcija u terminima
konzistentnosti podataka. Svaki nivo izolacije defini\v se koje vrste nekonzistentnosti
podataka jesu ili nisu dozvoljene. Ovi nivoi su:

- Read uncommitted
- Read committed
- Repeatable read
- Serializable

JDBC standard defini\v se naredne \v cetiri stati\v cke konstante interfejsa `Connection` 
koji odgovaraju nivoima izolacije ANSI SQL-92 standarda, redom:

- `TRANSACTION_READ_UNCOMMITTED`
- `TRANSACTION_READ_COMMITTED`
- `TRANSACTION_REPEATABLE_READ`
- `TRANSACTION_SERIALIZABLE`

Postavljanje nivoa izolacije transakcija za bazu podataka se mo\v ze izvr\v siti pozivom metoda `Connection.setTransactionIsolation(int level)`.

```java
// Get a Connection object
Connection con = DriverManager.getConnection(dbURL, userId, password);

// Set the transaction isolation level to read committed
con.setTransactionIsolation(Connection.TRANSACTION_READ_COMMITTED);
```

{% include lab/exercise.html broj="9.2" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji iz tabele `UKUPNIBODOVI` (videti ispod) izdvaja 10 najuspe\v snijih studenata. Za svakog studenta ispisati podatke iz te tabele i upitati korisnika da li \v zeli da dodeli tom studentu po\v casnih 10 ESPB. Ukoliko \v zeli, izvr\v siti odgovaraju\'cu izmenu. Nakon svih izmena, ispisati izve\v staj rada u kojem se vide izmene. Sve izmene i prikaz izve\v staja implementirati kao jednu transakciju. Omogu\'citi da nijedan drugi korisnik ne mo\v ze da vidi izmene tokom rada ovog programa." %}

Re\v senje: Pre početka izvršavanja, izvr\v siti naredni skript nad bazom podataka `STUD2020` koji \'ce pripremiti podatke:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_2/priprema_baze.sql, sql)

S obzirom da \'cemo menjati odgovaraju\'cu tabelu, naredbu `stmt` koja se koristi za kreiranje kursora koji prolazi tabelom moramo kreirati opcijom `ResultSet.CONCUR_UPDATABLE`. Me\dj utim, da bismo spre\v cili sve ostale programe da vide izmene, potrebno je da postavimo najstro\v ziji nivo izolacije, tj. da pozovemo metod `setIsolationLevel()` sa argumentom `Connection.TRANSACTION_SERIALIZABLE`.

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_2/Main.java, java)

### 9.1.3 Ta\v cke \v cuvanja u transakciji

Kao \v sto znamo, transakcija u bazi podataka se sadr\v zi od jedne ili vi\v se izmena u 
okviru jedne jedinice posla. Poni\v stavanje transakcije podrazumevano poni\v stava sve
izmene definisane u toj jedinici posla.

Ta\v cka \v cuvanja predstavlja marker koji odre\dj uje ta\v cku u transakciji do koje se
mogu poni\v stiti izmene. Drugim re\v cima, poni\v stavanje transakcije do neke ta\v cke
\v cuvanja \'ce poni\v stiti samo one izmene koje su nastale nakon te ta\v cke \v cuvanja
(a one koje su nastale pre ta\v cke \v cuvanja \'ce ostati neponi\v stene).

U JDBC aplikacijama, ta\v cke \v cuvanja se reprezentuju objektima interfejsa `Savepoint`.
Kako bi se markirala ta\v cka u teku\'coj transakciji, potrebno je pozvati metod
`Connection.setSavepoint()`. Ovaj metod kreira novu, neimenovanu ta\v cku \v cuvanja u
teku\'coj transakciji i vra\'ca objekat interfejsa `Statement` koji je reprezentuje.
Ovaj objekat se koristi za upravljanje ta\v ckom \v cuvanja nadalje. Naredni primer
ilustruje kreiranje nekoliko ta\v caka \v cuvanja u okviru transakcije:

```java
Connection con = DriverManager.getConnection(dbURL, userId, password);
con.setAutoCommit(false);

Statement stmt = con.createStatement();

stmt.executeUpdate("insert into person values ('John', 'Doe')");
Savepoint sp1 = con.setSavepoint(); // 1

stmt.executeUpdate("insert into person values ('Jane', 'Doe')");
Savepoint sp2 = con.setSavepoint(); // 2

stmt.executeUpdate("insert into person values ('Another', 'Unknown')");
Savepoint sp3 = con.setSavepoint(); // 3
```

Nakon kreiranja poslednje ta\v cke \v cuvanja, korisnik ima opciju da poni\v sti izmene
do bilo koje od ta\v caka \v cuvanja `spX`. Da bi se ova akcija ostvarila, potrebno je
pozvati metod `Connection.rollback(Savepoint savepoint)` \v ciji argument defini\v se
ta\v cku \v cuvanja do koje se vr\v si poni\v stavanje transakcije. Na primer, ako bismo
\v zeleli da poni\v stimo sve izmene nakon ta\v cke \v cuvanja 1, iskoristili bismo poziv:

```java
// Rolls back inserts 2 and 3
con.rollback(sp1);
```

Va\v zno je napomenuti da jednom kad smo poni\v stili izmene do neke ta\v cke \v cuvanja, 
(recimo, `sp1`), sve ta\v cke \v cuvanja koje su kreirane nakon ove ta\v cke \v cuvanja
(`sp2` i `sp3`) bi\'ce oslobo\dj ene i ne bi trebalo referisati na njih nadalje. Poku\v saj
da se oslobodi ve\'c oslobo\dj ena ta\v cka \v cuvanja rezultuje ispaljivanjem izuzetka 
`SQLException`. Na primer, naredni kod \'ce ispaliti izuzetak `SQLException` (pod 
pretpostavkom da prethodno nisu oslobo\dj ene ta\v cke \v cuvanja `sp1` i `sp2`):

```java
con.rollback(sp2); // Will release sp3
con.rollback(sp3); // Will throw an exception: sp3 is already released.
```

Primetimo da prilikom poni\v stavanja transakcije do neke ta\v cke \v cuvanja,
sama ta\v cka \v cuvanja do koje se izvr\v silo poni\v stavanje ne\'ce biti oslobo\dj ena.
Na primer, u primeru koda iznad, ta\v cka \v cuvanja `sp2` je ostala aktivna i korisnik
mo\v ze ponovo poni\v stiti izmene do nje. 

```java
con.rollback(sp2); // OK
con.rollback(sp2); // OK
```

{% include lab/exercise.html broj="9.3" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji pronalazi indekse i nazive predmeta za sva polaganja koja su bila neuspe\v sna. Sortirati podatke po indeksu rastu\'ce. Obezbediti da aplikacija bri\v se podatke o najvi\v se 10 studenata. Jednu transakciju \v cine brisanja za sve prona\dj ene studente. Prilikom obrade podataka, ispisati informacije o indeksu studenta, a zatim prikazati nazive predmeta za obrisana polaganja tog studenta. Nakon brisanja podataka o jednom studentu, upitati korisnika da li \v zeli da poni\v sti izmene za tog studenta (voditi ra\v cuna da brisanja za sve prethodne studente ostanu nepromenjena)." %}

Re\v senje: Ono \v sto je potrebno uraditi jeste, pre brisanja slogova za jednog studenta, postaviti ta\v cku \v cuvanja. Nakon \v sto se detektuje da se dohvatio slog za narednog studenta, aplikacija treba da pita korisnika da li \v zeli da poni\v sti izmene od postavljene ta\v cke \v cuvanja. Tako se obezbe\dj ujemo da samo obrisani redovi za teku\'ceg studenta budu poni\v steni, a ne i za sve prethodne.

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_3/Main.java, java)
include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_3/upit.sql, sql)

## 9.2 Rad u višekorisničkom okruženju

U poglavlju 5 smo govorili detaljno o problemima konkurentnog rada sa bazom podataka.
Aplikacije pisane u programskom jeziku Java, bilo one SQLJ ili JDBC, takođe "boluju" od
istih problema kao i aplikacije koje smo pisali u programskom jeziku C. Zbog toga ćemo
imati na umu sve napomene koje smo tada uveli, sa određenim napomenama koje slede u
daljem tekstu.

S obzirom da se u programskom jeziku Java greške prijavljuju kroz objekte klase `SQLException`, 
proveru da li je došlo do nekog problema konkurentnog okruženja možemo izvršiti proverom
koda greške, pozivom metoda `getErrorCode()` nad objektom klase `SQLException` koji
smo uhvatili. Zašto nam je ova informacija važna? Prisetimo se da, ukoliko dođe do nekog
problema konkurentnog okruženja, DB2 SUBP šalje grešku -911 ili -913. Dodatno, u slučaju da 
aplikacija zahteva ekskalaciju katanaca, ali menadžer baze podataka ne uspe da izvrši tu
operaciju, onda će aplikaciji biti prijavljena greška -912. U tom slučaju,
potrebno je izvršiti obradu isteka vremena ili pojave mrtve petlje, i poništiti eventualne
izmene. Evo jednog primera:

```java
// U main funkciji:

Connection con = null;
String sql = "SELECT ...";
Statement stmt = con.createStatement(
    ResultSet.TYPE_FORWARD_ONLY,
    ResultSet.CONCUR_UPDATABLE,
    ResultSet.HOLD_CURSORS_OVER_COMMIT
);
ResultSet kursor = otvoriKursor(stmt, sql);

// Petlja koja prolazi kroz kursor
while(true) {
    try {
        // Kod koji moze da dovede do problema
        // u visekorisnickom okruzenju
    }
    catch (SQLException e) {
        // Ako je doslo do izuzetka zbog katanaca...
        if (-913 <= e.getErrorCode() && e.getErrorCode() <= -911) {
            // ... onda ih je potrebno obraditi
            kursor.close();
            kursor = obradiCekanje("FETCH, UPDATE, ...", con, stmt, sql);
            continue;
        }
        // Inace, neka druga greska je u pitanju,
        // pa ju je potrebno proslediti kodu za obradu greske
        throw e;
    }
}

// *********************************************************
// Izvan main funkcije:
private static ResultSet otvoriKursor(Statement stmt, String sql) throws SQLException {
    ResultSet kursor = stmt.executeQuery(sql);
    return kursor;
}

private static ResultSet obradiCekanje(String codeHint, Connection con, Statement stmt, String sql) throws SQLException {
    System.out.printf("[%s] Objekat je zakljucan od strane druge transakcije!\n" +
        "Molimo sacekajte!\n", codeHint);

    try {
        con.rollback();
    } catch (SQLException e) {
    }

    return otvoriKursor(stmt, sql);
}
```

Naredni primeri ilustruju konstrukciju JDBC aplikacija koje koriste transakcioni rad u
višekorisničkom okruženju.

{% include lab/exercise.html broj="9.4" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji za svaki predmet koji je obavezan na studijskom programu čiji je identifikator 103, pita korisnika da li želi da poveća broj ESPB bodova za 1. Ukoliko je odgovor korisnika ”da”, izvršava se odgovarajuća naredba. Zadatak uraditi tako da aplikacija radi u višekorisničkom okruženju. Obrada jednog predmeta treba da predstavlja jednu transakciju. Postaviti istek vremena na 5 sekundi. Omogu\'citi da drugi korisnici mogu da pristupaju predmetima koje ovaj program trenutno obrađuje." %}

Re\v senje: Da bismo implementirali date zahteve, potrebno je da pamtimo koje smo predmete obra\dj ivali. Zato \'cemo kreirati promenljivu `obradjeniPredmeti` tipa `ArrayList<Integer>` koja \'ce \v cuvati informaciju o identifikatorima predmeta koje smo obradili. Zbog toga \v sto se primarni klju\v c tabele `PREDMET` sastoji samo od jedne kolone tipa `INTEGER`, to je \v sablonski parametar kolekcije `ArrayList` tipa `Integer`. U slu\v caju slo\v zenih primarnih klju\v ceva, neophodno je da kreiramo klasu koja sadr\v zi atribute koji odgovaraju kolonama slo\v zenog primarnog klju\v ca (videti [zadatke za ve\v zbu](#94-zadaci-za-ve\v zbu)). Dodatno, potrebno je da postavimo nivo izolovanosti stabilno \v citanje, kako bismo omogu\'cili da drugi programi mogu da \v citaju slogove koji se ne a\v zuriraju od strane na\v se aplikacije.

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_4/Main.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_4/upit.sql, sql)

## 9.3 Povezivanje na više baza podataka

Upravljanje podacima nad više baza podataka u JDBC aplikacijama je značajno jednostavnije
nego u C aplikacijama sa ugnežđenim SQL-om.

U JDBC aplikacijama možemo imati proizvoljan broj objekata interfejsa `Connection` i svaki
od njih predstavlja i ostvaruje konekciju ka jednoj bazi podataka. Pretpostavimo da
imamo dve konekcije ka bazama podataka `X` i `Y` ostvarene kroz objekte `conX` i `conY` interfejsa
`Connection`.

Ukoliko je potrebno da izvršimo naredbu nad bazom podataka `X`, onda je potrebno da kreiramo
objekat naredbe (bilo kroz klasu `Statement` ili `PreparedStatement`) koristeći objekat
`conX`. Za izvršavanje naredbe nad drugom bazom podataka `Y`, koristićemo objekat `conY`
za kreiranje objekta naredbe.

Naredni primer ilustruje korišćenje dve baze podataka: `VSTUD` i `MSTUD`.

{% include lab/exercise.html broj="9.5" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji omogućava konekciju na 2 baze (vstud i mstud). Program redom:

1. Zahteva od korisnika da unese broj bodova `B`.
2. Iz baze mstud izdvaja indeks, ime i prezime studenata koji su položili sve predmete koji nose više od `B` bodova.
3. Zatim, zahteva od korisnika da unese ocenu `O` (ceo broj od 6 do 10).
4. Iz baze vstud izlistava indeks, naziv, ocenu, godinu i oznaku ispitnog roka za sve studente koji nikada nisu dobili ocenu manju nego što je ocena `O`.
5. Nakon ispisivanja tih podataka, u bazi mstud, iz tabele ispit briše sva polaganja za studenta sa najmanjim brojem indeksa `I` iz dosije, i vraća `I`.
6. Na kraju, u bazi vstud, u tabeli `PREDMET` za sve predmete koje je položio student sa brojem indeksa `I`, uvećava broj bodova za jedan (osim ako je broj bodova veći od 10, tada ostavlja nepromenjeno stanje)." %}

Re\v senje: Datoteka `Main.java` implementira navedene funkcionalnosti, dok prate\'ce `*.sql` datoteke sadr\v ze SQL naredbe koje se koriste u re\v senju.

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_5/Main.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_5/izlistajStudenteMstud.sql, sql)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_5/izlistajPolaganjaVstud.sql, sql)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_5/uvecajBodoveZaPredmeteVstud.sql, sql)

## 9.4 Objektno-orijentisani pristup kreiranju JDBC aplikacija

Do sada smo naše JDBC aplikacije pisali u proceduralnom stilu - metod `main()` nam je služio kao alat koji je izvršavao sve potrebne aktivnosti, kao što su: povezivanje na bazu podataka, kreiranje objekata naredbi, izvršavanje naredbi, procesiranje podataka, oslobađanje resursa i diskonekcija sa baze podataka.

Ovaj pristup nije problematičan prilikom kreiranja jednostavnih aplikacija kao što su to bile aplikacije koje smo videli do sada. Međutim, što se složenost aplikacije povećava, ovakav pristup postaje izuzetno naporan za održavanje - što je i jedna od najvećih mana proceduralne paradigme programiranja. Neke od re\v senja ovih problema \'cemo prikazati kroz naredni zadatak.

{% include lab/exercise.html broj="9.6" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji izdvaja studente po studijskim programima. Za svaki studijski program ispisati naziv obim u ESPB i zvanje, a zatim spisak studenata (indeks, ime i prezime) koji su upisali taj program. Zadatak uraditi kori\v s\'cenjem objektno-orijentisanog pristupa dizajnu." %}

Re\v senje: Jednan od osnovnih koncepata objektno-orijentisane paradigme je da jedna klasa treba da implementira jednu jezgrovitu funkcionalnost.

Za početak, mo\v zemo napisati po jednu klasu za svaku tabelu koja \' ce se koristiti. U ovom slu\v caju to su tabele `STUDIJSKIPROGRAM` i `DOSIJE`, a odgovarajuće klase nazvaćemo `StudijskiProgram` i `Student`. Svaka klasa treba da sadr\v zi polja koja odgovaraju nekoj od kolona tabele. Jedan objekat klase predstavlja jedan slog odgovaraju\' ce tabele.

Potrebna nam je i klasa, na primer `Database`, koja implementira osnovne operacije za rad sa bazom. Zatim, ove funkcionalnosti mo\v zemo iskoristiti u implementaciji za rad sa bazom `STUD2020` tako \v sto \' cemo napisati posebnu klasu za ovu bazu, na primer `Stud2020`,  koja \' ce naslediti klasu `Database`. U ovoj klasi implementiramo zahteve kao metode klase. Time dobijamo naredne klase:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/Database.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/Stud2020.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/StudijskiProgram.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/Student.java, java)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/Main.java, java)

Naredne SQL datoteke sadr\v ze odgovaraju\'ce naredbe:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/studijskiProgrami.sql, sql)

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_6/studentiStudijskogPrograma.sql, sql)


## 9.5 Zadaci za ve\v zbu

{% include lab/exercise.html broj="9.7" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji ima mogu\'cnost da upravlja podacima o statistikama upisanih kurseva. Kreirati klasu `Main.java`. U toj klasi implementirati naredne metode nad datim bazama podataka. Nije dozvoljeno menjati potpise metoda; jedino je mogu\'ce dodavati izuzetke koje oni ispaljuju. Nije dozvoljeno implementirati opisane operacije van tela metoda (ali je mogu\'ce koristiti pomo\'cne metode):

(a) Napisati metod `private static ArrayList<Predmet> aPronadjiSvePredmete(Connection con, String upitZaNaziv) throws SQLException` koji pronalazi identifikatore i nazive svih predmeta \v ciji naziv predmeta po\v cinje niskom `upitZaNaziv`. Implementirati klasu `Predmet` koja ima dva polja koja predstavljaju identifikator i naziv predmeta. Metod vra\'ca listu instanci ove klase.

(b) Napisati metod `private static ArrayList<Predmet> bIzdvojiPredmeteKojiIspunjavajuUslov(Connection con, ArrayList<Predmet> predmeti) throws SQLException` koji od date liste predmeta `predmeti` izdvaja samo one predmete koji zadovoljavaju naredna dva uslova:
- Predmet mora da ima studente koji su ga upisali.
- Predmet mora da se ne nalazi u tabeli `STATISTIKAUPISANIHKURSEVA`.
Dopustiti da aplikacija mo\v ze da vidi nepotvr\dj ene izmene drugih aplikacija prilikom provere datih uslova.

(c) Napisati metod `private static void cObradiPredmete(Connection con, ArrayList<Predmet> predmeti) throws SQLException` koji izdvaja naredne informacije: (1) identifikator predmeta, (2) \v skolsku godinu, (3) broj studenata koji su upisali taj predmet u toj godini, (4) broj polaganja tog predmeta u toj godini, ali samo za one predmete koji zadovoljavaju uslove iz metoda pod (b). Ove informacije je potrebno ispisati na standardni izlaz, a zatim uneti u tabelu `STATISTIKAUPISANIHKURSEVA` metodom pod (d). Samo u ovom metodu proveravati gre\v ske koje se javljaju prilikom izvr\v savanja aplikacije u vi\v sekorisni\v ckom okru\v zenju. Postaviti istek vremena na 5 sekundi. Obrada jednog predmeta (ispis + unos) mora da predstavlja jednu transakciju. Omogu\'citi da nijedna druga aplikacija ne sme \v citati ili menjati podatke tokom obrade predmeta u ovom metodu.

(d) Napisati metod `private static void dUnesiNovuStatistiku(Connection con, int idPredmeta, short godina, Integer brojStudenata, Integer brojPolaganja) throws SQLException` koji unosi novi slog u tabelu `STATISTIKAUPISANIHKURSEVA` na osnovu argumenata koji mu se prosle\dj uju. Za kolonu `PONISTENI` postaviti vrednost `0`.

(e) Napisati metod `private static void ePonistiStatistike(Connection con, short godina, Scanner ulaz) throws SQLException` koji ponistava sve statistike (tj. postavlja kolonu `PONISTENI` na vrednost `1`) iz tabele `STATISTIKAUPISANIHKURSEVA` za one statistike iz godine koja se prosle\dj uje kao argument metoda. Poni\v stavanje svih statistika za datu godinu predstavlja jednu transakciju. Me\dj utim, potrebno je omogu\'citi da se nakon izmene jednog sloga korisnik pita da potvrdi izmene. Ukoliko ipak \v zeli da odustane od izmene teku\'ce statistike, omogu\'citi poni\v stavanje samo poslednje izmenjene statistike.

(f) Napisati metod `private static void fObrisiStatistike(Connection con) throws SQLException` koji bri\v se sve poni\v stene statistike iz tabele `STATISTIKAUPISANIHKURSEVA`. Za svaki slog je neophodno ispisati na standardni izlaz identifikator predmeta za slog koji se bri\v se, a zatim se izvr\v sava brisanje tog sloga. Brisanje svih poni\v stenih statistika predstavlja jednu transakciju.

(g) Napisati metod `private static void gPrikaziStatistike(Connection con) throws SQLException` koji ispisuje informacije iz tabele `STATISTIKAUPISANIHKURSEVA`. Sortirati ispis po identifikatoru predmeta rastu\'ce.

Aplikacija omogu\'cava korisniku da odabere jednu od narednih 5 opcija. Svaki put kada se opcija zavr\v si (osim u slu\v caju opcije 5), aplikacija ponovo zahteva od korisnika da unese jednu od narednih opcija:
1. `unos`: Aplikacija zahteva od korisnika da unese upit za naziv predmeta. Nakon unosa, aplikacija metodom pod (a) pronalazi sve kandidate. Zatim ispisuje sve predmete metodom pod (b). Nakon toga, vr\v si se obrada metodom pod (c).
2. `ponistavanje`: Aplikacija zahteva od korisnika da unese godinu studija. Zatim se metodom pod (e) vr\v si poni\v stavanje statistika.
3. `brisanje`: Aplikacija izvr\v sava metod pod (f).
4. `prikazivanje`: Aplikacija izvr\v sava metod pod (g).
5. `dalje`: Aplikacija zahteva od korisnika da unese ocenu i poziva metod pod (h). Nakon toga, aplikacija se zavr\v sava." %}

Re\v senje: Pre pokretanja programa, potrebno je pripremiti bazu `STUD2020` izvr\v savanjem narednog skripta nad tom BP:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/pripremaBaze.sql, sql)

Implementacija re\v senja se nalazi u narednim datotekama:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/Main.java, java)
include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/izdvajanjePredmetaSaUslovom.sql, sql)
include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/statistika.sql, sql)

{% include lab/exercise.html broj="9.8" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji za sve ispitne rokove pronalazi položene predmet u tom ispitnom roku Za svaki predmet program pronalazi koliko je kojih ocena postignuto i te podatke unosi u tabelu `ISPITNIROKOVIPOLAGANJA`. Kreirati datu tabelu na osnovu SQL koda ispod.

Pre jednog unosa podataka ispisati podatke koji ce biti uneti. Takođe, omogućiti da se podaci unose tako što korisnik mora da odobri unos podataka na svakih 20 redova (tzv. *batch* unos podataka). Napisati program tako da može da radi u višekorisničkom okruženju. Unos podataka za jedno polaganje predstavlja jednu transakciju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi. Obraditi sve moguće greške.

SQL naredbe za kreiranje i brisanje tabele sačuvati u datotekama 2a.sql i 2b.sql, redom, a `SELECT` naredbu kojim se izdvajaju potrebni podaci sačuvati u datoteci 2c.sql." %}

```sql
CREATE TABLE DA.ISPITNIROKOVIPOLAGANJA (
    GODINA SMALLINT NOT NULL,
    OZNAKA VARCHAR(20) NOT NULL,
    IDPREDMETA INTEGER NOT NULL,
    OCENA SMALLINT NOT NULL,
    BROJ INTEGER NOT NULL,
    NAZIVROKA VARCHAR(50),
    NAZIVPREDMETA VARCHAR(200),
    PRIMARY KEY(GODINA, OZNAKA, IDPREDMETA, OCENA),
    CONSTRAINT FK_PREDMET FOREIGN KEY (IDPREDMETA)
        REFERENCES DA.PREDMET (ID)
        ON DELETE CASCADE,
    CONSTRAINT FK_IR FOREIGN KEY (GODINA, OZNAKA)
        REFERENCES DA.ISPITNIROK (SKGODINA, OZNAKAROKA)
        ON DELETE CASCADE
)
```

Pomo\'c pri re\v savanju zadatka: Kreirati tabelu `OBRADJENAPOLAGANJA` na osnovu SQL koda ispod koja \'ce sadr\v zati informacije o ve\'c obra\dj enim polaganjima iz tabele `ISPITNIROKOVIPOLAGANJA`. Nakon svake obrade jednog polaganja, uneti novi red u ovu tabelu i potvrditi izmene.

```sql
CREATE TABLE OBRADJENAPOLAGANJA (
    GODINA SMALLINT NOT NULL,
    OZNAKA VARCHAR(20) NOT NULL,
    IDPREDMETA INTEGER NOT NULL,
    OCENA SMALLINT NOT NULL,
    PRIMARY KEY(GODINA, OZNAKA, IDPREDMETA, OCENA),
    CONSTRAINT FK_POLAGANJA FOREIGN KEY(GODINA, OZNAKA, IDPREDMETA, OCENA) 
        REFERENCES DA.ISPITNIROKOVIPOLAGANJA
        ON DELETE CASCADE
)
```

{% include lab/exercise.html broj="9.9" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji redom:

1. Naredbama `INSERT` unosi podatke o nekoliko stipendija u tabelu `STIPENDIJA`. Izračunati i broj unetih redova. U slučaju da je broj unetih redova jednak nuli, ispisati poruku ”Nijedan red nije dodat”, a inače ispisati poruku u unetom broju redova. Kreirati datu tabelu na osnovu SQL koda ispod.
2. Za svaku stipendiju, pita korisnika da li želi da promeni broj studenata za tu stipendiju i ukoliko je odgovor korisnika potvrdan, od korisnika traži da unese novi broj studenata i izvršava odgovarajuću naredbu.
3. Za svaku stipendiju, pita korisnika da li želi da obriše tu stipendiju i ukoliko je odgovor korisnika potvrdan, izvršava odgovarajuću naredbu.

Aplikacija treba da radi u višekorisničkom okruženju. Obrada jedne stipendije u svim zahtevima treba da predstavlja jednu transakciju. Postaviti istek vremena na 5 sekundi." %}

```sql
CREATE TABLE STIPENDIJA (
    ID INTEGER NOT NULL,
    NAZIV VARCHAR(100) NOT NULL,
    GODINA SMALLINT NOT NULL,
    BROJSTIPENDISTA SMALLINT NOT NULL,
    VISINASTIPENDIJE SMALLINT,
    MINPROSEK FLOAT,
    NAPOMENA VARCHAR(50),
    PRIMARY KEY (ID)
)
```
