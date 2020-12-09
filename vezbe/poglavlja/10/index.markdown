---
layout: page
title: 10. Uvod u razvojno okruženje Hibernate
under_construction: true
---

Prilikom izrade aplikacija, Java programeri se oslanjaju na objektno-orijentisane koncepte
koji, kako im i samo ime kaže, počivaju na upotrebi objekata (i naravno, ostalih koncepata
koji se dalje zasnivaju na njima, poput klase, učauravanja, interfejsi i drugi). Ovi objekti
modeliraju poslovnu logiku iz realnog sveta, definisanu od strane naručilaca proizvoda.
Podaci u memoriji, sa kojima aplikacija upravlja, nisu korisni ukoliko se ne mogu negde
trajno skladištiti. Dodatno, veliki broj podataka počiva upravo iz nekih skladišta podataka,
te je potrebno da aplikacije pristupaju takvim izvorima informacija. Tradicionalno,
ali i dalje u ogromnoj meri, ovi podaci se zapisuju u relacionim bazama podataka zbog
različitih prednosti koje su nam poznate iz dobro razrađene teorije relacionog računa na
kojima ove baze podataka počivaju.

Ovim se otvara naredno pitanje — ako Java aplikacije rade sa podacima u memoriji koji su
zapisani kao objekti, a naši podaci od interesa se skladište u relacionim bazama podataka
koji su zapisani u tabelama, da li je moguće dizajnirati sistem koji će automatski izvršiti
prevođenje podataka iz jednog oblika u drugi i obrnuto? Ovaj problem se naziva problem
objektno-relacionog preslikavanja (engl. Object-Relation Mapping problem) i odgovor na
pitanje je da može. U ovom poglavlju biće predstavljeno okruženje za razvoj Hibernate,
koje omogućava Java programerima da u svojim aplikacijama implementiraju poslovnu
logiku, dok se operacije niskog nivoa, kao što su čitanje, skladištenje, brisanje i menjanje
podataka, izvršavaju u pozadini, čime se veliki deo posla olakšava.

In this chapter, we’ll think of the problems of data storage and sharing in the context of
an object-oriented application that uses a domain model. Instead of directly working
with the rows and columns of a java.sql.ResultSet, the business logic of an application interacts with the application-specific object-oriented domain model. If the SQL
database schema of an online auction system has ITEM and BID tables, for example,
the Java application defines Item and Bid classes. Instead of reading and writing the
value of a particular row and column with the ResultSet API, the application loads
and stores instances of Item and Bid classes. 

At runtime, the application therefore operates with instances of these classes.
Each instance of a Bid has a reference to an auction Item, and each Item may have a
collection of references to Bid instances. The business logic isn’t executed in the
database (as an SQL stored procedure); it’s implemented in Java and executed in the
application tier. This allows business logic to use sophisticated object-oriented concepts such as inheritance and polymorphism. 

Upotreba Hibernate okruženja za razvoj će biti prikazana kroz jedan veći primer, koji će
biti izrađen deo-po-deo. Ovo znači da, pored toga što će se u projektu povećavati broj
klasa sa (gotovo) svakim zahtevom, i same klase će biti proširivane kako bi zadovoljile sve
zahteve.

Cilj ovog poglavlja jeste razvoj aplikacije koja ispunjava naredne zahteve (svi zahtevi se
implementiraju nad poznatom bazom podataka `VSTUD`):

1. Unos podataka o novom smeru u tabelu `SMER` sa narednim podacima:

| Kolona | Vrednost |
| --- | --- |
| Identifikator | 300 |
| Oznaka | MATF_2019 |
| Naziv | Novi MATF smer u 2019. godini |
| Broj semestara | 8 | 
| Broj bodova  | 240 | 
| Zvanje |  Diplomirani informaticar | 
| Opis  | Novi smer na Matematickom fakultetu | 

{:start="2"}
2. Čitanje podataka o prethodno unetom smeru iz tabele `SMER`.
3. Ažuriranje podataka o prethodno unetom smeru iz tabele `SMER`. 
4. Brisanje podataka o prethodno unetom smeru iz tabele `SMER`.
5. Unos podataka o novom ispitnom roku (jun 2019. godine) u tabelu `ISPITNI_ROK`.
6. Brisanje podataka o prethodno unetom ispitnom roku iz tabele `ISPITNI_ROK`.
7. Ispisivanje podataka o svim ispitnim rokovima.
8. Ispisivanje podataka o ispitnom roku čija se oznaka roka i godina roka unose sa standarnog ulaza.
9. Ispisivanje naziva svih smerova. Nakon svakog naziva smera, ispisuju se indeks, ime i prezime svih studenata na tom smeru. Dopuniti ispis o studentu tako da se za svakog studenta ispisuje i prosek.
10. Za zadati indeks studenta ispisati nazive svih položenih predmeta i dobijene ocene. Implementirati dva metoda — jedan koji ne koristi Hibernate JPA Criteria API i drugi koji ga koristi.

Takođe, omogućiti da obrada svakog zahteva predstavlja zasebnu transakciju.

## 10.1 Podešavanje Hibernate projekta

Hibernate biblioteka se veoma jednostavno instalira. Sa veze [https://hibernate.org/orm/](https://hibernate.org/orm/) potrebno je preuzeti odgovaraju\'cu verziju biblioteke. Mi \'cemo raditi sa poslednjom stabilnom verzijom, a to je verzija 5.4 koja se mo\v ze preuzeti klikom na dugme _Download Zip archive_ sa [ove veze](https://hibernate.org/orm/releases/5.4/). Preuzetu arhivu je potrebno otpakovati na neku lokaciju. Na virtualnoj ma\v sini "Baze podataka 2019", ta lokacija je `/opt/hibernate-5.4.10/`. Dodatno, Java projekat koji budemo kreirali mora da sadr\v zi informaciju i o implementaciji JDBC drajvera za DB2. Pode\v savanje Java projekta sa podr\v skom za razvoj Hibernate aplikacija se vr\v si narednim koracima:

1. Otvoriti IBM Data Studio.
2. Iz glavnog menija odabrati _Window_ -> _Perspective_ -> _Open Perspective_ -> _Other_ -> _Java_ -> _OK_
3. Iz glavnog menija odabrati _File_ -> _New_ -> _Java Project_

   1. U polje _Project name_ uneti naziv Java projekta: `poglavlje_10`.
   2. Iz padaju\'ce liste u ta\v cki _Use an execution environment JRE_ odabrati _JavaSE-1.8_.
   3. Odabrati _Finish_.

4. Desni klik na projekat koji je napravljen u _Package Explorer_ -> _Properties_

   1. Odabrati iz leve liste tab _Java Build Path_.
   2. Odabrati iz gornje liste karticu _Libraries_.
   3. Podesiti podr\v sku za JDBC:

      1. Odabrati _Add External JARS_.
      2. U prozoru koji se otvori odabrati: _Other Locations_ -> _Computer_ -> _opt_ -> _ibm_ -> _db2_ -> _V11.5_ -> _java_.
      3. Sa ove lokacije ozna\v citi datoteke _db2jcc4.jar_ i _db2jcc_licence_cu.jar_, pa odabrati _OK_.

   4. Podesiti podr\v sku za Hibernate:

      1. Odabrati _Add Library_.
      2. U prozoru koji se otvori odabrati: _User Library_ -> _Next_ -> _User Libraries_ -> _New_ -> Uneti naziv _Hibernate_ -> _OK_.
      3. Sada je potrebno dodati sve pakete koji se nalaze u poddirektorijumima `envers`, `jpa-metamodel-generator`, `osgi` i `required` na putanji `/opt/hibernate-5.4.10/lib/`. Postupak \'ce biti opisan za direktorijum `envers`, a Vi treba da ga ponovite korak-po-korak za sve ostale navedene direktorijume.

         1. Kliknuti na napravljenu biblioteku _Hibernate_ (da bi Vam bilo omogu\'ceno dugme _Add External JARS_).
         2. Odabrati _Add External JARS_.
         3. U prozoru koji se otvori odabrati: _Other Locations_ -> _Computer_ -> _opt_ -> _hibernate-5.4.10_ -> _lib_ -> _envers_.
         4. Sa ove lokacije ozna\v citi sve datoteke, pa odabrati _OK_.

       4. Kada su svi potrebni paketi uklju\v ceni, odabrati _OK_ -> _Finish_ -> _OK_.

   5. Ovim ste uspe\v sno dodali podr\v sku za JDBC i Hibernate aplikacije u Va\v sem projektu.

## 10.2 Podešavanje konekcije na bazu podataka

To create a connection to the database, Hibernate must know the details of our database,
tables, classes, and other mechanics. This information is ideally provided as an XML file
(usually named `hibernate.cfg.xml`) or as a simple text file with name/value pairs (usually
named `hibernate.properties`).

For this exercise, we use XML style. We name this file `hibernate.cfg.xml` so the framework
can load this file automatically.

The following snippet describes such a configuration file. Because we are using IBM DB2
as the database, the connection details for the IBM DB2 database are declared in this
`hibernate.cfg.xml` file.

Da bismo napravili ovu datoteku, potrebno je da desnim klikom na naziv projekta otvorimo
padajući meni iz kojeg biramo _New_ -> _Other_ i onda pronađemo _XML File_ iz filtera
_XML_. Klikom na _Next_, potrebno je da unesemo naziv datoteke — u ovom slučaju, to je
_hibernate.cfg.xml_, odaberemo da se datoteka smesti u direktorijum _src_ i odaberemo
_Finish_.

Ova datoteka će se automatski otvoriti u XML pregledaču, ali je nama neophodno da
bude otvorena kao tekstualna datoteka. Ovo se može uraditi desnim klikom na naziv
datoteke `hibernate.cfg.xml` u _Package Explorer_ pogledu, a zatim biranjem 
_Open with_ -> _Text Editor_. U ovu datoteku je potrebno smestiti sledeće:

include_source(vezbe/primeri/poglavlje_10/src/hibernate.cfg.xml, xml)

This file has enough information to get a live connection to an IBM DB2 database.

The preceding properties can also be expressed as name/value pairs. For example, here’s
the same information represented as name/value pairs in a text file titled `hibernate.properties`:

```
hibernate.connection.driver_class = com.ibm.db2.jcc.DB2Driver
hibernate.dialect = org.hibernate.dialect.DB2Dialect
hibernate.connection.url = jdbc:db2://localhost:50001/VSTUD
hibernate.connection.username = student
hibernate.connection.password = abcdef
```

Property `connection.url` indicates the URL to which we should be connected; `driver_class`
represents the relevant `Driver` class to make a connection, and the `dialect` indicates which
database dialect we are using (IBM DB2, in this case). Similarly, `connection.username`
and `connection.password` specifies the username and password for connecting to a database.

If you are following the `hibernate.properties` file approach, note that all the properties
are prefixed with "`hibernate`" and follow a pattern — `hibernate.* = value`.

## 10.3 Fabrika sesija

Pre nego što pređemo na implementiranje klasa i definisanje preslikavanja između tabela
u bazi podataka i klasa u programskom jeziku Java, pogledajmo kako se može kreirati
jednostavan klijent koji \'ce koristiti informacije iz `hibernate.cfg.xml` datoteke
za kreiranje konekcije ka bazi podataka.

Hibernate’s native bootstrap API is split into several stages, each giving you access
to certain configuration aspects. Building a `SessionFactory` looks like this: 

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_1/HibernateUtil.java, java)

First, create a `StandardServiceRegistry`: 

```java
StandardServiceRegistry registry = 
        new StandardServiceRegistryBuilder()
            .configure()
            .build();
```

`StandardServiceRegistryBuilder` helps you create the immutable service registry with chained method calls. Configure the services registry by calling the method `configure` on it. Finally, call the method `build` to create said service registry.

With the `StandardServiceRegistry` built and immutable, you can move on to the next
stage: telling Hibernate which persistent classes are part of your mapping metadata.
Configure the metadata sources as follows:

```java
MetadataSources metadataSources = new MetadataSources(serviceRegistry);
metadataSources.addAnnotatedClass(Smer.class);
MetadataBuilder metadataBuilder = metadataSources.getMetadataBuilder();
```

The `MetadataSources` API has many methods for adding mapping sources; check the
Javadoc for more information. The next stage of the boot procedure is building all
the metadata needed by Hibernate, with the `MetadataBuilder` you obtained from the
metadata sources. 

You can then query the metadata to interact with Hibernate’s completed configuration programmatically, or continue and build the final `SessionFactory`: 

```java
Metadata metadata = metadataBuilder.build();
SessionFactory sessionFactory = metadata.buildSessionFactory();
```

This builder helps you create the immutable service registry with chained method calls.

Opisani kod stavljamo u stati\v cki blok klase `HibernateUtil` kako bi se on izvr\v sio prvi put kada se ova klasa bude koristila. U na\v sem slu\v caju, to podrazumeva prvi put kada instanca fabrike sesija bude bila iskori\v s\'cena u `main` funkciji na\v se aplikacije.

Note that we don’t have to explicitly mention the mapping or configuration or properties
files, because the Hibernate runtime looks for default filenames, such as `hibernate.cfg.xml`
or `hibernate.properties`, in the classpath and loads them. If we have a nondefault
name, make sure you pass that as an argument — like `configure("my-hib-cfg.xml")`, for example.

## 10.4 Objektno-relaciono preslikavanje jedne klase

{% include lab/exercise.html broj="10.1" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate redom: \n
\n
1. unosi podatak o novom smeru u tabeli `SMER` sa podacima iz naredne tabele,\n
2. čita podatke o smeru sa identifikatorom `300` iz tabele `SMER`,\n
3. ažurira podatke o smeru sa identifikatorom `300` iz tabele `SMER`,\n
4. čita podatke o smeru sa identifikatorom `300` iz tabele `SMER`, \n
5. briše podatke o smeru sa identifikatorom `300` iz tabele `SMER`,\n
6. čita podatke o smeru sa identifikatorom `300` iz tabele `SMER`." %}

| Kolona | Vrednost |
| --- | --- |
| Identifikator | 300 |
| Oznaka | MATF_2019 |
| Naziv | Novi MATF smer u 2019. godini |
| Broj semestara | 8 | 
| Broj bodova  | 240 | 
| Zvanje |  Diplomirani informaticar | 
| Opis  | Novi smer na Matematickom fakultetu | 

Re\v senje: Da bismo rešili ovaj zadatak, potrebno je da kreiramo klasu `Smer` i da joj dodamo svojstva koja odgovaraju kolonama u tabeli `SMER`:

```java
package zadatak_10_1;

class Smer {
    private int id_smera;
    private String Oznaka;
    private String Naziv;
    private int Semestara;
    private int Bodovi;
    private Integer Nivo;
    private String Zvanje;
    private String Opis;
}
```

Primetimo da je klasa `Smer` definisana na nivou vidljivosti paketa. Za ovo smo se opredelili zbog toga \v sto \'ce implementacija svakog novog zahteva zahtevati novi paket u projektu `poglavlje_10`. Trenutne klase koje smo napisali - `HibernateUtil` i `Smer` - \v cuvaju se u paketu `zadatak_10_1` i va\v ze samo za njega, tako da nema potrebe da budu javne vidljivosti.

Sada je potrebno da definišemo objektno-relaciono preslikavanje između klase `Smer` i tabele `SMER` u bazi podataka.

U Hibernate radnom okviru je moguće korišćenje dva pristupa za definisanje preslikavanja:

1. Korišćenjem XML datoteka — Za svaku tabelu se definiše XML datoteka koja je struktuirana na odgovarajući način i koristi XML elemente i njihove atribute za definisanje preslikavanja.

2. Korišćenjem Java anotacija — Koriste se Java konstrukti oblika  `@NazivAnotacije` i njihova svojstva za definisanje preslikavanja.

Mi ćemo u daljem tekstu koristiti pristup zasnovan na Java anotacijama.

Hibernate uses the Java Persistence API (JPA) annotations. JPA is the standard specification dictating the persistence of Java objects. So the preceding annotations are imported from the `javax.persistence` package.

Each persistent object is tagged (at a class level) with an `@Entity` annotation. The `@Table` annotation declares our database table where these entities will be stored. Ideally, we should not have to provide the `@Table` annotation if the name of the class and the table name are the same (in our example, the class is `Smer`, whereas the table name is `SMER`, which is fine):

```java
@Entity
@Table(name = "SMER")
class Smer {
    ...
```

Sada je potrebno da definišemo preslikavanje kolona. All persistent entities must have their identifiers defined. The `@Id` annotation indicates that the variable is the unique identifier of the object instance (in other words, a primary key). When we annotate the id_smera variable with the `@Id` annotation, as in the preceding example, Hibernate maps a field called `id_smera` from our table `Smer` to the `id_smera` variable on the `Smer` class:

```java
@Entity
@Table(name = "SMER")
class Smer {
    @Id
    private int id_smera;
    ...
```

If your variable doesn’t match the column name, you must specify the column name using the `@Column` annotation. Dodatno, ukoliko kolona ne može imati `NULL` vrednosti u bazi podataka, potrebno je postaviti još i svojstvo `nullable` na vrednost `false` u anotaciji `@Column`:

```java
@Entity
@Table(name = "SMER")
class Smer {
    @Id
    private int id_smera;

    @Column(name = "oznaka", nullable = false)
    private String Oznaka;

    @Column(name = "naziv", nullable = false)
    private String Naziv;

    @Column(name = "semestara", nullable = false)
    private Integer Semestara;

    @Column(name = "bodovi", nullable = false)
    private Integer Bodovi;

    @Column(name = "id_nivoa", nullable = false)
    private Integer Nivo;

    @Column(name = "zvanje", nullable = false)
    private String Zvanje;

    @Column(name = "opis", nullable = true)
    private String Opis;
    ...
```

Takođe, s obzirom da su ova svojstva deklarisana modifikatorom `private`, potrebno je implementirati metode za postavljanje i dohvatanje njihovih vrednosti. U ovu svrhu, može nam pomoći alat IBM Data Studio: 

- Desni klik na prazan deo koda -> _Source_ -> _Generate Getters and Setters_ -> _Select All_ -> _OK_

Ovim će nam biti generisane odgovarajuće metode. Možete istražiti sve mogućnosti ove opcije, kao što su generisanje samo postavljačkih ili dohvatačkih metoda, generisanje metoda samo za neka svojstva i drugo.

Cela implementacija klase `Smer` data je u nastavku.

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_1/Smer.java, java)

## 10.5 Unos jednog sloga

Napišimo sada i klasu `Main` koja će sadržati statički metod `main` u kojem ćemo testirati rad naše aplikacije. Trenutno, metod `main` će uraditi dve stvari: prva je pozivanje statičke funkcije za unos novog smera, a druga je zatvaranje fabrike sesija:

```java
package zadatak_10_1;

import org.hibernate.Session;
import org.hibernate.Transaction;

class Main {
    
    public static void main(String[] args) {
        System.out.println("Pocetak rada...\n");
        
        insertSmer();
        
        System.out.println("Zavrsetak rada.\n");
        
        // Zatvaranje fabrike sesija
        HibernateUtil.getSessionFactory().close();
    }
    
    ...
```

Sada prelazimo na implementaciju metoda `insertSmer` koji treba da unese novi red u tabelu `Smer`. 

Sve akcije nad bazom podataka se izvršavaju u okviru tzv. _sesija_ (engl. _session_). Da bismo mogli da radimo sa bazom podataka, potrebno je da otvorimo novu sesiju, što nam je omogućeno metodom `openSession` iz klase `SessionFactory`.

Sledeći korak jeste kreiranje objekta klase `Smer` i postavljanje odgovarajućih vrednosti. Na ovaj način smo podatke smestili u memoriju računara. Ono što je potrebno uraditi da bi se oni trajno skladi\v stili u bazu podataka jeste pozvati metod `save` nad objektom sesije (tj. instancom klase `Session` koju smo dobili pozivom `openSession`). Na kraju, potrebno je da zatvorimo sesiju. Kod bi mogao da izgleda kao u nastavku:

```java
private static void insertSmer() {
    Session session = HibernateUtil.getSessionFactory().openSession();
    Smer smer = new Smer();
    
    smer.setId_smera(300);
    smer.setOznaka("MATF_2019");
    smer.setNaziv("Novi MATF smer u 2019. godini");
    smer.setSemestara(8);
    smer.setBodovi(240);
    smer.setNivo(110);
    smer.setZvanje("Diplomirani informaticar");
    smer.setOpis("Novi smer na Matematickom fakultetu");
    
    session.save(smer);
    System.out.println("Smer je sacuvan");
    
    session.close();
}
```

Iz ovog dela koda vidimo koliko je jednostavno trajno skladištiti podatke u bazu podataka, koji su se nalazili u memoriji računara. Ipak, postoji još jedna stvar kojom treba dopuniti prethodni kod.

## 10.6 Implementacija transakcija

Napomenuli smo na početku poglavlja da aplikacija koju budemo kreirali mora da radi tako da svaki zahtev predstavlja jednu transakciju. Ono što je dobra vest jeste da je definisanje transakcija u Hibernate radnom okviru veoma jednostavna procedura. Pogledajmo naredni kod:

```java
Session session = HibernateUtil.getSessionFactory().openSession();
Smer smer = new Smer();

// Postavljanje odgovarajucih vrednosti za smer ide ovde ...

Transaction TR = null;
try {
    TR = session.beginTransaction();
    
    session.save(smer);
    
    TR.commit();
    System.out.println("Smer je sacuvan");
} catch (Exception e) {
    System.out.println("Cuvanje smera nije uspelo! Transakcija se ponistava!");
    
    if (TR != null) {
        TR.rollback();
    }
} finally {
    session.close();
}
```

Transakcije se u Hibernate radnom okviru predstavljaju klasom `Transaction`. We initiate a transaction by invoking the `session.beginTrasaction()` method, which creates a new `Transaction` object and returns the reference to us. It gets associated with the session and is open until that transaction is committed or rolled back.

We perform the required work in a transaction, then issue a commit on this transaction. At this stage, the entities are persisted to the database. While persisting, if for whatever reason there are any errors, the Hibernate runtime will catch and throw a `HibernateException` (which is an unchecked `RuntimeException`). We then have to catch the exception and roll back the transaction.

## 10.7 Dohvatanje jednog sloga

S obzirom da smo u prethodnom primeru postavili sva preslikavanja i pripremne korake,
ispostaviće se da se dohvatanje jednog sloga iz baze podataka sastoji u jednostavnom
pozivanju odgovarajućih metoda. Nakon što dohvatimo jedan slog, možemo ga menjati ili
obrisati, o čemu će biti reči u narednoj sekciji.

Ukoliko je potrebno da pronađemo (dohvatimo) red iz tabele sa odgovarajućim primarnim
ključem, na raspolaganju nam je metod `load()` definisan nad objektima klase `Session`.
Postoji više preopterećenja ovog metoda, a ono koje ćemo mi koristiti jeste potpisa:

```java
public void load(Object object, Serializable id)
```

Prvi argument ovog metoda bi trebalo da bude prazna instanca klase reda koji želimo da
učitamo, a drugi argument je instanca primarnog ključa. Nakon izvršavanja ovog metoda,
objekat object će biti popunjen vrednostima iz kolona reda čiji je primarni ključ zadat sa
`id`.

Ukoliko nismo sigurni da slog sa zadatim primarnim ključem postoji u tabeli, nije dobro
koristiti metod `load()`. U tom slučaju, bolje je koristiti metod `get()`. Razlika je u tome
što, ukoliko primarni ključ nije pronađen u bazi podataka, metod `load()` će izbaciti izuzetak,
dok će metod `get()` vratiti null referencu, što je lakše za korišćenje. Ipak, njegova
upotreba je nešto drugačija jer su nam na raspolaganju naredna dva preopterećenja:

```java
public <T> T get(Class<T> clazz, Serializable id)
public Object get(String entityName, Serializable id)
```

Prvi od njih je šablonskog tipa i koristi klasu da odredi iz koje tabele dohvata slog, a drugi
koristi naziv entiteta, koji je moguće dobiti poziv metoda `getEntityName()` nad objektom
klase `Session`:

```java
public String getEntityName(Object object)
```

Slede primeri kodova koji koriste metod `get()`:

```java
// Get an id from some other Java class,
// for instance, through a web application
Supplier supplier = session.get(Supplier.class, id);
if (supplier == null) {
    System.out.println("Supplier not found for id " + id);
    return;
}

// ili...

String entityName = session.getEntityName(supplier);
Supplier secondarySupplier =
        (Supplier) session.load(entityName, id);
```

## 10.8 Brisanje jednog sloga

Brisanje slogova iz baze podataka se jednostavno vrši pozivanjem metoda `delete()` nad
objektom klase `Session`:

```java
public void delete(Object object)
```

Argument ovog metoda je trajni objekat. Naravno, postoje i složeniji metodi brisanja
podataka.

Cela implementacija klase `Main` data je u nastavku.

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_1/Main.java, java)

## 10.9 Složeni ključ

Upravljanje tabelama koje imaju jednostavne primarne ključeve, kao što je to tabela `SMER`,
vrlo je jednostavno. Nešto složenije je rukovati tabelom čiji se primarni ključ sastoji od
nekoliko kolona. Takvi primarni ključevi se nazivaju _složeni ključevi_ (engl. _compound primary key_).

You must create a class to represent this primary key. It will not require a primary key
of its own, of course, but it must be visible to entity class, must have a default constructor,
must be serializable, and must implement `hashCode()` and `equals()` methods to allow the
Hibernate code to test for primary key collisions (i.e., they must be implemented with the
appropriate database semantics for the primary key values).

Your three strategies for using this primary key class once it has been created are as
follows:

1. Mark it as `@Embeddable` and add to your entity class a normal property for it, marked
with `@Id`.

2. Add to your entity class a normal property for it, marked with `@EmbeddableId`.

3. Add properties to your entity class for all of its fields, mark them with `@Id`, and
mark your entity class with `@IdClass`, supplying the class of your primary key class.

Hajde da pro\dj emo kroz svaku od opisanih strategija i prika\v zemo njihove primere.

### 10.9.1 Prva strategija - `@Embeddable` i `@Id`

The use of `@Id` with a class marked as `@Embeddable`, as shown in the following example, is
the most natural approach. The `@Embeddable` annotation allows you to treat the compound
primary key as a single property, and it permits the reuse of the `@Embeddable` class in other
tables.

Iz `CPKBook.java` datoteke:

```java
// Ovo je klasa koja ima slozeni kljuc ISBN
@Entity
public class CPKBook {
	@Id
	ISBN id;
	
	...
	
}
```

Iz `ISBN.java` datoteke:

```java
// Ovo je klasa koja predstavlja slozeni kljuc:
// 1. Anotiramo je anotacijom @Embeddable
@Embeddable
// 2. Implementira interfejs java.io.Serializable
public class ISBN implements Serializable {
	// Naziv "group" je nevalidan naziv kolone u SQL-u
	@Column(name="group_number") 
	int group;
	int publisher;
	int title;
	int checkdigit;
	
	// 3. Ima podrazumevani konstruktor
	public ISBN() {
	}
	
	// Get i set metodi
	...
    
    // 4. Prevazilazi metode equals i hashCode
	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof ISBN)) return false;
		
		ISBN isbn = (ISBN) o;
	
		if (checkdigit != isbn.checkdigit) return false;
		if (group != isbn.group) return false;
		if (publisher != isbn.publisher) return false;
		if (title != isbn.title) return false;
	
		return true;
	}
	
	@Override
	public int hashCode() {
        return Objects.hash(this.group, this.publisher, this.title, this.checkdigit);
	}
}
```

### 10.9.2 Druga strategija - `@EmbeddedId`

The next most natural approach is the use of the `@EmbeddedId` annotation. Here, the primary key
class cannot be used in other tables since it is not an `@Embeddable` entity, but it does allow
us to treat the key as a single attribute of the "table class".

\v Cesto se ovakva klasa implementira kao deo klase koja preslikava tabelu, upravo iz razloga \v sto 
predstavlja njen deo, tj. ne\'ce se koristiti kao primarni klju\v c neke druge klase.

```java
@Entity
public class EmbeddedPKBook {
	@EmbeddedId
	EmbeddedISBN id;

	@Column
	String name;
	
	// Get/set metodi

	static class EmbeddedISBN implements Serializable {
		@Column(name="group_number")
		int group;
		int publisher;
		int title;
		int checkdigit;
		
		public ISBN() {
		}
		
		// Get/set metodi, equals, hashCode...
	}
}
```

### 10.9.3 Tre\'ca strategija - `@IdClass` i `@Id`

Finally, the use of the `@IdClass` and `@Id` annotations allows us to map the compound
primary key class using properties of the entity itself corresponding to the names of the
properties in the primary key class. The names must correspond (there is no mechanism
for overriding this), and the primary key class must honor the same obligations as with
the other two techniques. The only advantage to this approach is its ability to "hide" the
use of the primary key class from the interface of the enclosing entity.

The `@IdClass` annotation takes a value parameter of `Class` type, which must be the class
to be used as the compound primary key. The fields that correspond to the properties of
the primary key class to be used must all be annotated with `@Id` — note in the following
code example that the class properties `group`, `publisher`, `title` and `checkdigit` are so
annotated, and the `EmbeddedISBN` class is not mapped as `@Embeddable`, but it is supplied as
the value of the `@IdClass` annotation.

```java
@Entity
@IdClass(IdClassBook.EmbeddedISBN.class)
public class IdClassBook {
	@Id
	int group;
	@Id
	int publisher;
	@Id
	int title;
	@Id
	int checkdigit;
	String name;
	
	public IdClassBook() {
	}
	
	// Get/set metodi
	
	static class EmbeddedISBN implements Serializable {
		@Column(name="group_number")
		int group;
		int publisher;
		int title;
		int checkdigit;
		
		public ISBN() {
		}
		
		// Get/set metodi, equals, hashCode...
	}
}
```

{% include lab/exercise.html broj="10.3" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate implementira unos podataka o novom ispitnom roku (jun 2019. godine) u tabelu `ISPITNI_ROK`, a zatim briše podatke o unetom ispitnom roku iz tabele `ISPITNI_ROK`." %}

Re\v senje: Potrebno je da prvo napravimo klasu
koja će predstavljati složeni ključ. Nazovimo je `IspitniRokId`. U nastavku je data njena
implementacija. Obratiti pažnju na metode `equals()` i `hashCode()`. Definicije ovih metoda
će biti gotovo identične za sve klase koje ih prevazilaze iz klase `Object`:

- Metod `equals()` prvo ispituje da li je reference objekata `this` i `o` (koji se prosleđuje
kao argument metoda) poklapaju. Zatim proverava da li drugi objekat (`o`) predstavlja referencu 
iste klase kao objekat sa kojim se poredi (`this`). Zatim proverava da
li se svi atributi objekata poklapaju pozivajući metod `Objects.equals` za svaki par
atributa.

- Metod `hashCode()` jednostavno poziva metod `Objects.hash` koja prima proizvoljni
broj argumenata i vraća heš vrednost na osnovu njihovih vrednosti. Obično je bolje
koristiti ovaj metod nego implementirati svoju heš funkciju.

Cela implementacija je data u nastavku:

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_2/IspitniRokId.java, java)

Zatim je potrebno specifikovati instancu ove klase kao primarni ključ klase `IspitniRok.java`, 
koju takođe kreiramo:

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_2/IspitniRok.java, java)

Ne zaboravimo da dodamo `IspitniRok` kao anotiranu klasu u konfiguraciju:

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_2/HibernateUtil.java, java)

Definisane klase se sada jednostavno koriste u metodima `insertIspitniRok()` i `deleteIspitniRok()` proširene klase `Main.java` iz prethodnog primera:

include_source(vezbe/primeri/poglavlje_10/src/zadatak_10_2/Main.java, java)


## 10.10 Zadaci za vežbu

{% include lab/exercise.html broj="10.4" tekst="Napisati Java aplikaciju koja kori\v s'cenjem biblioteke Hibernate redom:\n
  1. Unosi podatak o novom novou kvalifikacije u tabelu `NIVO_KVALIFIKACIJE` sa podacima iz naredne tabele. \n
  2. Ispisuje podatake o novou kvalifikacije sa identifikatorom `42` iz tabele `NIVO_KVALIFIKACIJE`.\n
  3. Ažurira stepen za nivo kvalifikacije sa identifikatorom `42` iz tabele `NIVO_KVALIFIKACIJE`. Stepen postaviti na vrednost `III`. \n
  4. Ispisuje podatake o novou kvalifikacije sa identifikatorom `42` iz tabele `NIVO_KVALIFIKACIJE`.\n
  5. Briše podatake o novou kvalifikacije sa identifikatorom `42` iz tabele `NIVO_KVALIFIKACIJE`.\n
  6. Ispisuje podatake o novou kvalifikacije sa identifikatorom `42` iz tabele `NIVO_KVALIFIKACIJE`.\n
\n
Svaki zahtev implementirati kao posebnu transakciju." %}

| Kolona | Vrednost |
| --- | --- |
| Identifikator | 42 |
| Naziv | Novi nivo kvalifikacije |
| Stepen | II |

{% include lab/exercise.html broj="10.5" tekst="Napisati Java aplikaciju koja kori\v s'cenjem biblioteke Hibernate redom:\n
  1. Unosi podatak o novom predmetu u tabelu `PREDMET` sa identifikatorom predmeta `id` i ostalim podacima koji se unose sa standardnog ulaza.\n
  2. Ispisuje podatake o predmetu sa identifikatorom `id` iz tabele `PREDMET`.\n
  3. Proverava da li korisnik želi da ažurira broj bodova za predmet sa identifikatorom `id` u tabeli `PREDMET`. Ukoliko korisnik odgovori potvrdno, izvršava odgovarajuće ažuriranje. Novi broj bodova unosi se sa standardnog ulaza.\n
  4. Ispisuje podatake o predmetu sa identifikatorom `id` iz tabele `PREDMET`.\n
  5. Briše podatake o predmetu sa identifikatorom `id` iz tabele `PREDMET`.\n
  6. Ispisuje podatake o predmetu sa identifikatorom `id` iz tabele `PREDMET`.
\n\n
Svaki zahtev implementirati kao posebnu transakciju. " %}

