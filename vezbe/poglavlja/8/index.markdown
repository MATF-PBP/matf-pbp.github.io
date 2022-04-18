---
layout: page
title: 8. Osnovni koncepti programiranja Java/SQL aplikacija sa dinamičkim SQL naredbama (JDBC)
under_construction: true
---

Java Database Connectivity (JDBC) predstavlja Java interfejs za programiranje aplikacija (API) koji standardizuje
na\v cine za pristup i upravljanje bazama podataka. JDBC kod je jednostavno portabilizan izme\dj u nekoliko
RSUBP proizvo\dj a\v ca. Jedina promena u kodu koja se o\v cekuje kada se JDBC kod portuje za neki drugi RSUBP
u odnosu na onaj za koji je prvobitno napisan jeste koji JDBC drajver se u\v citava i niska za konekciju na bazu 
podataka. JDBC koristi isklju\v civo dinami\v cko izvr\v savanje SQL naredbi i veoma je popularan u praksi.

Pre nego \v sto se izvr\v savaju SQL naredbe u bilo kom JDBC programu, aplikacija se mora povezati na bazu podataka.

![](./Slike/jdbc.png)

Iako postoji nekoliko tipova JDBC drajvera kao \v sto su tipovi 1, 2, 3 i 4, tipovi 1 i 3 nisu \v cesto kori\v s\'ceni,
i Db2 podr\v ska za ove tipove je uklonjena iz novijih verzija ovog SUBP. Za tip 2 postoje dva drajvera, kao \v sto \'cemo videti,
ali jedan od njih je tako\dj e zastareo, te bi ga trebalo izbegavati.

Kao \v sto je vidljivo iz naredne tabele, tipovi 2 i 4 JDBC drajvera su podr\v zani Db2 SUBP. Tip 2 drajver zahteva 
instalaciju Db2 klijenta, s obzirom da se on koristi kako bi drajver kreirao komunikacioni kanal ka bazi podataka.
Sa druge strane, tip 4 je \v cist Java klijent, te nije potrebno instalirati Db2 klijent, ali sam drajver mora biti
instaliran na ma\v sini gde se JDBC aplikacija pokre\v ce. 

| Tip drajvera | Ime drajvera | Datoteka | Podr\v zava | Najmanja verzija Java SDK |
| ----------- | ----------- | ----------- | -------- | -------------------------------------- |
| Tip 2 | DB2 JDBC Type 2 Driver for Linux, UNIX® and Windows (Deprecated) | `db2java.zip` | JDBC 1.2, JDBC 2.0 | 1.4.2 |
| Tip 2, Tip 4 | IBM Data Server Driver for JDBC and SQLJ | `db2jcc.jar` i `sqlj.zip` | JDBC 3.0 saglasni | 1.4.2 |
| Tip 2, Tip 4 | IBM Data Server Driver for JDBC and SQLJ | `db2jcc4.jar` i `sqlj4.zip` | JDBC 4.0 i ranije | 6 |

Kao \v sto je pomenuto ranije, a tako\dj e je prikazano u tabeli iznad, JDBC drajver tipa 2 je dostupan u dva razli\v cita
drajvera. Ipak, Db2 JDBC Type 2 Driver for Linux, UNIX and Windows, \v cija je implementacija data datotekom `db2java.zip` je zastareo.

Kada se instalira Db2 server, Db2 klijent ili IBM Data Server Driver za JDBC i SQLJ, tj. datoteke `db2jcc.jar` i `sqlj.zip` 
koje su saglasne sa JDBC 3.0 automatski su dodate u "classpath".

## 8.1 Kreiranje konekcije

Kolekcija ka bazi podataka se mo\v ze dobiti kori\v s\'cenjem klase `DriverManager` iz `java.sql` paketa.

Paket `java.sql` defini\v se klase i interfejse koji su neophodni kako bi JDBC program pristupio
podacima koji su skladi\v steni u relacionim bazama podataka. Ovaj API se mo\v ze koristiti za 
povezivanje na relacionu bazu podataka i upravljanje podacima (\v citanje, unos, a\v zuriranje, brisanje i dr.)
koji su skladi\v steni u formi tabela prema SQL standardu. Interfejsi koji su definisani u ovom paketu
implementirani su u klasa odgovaraju\'cih drajvera i njihova definicija se mo\v ze razlikovati izme\dj u
proizvo\dj aca.

Pre ostvarivanja konekcije, potrebno je u\v citati klase iz drajvera i registrovati ih za upotrebu kori\v s\'cenjem klase
`DriverManager`. Proizvoljan broj drajvera se mo\v ze u\v citati u registrovati klasom `DriverManager` kori\v s\'cenjem metoda `forName`:

```java
class ProgramName {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        }
        catch (Exception e) {
            e.printStackTrace();

            System.exit(1);
        }
    }
    // ...
}
```

Java podržava poseban blok, nazvan statički blok (koji se takođe naziva statička klauzula) 
koji se može koristiti za statičke inicijalizacije klase. Ovaj kod unutar statičkog bloka 
se izvršava samo jednom: prvi put kada napravite objekat te klase ili prvi put kada 
pristupite statičkom članu te klase (čak i ako nikada ne napravite objekat te klase). 
Koristićemo statičke blokove u našim programima za učitavanje i registraciju DB2 JDBC drajvera, 
kao što je prikazano u gornjem primeru koda.

Metod `forName` kao argument o\v cekuje nisku \v cija vrednost je naziv paketa koji implementira
interfejse definisane paketom `java.sql`.

Konekcija ka bazi podataka se mo\v ze ostvariti pozivom metoda `getConnection` klase `DriverManager`. 
Ovaj metod prihvata nisku (URL) kao ulaz, koji sadr\v zi informacije potrebne za povezivanje sa bazom podataka. 
Metod vraca objekat interfejsa `Connection`, koji se mo\v ze koristiti za upravljanje konekcijom ka bazi podataka. 
Tipičan format URL-a za drajver tipa 4 je:

```
jdbc:db2://<naziv servera>:<broj porta>/<ime baze podataka>
```

Konekcija ka bazi podataka se raskida pozivom metoda `close` nad objektom interfejsa `Connection`:

```java
public static void main(String argv[]) {
    Connection con = null;
    String url = "jdbc:db2://localhost:50000/stud2020";
    
    // Povezivanje na bazu podataka
    con = DriverManager.getConnection(url, "student", "abcdef");

    // Kod programa...

    // Raskidanje konekcije sa bazom podataka
    con.close();
}
```

## 8.2 Obrada SQL grešaka

Kao \v sto znamo iz ranijih poglavlja, naredbe za povezivanje i raskidanje konekcije
(kao i druge SQL naredbe), mogu proizvesti SQL gre\v ske. U JDBC aplikacijama,
kao i u svim drugim Java aplikacijama, obrada gre\v saka se vr\v si pomo\'cu izuzetaka i
`try-catch` blokovima. JDBC aplikacija ispaljuje objekat klase `SQLException` svaki put
kada do\dj e do SQL gre\v ske ili `SQLWarning` svaki put kada do\dj e do SQL upozorenja
prilikom izvr\v savanja SQL naredbi. Tako\dj e, svi metodi JDBC API-ja koji mogu dovesti
do SQL gre\v saka u svom potpisu sadr\v ze klauzulu `throws SQLException`, \v sto zna\v ci
da je pozive ovakvih metoda neophodno obuhvatiti nekim `try-catch` blokom u programu.

Objekat klase `SQLException` sadr\v zi razne korisne informacije o SQL gre\v sci koja je
podignuta u SUBP, a neke od njih su navedene u narednoj tabeli:

| Informacija | Opis | Metod koji se koristi za dohvatanje informacije |
| --- | --- | --- |
| Poruka | Tekstualna reprezentacija SQL gre\v ske. | `getMessage` |
| SQLSTATE | SQLState niska. | `getSQLState` |
| SQLCODE | Celobrojna vrednost koja indikuje vrstu gre\v ske koja je podignuta u SUBP. | `getErrorCode` |

Pored navedenih informacija, Db2 JCC drajver pruža dodatni interfejs `com.ibm.db2.jcc.DB2Diagnosable`. 
Ovaj interfejs daje više informacija u vezi sa greškom koja se dogodila prilikom pristupanja Db2 bazi podataka.

Primer obrade `SQLExpection` izuzetaka dat je narednim kodom:

```java
Connection con = null;

try {
    // Povezivanje na bazu podataka
    con = DriverManager.getConnection(url, "student", "abcdef");

    // Kod programa...

    // Raskidanje konekcije sa bazom podataka
    con.close();
}
catch (SQLException e) {
    e.printStackTrace();

    System.out.println(
        "SQLCODE: " + e.getErrorCode() + "\n" +
        "SQLSTATE: " + e.getSQLState() + "\n" +
        "PORUKA: " + e.getMessage());

    // Treba da zatvorimo konekciju ka bazi podataka i u slučaju greške.
    // S obzirom da metod close takođe može ispaliti SQLException,
    // potrebno ga je uhvatiti, ali biramo da ga ne obrađujemo dalje.
    try {
        if (null != con) {
            con.close();
        }
    } catch (SQLException e2) {
    }

    System.exit(2);
}
catch (Exception e) {
    e.printStackTrace();

    // Isto kao u catch bloku iznad.
    try {
        if (null != con) {
            con.close();
        }
    } catch (SQLException e2) {
    }

    System.exit(3);
}
```

S obzirom da objekti interfejsa `Connection` implementiraju interfejs `AutoCloseable`, možemo koristiti i `try-with-resources` naredbu u okviru koje ćemo otvoriti konekciju. Naredba se stara o otvorenim resursima i na kraju ih automatski zatvara. 

```java
public static void main(String argv[]) {
    String url = "jdbc:db2://localhost:50000/stud2020";

    try (
        Connection con = DriverManager.getConnection(url, "student", "abcdef");
    ) {
        // Kod programa...
    } catch (SQLException e) {
        e.printStackTrace();

        System.out.println(
            "SQLCODE: " + e.getErrorCode() + "\n" +
            "SQLSTATE: " + e.getSQLState() + "\n" +
            "PORUKA: " + e.getMessage());

        System.exit(2);
    }
    catch (Exception e) {
        e.printStackTrace();

        System.exit(3);
    }
}
```

U slu\v caju da se vi\v se izuzetaka tipa `SQLExceptions` ispali, oni su ulan\v cani.
Informacija o narednom izuzetku se mo\v ze dobiti pozivom metoda `getNextException` nad
trenutnim `SQLException` objektom koji se obra\dj uje. Ovaj metod vra\'ca `null` ako je
teku\'ci `SQLException` objekat poslednji u lancu izuzetaka. Zbog toga se mo\v ze
koristiti `while` petlja u `catch` bloku programa kako bi se obradili `SQLException` objekti
jedan-po-jedan.

## 8.3 Upravljanje podacima

Nakon ostvarivanja konekcije ka bazi podataka, podaci se mogu dohvatiti, unositi, a\v zurirati
ili obrisati iz relacionih tabela kori\v s\'cenjem SQL naredbi. JDBC drajver implementira dva 
interfejsa, `Statement` i `PreparedStatement` za ove potrebe. Objekti jednog od ova dva interfejsa
su neophodni kako bi se SQL naredbe izvr\v sile nad bazom podataka. U nastavku govorimo o 
slu\v cajevima upotrebe ovih interfejsa, ali pre toga \'cemo re\'ci ne\v sto o tipovima podataka.

### 8.3.1 Tipovi podataka

Da bismo napisali efikasne JDBC programe, potrebno je da koristimo najbolja preslikavanja između 
Java tipova podataka i tipova kolona tabela.

Naredna tabela sumira mapiranja Db2 tipova podataka u Java tipove podataka za familiju metoda 
`ResultSet.getXXX` u JDBC programima. Ova tabela ne navodi tipove Java numeričkih omotača, 
koji se dohvataju pomoću metoda `ResultSet.getObject`.

| SQL tip podataka | Preporu\v ceni Java tip podataka | Drugi podr\v zani Java tipovi podataka |
| --- | --- | --- |
| SMALLINT | short | byte, int, long, float, double, java.math.BigDecimal, boolean, java.lang.String |
| INTEGER | int | short, byte, long, float, double, java.math.BigDecimal, boolean, java.lang.String |
| BIGINT | long | int, short, byte, float, double, java.math.BigDecimal, boolean, java.lang.String |
| DECIMAL(p,s) or NUMERIC(p,s) | java.math.BigDecimal | long, int, short, byte, float, double, boolean, java.lang.String |
| DECFLOAT(n) | java.math.BigDecimal | long, int, short, byte, float, double, java.math.BigDecimal, boolean, java.lang.String |
| REAL | float | long, int, short, byte, double, java.math.BigDecimal, boolean, java.lang.String |
| DOUBLE | double | long, int, short, byte, float, java.math.BigDecimal, boolean, java.lang.String |
| CHAR(n) | java.lang.String | long, int, short, byte, float, double, java.math.BigDecimal, boolean, java.sql.Date, java.sql.Time, java.sql.Timestamp, java.io.InputStream, java.io.Reader |
| VARCHAR(n) | java.lang.String | long, int, short, byte, float, double, java.math.BigDecimal, boolean, java.sql.Date, java.sql.Time, java.sql.Timestamp, java.io.InputStream, java.io.Reader |
| DATE | java.sql.Date | java.sql.String, java.sql.Timestamp |
| TIME | java.sql.Time | java.sql.String, java.sql.Timestamp |
| TIMESTAMP, TIMESTAMP(p), TIMESTAMP WITH TIME ZONE, TIMESTAMP(p) WITH TIME ZONE | java.sql.Timestamp | java.sql.String, java.sql.Date, java.sql.Time, java.sql.Timestamp |

Naredna tabela sumira preslikavanja Java tipova podataka u Db2 tipove podataka za familije metoda `PreparedStatement.setXXX` i `ResultSet.updateXXX` u JDBC programima. Kada je navedeno više od jednog Java tipa podataka, prvi tip podataka je preporučeni tip podataka.

| Java tip podataka | Db2 tip podataka |
| --- | --- |
| short, java.lang.Short | SMALLINT |
| boolean, byte, java.lang.Boolean, java.lang.Byte | SMALLINT |
| int, java.lang.Integer | INTEGER |
| long, java.lang.Long | BIGINT |
| java.math.BigInteger | BIGINT |
| java.math.BigInteger | CHAR(n) |
| float, java.lang.Float | REAL |
| double, java.lang.Double | DOUBLE |
| java.math.BigDecimal | DECIMAL(p,s) |
| java.math.BigDecimal | DECFLOAT(n) |
| java.lang.String | CHAR(n) |
| java.lang.String | VARCHAR(n) |
| java.sql.Date | DATE |
| java.sql.Time | TIME |
| java.sql.Timestamp | TIMESTAMP, TIMESTAMP(p), TIMESTAMP WITH TIME ZONE, TIMESTAMP(p) WITH TIME ZONE |
| java.util.Date | CHAR(n) |
| java.util.Date | VARCHAR(n) |
| java.util.Date | DATE |
| java.util.Date | TIME |
| java.util.Date | TIMESTAMP, TIMESTAMP(p), TIMESTAMP WITH TIME ZONE, TIMESTAMP(p) WITH TIME ZONE |

### 8.3.2 Interfejs `Statement`

Objekti interfejsa `Statement` mogu se iskoristiti za izvr\v savanje SQL naredbi koji ne 
sadr\v ze parametarske oznake. Ovi objekti se kreiraju pozivom metoda `createStatement` nad 
objektima interfejsa `Connection`. Mogu\'ce je kreirati proizvoljan broj objekata `Statement`
nad jednim objektom `Connection` i svi ti objekti \'ce biti vezani za konekciju koja je
opisana istim objektom `Connection` (drugim re\v cima, sve te SQL naredbe \'ce biti izvr\v sene
nad istom bazom podataka).

Interfejs `Statement` defini\v se dve vrste metoda za izvr\v savanje SQL naredbi. Oba metoda 
kao prvi parametar o\v cekuju nisku koja sadr\v zi validnu SQL naredbu. U zavisnosti od vrste 
SQL naredbe, koriste se naredni metodi:

- Metod `executeQuery` se koristi kada se kao rezultat izvr\v savanja SQL naredbe o\v cekuje kursor (na primer, prilikom izvr\v savanja naredbe `SELECT`). Metod `executeQuery` vra\'ca objekat interfejsa `ResultSet`, koji predstavlja skup rezultata koji se vra\'ca naredbom `SELECT`. Objekti interfejsa `ResultSet` mogu se koristiti za prolazak kroz redove rezultuju\'ce tabele.
- Metod `executeUpdate` se koristi za a\v zuriranje podataka (na primer, `INSERT`, `UPDATE`, `DELETE` i `MERGE` naredbama). Metod `executeUpdate` vra\'ca celobrojnu vrednost, koja indikuje broj redova koji je unet, a\v zuriran ili obrisan u bazi podataka, u zavisnosti od tipa SQL naredbi. Ovaj metod se koristi i u slu\v caju izvr\v savanja DDL naredbi, kao \v sto su `CREATE`, `ALTER`, `DROP`, `GRANT`, `REVOKE` i dr.

U nastavku dajemo dve procedure koje opisuju upotrebu opisanih metoda.

{% include lab/begin-recipe.html naziv="Obrada kursora u JDBC aplikacijama (slu\v caj bez parametarskih oznaka)" %}

Da bismo iterirali kroz rezultuju\'cu tabelu naredbe `SELECT` koja ne sadr\v zi parametarske oznake, potrebno je da ispratimo naredne korake:

1. Napisati SQL naredbu kao nisku.
```java
String sql = "SELECT ...";
```
2. Kreirati objekat interfejsa `Statement` pozivom metoda `Connection.createStatement()`.
```java
Statement stmt = con.createStatement();
```
3. Kreirati objekat interfejsa `ResultSet` pozivom metoda `Statement.executeQuery(String sql)`.
```java
ResultSet kursor = stmt.executeQuery(sql);
```
4. Pozicionirati se na red koji je potrebno pročitati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    // ...
}
```
5. Pozvati odgovarajući metod nad objektom interfejsa `ResultSet` iz familije metoda `ResultSet.getXXX(int columnIndex)` za dohvatanje vrednosti iz kolone rezultujuće tabele sa indeksom `columnIndex`. Na primer:
```java
while (kursor.next()) {
    int indeks = kursor.getInt(1);
    String ime = kursor.getString(2);
    // ...
}
```
   1. Eventualno proveriti da li je dohvaćena `NULL` vrednost (videti [sekciju o nedostaju\'cim vrednostima](#834-rukovanje-nedostajućim-vrednostima)).
6. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora.
7. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe.

{% include lab/end-recipe.html %}

{% include lab/begin-recipe.html naziv="Obrada a\v zuriraju\'cih SQL naredbi u JDBC aplikacijama (slu\v caj bez parametarskih oznaka)" %}

Da bismo izmenili podatke u bazi podataka nekom SQL naredbom bez parametarskih oznaka, potrebno je da ispratimo naredne korake:

1. Napisati SQL naredbu kao nisku.
```java
String sql = "DELETE ...";
```
2. Kreirati objekat interfejsa `Statement` pozivom metoda `Connection.createStatement()`.
```java
Statement stmt = con.createStatement();
```
3. Pozvati metod `stmt.executeUpdate(String sql)`.
```java
int brojRedova = stmt.executeUpdate(sql);
```
4. Pozvati metod `stmt.close()` radi zatvaranja objekta naredbe.

{% include lab/end-recipe.html %}

Naredni zadaci ilustruju opisane procedure na konkretnim primerima.

{% include lab/exercise.html broj="8.1" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izlistava oznake i nazive svih predmeta koji imaju više od 20 ESPB bodova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_1/Main.java, java)

{% include lab/exercise.html broj="8.2" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji u tabelu `PREDMET` unosi podatak o predmetu čiji je identifikator 2001, oznaka Pred1, naziv Predmet 1 i nosi `5` ESPB bodova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_2/Main.java, java)

### 8.3.3 Interfejs `PreparedStatement`

Objekti interfejsa `PreparedStatement` obavezno se koriste u slu\v caju kada SQL naredbe sadr\v ze
parametarske oznake. `PreparedStatement` objekti se kreiraju pozivom metoda `prepareStatement` nad objektima `Connection`.
Za razliku od metoda `createStatement`, metod `prepareStatement` o\v cekuje jedan parametar,
a to je niska koja predstavlja SQL naredbu sa parametarskim oznakama, za koju \'ce biti izvr\v sena priprema.
Pun potpis ovog metoda je:

```java
PreparedStatement prepareStatement(String sql) throws SQLException
```

Postavljanje vrednosti parametarskim oznakama se vr\v si pozivom nekog metoda iz familije
`PreparedStatement.setXXX`, gde `XXX` ozna\v cava tip parametarske oznake koja se postavlja.
Neki od primera `setXXX` metoda su:

- `setInt`,
- `setString`,
- `setDouble`,
- `setBytes`,
- `setClob`,
- `setBlob`
- ...

Nakon postavljanja vrednosti parametara, SQL naredba se mo\v ze izvr\v siti pozivom nekog
od ve\'c opisanih metoda `executeQuery` ili `executeUpdate`, zavisno od vrste SQL naredbe.
Za razliku od istoimenih metoda interfejsa `Statement`, ovi metodi se pozivaju bez prosle\dj ivanja
SQL naredbe (s obzirom da smo naredbu ve\'c prosledili prilikom konstrukcije `PreparedStatement` objekta).

Naredne procedure opisuju korake neophodne za kori\v s\'cenje interfejsa `PreparedStatement`.

{% include lab/begin-recipe.html naziv="Obrada kursora u JDBC aplikacijama (slu\v caj sa parametarskim oznakama)" %}

Da bismo iterirali kroz rezultuju\'cu tabelu naredbe `SELECT` koja sadr\v zi parametarske oznake, potrebno je da ispratimo naredne korake:

1. Napisati SQL naredbu kao nisku.
```java
String sql = "SELECT ... WHERE INDEKS = ? AND IME = ? ...";
```
2. Kreirati objekat interfejsa `PreparedStatement` pozivom metoda `Connection.prepareStatement(String sql)`.
```java
PreparedStatement stmt = con.prepareStatement(sql);
```
3. Pozvati odgovarajući metod iz familije metoda `PreparedStatement.setXXX(int parameterIndex, XXX x)` 
za postavljanje vrednosti parametarske oznake u naredbi. Na primer:
```java
stmt.setInt(1, 20200134);
stmt.setString(2, "Ivana");
```
4. Kreirati objekat interfejsa `ResultSet` pozivom metoda `PreparedStatement.executeQuery()`.
```java
ResultSet kursor = stmt.executeQuery();
```
5. Pozicionirati se na red koji je potrebno pročitati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    // ...
}
```
6. Pozvati odgovarajući metod iz familije metoda `ResultSet.getXXX(int columnIndex)` za dohvatanje vrednosti 
iz kolone rezultujuće tabele sa indeksom `columnIndex`. Na primer:
```java
while (kursor.next()) {
    int indeks = kursor.getInt(1);
    String ime = kursor.getString(2);
    // ...
}
```
   1. Eventualno proveriti da li je dohvaćena `NULL` vrednost (videti [sekciju o nedostaju\'cim vrednostima](#834-rukovanje-nedostajućim-vrednostima)).
7. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora.
8. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe.

{% include lab/end-recipe.html %}

{% include lab/begin-recipe.html naziv="Obrada a\v zuriraju\'cih SQL naredbi u JDBC aplikacijama (slu\v caj sa parametarskim oznakama)" %}

Da bismo izmenili podatke u bazi podataka nekom SQL naredbom sa parametarskim oznakama, potrebno je da ispratimo naredne korake:

1. Napisati SQL naredbu kao nisku.
```java
String sql = "DELETE ... WHERE INDEKS = ? ...";
```
2. Kreirati objekat interfejsa `PreparedStatement` pozivom metoda `Connection.prepareStatement(String sql)`.
```java
PreparedStatement stmt = con.prepareStatement(sql);
```
3. Pozvati odgovarajući metod iz familije metoda `PreparedStatement.setXXX(int parameterIndex, XXX x)` 
za postavljanje vrednosti parametarske oznake u naredbi. Na primer:
```java
stmt.setInt(1, 20200134);
```
4. Pozvati metod `stmt.executeUpdate()`.
```java
int brojRedova = stmt.executeUpdate();
```
5. Pozvati metod `stmt.close()` radi zatvaranja objekta naredbe.

{% include lab/end-recipe.html %}

Naredni zadaci demonstriraju upotrebu interfejsa `PreparedStatement`.

{% include lab/exercise.html broj="8.3" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji učitava dva cela broja, X i Y, a zatim svim predmetima koji imaju X ESPB bodova, postavlja broj bodova na Y. Nakon toga ispisati broj ažuriranih redova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_3/Main.java, java)

{% include lab/exercise.html broj="8.4" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji sa standardnog ulaza učitava ceo broj N i izdvaja indeks, ime,
prezime i naziv studijskog programa svih studenata koji su položili tačno N predmeta, kao i spisak tih
predmeta (naziv i ocena)." %}

Rešenje: S obzirom da je SQL upit koji se koristi u ovom zadatku nešto složeniji, da ga ne bismo zapisivali u Java kodu kao nisku, taj upit ćemo začuvati u datoteci `upit.sql` u istom direktorijumu (paketu) kao i `Main.java` datoteku koja sadrži rešenje ovog zadatka. Dodatno, u Java kodu ćemo pronaći ovu datoteku na sistemu datoteka i učitati njen sadržaj kao nisku da bismo je zatim izvršili. Posebno treba obratiti pažnju na parametarsku oznaku u samom upitu, koja će biti zamenjena odgovarajućom vrednošću u fazi izvršavanja programa.

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_4/upit.sql, sql)

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_4/Main.java, java)

### 8.3.4 Rukovanje nedostajućim vrednostima

Što se tiče rada sa podacima koji su potencijalno nedostajući, na raspolaganju nam
je metod `ResultSet.wasNull`, koji je potrebno pozvati odmah nakon pozivanja metoda dohvatača za neku kolonu. Ukoliko je vrednost za tu kolonu bila `NULL`, onda će metod
`wasNull` vratiti vrednost `true`, a inače će vratiti `false`. Napomenimo da se poziv `wasNull`
metoda vezuje samo za poslednji poziv metoda `getXXX`, te samim tim i na poslednju dohvaćenu kolonu. Zbog toga je potrebno više puta pozivati ovaj metod ukoliko ima više
potencijalno nedostajućih kolona:

```java
ResultSet res = ...

while (res.next()) {
    // Vrednosti u ovoj koloni ne mogu biti NULL,
    // pa nemamo ni proveru za nju.
    int kolona1_notnull = res.getInt(1);

    // Vrednosti u ovoj koloni mogu biti NULL,
    // pa zato odmah nakon dohvatanja vrednosti,
    // pozivamo metod wasNull() da proverimo da li je NULL.
    int kolona2_nullable = res.getInt(2);
    boolean kol2IsNull = res.wasNull();

    // Vrednosti u ovoj koloni mogu biti NULL,
    // pa zato odmah nakon dohvatanja vrednosti,
    // pozivamo metod wasNull() da proverimo da li je NULL.
    // Ovaj poziv metoda wasNull() se odnosi na kolonu 3, a ne na 2.
    Date kolona3_nullable = res.getDate(3);
    boolean kol3IsNull = res.wasNull();
    
    ...
}
```

{% include lab/exercise.html broj="8.5" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izdvaja ime, prezime i datum diplomiranja za sve studentkinje (pol = 'z') programa \v ciji je identifikator 202 iz tabele `DOSIJE`. Ukoliko datum diplomiranja nije poznat, ispisati `Nije diplomirala`." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_5/Main.java, java)

### 8.3.5 Podešavanje kursora

Objekat interfejsa `ResultSet` održava kursor koji pokazuje na red u rezultujućoj tabeli. Moguće je pomerati kursor na specifičan red kako bismo dohvatili podatke o tom redu ili izmenili vrednosti kolona. Kursor može da pokazuje na tačno jedan red u nekom trenutku. Red na koji kursor pokazuje se naziva _tekući red_ kursora. U zavisnosti od podešavanja kursora, moguće je pomerati kursor na različite načine.

Pre nego što se upustimo u konkretne primere, važno je da prodiskutujemo o narednim svojstvima svakog kursora:

- Usmerenost (eng. _scrollability_)
- Tipiziranost (eng. _concurrency_)
- Zadrživost (eng. _holdability_)

#### Usmerenost kursora

Usmerenost definiše operacije kojima kursor prolazi kroz redove. Podrazumevano, kursor je usmeren samo unapred, odnosno, kursorom je moguće prolaziti samo od prvog do poslednjeg reda. Kada dođemo do poslednjeg reda, onda se ne možemo vratiti unazad, već je potrebno ponovo da otvorimo kursor. Ipak, moguće je definisati kursor koji je usmeren unapred i unazad, odnosno, moguće je kretati se proizvoljno kroz redove rezultujuće tabele. Ovakav kursor se često naziva _dvosmerni kursor_.

Dvosmerni kursor ima još jedno svojstvo koje se naziva _osetljivost na izmene_ (eng. update sensitivity). Ovim svojstvom se definiše da li će se izmene u bazi podataka odraziti na redove rezultujuće tabele kroz koji se prolazi (otvorenim) dvosmernim kursorom. Kursor koji je osetljiv na izmene će prikazati izmene napravljene u bazi podataka, dok kursor koji nije osetljiv na izmene neće prikazati takve izmene. Naredne tri konstante interfejsa `ResultSet` se koriste za definisanje usmerenosti kursora:

- `ResultSet.TYPE_FORWARD_ONLY`: Dozvoljava kursoru da prolazi samo unapred kroz redove.
- `ResultSet.TYPE_SCROLL_SENSITIVE`: Kreira dvosmerni kursor koji je osetljiv na izmene.
- `ResultSet.TYPE_SCROLL_INSENSITIVE`: Kreira dvosmerni kursor koji nije osetljiv na izmene. Kada se kursor otvori, skup redova rezultujuće tabele neće biti promenjen dok ovakav kursor prolazi kroz te redove. Ako želimo da dohvatimo izmenjen skup redova, potrebno je da ponovo izvršimo upit (tj. da ponovo otvorimo kursor).

#### Tipiziranost kursora

Tipiziranost se odnosi na sposobnost kursora da ažurira podatke kroz koje prolazi. Podrazumevano, kursor koji se napravi je ograničen samo za čitanje i ne dozvoljava nam da ažuriramo redove rezultujuće tabele. Ako želimo da koristimo kursor da ažuriramo podatke nad bazom podataka, potrebno je da upit koji je pridružen kursoru vrati skup podataka koji se može ažurirati od strane JDBC drajvera. Naredne dve konstante interfejsa `ResultSet` se koriste za definisanje tipiziranosti kursora.

- `ResultSet.CONCUR_READ_ONLY`: Ograničava kursor samo za čitanje.
- `ResultSet.CONCUR_UPDATABLE`: Omogućava da kursor ažurira podatke. 

Važno je napomenuti da, ako pokušamo da napravimo kursor koji je ažurirajući, ali nije osetljiv na izmene (tj. koristimo kombinaciju konstanti `ResultSet.TYPE_SCROLL_INSENSITIVE` i `ResultSet.CONCUR_UPDATABLE`), JDBC drajver će implicitno ograničiti takav kursor samo za čitanje, tako da neće podržavati operacije unosa, brisanja i izmene. Pokušaj da se ovakve operacije izvrše nad takvim kursorom rezultovaće ispaljivanjem `SQLException` izuzetka.

#### Zadrživost kursora

Zadrživost kursora se odnosi na stanje kursora nakon potvrđivanja izmena transakcije u kojoj je taj kursor otvoren. Kursor se može ili zatvoriti ili otvoriti u trenutku potvrđivanja izmena transakcije. Podrazumevano ponašanje zavisi od JDBC drajvera i može se proveriti pozivom metoda `getHoldability()` nad objektom interfejsa `Connection`. Metod vraća jednu od naredne dve konstante, koje se takođe mogu koristiti i za eksplicitno definisanje zadrživosti na nivou pojedinačnih kursora:

- `ResultSet.HOLD_CURSORS_OVER_COMMIT`: Ostavlja kursor u otvorenom stanju nakon što se izmene potvrde u transakciji.
- `ResultSet.CLOSE_CURSORS_AT_COMMIT`: Zatvara kursor nakon što se izmene potvrde u transakciji.

#### Definisanje svojstava kursora

Da bismo definisali ponašanje `ResultSet` objekta, potrebno je da prosledimo odgovarajuće opcije metodima `createStatement`, odnosno, `prepareStatement`, koji su definisani nad objektom interfejsa `Connection`, u zavisnosti od toga da li želimo da naredbu izvršimo kroz interfejse `Statement` ili `PreparedStatement`, redom.

U slučaju kreiranja objekta `Statement`, na raspolaganju su nam naredna dva preopterećenja metoda `createStatement`:

- Metod sa dva argumenta:
   - `int resultSetType` — Usmerenost kursora.
   - `int resultSetConcurrency` — Tipiziranost kursora.
- Metod sa tri argumenta:
   - `int resultSetType` — Usmerenost kursora.
   - `int resultSetConcurrency` — Tipiziranost kursora.
   - `int resultSetHoldability` — Zadrživost kursora.

U slučaju kreiranja objekta `PreparedStatement`, na raspolaganju su nam naredna dva preopterećenja metoda `prepareStatement`, sa istim značenjima parametara kao i u slučaju metoda `createStatement`:

- Metod sa tri argumenta:
   - `String sql` — Niska koja sadrži tekstualni oblik SQL naredbe. Može sadržati nula ili više parametarskih oznaka.
   - `int resultSetType`
   - `int resultSetConcurrency`
- Metod sa četiri argumenta:
   - `String sql`
   - `int resultSetType`
   - `int resultSetConcurrency`
   - `int resultSetHoldability`

{% include lab/exercise.html broj="8.6" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje podatke o ispitnim rokovima koristeći kursor kome je omogućeno kretanje i unazad kroz podatke. Podatke urediti po nazivu rastuće, ali ih ispisivati opadajuće." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_6/Main.java, java)

### 8.3.6 Ažuriranje redova korišćenjem ResultSet kursora

Postoje dve procedure za ažuriranje informacija u bazi podataka na osnovu `ResultSet` kursora:

- Korišćenjem pozicionirajuće `UPDATE` naredbe.
- Korišćenjem JDBC metoda `updateXXX` i `updateRow`.

Kao što znamo, možemo koristiti pozicionirajuću `UPDATE` naredbu za menjanje podataka u bazi podataka, na osnovu tekućeg reda kursora, tako što se na kursor referiše u `WHERE CURRENT OF` klauzi naredbe. Da bismo dohvatili naziv `ResultSet` kursora, možemo iskoristiti metod `ResultSet.getCursorName`, koji vraća nisku sa nazivom kursora koji je vezan za taj `ResultSet`. Na primer, naredni fragment koda ilustruje kako možemo ažurirati broj bodova za sve predmete iz tabele `PREDMET` korišćenjem pozicionirajuće `UPDATE` naredbe:

```java
String upit =
    "SELECT OZNAKA, " +
    "       NAZIV, " + 
    "       ESPB " +
    "FROM   DA.PREDMET " +
    "FOR    UPDATE OF ESPB";

Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(upit);

String nazivKursora = rs.getCursorName();
String azuriranje =
    "UPDATE DA.PREDMET " +
    "SET    ESPB = ? " +
    "WHERE  CURRENT OF " + nazivKursora;
PreparedStatement ps = con.prepareStatement(nazivKursora);

try (Scanner scanner = new Scanner(System.in)) {
    while (rs.next()) {
        System.out.println("Unesite nove ESPB bodove za predmet " + rs.getString(2));
        int newESPB = scanner.nextInt();

        ps.setInt(1, newESPB);
        ps.executeUpdate();
    }
}
```

Drugi pristup podrazumeva kori\v s\'cenje specifi\v cnih metoda radi a\v zuriranja podataka u kursoru, \v cime se efektivno vr\v si a\v zuriranje u bazi podataka. 

{% include lab/begin-recipe.html naziv="A\v zuriranje slogova pomo\'cu a\v zuriraju\'cih kursora" %}

Naredni koraci opisuju postupak a\v zuriranja postoje\'cih slogova u `ResultSet`:

1. Kreirati objekat interfejsa `Statement` pozivom metoda 
```java
Statement stmt = con.createStatement(..., ResultSet.CONCUR_UPDATABLE, ...);
```
2. Kreirati objekat interfejsa `ResultSet` pozivom metoda 
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Pozicionirati se na slog koji je potrebno ažurirati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    kursor.absolute(5); // Samo ako kursor nije tipa TYPE_FORWARD_ONLY
    // ...
}
```
4. Pozvati odgovarajući metod iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)` za ažuriranje vrednosti kolone tabele sa indeksom `columnIndex` novom vrednošću `x`.
```java
kursor.updateInt(1, 100);
```
   1. Ako želimo da poništimo izmene, možemo pozvati metod `kursor.cancelRowUpdates()`
   1. Ukoliko želimo da zapravo izvršimo izmene nad slogom u BP, potrebno je da pozovemo metod `ResultSet.updateRow()`. U suprotnom će, prelaskom na drugi red, sve izmene biti ignorisane.
```java
kursor.updateRow();
```
7. Nakon svih iteracija, zatvoriti kursor.
```java
kursor.close();
```
8. Nakon zatvaranja kursora, zatvoriti naredbu.
```java
stmt.close();
```

{% include lab/end-recipe.html %}

Pogledajmo primer upotrebe u narednom zadatku.

{% include lab/exercise.html broj="8.7" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje sadržaj tabele
`PREDMET` i, u istoj iteraciji, ukoliko je broj bodova jednak X, postavlja se broj bodova na
Y i ispisuje se poruka da je promena izvršena, zajedno sa ispisom novih podataka o
tom predmetu. Brojevi X i Y se učitavaju sa standardnog ulaza." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_7/Main.java, java)

### 8.3.7 Unos redova korišćenjem ResultSet kursora

Ono \v sto nismo imali prilike da vidimo jeste da se pomo\'cu kursora mogu uneti novi slogovi u tabelu. Za unos novog sloga u tabelu koristi se specijalni "slog za unos". Ovaj specijalni slog zapravo predstavlja bafer za konstruisanje novog sloga koji \'ce biti unet u odgovaraju\'cu tabelu.

Konstrukcija novog sloga se vrši u dva koraka:
1. Potrebno je postaviti vrednosti svih kolona rezultujuće tabele u "slogu za unos" pozivom odgovarajućih metoda iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)`
2. Kada su svim kolonama u "slogu za unos" dodeljene odgovaraju\'ce vrednosti, tada je potrebno izvr\v siti unos baferisanog "sloga za unos" u rezultujuću tabelu.

Unos baferisanog "slogu za unos" se izvr\v sava pozivom metoda `void ResultSet.insertRow()`. Me\dj utim, da bismo zapravo uspeli da konstrui\v semo ovaj specijalni slog, neophodno je da signaliziramo kursoru da \v zelimo da menjamo ba\v s taj red, a ne teku\'ci red kroz koji se iterira u kursoru. Ovo je mogu\'ce uraditi pozivom metoda `void ResultSet.moveToInsertRow()`. Prilikom poziva ovog metoda, kursor se pozicionira na "slog za unos" i ujedno se pamti redni broj tekućeg sloga koji se obrađuje u kursoru. Nakon \v sto smo zavr\v sili proceduru za unos opisanu prethodnim koracima, potrebno je pozvati metod `void ResultSet.moveToCurrentRow()` koji \'ce ponovo pozicionirati kursor na prethodno zapamćeni tekući slog koji se obrađuje u kursoru.

{% include lab/begin-recipe.html naziv="Unos slogova pomo\'cu a\v zuriraju\'cih kursora" %}

Celokupna procedura za unos novog sloga je data narednim koracima:

1. Kreirati objekat interfejsa `Statement` za rad sa a\v zuriraju\'cim kursorima. 
```java
Statement stmt = con.createStatement(..., ResultSet.CONCUR_UPDATABLE, ...);
```
2. Kreirati objekat interfejsa `ResultSet`. 
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Otvoriti kursor i pozicionirati se na "slog za unos". 
```java
while (kursor.next()) {
    kursor.moveToInsertRow();
    // ...
}
```
4. Pozvati odgovarajući metod iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)` za postavljanje vrednosti kolone "sloga za unos" sa indeksom `columnIndex` novom vrednošću `x`. Na primer:
```java
kursor.updateInt(1, 100);
```
5. Ukoliko želimo da zapravo unesemo novi slog u BP, potrebno je da pozovemo metod `ResultSet.insertRow()`. U suprotnom će, prelaskom na drugi red, sve izmene biti ignorisane.
```java
kursor.insertRow();
```
6. Pozicionirati se na tekući slog u rezultujućoj tabeli kursora pozivom metoda `ResultSet.moveToCurrentRow()`.
```java
kursor.moveToCurrentRow();
```
7. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora.
8. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe.

{% include lab/end-recipe.html %}

Naredni zadatak ilustruje upotrebu opisane procedure.

{% include lab/exercise.html broj="8.8" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji:
1. Kreira tabelu `UNETIPREDMETI` \v cije su kolone: (1) identifikator predmeta i (2) broj polo\v zenih ispita za taj predmet. Postaviti odgovaraju\'ce primarne i strane klju\v ceve.
2. Za svaki predmet koji nije prethodno obra\dj en (tj. koji se ne nalazi u tabeli `UNETIPREDMETI`) pronalazi statistiku koja se sastoji od njegovog identifikator i broj polo\v zenih ispita.
3. Za svaku prona\dj enu statistiku ispisuje podatke na standardni izlaz i pita korisnika da li \v zeli da unete statistiku u tabelu `UNETIPREDMETI`. Ukoliko korisnik potvrdi, potrebno je uneti statistiku u datu tabelu i ispisati poruku o uspehu. U suprotnom, ispisati poruku da je korisnik poni\v stio unos." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_8/Main.java, java)

### 8.3.8 Brisanje redova korišćenjem ResultSet kursora

Poput ažuriranja podataka, i za brisanje podataka postoje dve procedure:

- Korišćenjem pozicionirajuće `DELETE` naredbe.
- Korišćenjem JDBC metoda `deleteRow`.

Kao što znamo, možemo koristiti pozicionirajuću DELETE naredbu za brisanje podataka iz
baze podataka, na osnovu tekućeg reda kursora, tako što se na kursor referiše u `WHERE CURRENT OF` klauzi naredbe. Procedura za dohvatanje naziva kursora i izvršavanje pozicionirajuće `DELETE` naredbe je ekvivalentna prethodno opisanoj proceduri za ažuriranje podataka korišćenjem pozicionirajuće `UPDATE` naredbe, te ćemo samo prikazati fragment koda koji ilustruje ovu proceduru:

```java
String upit =
    "SELECT OZNAKA, " +
    "       NAZIV, " + 
    "       ESPB " +
    "FROM   DA.PREDMET";

Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(upit);

String nazivKursora = rs.getCursorName();
String brisanje =
    "DELETE FROM DA.PREDMET " +
    "WHERE  CURRENT OF " + nazivKursora;
Statement ps = con.prepareStatement();

while (rs.next()) {
    String nazivPredmeta = rs.getString(2);

    ps.executeUpdate(brisanje);
    System.out.println("Obrisan je predmet " + nazivPredmeta);
}
```

Brisanje sloga iz objekta `ResultSet` je jednostavnije nego a\v zuriranje ili unos sloga. 

{% include lab/begin-recipe.html naziv="Brisanje slogova pomo\'cu a\v zuriraju\'cih kursora" %}

Naredni koraci defini\v su operaciju brisanja sloga iz kursora:

1. Kreirati objekat interfejsa `Statement` za rad sa bri\v su\'cim kursorima.
```java
Statement stmt = con.createStatement(..., ResultSet.CONCUR_UPDATABLE, ...);
```
2. Kreirati objekat interfejsa `ResultSet`.
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Pozicionirati se na slog koji je potrebno obrisati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    kursor.absolute(5); // Samo ako kursor nije tipa TYPE_FORWARD_ONLY
    // ...
}
```
4. Pozvati metod `kursor.deleteRow()` koji će obrisati slog u bazi podataka
5. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora
6. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

{% include lab/end-recipe.html %}

Metod `deleteRow()` bri\v se onaj red na koji je `ResultSet` pozicioniran. Ne postoji na\v cin
da se operacija brisanja poni\v sti (osim poni\v stavanjem transakcije, o \v cemu \'ce biti 
vi\v se re\v ci u narednom poglavlju).

{% include lab/exercise.html broj="8.9" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji briše sve nepoložene ispite u \v skolskoj godini koja se zadaje sa standarnog ulaza. Nakon svakog brisanja ispita, ispisati naredne informacije o njemu na standardni izlaz: indeks, oznaku roka, \v skolsku godinu i identifikator predmeta." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_9/Main.java, java)

### 8.3.9 Ugnežđeni kursori

U JDBC aplikacijama se kursori jednostavno ugnežđuju: potrebno je celokupnu obradu
unutrašnjeg kursora smestiti u okviru obrade jednog reda iz spoljašnjeg kursora. Naredni
primer ilustruje upotrebu ugnežđenih kursora.

{% include lab/exercise.html broj="8.10" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izdvaja indeks, ime, prezime i naziv studijskog programa svih studenata koji su položili tačno N predmeta, kao i spisak tih predmeta (naziv i ocena). Broj N se učitava sa standardnog ulaza. Za svakog studenta napraviti posebnu sekciju izveštaja." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/ispiti.sql, sql)
include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/predmeti.sql, sql)
include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/Main.java, java)

## 8.4 Zadaci za vežbu

{% include lab/exercise.html broj="8.11" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje podatke za predmet čiji je identifikator `2027`." %}

{% include lab/exercise.html broj="8.12" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje broj studenata koji su upisali studije u \v skolskoj godini koja se unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.13" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji za svaku ocenu od 6 do 10 ispisuje ime i prezime studenta koji je poslednji položio neki ispit sa tom ocenom. U slučaju da ima više takvih studenata, klauzom `LIMIT 1` naredbe `SELECT INTO` se osigurati da bude vra\' cen najviše 1 red. (Pomoć: Koristiti `for` petlju za menjanje vrednosti matične promenljive koja sadrži ocenu, pa u svakoj iteraciji dohvatiti informaciju za tekuću vrednost te matične promenljive.)" %}

{% include lab/exercise.html broj="8.14" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se dodaje da je za polaganje predmeta čiji je identifikator `2327` uslov da se položi predmet čiji je identifikator `1588`." %}

{% include lab/exercise.html broj="8.15" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se u tabelu `NIVOKVALIFIKACIJE` dodaje novi nivo čiji se identifikator i naziv." %}

{% include lab/exercise.html broj="8.16" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se, za sve položene ispite čiji se naziv predmeta unosi sa standardnog ulaza, ocena uvećava za 1." %}

{% include lab/exercise.html broj="8.17" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se u tabeli `USLOVNIPREDMET` brišu sve uslovnosti za predmet čiji se identifikator unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.18" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se brišu svi podaci o ispitima za studenta čiji se indeks unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.19" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji iz tabele `PREDMET` briše podatak o predmetu čija se oznaka unosi sa standardnog ulaza. " %}
