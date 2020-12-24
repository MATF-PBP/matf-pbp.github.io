---
layout: page
title: 9. Napredne tehnike razvijanja Java/SQL aplikacija
under_construction: true
---

U ovom poglavlju ćemo demonstrirati rad sa transakcijama, rad u konkurentnom okruženju,
kao i povezivanje na više baza podataka u JDBC aplikacijama.

## 9.1 Transakcioni rad

JDBC supports the following concepts:

- Setting the Auto-Commit Mode
- Transaction isolation level
- Savepoints

### 9.1.1 Auto-Commit Mode

When you connect to a database, the auto-commit property for the `Connection` object
is set to `true` by default. If a connection is in the auto-commit mode, a SQL statement
is committed automatically after its successful execution. If a connection is not in the
auto-commit mode, you must call the `commit()` or `rollback()` method of the `Connection`
object to commit or roll back a transaction. Typically, you disable the auto-commit mode
for a connection in a JDBC application, so the logic in your application controls the
final outcome of the transaction. To disable the auto-commit mode, you need to call the
`setAutoCommit(false)` on the `Connection` object after a connection has been established.
If a connection URL allows you to set the auto-commit mode, you can also specify it as
part of the connection URL. You set the auto-commit mode of your connection in the
`JDBCUtil.getConnection()` method to false after you get a `Connection` object.

```java
// Get a connection
Connection con = DriverManager.getConnection(dbURL, userId, password);

// Set the auto-commit off
con.setAutoCommit(false);
```

If you have enabled the auto-commit mode for your connection, you cannot use its `commit()`
and `rollback()` methods. Calling the `commit()` and `rollback()` methods on a `Connection`
object, which has enabled the auto-commit mode, throws a `SQLException`.

If the `setAutoCommit()` method is called to change the auto-commit mode of a connection
in the middle of a transaction, the transaction is committed at that time. Typically, you
would set the auto-commit mode of a connection just after connecting to the database.

#### Committing and Rolling Back Transactions

If the auto-commit mode is disabled for a connection, you can use the `commit()` or `rollback()`
method to commit or roll back a transaction. Typical code in a JDBC application
that performs a database transaction is as shown:

```java
// Get a connection
Connection con = DriverManager.getConnection(dbURL, userId, password);
// Set the auto-commit off
con.setAutoCommit(false);

try {
    // Perform database transaction activities here
    
    // Successful scenario:
    con.commit();
    // Close the connection
    con.close();
}
catch (SQLException e) {
    System.out.println("An error occured: " + e.getMessage());
    System.out.println("Rolling back the transaction");

    try {
        // Unsuccessful scenario:
        con.rollback();
        // Close the connection
        con.close();
    }
    catch (SQLException e) {
    }

    System.exit(1);
}
```

{% include lab/exercise.html broj="9.1" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji redom:\n
\n
1. Pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.\n
2. Briše studenta sa pronađenim indeksom iz tabele `ISPIT` i ispisuje poruku korisniku o uspešnosti brisanja.\n
3. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.\n
4. Pita korisnika da li želi da potvrdi ili poništi izmene. U zavisnosti od korisnikovog odgovora, aplikacija potvrđuje ili poništava izmene uz ispisivanje poruke korisniku.\n
5. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.\n" %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_1/Main.java, java)

### 9.1.2 Transaction Isolation Level

The ANSI SQL-92 standard defines four transaction isolation levels in terms of the data
consistency. Each isolation level defines what kinds of data inconsistencies are allowed, or
not allowed. The four transaction isolation levels are as follows:

- Read uncommitted
- Read committed
- Repeatable read
- Serializable

Java defines the following four constants in the `Connection` interface that correspond to
the four isolation levels defined by the ANSI SQL-92 standard:

- `TRANSACTION_READ_UNCOMMITTED`
- `TRANSACTION_READ_COMMITTED`
- `TRANSACTION_REPEATABLE_READ`
- `TRANSACTION_SERIALIZABLE`

You can set the isolation level of a transaction for a database connection using the `setTransactionIsolation(int level)` method of the `Connection` interface.

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

### 9.1.3 Savepoints in a Transaction

A database transaction consists of one or more changes as a unit of work. A savepoint in
a transaction is like a marker that marks a point in a transaction so that, if needed, the
transaction can be rolled back (or undone) up to that point.

An object of the `Savepoint` interface represents a savepoint in a transaction. To mark a
savepoint in a transaction, you simply call the `setSavepoint()` method of the `Connection`.
The `setSavepoint()` method is overloaded. One version accepts no argument and another
accepts a string, which is the name of the savepoint. The `setSavepoint()` method returns
a `Savepoint` object, which is your marker and you must keep it for future use. Here’s an
example:

```java
Connection con = DriverManager.getConnection(dbURL, userId, password);
con.setAutoCommit(false);

Statement stmt = con.createStatement();

stmt.execute("insert into person values ('John', 'Doe')");
Savepoint sp1 = con.setSavepoint(); // 1

stmt.execute("insert into person values ('Jane', 'Doe')");
Savepoint sp2 = con.setSavepoint(); // 2

stmt.execute("insert into person values ('Another', 'Unknown')");
Savepoint sp3 = con.setSavepoint(); // 3
```

At this point, you have finer control on the transaction if you want to undo any of these
three inserts into the person table. Now you can use another version of the `rollback()`
method of the `Connection`, which accepts a `Savepoint` object. If you want to undo all
changes that were made after savepoint 1, you can do so as follows:

```java
// Rolls back inserts 2 and 3
con.rollback(sp1);
```

Once you roll back up to a savepoint (say, `spx`), all savepoints that were created after the
savepoint spx are released and you cannot refer to them again. If you refer to a released
savepoint, the JDBC driver will throw a `SQLException`. The following snippet of code
will throw a `SQLException`:

```java
con.rollback(sp2); // Will release sp3
con.rollback(sp3); // Will throw an exception: sp3 is already released.
```

Note that when you roll back a transaction to a savepoint, that savepoint itself is not
released. When you call, for example, `con.rollback(sp2)`, savepoint `sp2` remains valid. You
can add more savepoints afterward and roll back up to savepoint `sp2` again.

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
problema konkurentnog okruženja, DB2 SUBP šalje grešku -911 ili -913. U tom slučaju,
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
        if (e.getErrorCode() == -911 || e.getErrorCode() == -913) {
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

{% include lab/exercise.html broj="9.5" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji omogućava konekciju na 2 baze (vstud i mstud). Program redom:\n
\n
1. Zahteva od korisnika da unese broj bodova `B`.\n
2. Iz baze mstud izdvaja indeks, ime i prezime studenata koji su položili sve predmete koji nose više od `B` bodova.\n
3. Zatim, zahteva od korisnika da unese ocenu `O` (ceo broj od 6 do 10).\n
4. Iz baze vstud izlistava indeks, naziv, ocenu, godinu i oznaku ispitnog roka za sve studente koji nikada nisu dobili ocenu manju nego što je ocena `O`.\n
5. Nakon ispisivanja tih podataka, u bazi mstud, iz tabele ispit briše sva polaganja za studenta sa najmanjim brojem indeksa `I` iz dosije, i vraća `I`.\n
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

{% include lab/exercise.html broj="9.7" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji ima mogu\'cnost da upravlja podacima o statistikama upisanih kurseva. Kreirati klasu `Main.java`. U toj klasi implementirati naredne metode nad datim bazama podataka. Nije dozvoljeno menjati potpise metoda; jedino je mogu\'ce dodavati izuzetke koje oni ispaljuju. Nije dozvoljeno implementirati opisane operacije van tela metoda (ali je mogu\'ce koristiti pomo\'cne metode):\n
\n
(a) Napisati metod `private static ArrayList<Predmet> aPronadjiSvePredmete(Connection con, String upitZaNaziv) throws SQLException` koji pronalazi identifikatore i nazive svih predmeta \v ciji naziv predmeta po\v cinje niskom `upitZaNaziv`. Implementirati klasu `Predmet` koja ima dva polja koja predstavljaju identifikator i naziv predmeta. Metod vra\'ca listu instanci ove klase.\n
\n
(b) Napisati metod `private static ArrayList<Predmet> bIzdvojiPredmeteKojiIspunjavajuUslov(Connection con, ArrayList<Predmet> predmeti) throws SQLException` koji od date liste predmeta `predmeti` izdvaja samo one predmete koji zadovoljavaju naredna dva uslova:\n
- Predmet mora da ima studente koji su ga upisali.\n
- Predmet mora da se ne nalazi u tabeli `STATISTIKAUPISANIHKURSEVA`.\n
Dopustiti da aplikacija mo\v ze da vidi nepotvr\dj ene izmene drugih aplikacija prilikom provere datih uslova.\n
\n
(c) Napisati metod `private static void cObradiPredmete(Connection con, ArrayList<Predmet> predmeti) throws SQLException` koji izdvaja naredne informacije: (1) identifikator predmeta, (2) \v skolsku godinu, (3) broj studenata koji su upisali taj predmet u toj godini, (4) broj polaganja tog predmeta u toj godini, ali samo za one predmete koji zadovoljavaju uslove iz metoda pod (b). Ove informacije je potrebno ispisati na standardni izlaz, a zatim uneti u tabelu `STATISTIKAUPISANIHKURSEVA` metodom pod (d). Samo u ovom metodu proveravati gre\v ske koje se javljaju prilikom izvr\v savanja aplikacije u vi\v sekorisni\v ckom okru\v zenju. Postaviti istek vremena na 5 sekundi. Obrada jednog predmeta (ispis + unos) mora da predstavlja jednu transakciju. Omogu\'citi da nijedna druga aplikacija ne sme \v citati ili menjati podatke tokom obrade predmeta u ovom metodu.\n
\n
(d) Napisati metod `private static void dUnesiNovuStatistiku(Connection con, int idPredmeta, short godina, Integer brojStudenata, Integer brojPolaganja) throws SQLException` koji unosi novi slog u tabelu `STATISTIKAUPISANIHKURSEVA` na osnovu argumenata koji mu se prosle\dj uju. Za kolonu `PONISTENI` postaviti vrednost `0`.\n
\n
(e) Napisati metod `private static void ePonistiStatistike(Connection con, short godina, Scanner ulaz) throws SQLException` koji ponistava sve statistike (tj. postavlja kolonu `PONISTENI` na vrednost `1`) iz tabele `STATISTIKAUPISANIHKURSEVA` za one statistike iz godine koja se prosle\dj uje kao argument metoda. Poni\v stavanje svih statistika za datu godinu predstavlja jednu transakciju. Me\dj utim, potrebno je omogu\'citi da se nakon izmene jednog sloga korisnik pita da potvrdi izmene. Ukoliko ipak \v zeli da odustane od izmene teku\'ce statistike, omogu\'citi poni\v stavanje samo poslednje izmenjene statistike.\n
\n
(f) Napisati metod `private static void fObrisiStatistike(Connection con) throws SQLException` koji bri\v se sve poni\v stene statistike iz tabele `STATISTIKAUPISANIHKURSEVA`. Za svaki slog je neophodno ispisati na standardni izlaz identifikator predmeta za slog koji se bri\v se, a zatim se izvr\v sava brisanje tog sloga. Brisanje svih poni\v stenih statistika predstavlja jednu transakciju.\n
\n
(g) Napisati metod `private static void gPrikaziStatistike(Connection con) throws SQLException` koji ispisuje informacije iz tabele `STATISTIKAUPISANIHKURSEVA`. Sortirati ispis po identifikatoru predmeta rastu\'ce.\n
\n
Aplikacija omogu\'cava korisniku da odabere jednu od narednih 5 opcija. Svaki put kada se opcija zavr\v si (osim u slu\v caju opcije 5), aplikacija ponovo zahteva od korisnika da unese jednu od narednih opcija:\n
1. `unos`: Aplikacija zahteva od korisnika da unese upit za naziv predmeta. Nakon unosa, aplikacija metodom pod (a) pronalazi sve kandidate. Zatim ispisuje sve predmete metodom pod (b). Nakon toga, vr\v si se obrada metodom pod (c).\n
2. `ponistavanje`: Aplikacija zahteva od korisnika da unese godinu studija. Zatim se metodom pod (e) vr\v si poni\v stavanje statistika.\n
3. `brisanje`: Aplikacija izvr\v sava metod pod (f).\n
4. `prikazivanje`: Aplikacija izvr\v sava metod pod (g).\n
5. `dalje`: Aplikacija zahteva od korisnika da unese ocenu i poziva metod pod (h). Nakon toga, aplikacija se zavr\v sava." %}

Re\v senje: Pre pokretanja programa, potrebno je pripremiti bazu `STUD2020` izvr\v savanjem narednog skripta nad tom BP:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/pripremaBaze.sql, sql)

Implementacija re\v senja se nalazi u narednim datotekama:

include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/Main.java, java)
include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/izdvajanjePredmetaSaUslovom.sql, sql)
include_source(vezbe/primeri/poglavlje_9/src/zadatak_9_7/statistika.sql, sql)

{% include lab/exercise.html broj="9.8" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji za sve ispitne rokove pronalazi položene predmet u tom ispitnom roku Za svaki predmet program pronalazi koliko je kojih ocena postignuto i te podatke unosi u tabelu `ISPITNIROKOVIPOLAGANJA`. Kreirati datu tabelu na osnovu SQL koda ispod.\n
\n
Pre jednog unosa podataka ispisati podatke koji ce biti uneti. Takođe, omogućiti da se podaci unose tako što korisnik mora da odobri unos podataka na svakih 20 redova (tzv. *batch* unos podataka). Napisati program tako da može da radi u višekorisničkom okruženju. Unos podataka za jedno polaganje predstavlja jednu transakciju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi. Obraditi sve moguće greške.\n
\n
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

{% include lab/exercise.html broj="9.9" tekst="Napisati Java program u kojem se SQL naredbe izvr\v savaju dinami\v cki koji redom:\n
\n
1. Naredbama `INSERT` unosi podatke o nekoliko stipendija u tabelu `STIPENDIJA`. Izračunati i broj unetih redova. U slučaju da je broj unetih redova jednak nuli, ispisati poruku ”Nijedan red nije dodat”, a inače ispisati poruku u unetom broju redova. Kreirati datu tabelu na osnovu SQL koda ispod.\n
2. Za svaku stipendiju, pita korisnika da li želi da promeni broj studenata za tu stipendiju i ukoliko je odgovor korisnika potvrdan, od korisnika traži da unese novi broj studenata i izvršava odgovarajuću naredbu.\n
3. Za svaku stipendiju, pita korisnika da li želi da obriše tu stipendiju i ukoliko je odgovor korisnika potvrdan, izvršava odgovarajuću naredbu.\n
\n
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
