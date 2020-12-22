---
layout: page
title: 11. Napredna objektno-relaciona preslikavanja
under_construction: true
---

U prethodnom poglavlju smo se upoznali sa osnovama objektno-relacionih preslikavanja. Ovo poglavlje \'ce biti posve\'ceno naprednijim konceptima. Upozna\'cemo se sa jezikom HQL, koja predstavlja pandan jeziku SQL, samo u radu sa Java klasama i objektima. Dodatno, obrati\'cemo veliku pa\v znju problemu objektno-relacionih preslikavanja stranih klju\v ceva i na\v cinima za njihovu implementaciju.

## 11.1 Rad sa skupovima slogova

Za rad sa potencijalno većim brojem slogova u tabeli, na raspolaganju su nam stoji _HQL_ (_Hibernate Query Language_). While most ORM tools and object databases offer an object query language, Hibernate’s HQL stands out as complete and easy to use. HQL was inspired by SQL and is a major inspiration for the Java Persistence Query Language (JPQL).

### 11.1.1 Menjanje

`UPDATE` alters the details of existing objects in the database. In-memory entities, managed
or not, will not be updated to reflect changes resulting from issuing `UPDATE` statements.
Here’s the syntax of the `UPDATE` statement:

```sql
UPDATE [VERSIONED]
	[FROM] path [[AS] alias] [, ...]
	SET property = value [, ...]
	[WHERE logicalExpression]
```

The fully qualified name of the entity or entities is `path`. The `alias` names may be used
to abbreviate references to specific entities or their properties, and must be used when
property names in the query would otherwise be ambiguous. `VERSIONED` means that the
update will update timestamps, if any, that are part of the entity being updated. The
`property` names are the names of properties of entities listed in the `FROM` path.

An example of the update in action might look like this:

```java
Query query = session.createQuery(
        "UPDATE Person SET creditscore = :score WHERE firstname = :name");
query.setInteger("score", 612);
query.setString("name", "John Q. Public");
int modifications = query.executeUpdate();
```

### 11.1.2 Brisanje

`DELETE` removes the details of existing objects from the database. In-memory entities will
not be updated to reflect changes resulting from `DELETE` statements. This also means
that Hibernate’s cascade rules will not be followed for deletions carried out using HQL.
However, if you have specified cascading deletes at the database level (either directly or
through Hibernate, using the `@OnDelete` annotation), the database will still remove the
child rows. This approach to deletion is commonly referred to as ”bulk deletion”, since it
is the most efficient way to remove large numbers of entities from the database. Here’s
the syntax of the `DELETE` statement:

```sql
DELETE
    [FROM] path [[AS] alias]
    [WHERE logicalExpression]
```

The fully qualified name of the entity or entities is `path`. The `alias` names may be used
to abbreviate references to specific entities or their properties, and must be used when
property names in the query would otherwise be ambiguous.

In practice, deletes might look like this:

```java
Query query = session.createQuery(
	    "DELETE FROM Person WHERE accountstatus = :status");
query.setString("status", "purged");
int rowsDeleted = query.executeUpdate();
```

### 11.1.3 Unošenje

A HQL `INSERT` cannot be used to directly insert arbitrary entities — it can only be used to
insert entities constructed from information obtained from `SELECT` queries (unlike ordinary
SQL, in which an `INSERT` command can be used to insert arbitrary data into a table, as
well as insert values selected from other tables). Here’s the syntax of the `INSERT` statement:

```java
INSERT
    INTO path ( property [, ...])
    select
```

The name of an entity is `path`. The `property` names are the names of properties of entities
listed in the `FROM` path of the incorporated `SELECT` query. The select query is a HQL
`SELECT` query (as described in the next section). As this HQL statement can only use data
provided by a HQL select, its application can be limited. An example of copying users to
a purged table before actually purging them might look like this:

```java
Query query = session.createQuery(
	    "INSERT INTO PURGED_USERS(id, name, status) " +
	    "SELECT id, name, status FROM User WHERE status = :status");
query.setString("status", "purged");
int rowsCopied = query.executeUpdate();
```

### 11.1.4 \v Citanje

A HQL `SELECT` is used to query the database for classes and their properties. As noted
previously, this is very much a summary of the full expressive power of HQL `SELECT` queries;
however, for more complex joins and the like, you may find that using the Criteria API is
more appropriate. Here’s the syntax of the `SELECT` statement:

```sql
[SELECT [DISTINCT] property [, ...]]
    FROM path [[AS] alias] [, ...] [FETCH ALL PROPERTIES]
    WHERE logicalExpression
    GROUP BY property [, ...]
    HAVING logicalExpression
    ORDER BY property [ASC | DESC] [, ...]
```

The fully qualified name of the entity or entities is `path`. The `alias` names may be used
to abbreviate references to specific entities or their properties, and must be used when
property names in the query would otherwise be ambiguous. The property names are the
names of properties of entities listed in the `FROM` path.

If `FETCH ALL PROPERTIES` is used, then lazy loading semantics will be ignored, and all the
immediate properties of the retrieved object(s) will be actively loaded (this does not apply
recursively).

When the properties listed consist only of the names of aliases in the `FROM` clause, the
`SELECT` clause can be omitted in HQL.

#### The FROM Clause and Aliases

We have already discussed the basics of the from clause in HQL. The most important
feature to note is the _alias_. Hibernate allows you to assign aliases to the classes in your
query with the as clause. Use the aliases to refer back to the class inside the query. For
instance, instead of a simple HQL query

```sql
FROM Supplier
```

the example could be the following:

```sql
FROM Product AS p
```

The `AS` keyword is optional — you can also specify the alias directly after the class name,
as follows:

```sql
FROM Product p
```

If you need to fully qualify a class name in HQL, just specify the package and class name.
Hibernate will take care of most of this behind the scenes, so you really need this only
if you have classes with duplicate names in your application. If you have to do this in
Hibernate, use syntax such as the following:

```sql
FROM businessapp.model.Product
```

#### The SELECT Clause and Projection

The select clause provides more control over the result set than the from clause. If you
want to obtain the properties of objects in the result set, use the select clause. For instance,
we could run a projection query on the products in the database that only returned the
names, instead of loading the full object into memory, as follows:

```sql
SELECT product.name FROM Product product
```

The result set for this query will contain a `List` of Java `String` objects. Additionally, we
can retrieve the prices and the names for each product in the database, like so:

```sql
SELECT product.name, product.price FROM Product product
```

This result set contains a `List` of `Object` arrays (therefore, `List<Object[]>`) — each array
represents one tuple of properties (in this case, a pair that represents name and price).

If you’re only interested in a few properties, this approach can allow you to reduce network
traffic to the database server and save memory on the application’s machine.

#### Using Named Parameters

Hibernate supports named parameters in its HQL queries. The simplest example of named
parameters uses regular SQL types for the parameters:

```java
String hql = "FROM Product WHERE price > :price";
Query query = session.createQuery(hql);
query.setParameter("price", 25.0);
List results = query.list();
```

Metod `setParameter()` uzima naziv parametarske oznake kao prvi argument i vrednost
kojom ona treba biti zamenjena kao drugi argument. Hibernate će automatski pokušati da dedukuje
tip na osnovu pozicije imenovane parametarske oznake u upitu. Alternativno, postoji preoptere\'cenje
ovog metoda koje prihvata dodatni argument kojim se navodi tip te parametarske oznake.
Naredni kod navodimo tek radi kompletnosti, bez ula\v zenja u detalje. Jedino na \v sta \'cemo
skrenuti pa\v znju jeste metod `getSingleResult` klase `org.hibernate.query.Query`
koji dohvata ta\v cno jedan rezultat iz tabele koji zadovoljava HQL upit.

```java
// Dohvatamo model koji sadrzi meta-informacije.
MetamodelImplementor metamodelImplementor = 
        (MetamodelImplementor) HibernateUtil.getSessionFactory().getMetamodel();

// Izracunavamo tip atributa `caption` u klasi `Photo`.
Type captionType = metamodelImplementor
		.entityPersister( Photo.class.getName() )
		.getPropertyType( "caption" );

// HQL upit kojim dohvatamo sve slike koje imaju odgovarajuci naziv.
String hql = 
        "select p " +
        "from Photo p " +
        "where upper(caption) = upper(:caption) ";
// Koristimo varijantu metoda `setParameter()` sa tri argumenta
// kako bismo nagovestili Hibernate-u koji je tip parametarske oznake sa nazivom "caption".
Photo photo = (Photo) session.createQuery(hql, Photo.class )
        .setParameter("caption", new Caption("Moja prva fotografija u novoj godini"), captionType)
        .getSingleResult();
```

Normally, you do not know the values that are to be substituted for the named parameters;
if you did, you would probably encode them directly into the query string. When the value
to be provided will be known only at run time, you can use some of HQL’s object-oriented
features to provide objects as values for named parameters. The Query interface has a
`setEntity()` method that takes the name of a parameter and an object.

Using this functionality, we could retrieve all the products that have a supplier whose
object we already have:

```java
String supplierHQL = "FROM Supplier WHERE name = 'MegaInc'";
Query supplierQuery = session.createQuery(supplierHQL);
Supplier supplier = (Supplier) supplierQuery.list().get(0);

String hql = "FROM Product AS product WHERE product.supplier = :supplier";
Query query = session.createQuery(hql);
query.setEntity("supplier", supplier);
List results = query.list();
```

#### Associations

Associations allow you to use more than one class in HQL query, just as SQL allows you
to use joins between tables in a relational database. You add an association to a HQL
query with the `JOIN` clause.

Hibernate supports five different types of joins: `INNER JOIN`, `CROSS JOIN`, `LEFT OUTER JOIN`,
`RIGHT OUTER JOIN`, and `FULL OUTER JOIN`. If you use `CROSS JOIN`, just specify both classes
in the `FROM` clause (`FROM Product p, Supplier s`). For the other joins, use a `JOIN` clause
after the `FROM` clause. Specify the type of join, the object property to join on, and an alias
for the other class.

For example, you can use `INNER JOIN` to obtain the supplier for each product, and then retrieve the
supplier name, product name, and product price, as so:

```sql
SELECT s.name, p.name, p.price FROM Product p INNER JOIN p.supplier AS s
```

You can retrieve the objects using similar syntax:

```sql
FROM Product p INNER JOIN p.supplier AS s
```

Notice that Hibernate does not return `Object` objects in the result set; instead, Hibernate
returns `Object` arrays in the results. This join results in a projection, thus the use of
the `Object` arrays. You will have to access the contents of the `Object` arrays to get the
`Supplier` and the `Product` objects.

{% include lab/exercise.html broj="11.1" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate ispisuje podatke o svim ispitnim rokovima." %}

Re\v senje: Zahtev je implementiran u metodu `readispitniRokovi()` klase `Main.java`:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_1/Main.java, java)

## 11.2 Asocijativne veze i strani ključevi

Jedan od najznačajnijih elemenata relacionih baza podataka jesu tzv. _asocijativne veze_ koje se ostvaruju između tabela. Na šta mislimo pod tim pojmom? Neka su nam date dve tabele `STUDENT` i `KNJIGA` \v cije su sheme date u nastavku:

```sql
CREATE TABLE STUDENT (
    ID_STUDENTA INTEGER NOT NULL,
    IME_PREZIME VARCHAR(50) NOT NULL,
    NAZIV_SMERA VARCHAR(50) NOT NULL,
    PRIMARY KEY (ID_STUDENTA)
)

CREATE TABLE KNJIGA (
    ID_KNJIGE INTEGER NOT NULL,
    NAZIV_KNJIGE VARCHAR(50) NOT NULL,
    AUTOR VARCHAR(50) NOT NULL,
    PRIMARY KEY (ID_KNJIGE)
)
```

Ove dve tabele trenutno nisu povezane, odnosno, slogovi iz tabele `STUDENT` i slogovi iz tabele `KNJIGA` "žive" potpuno odvojeno i bilo kakva izmena jedne tabele nikako ne utiče na drugu tabelu. Međutim, šta ukoliko je potrebno da čuvamo informaciju o tome koji student je pozajmio koju knjigu iz biblioteke? Implementiranje ovog zahteva će kreirati asocijativnu vezu između ovih tabela jer sada postoji zavisnost među podacima iz ovih tabela.

U zavisnosti od toga koja je semantika veze koja se ostvaruje podacima, razlikujemo nekoliko tipova veza. Da bismo odabrali ispravan tip veze, potrebno je da sebi postavimo naredna pitanja:

- _P1_: Može li jedna knjiga pripadati više studenata?
- _P2_: Može li student imati više od jedne knjige?

Odgovori na ova pitanja, koja su postavljena u poslovnom domenu, direktno utiču na tip asocijativne veze koja se formira. Tipovi veza, zajedno sa odgovorima, dati su u narednoj tabeli:

| Odgovor na _P1_ | Odgovor na _P2_ | Veza između `STUDENT` i `KNJIGA` |
| --- | --- | --- |
| Ne | Ne | Jedan-ka-jedan |
| Da | Ne | Više-ka-jedan |
| Ne | Da | Jedan-ka-više |
| Da | Da | Više-ka-više |

### 11.2.1 Veza jedan-ka-jedan

Veza jedan-ka-jedan se može ostvariti na nekoliko načina. U najjednostavnijoj varijanti, svojstva obe klase se čuvaju u istoj tabeli:

```sql
CREATE TABLE STUDENTKNJIGA (
    SKID INTEGER NOT NULL,
    IME_PREZIME ...,
    NAZIV_SMERA ...,
    NAZIV_KNJIGE ...,
    AUTOR ...,
    PRIMARY KEY (SKID)
)
```

U Java domenu:

```java
@Entity
Class StudentKnjiga {
    @Id
    private Integer skid;

    // Ostala polja i get/set metodi
}
```

Alternativno, trajni objekti se mogu čuvati u različitim tabelama, od kojih svaka ima svoj primarni ključ, ali za svakog studenta i svaku knjigu mora da važi naredno pravilo: student koji je vlasnik neke knjige i ta knjiga moraju imati isti primarni ključ.

Moguće je održavati strani ključ od jednog entiteta ka drugom, ali ne bismo smeli imali dvosmerni strani ključ u ovakvom odnosu jer bismo kreirali kružnu zavisnost. Naravno, možemo da ne kreiramo strani ključ i da ostavimo Hibernate-u da se stara o tome. U tom slučaju bi tabele izgledale:

```sql
CREATE TABLE STUDENT (
    ID_STUDENTA INTEGER NOT NULL,
    IME_PREZIME ...,
    NAZIV_SMERA ...,
    PRIMARY KEY (ID_STUDENTA)
)

CREATE TABLE KNJIGA (
    ID_KNJIGE INTEGER NOT NULL,
    NAZIV_KNJIGE ...,
    AUTOR ...,
    PRIMARY KEY (ID_KNJIGE)
)
```

Dok bi klase izgledale:

```java
@Entity
class Student {
    @Id
    private Integer id_studenta;
    
    // ...
}

@Entity
class Knjiga {
    @Id
    private Integer id_knjige;

    // ...
}
```

Poslednja opcija je da se održava odnos stranim ključem između dve tabele, pri čemu
se postavlja opcija unique nad kolonom koja predstavlja strani ključ. Ovaj pristup ima
prednost u tome što jednostavnim uklanjanjem opcije unique, od veze jedan-ka-jedan može
se ostvariti veza više-ka-jedan. U nastavku dajemo kako bi izgledale tabele u ovom
pristupu:

```sql
CREATE TABLE STUDENT (
    ID_STUDENTA INTEGER NOT NULL,
    IME_PREZIME ...,
    NAZIV_SMERA ...,
    PRIMARY KEY (ID_STUDENTA)
)

CREATE TABLE KNJIGA (
    ID_KNJIGE INTEGER NOT NULL,
    NAZIV_KNJIGE ...,
    AUTOR ...,
    ID_STUDENTA INTEGER NOT NULL,
    PRIMARY KEY (ID_KNJIGE),
    FOREIGN KEY (ID_STUDENTA)
        REFERENCES STUDENT,
    UNIQUE (ID_STUDENTA)
)
```

Dok bi klase izgledale:

```java
@Entity
class Student {
    @Id
    private Integer id_studenta;

    @OneToOne(mappedBy="student")
    private Knjiga knjiga;
    
    // Get/set metodi
}

@Entity
class Knjiga {
    @Id
    private Integer id_knjige;

    // Ostala polja

    @OneToOne
    private Student student;

    // Get/set metodi
}
```

Ono što prvo primećujemo u kodu jeste da smo uveli novu anotaciju `@OneToOne`. Ova anotacija se koristi ukoliko jedna klasa sadrži referencu na objekat druge klase (na primer, `Knjiga` sadrži polje `student` koje čuva referencu na klasu `Student`) i ukoliko želimo da kažemo Hibernate-u da je veza koja se ostvaruje jedan-ka-jedan.

Da klasa `Student` ne čuva referencu ka klasi `Knjiga` (kroz polje `knjiga`), time bi ovaj odnos bio rešen. Međutim, ukoliko nam je potrebno da obe klase čuvaju referencu jedna ka drugoj, onda dobijamo situaciju kao u prethodnom Java kodu. Ovakav pristup dovodi do toga da je veza koja se ostvaruje _bidirekciona_, odnosno, za datog studenta možemo dobiti referencu na knjigu čiji je on vlasnik, ali takođe, za datu knjigu, možemo dobiti referencu na studenta koji je njen vlasnik. U tom slučaju, potrebno je da obe reference (polja `knjiga` i `student`) u obe klase (`Student` i `Knjiga`, redom) budu dekorisana anotacijom `@OneToOne`.

Međutim, ovim i dalje nije re\v sen problem jer kako Hibernate da zna da li se ova bidirekciona veza održava u tabeli `STUDENT` ili u tabeli `KNJIGA`? I drugi tipovi veze mogu dovesti do ovakve situacije i rešenje se sastoji u postavljanju opcije `mappedBy` u odgovaraju\'coj anotaciji (u ovom primeru `@OneToOne`) kojom se navodi koje polje u **drugoj klasi** ostvaruje vezu stranog ključa. U ovom slučaju, želimo da postoji strani ključ nad kolonom `ID_STUDENTA` u tabeli `KNJIGA`, pa zato **u klasi `Student` navodimo da je
veza ostvarena kroz polje `student` u klasi `Knjiga`**. Vrlo je va\v zno obratiti pa\v znju na to gde se ova opcija postavlja kako bismo ispravno implementirali ovakvu zavisnost!

### 11.2.2 Veza jedan-ka-više/više-ka-jedan

Ova dva tipa veze u su\v stini predstavljaju jedan tip veze, zato \v sto, ako je iz ugla tabele `A` ostvarena veza jedan-ka-vi\v se ka tabeli `B`, onda je posledi\v cno iz ugla tabele `B` ostvarena veza vi\v se-ka-jedan ka tabeli `A`. Va\v zi i obratno. Veza jedan-ka-više (ili iz perspektive druge klase, više-ka-jedan) može biti jednostavno reprezentovana stranim ključem ka primarnom ključu tabele koja se nalazi na "jedan" kraju ove veze u tabeli koja se nalazi na "više" kraju ove veze, bez dodatnih ograničenja.

Pretpostavimo da jedan student mo\v ze imati vi\v se knjiga i da jedna knjiga mo\v ze pripadati ta\v cno jednom studentu. U tom slu\v caju, ostvaruje se veza jedan-ka-vi\v se od tabele `STUDENT` ka tabeli `KNJIGA` (odnosno, veza vi\v se-ka-jedan od tabele `KNJIGA` ka tabeli `STUDENT`). Shema tabela bi izgledala:

```sql
CREATE TABLE STUDENT (
    ID_STUDENTA INTEGER NOT NULL,
    IME_PREZIME ...,
    NAZIV_SMERA ...,
    PRIMARY KEY (ID_STUDENTA)
)

CREATE TABLE KNJIGA (
    ID_KNJIGE INTEGER NOT NULL,
    NAZIV_KNJIGE ...,
    AUTOR ...,
    ID_STUDENTA INTEGER NOT NULL,
    PRIMARY KEY (ID_KNJIGE),
    FOREIGN KEY (ID_STUDENTA)
        REFERENCES STUDENT
)
```

Implementacija klasa u Java domenu izgleda:

```java
@Entity
class Student {
    @Id
    private Integer id_studenta;

    @OneToMany(mappedBy="student")
    private List<Knjiga> knjige;
    
    // Get/set metodi
}

@Entity
class Knjiga {
    @Id
    private Integer id_knjige;
    
    // Ostala polja
    
    @ManyToOne
    @JoinColumn(name="id_studenta")
    private Student student;
    
    // Get/set metodi
}
```

Ono što bi trebalo da primetimo iz prethodnog koda je sledeće:

1. Koristimo anotacije `@OneToMany` i `@ManyToOne` da definišemo odgovarajući tip veze između ovih klasa. U klasi koja predstavlja "jedan" stranu veze (`Student`) koristimo `@OneToMany`, a u klasi koja predstavlja "vi\v se" stranu te iste veze (`Knjiga`) koristimo `@ManyToOne`.
2. U klasi `Student` vi\v se nema smisla \v cuvati referencu na jednu knjigu, po\v sto jedan student mo\v ze da poseduje vi\v se knjiga. Zbog toga, potrebno je da \v cuvamo kolekciju referenci na knjige. Mo\v zemo koristiti razne vrste kolekcija, kao \v sto su `java.util.List<T>`, `java.util.Set<T>`, `java.util.Map<K, V>`, itd.
3. Možemo koristiti anotaciju `@JoinColumn` da specifikujemo naziv kolone koja učestvuje u stranom ključu koji se kreira u ovoj vezi. Ovo je posebno korisno ukoliko se kolone koje u\v cestvuju u stranom klju\v cu ne zovu isto.

Alternativno, veza jedan-ka-više/više-ka-jedan može se održavati postojanjem treće tabele koja se naziva _spojna tabela_ (engl. _link table_). U našem primeru, ovakva tabela bi održavala strane ključeve ka tabelama `STUDENT` i `KNJIGA`, a te kolone bi učestvovale u primarnom ključu spojne tabele. Dodatno, mora biti postavljeno ograničenje jedinstvenosti nad jednom stranom odnosa — inače, spojna tabela može predstavljati sve moguće kombinacije, čime bi se ostvarila veza više-ka-više. Dajmo primer sheme spojne tabele:

```sql
CREATE TABLE STUDENTKNJIGALINK (
    ID_STUDENTA INTEGER NOT NULL,
    ID_KNJIGE INTEGER NOT NULL,
    PRIMARY KEY (ID_STUDENTA, ID_KNJIGE),
    FOREIGN KEY (ID_STUDENTA)
    REFERENCES STUDENT,
    FOREIGN KEY (ID_KNJIGE)
        REFERENCES KNJIGA,
    UNIQUE (ID_KNJIGE)
)
```

Korišćenje spojne tabele se ostvaruje korišćenjem anotacije `@JoinTable`, o kojoj \'ce biti re\v ci u nastavku.

### 11.2.3 Veza više-ka-više

Kao što smo napomenuli na kraju prethodne podsekcije, ako se ograničenje jedinstvenosti ne primeni na "jedan" kraju bidirekcione asocijativne veze prilikom korišćenja spojne tabele, ta veza postaje više-ka-više veza. Sve moguće varijante parova `(ID_STUDENTA, ID_KNJIGE)` mogu biti reprezentovane, ali nije moguće da jedan student ima više puta istu knjigu, s obzirom da bi u takvoj situaciji došlo do dupliciranja složenog primarnog ključa te spojne tabele.

Ukoliko umesto tog pristupa dodelimo spojnoj tabeli svoj primarni ključ, koji ne sadrži kolone koje ulaze kao deo stranih ključeva ka drugim tabelama, onda time možemo reprezentovati punu više-ka-više vezu, naravno, ukoliko to odgovara domenskom problemu. U tom slučaju, shema spojne tabele bi mogla izgledati kao u narednom kodu:

```sql
CREATE TABLE STUDENTKNJIGALINK (
    SUROGAT_ID INTEGER NOT NULL,
    ID_STUDENTA INTEGER NOT NULL,
    ID_KNJIGE INTEGER NOT NULL,
    PRIMARY KEY (SUROGAT_ID),
    FOREIGN KEY (ID_STUDENTA)
        REFERENCES STUDENT,
    FOREIGN KEY (ID_KNJIGE)
        REFERENCES KNJIGA
)
```

Bez obzira na pristup u SQL domenu, u Java domenu se koristi anotacija `@ManyToMany` i ona se navodi u obe klase. Kao i do sada, ukoliko je veza bidirekciona, potrebno je navesti `mappedBy` opciju u odgovaraju\'coj anotaciji. Ako jedna klasa navede ovu opciju, onda je druga strana vlasnik asocijacije i vrednost opcije mora biti polje te druge klase. Dajemo primer korišćenja kroz klase `Student` i `Knjiga`:

```java
@Entity
class Student {
    @Id
    private Integer id_studenta;

    @ManyToMany(mappedBy="studenti")
    private List<Knjiga> knjige;
    
    // Get/set metodi
}

@Entity
class Knjiga {
    @Id
    private Integer id_knjige;
    
    @ManyToMany
    @JoinTable(
        name = "STUDENTKNJIGALINK", 
        joinColumns = { @JoinColumn(name = "ID_KNJIGE") }, 
        inverseJoinColumns = { @JoinColumn(name = "ID_STUDENTA") }
    )
    private List<Student> studenti;
    
    // Get/set metodi
}
```

Anotacija `@JoinTable` u prethodnom fragmentu koda ukazuje na to da ce biti kreirana spojna tabela pod nazivom `STUDENTKNJIGALINK`. Atributom `joinColumns` anotacije `@JoinTable` se postavljaju kolone stranog kljuca spojne tabele koje referi\v su na primarni klju\v c entiteta koji je odgovoran za odrzavanje asocijacije (ovde je to `Knjiga`). Nasuprot tome, atributom `inverseJoinColumns` se postavljaju kolone stranog klju\v ca spojne tabele koje referi\v su na primarni klju\v c _onog drugog_ entiteta (ovde je to `Student`).

### 11.2.4 Strani ključevi u složenim primarnim ključevima

Pretpostavimo da imamo tabelu `KORISNIK` čiji se složeni primarni klju\v c sastoji od kolona `KORISNICKO_IME` i `ID_ODELJENJA`, kao i da imamo tabelu `ODELJENJE` koja sadrži primarni ključ `ID_ODELJENJE`. Takođe, postoji ograni\v cenje stranog ključa `FK1` koji postavlja ograničenje na kolonu `ID_ODELJENJA` iz tabele `KORISNIK` u odnosu na kolonu `ID_ODELJENJE` iz tabele `ODELJENJE` i koji predstavlja odnos više-ka-jedan (jedno odeljenje može imati više korisnika, a jedan korisnik može pripadati samo jednom odeljenju). Ova situacija je ilustrovana na narednoj slici.

![Strani ključevi u složenim primarnim ključevima](./Slike/strani1.png)

Problem koji se ovde javlja jeste da postoji strani klju\v c ka vrednosti iz druge tabele (`Odeljenje`) kao deo slo\v zenog primarnog klju\v ca u prvoj tabeli (`Korisnik`). Ovo je o\v cigledno problem dupliciranih podataka i prilikom trajnog \v cuvanja podataka, Hibernate mora da zna koju \'ce vrednost smatrati za "ispravnu". Klasu koja sadr\v zi ovakvu specijalnu situaciju potrebno je anotirani anotacijom `@MapsId`. Njena vrednost je naziv polja u slo\v zenom primarnom klju\v cu te tabele koja \v cuva informaciju o stranom klju\v cu ka drugoj tabeli. Pogledajmo kako bismo razre\v sili ovaj problem na prethodno opisanoj situaciji:

```java
public class KorisnikId implements Serializable {
    protected String korisnicko_ime;
    protected Long id_odeljenja;
    // ...
}

@Entity
public class Korisnik {
    @EmbeddedId
    protected KorisnikId id;

    @ManyToOne
    @MapsId("id_odeljenja")
    protected Odeljenje odeljenje;

    public Korisnik(KorisnikId id) {
        this.id = id;
    }

    // ...
}
```

Anotacija `@MapsId` kaže Hibernate razvojnom okruženju da ignoriše vrednost polja `KorisnikId.id_odeljenja` prilikom čuvanja instance klase `Korisnik`. Hibernate će umesto toga koristiti identifikator iz klase `Odeljenje` dodeljen polju `Korisnik.odeljenje` prilikom čuvanja sloga u tabeli `KORISNIK`.

Alternativni pristup ovom preslikavanju jeste da se umesto anotacije `@MapsId` koristi anotacija `@JoinColumn` kojom se navodi nazivi kolona koje u\v cestvuju u formiranju stranog klju\v ca. U toj situaciji je neophodno navesti i vrednost `false` za opcije `insertable` i `updatable` ove anotacije sa narednim zna\v cenjem:

- Svojstvo `insertable` uzima podrazumevano vrednost `true`, a ukoliko se postavi na `false`, onda anotirano polje se neće nalaziti u `INSERT` naredbama generisanim od strane Hibernate alata (drugim rečima, neće biti trajno upisano u bazu podataka).

- Svojstvo `updatable` uzima podrazumevano vrednost `true`, a ukoliko se postavi na `false`, onda anotirano polje se neće nalaziti u `UPDATE` naredbama generisanim od strane Hibernate alata (drugim rečima, neće biti promenjeno jednom kada se trajno upiše u bazu podataka).

Kori\v s\'cenje ovog pristupa dovodi do narednog koda:

```java
public class KorisnikId implements Serializable {
    protected String korisnicko_ime;
    protected Long id_odeljenja;
    // ...
}

@Entity
public class Korisnik {
    @EmbeddedId
    protected KorisnikId id;

    @ManyToOne
    @JoinColumn(name="id_odeljenja",
                referencedColumnName = "id_odeljenja",
                insertable = false,
                updatable = false))
    protected Odeljenje odeljenje;

    public Korisnik(KorisnikId id) {
        this.id = id;
    }

    // ...
}
```

Jednostavnije rečeno, ne želimo da menjamo podatke u tabeli `ODELJENJE` menjanjem polja `id_odeljenja` iz klase `KorisnikId` (čime ga obeležavamo da je _samo za čitanje_ (engl. _readonly_)).

Napomenimo jo\v s i da, ukoliko tabela ima vi\v se stranih klju\v ceva, onda je kori\v s\'cenje anotacije `@JoinColumn` neophodno da bi se navelo koje kolone u\v cestvuju u formiranju ograni\v cenja stranog klju\v ca. Naravno, ukoliko se `@JoinColumn` koristi u kombinaciji sa `@MapsId`, onda nije neophodno navesti opcije `insertable` i `updatable`.

Ova anotacija se još koristi i kada se zbog bidirekcione veze duplicira strani ključ. U tom slučaju se navode sve četiri opcije.

{% include lab/exercise.html broj="11.2" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate za sve studente, koji su rođeni u mestu koje se unosi sa standardnog ulaza i upisali su studijski program obima epsb koji se unosi sa standardnog ulaza, ispisuje ime, prezime i naziv studijskog programa." %}

Re\v senje: Tabelu `DOSIJE` do sad nismo koristili pa je potrebno da kreiramo odgovarajuću klasu i definišemo preslikavanje u tabelu `DOSIJE`, a onda i vezu sa klasom `StudijskiProgram`. Vezu definišemo koa više-ka-jedan u klasi `Student` jer više studenata mogu upisati isti studijski program. Pošto tabela dosije sadrži polje `idprograma`, a dodavanjem ove veze dupliciramo podatke (polje `StudijskiProgram studijskiProgram`  takođe sadrži informaciju o identifikatoru) potrebno je dodati i anotaciju `@JoinColumn` za polje `studijskiProgram`:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_2/Student.java, java)

Ova veza je definisana u klasi `StudijskiProgram` kao jedan-ka-više, s obzirom da jedan studijski program može sadržati više studenata:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_2/StudijskiProgram.java, java)


Zahtev je implementiran u metodu `readStudenti()` klase `Main.java`:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_2/Main.java, java)


### 11.2.5 Strani ključevi ka složenim ključevima

Slična situacija se javlja kada je potrebno mapirati asocijativnu vezu složenog stranog ključa. Pogledajmo DDL za naredne dve tabele:

```sql
CREATE TABLE KORISNIK (
    KORISNICKO_IME INTEGER NOT NULL,
    ID_ODELJENJA INTEGER NOT NULL,

    PRIMARY KEY (KORISNICKO_IME, ID_ODELJENJA)
)

CREATE TABLE ARTIKAL (
    ID INTEGER NOT NULL,
    KORISNICKO_IME_PRODAVCA INTEGER NOT NULL,
    ID_ODELJENJA_PRODAVCA INTEGER NOT NULL,
    
    PRIMARY KEY (ID),
    
    FOREIGN KEY (
        KORISNICKO_IME_PRODAVCA,
        ID_ODELJENJA_PRODAVCA)
        REFERENCES KORISNIK
)
```

Zgodno je ilustrovati ovu situaciju crte\v zom:

![Strani ključevi ka složenim ključevima](./Slike/strani2.png)

U ovoj shemi, prodavac artikla je predstavljen složenim stranim ključem u tabeli `ARTIKAL`. Ova veza predstavlja odnos jedan-ka-više (jedan korisnik je vlasnik više artikala, a jedan artikal pripada samo jednom korisniku). U domenskom modelu, ovo preslikavanje se može rešiti na sledeći način:

```java
///////////////////////////////////
// BEGIN Korisnik.java

@Embeddable
public class KorisnikId implements Serializable {
    Integer korisnicko_ime;
    Integer id_odeljenja;
    
    // ...
}

@Entity
public class Korisnik {
    @Id
    KorisnikId id;
    
    // ...
}

// END Korisnik.java

///////////////////////////////////
// BEGIN Artikal.java

@Entity
public class Artikal {
    @Id
    Integer id_artikla;

    @ManyToOne
    @JoinColumns({
        @JoinColumn(name = "korisnicko_ime_prodavca",
                    referencedColumnName = "korisnicko_ime"),
        @JoinColumn(name = "id_odeljenja_prodavca",
                    referencedColumnName = "id_odeljenja")
    })
    Korisnik prodavac;
    // ...
}

// END Artikal.java
```

Anotacija `@JoinColumns` predstavlja listu kolona u složenom stranom ključu koje učestvuju u ovoj asocijativnoj vezi. Svaka kolona se ostvaruje navo\dj enjem jedne `@JoinColumn` anotacije. Napomenimo da ne bi trebalo zaboraviti postavljanje vrednosti opcije `referencedColumnName` u `@JoinColumn` anotacijama, čime se ostvaruje veza između izvora i cilja stranog ključa. Hibernate nas, nažalost, neće upozoriti ukoliko ovo zaboravimo, a može dovesti do problema usled pogrešnog redosleda kolona u generisanoj shemi.

### 11.2.6 Složeni strani ključevi kao delovi složenih primarnih ključeva

Najkomplikovaniji slučaj se može pojaviti ukoliko se izmeni shema tabele `ARTIKAL` tako da kolone stranog ključa učestvuju u složenom primarnom ključu, kao u narednoj shemi:

```sql
CREATE TABLE ARTIKAL (
    ID INTEGER NOT NULL,
    KORISNICKO_IME_PRODAVCA INTEGER NOT NULL,
    ID_ODELJENJA_PRODAVCA INTEGER NOT NULL,

    PRIMARY KEY (ID,
        KORISNICKO_IME_PRODAVCA,
        ID_ODELJENJA_PRODAVCA),
    
    FOREIGN KEY (
        KORISNICKO_IME_PRODAVCA,
        ID_ODELJENJA_PRODAVCA)
        REFERENCES KORISNIK
)
```

Naredna slika nam mo\v ze pomo\'ci u analiziranju ove situacije:

![Složeni strani ključevi kao delovi složenih primarnih ključeva](./Slike/strani3.png)

Zapravo, ako malo bolje pogledamo, mo\v zemo primetiti da ovaj slu\v caj nastaje kombinacijom prethodna dva slu\v caja — sa jedne strane imamo situaciju da strani klju\v c predstavlja deo slo\v zenog primarnog klju\v ca, a tako\dj e imamo situaciju da je strani klju\v c slo\v zen. Dakle, sve \v sto je potrebno da uradimo jeste da kombinujemo re\v senja oba problema, tj. da iskoristimo anotacije `@MapsId` i `@JoinColumns`, zajedno sa odgovarajućom anotacijom za tip veze, kao u narednom kodu:

```java
public class ArtikalId implements Serializable {
    Integer id;
    KorisnikId id_korisnika;
    
    // ...
}

@Entity
public class Artikal {
    @EmbeddedId
    ArtikalId id_artikla;

    // Naredne anotacije se vezuju za polje `Korisnik prodavac` ispod.
    // Odvojene su radi lakseg analiziranja i komentarisanja.
    // 1. Vezujemo `ArtikalId.id_korisnika` za identifikator klase `Korisnik`
    @MapsId("id_korisnika")

    // 2. Definisemo po cemu se vrsi spajanje za slozeni strani kljuc
    // koji postoji izmedju klasa `Artikal` i `Korisnik`
    @JoinColumns({
        @JoinColumn(name = "korisnicko_ime_prodavca",
                    referencedColumnName = "korisnicko_ime"),
        @JoinColumn(name = "id_odeljenja_prodavca",
                    referencedColumnName = "id_odeljenja")
    })

    // 3. Definisemo tip veze izmedju `Artikal` i `Korisnik`
    @ManyToOne

    Korisnik prodavac;

    // ...
}
```

{% include lab/exercise.html broj="11.3" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate ispisuje nazive svih studijskih programa. Nakon svakog naziva studijskog programa, ispisuju se indeks, ime, prezime i prosek svih studenata na tom studijskom programu." %}

Ovaj zahtev je do sada najsloženiji jer uvodi dosta novih klasa i preslikavanja. Za početak, s obzirom da su nam potrebni podaci iz tabele `ISPIT` koju do sada nismo koristili, potrebno je da kreiramo klasu `Ispit`, i definišemo preslikavanja između njih i odgovarajućih tabela, kao i između samih klasa. Za početak, dajmo njenu celu definiciju, pa ćemo obratiti pažnju na neke detalje:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/Ispit.java, java)

Prvo što vidimo jeste da klasa Ispit sadrži složeni ključ `IspitId`. Dajmo i definiciju ove klase:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/IspitId.java, java)

Ono što primećujemo jeste da u okviru ovog primarnog ključa se nalazi drugi primarni ključ i to za klasu `IspitniRok`, odnosno, klasa `IspitniRokId`. Ovde vidimo da možemo klase koje predstavljaju primarne ključeve nekih klasa (kao što je `IspitniRokId`) koristiti u klasama koje predstavljaju primarne ključeve drugih klasa (kao što je `IspitId`), ali samo ako su oni kreirane prvim pristupom kreiranja stranih ključeva — pomoću anotacija `@Id` i `@Embeddable`. Sad nam je jasno zašto smo upravo taj pristup koristili za `IspitniRokId`. \v Stavi\v se, preslikavanje na ovaj na\v cin je obavezno ukoliko \v zelimo da sa\v cuvamo sva ograni\v cenja u poslovnom domenu. Alternativni pristup, u kojem bi se kolone `SKGODINA` i `OZNAKAROKA` iz tabele `ISPIT` preslikavala pomo\'cu dva polja u klasi `IspitId`, ne bi o\v cuvao organi\v cenje da obe kolone predstavljaju slo\v zeni strani klju\v c ka tabeli `ISPITNIROK`. Zbog toga, klasa `IspitId` mora imati polje tipa `IspitniRokId` kao u kodu iznad.

Vratimo se nazad na klasu `Ispit`. Nakon deklarisanja polja `ocena` i `status`, potrebno je rešiti asocijativne veze ka klasama `Student` i `IspitniRok`. 

Veza ka klasi `Student` je dovoljno jednostavna — jedan ispit pripada tačno jednom studentu, dok jedan student može imati više ispita, dakle, veza je više-ka-jedan, odnosno, koristimo anotaciju `@ManyToOne` (ovaj zaključak je donesen iz ugla klase `Ispit`). Dodatno, potrebno je da postavimo anotaciju `@MapsId` kako bismo ignorisali vrednost iz primarnog klju\v ca `IspitId` pri \v cuvanju ovog podatka — umesto njega, bi\'ce kori\v s\'cena vrednost polja `indeks` iz klase `Student`. Tako\dj e, s obzirom da ova tabela ima vi\v se stranih klju\v ceva, potrebno je da koristimo anotaciju `@JoinColumn` da specifikujemo precizno koje kolone u\v cestvuju u ograni\v cenju ovog stranog klju\v ca. Alternativno, mo\v zemo ga postaviti samo za \v citanje, kao \v sto smo diskutovali u podsekciji [11.2.4](#1124-strani-ključevi-u-složenim-primarnim-ključevima).

Druga veza je prema klasi `IspitniRok`. Ukoliko pogledamo kako je sve do sada postavljeno, videćemo da je ovo instanca problema prikazana u podsekciji [11.2.6](#1126-složeni-strani-ključevi-kao-delovi-složenih-primarnih-ključeva). Da se podsetimo, problem je u tome što se u tabeli `ISPIT` nalazi složeni strani ključ ka tabeli `ISPITNIROK`, pri čemu kolone složenog stranog ključa učestvuju kao deo primarnog ključa. Rešenje je u kreiranju polja klase `IspitniRok` i odgovarajućim anotiranjem:

- Koristimo anotaciju `@MapsId` da ignorišemo kolone zadate poljem `idRoka` iz primarnog ključa klase `IspitId`. Umesto toga, koristićemo polja iz objekta klase `IspitniRok` koji anotiramo.

- Koristimo anotaciju `@JoinColumns` da definišemo po kojim kolonama se vrši odgovarajuće preslikavanje za složeni strani ključ. Vrednost ove anotacije je lista `@JoinColumn` anotacija, za svaku kolonu u složenom stranom ključu.

- Koristimo anotaciju `@ManyToOne` da definišemo tip preslikavanja. S obzirom da više ispita pripada jednom ispitnom roku, a da jedan ispitni rok sadrži više ispita, veza je više-ka-jedan (opet, posmatrano iz ugla klase `Ispit` koju trenutno analiziramo).

Naravno, ovo podrazumeva izmenu i u klasi `IspitniRok`, gde sada definišemo vezu između ispitnih rokova i ispita — jedan ispitni rok ima više ispita, te koristimo anotaciju `@OneToMany` za polje tipa `java.util.List<Ispit>`. S obzirom da je ova veza dvosmerna, moramo reći koja klasa je odgovorna za održavanje takve veze (tj. za održavanje referencijalnog integriteta stranim ključem). S obzirom da klasa `Ispit` odr\v zava tu vezu, onda u klasi `IspitniRok` postavljamo opciju `mappedBy`, kao što je i urađeno u klasi `IspitniRok`:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/IspitniRok.java, java)

Dopunimo i klasu `Student`:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/Student.java, java)

S obzirom da je potrebno da izračunamo prosek položenih predmeta za datog studenta, definišemo metod `prosek()` koji koristi listu svih ispita studenta. Za listu ispita smo morali da napravimo odgovarajuće preslikavanje izme\dj u klasa `Student` i `Ispit`. S obzirom da jedan student može imati više ispita, koristimo vezu jedan-ka-više. Dodatno, moramo da navedemo po čemu se vrši "spajanje" između klasa, i to je u ovom slučaju, po koloni `indeks`.

Konačno, da bismo završili implementaciju, potrebno je da registrujemo anotirane klase u klasi `HibernateUtil` i kreiramo metod u klasi `Main` koji testira implementirane klase i metode:

include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/HibernateUtil.java, java)
include_source(vezbe/primeri/poglavlje_11/src/zadatak_11_3/Main.java, java)


## 11.3 Zadaci za vežbu

{% include lab/exercise.html broj="11.4" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate za svaki studijski program na osnovnim akademskim studijama izdvaja listu obaveznih predmeta. Prvo ispisati sve podatke o studijskom programu, a zatim oznaku, naziv i broj ESPB bodova svih obaveznih predmeta za taj studijski program." %}

{% include lab/exercise.html broj="11.5" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate za svaki predmet ispisuje oznaku  i naziv, a zatim i spisak studenata koji su upisali taj predmet u \v skolskoj godini koja se unosi sa standardnog ulaza. Rezultat urediti prema prezimenu i imenu studenta rastuće." %}

{% include lab/exercise.html broj="11.6" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate za svaki studijski program koji je u poslednjih 10 godina upisalo više od 30 studenata izdvajaju podaci o najmlađem studentu koji je upisao taj studijski program. Za najmlađeg studenta po studijskom programu izdvojiti naziv studijskog programa koji studira, indeks, ime i prezime studenta, datum upisa na fakultet, broj položenih predmeta i prosečnu ocenu zaokruženu na 2 decimale. Rezultat urediti prema nazivu studijskog programa." %}