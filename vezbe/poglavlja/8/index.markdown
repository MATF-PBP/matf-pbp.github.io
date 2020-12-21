---
layout: page
title: 8. Osnovni koncepti programiranja Java/SQL aplikacija sa dinamičkim SQL naredbama (JDBC)
under_construction: true
---

Java Database Connectivity (JDBC) is a Java programming API that standardizes the
means to work and access databases. In JDBC the code is easily portable between several
RDBMS vendors. The only changes required to the code are normally which JDBC driver
to load and the connection string. JDBC uses only dynamic SQL and it is very popular.

Before you can execute SQL statements in any SQL program, you must be connected to a data source.

![](./Slike/jdbc.png)

Though there are several types of JDBC drivers such as type 1, 2, 3 and 4; type 1 and 3
are not commonly used, and DB2’s support of these types has been deprecated. For type
2, there are two drivers as we will describe shortly, but one of them is also deprecated.

Type 2 and type 4 are supported with DB2 software, as shown in Table 9.1. Type 2 drivers
need to have a DB2 client installed, as the driver uses it to establish communication to
the database. Type 4 is a pure Java client, so there is no need for a DB2 client, but the
driver must be installed on the machine where the JDBC application is running.

| Driver Type | Driver Name | Packaged as | Supports | Minimum level of SDK for Java required |
| ----------- | ----------- | ----------- | -------- | -------------------------------------- |
| Type 2 | DB2 JDBC Type 2 Driver for Linux, UNIX® and Windows (Deprecated) | db2java.zip | JDBC 1.2 and JDBC 2.0 | 1.4.2 |
| Type 2 and Type 4 | IBM Data Server Driver for JDBC and SQLJ | db2jcc.jar and sqlj.zip | JDBC 3.0 compliant | 1.4.2 |
| Type 2 and Type 4 | IBM Data Server Driver for JDBC and SQLJ | db2jcc4.jar and sqlj4.zip | JDBC 4.0 and earlier | 6 |

As mentioned earlier and shown also in Table 9.1, Type 2 is provided with two different
drivers; however the DB2 JDBC Type 2 Driver for Linux, UNIX and Windows, with
filename db2java.zip is deprecated.

When you install a DB2 server, a DB2 client or the IBM Data Server Driver for JDBC and
SQLJ, the db2jcc.jar and sqlj.zip files compliant with JDBC 3.0 are automatically added
to your classpath.

## 8.1 Kreiranje konekcije

A connection to a database can be obtained using the DriverManager class of the java.sql
package.

Java.sql package defines the classes and interfaces required for the JDBC program to access
the relation data stored in a database. These APIs can be used to connect to the relational
database and manipulate the data (insert, update, delete, and so on) stored in tabular
form using the SQL standard. The interfaces defined in this package are implemented by
the driver specific classes and the definition can differ from vendor to vendor.

Before getting connection, the driver specific classes must be loaded and registered to the
`DriverManager`. Any number of drivers can be loaded and registered with the `DriverManager` using the `forName` method:

```java
class ProgramName {
    static {
        try {
            Class.forName("com.ibm.db2.jcc.DB2Driver");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    // ...
}
```

Java supports a special block, called static block (also called static clause) which can be
used for static initializations of a class. This code inside static block is executed only
once: the first time you make an object of that class or the first time you access a static
member of that class (even if you never make an object of that class). We will be using
static blocks in our programs to load and register DB2 JDBC driver, as shown in the code
example above.

The `forName` method take a string argument whose value is the name of the package which
implements the interfaces defined in `java.sql` package.

The connection to a database can be obtained by calling the `getConnection` method of
`DriverManager` class. This method takes a string value (URL) as an input, which gives
the information required to connect to the database. A typical URL format for Type 4
driver is:

```
jdbc:db2://<servername>:<port number>/<database name>
```

The connection to a database is closed by calling `close` method of `Connection` class.

The code in the following example returns the connection as `Connection` class object:

```java
public static void main(String argv[]) {
    Connection con = null;
    String url = "jdbc:db2://localhost:50000/stud2020";
    
    // Open database connection
    con = DriverManager.getConnection(url, "student", "abcdef");

    // Do something...

    // Close database connection
    con.close();
}
```

Možemo koristiti i try-with-resources naredbu u okviru koje ćemo otvoriti konekciju. Naredba se stara o otvorenim resursima i na kraju ih automatski zatvara. Neophodno je da deklarisani resursi implementiraju interfejs `AutoClosable`. 

```java
public static void main(String argv[]) {
    String url = "jdbc:db2://localhost:50000/stud2020";
    try (Connection con = DriverManager.getConnection(url, "student", "abcdef")) {

        // Do something...
    }
}
```

## 8.2 Obrada SQL grešaka

Just like any Java program, in JDBC, exception handling is done using the try-catch
block. A DB2 application throws a `SQLException` whenever it encounters a SQL error or
a `SQLWarning` whenever it encounters a SQL warning when executing SQL statements.

An object of `SQLException` is created and thrown whenever an error occurs while accessing
the database. The `SQLException` object provides the information listed in Table 11.1.

| SQLException information | Description | Method to retrieve this information |
| --- | --- | --- |
| Message | Textual representation of the error code. | getMessage |
| SQLState | The SQLState string. | getSQLState |
| ErrorCode | An integer value that indicates the error which caused the exception to be thrown. | getErrorCode |

Apart from the above information, the DB2 JCC driver provides an extra interface
`com.ibm.db2.jcc.DB2Diagnosable`. This interface gives more information regarding the
error that occurred while accessing the DB2 database.

If multiple `SQLExceptions` are thrown, they are chained. The next exception information
can be retrieved by calling the getNextException method of the current `SQLException`
object. This method will return null if the current `SQLException` object is last in the
chain. A while loop in the catch block of the program can be used to retrieve all the
`SQLException` objects one by one.

```java
Connection con = null;

try {
    // Open database connection
    con = DriverManager.getConnection(url, username, password);

    // Do something...

    // Close database connection
    con.close();
}
catch (SQLException e) {
    e.printStackTrace();

    System.out.println(
        "SQLCODE: " + e.getErrorCode() + "\n" +
        "SQLSTATE: " + e.getSQLState() + "\n" +
        "PORUKA: " + e.getMessage());

    // We need to close the database connection,
    // even if error occures.
    // Since the close method can also throw SQLException,
    // we need to catch it,
    // but not deal with it.
    try {
        if (null != con) {
            con.close();
        }
    } catch (SQLException e2) {
    }

    System.exit(1);
}
catch (Exception e) {
    e.printStackTrace();

    // Same as in above catch clause.
    try {
        if (null != con) {
            con.close();
        }
    } catch (SQLException e2) {
    }

    System.exit(2);
}
```

## 8.3 Upravljanje podacima

After getting the connection, data can be selected, inserted, updated, or deleted from the
relational tables using SQL statements. JDBC driver implements two interfaces `Statement`
and `PreparedStatement` for this purpose. An object of any of these classes is required for
running an SQL statement.


### 8.3.1 Tipovi podataka

To write efficient JDBC and SQLJ programs, you need to use the best mappings between
Java™ data types and table column data types.

Table 9.2 summarizes the mappings of Db2 or IBM Informix data types to Java data types
for ResultSet.getXXX methods in JDBC programs, and for iterators in SQLJ programs.
This table does not list Java numeric wrapper object types, which are retrieved using
ResultSet.getObject.

| SQL data type | Recommended Java data type or Java object type | Other supported Java data types |
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

Table 9.3 summarizes the mappings of Java data types to database data types for PreparedStatement.setXXX or ResultSet.updateXXX methods in JDBC programs, and for
input host expressions in SQLJ programs. When more than one Java data type is listed,
the first data type is the recommended data type.

| Java data type | Database data type |
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

### 8.3.2 Interfejs Statement

An object of `Statement` (or class implementing the `Statement` interface) can be used to
execute the SQL statement which does not contain parameter markers. An object can be
created from the `Connection` object using `createStatement` method.

Any number of statements can be created for a particular connection object.

`Statement` interface defines `executeQuery` and `executeUpdate` methods to execute a query
statement. The `executeQuery` method is used when the result set is expected (for example, for the `SELECT` statement) as output of the query. Alternatively, `executeUpdate`
method is used for updating the database contents (for example, `INSERT`, `UPDATE`, and
`DELETE` statements). The `executeQuery` method returns the `ResultSet` object, which represents a set of rows returned by the `SELECT` query. This `ResultSet` object can be used
to fetch the result row by row. Method `executeUpdate` returns an integer value, which
indicates the number of rows updated, inserted, or deleted from the database based on
the type of SQL statement.

The following example illustrates the retrieval of data using the `Statement.executeQuery`
method. This method returns a result table in a `ResultSet object`. After you obtain the
result table, you need to use `ResultSet` methods to move through the result table and
obtain the individual column values from each row.

Da bismo iterirali kroz rezultuju\'cu tabelu naredbe `SELECT` koja ne sadr\v zi parametarske oznake, potrebno je da ispratimo naredne korake:

1. Kreirati objekat interfejsa `Statement` pozivom metoda
```java
Statement stmt = con.createStatement(...);
```
2. Kreirati objekat interfejsa `ResultSet` pozivom metoda
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Pozicionirati se na red koji je potrebno pročitati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    // ...
}
```
4. Pozvati odgovarajući metod nad objektom `kursor` iz familije metoda `ResultSet.getXXX(int columnIndex)` za dohvatanje vrednosti iz kolone rezultujuće tabele sa indeksom `columnIndex` 
5. Eventualno proveriti da li je dohvaćena `NULL` vrednost pozivom `kursor.wasNull()`
6. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora
7. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

{% include lab/exercise.html broj="8.1" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izlistava šifre i nazive svih predmeta koji imaju više od 20 ESPB bodova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_1/Main.java, java)

The `Statement.executeUpdate` is one of the JDBC methods that you can use to update
tables. You can use the `Statement.executeUpdate` method to do the following things:

- Execute data definition statements, such as `CREATE`, `ALTER`, `DROP`, `GRANT`, `REVOKE`.
- Execute `INSERT`, `UPDATE`, `DELETE`, and `MERGE` statements that do not contain parameter markers.

Da bismo izvr\v sili ove SQL naredbe bez parametarskih oznaka, potrebno je da ispratimo naredne korake:

1. Kreirati objekat interfejsa `Statement` pozivom metoda
```java
Statement stmt = con.createStatement();
```
2. Pozvati metod `stmt.executeUpdate(String sql)`
3. Pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

Method `Statement.executeUpdate` returns an integer which represents the number of updated rows.

{% include lab/exercise.html broj="8.2" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji u tabelu `PREDMET` unosi podatak o predmetu čiji je identifikator 2001, šifra Pred1, naziv Predmet 1, koji se sluša u prvom semestru i nosi `5` ESPB bodova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_2/Main.java, java)

### 8.3.3 Interfejs `PreparedStatement`

An object of `PreparedStatement` (or a class implementing the `PreparedStatement` interface)
can be used to run the queries, which can contain parameter markers. A `PreparedStatement` object can be created using the `prepareStatement` method of `Connection` object.
`PreparedStatement` extends the `Statement` interface.

If the SQL statement contains parameter markers, the values for these parameter markers
need to be set before executing the statement. Value can be set using `setXXX` methods
of `PreparedStatement` object where `XXX` denoted the data type of the parameter marker.
`setXXX` methods are also called setter methods.

The following are the examples of setXXX methods:

- setInt,
- setString,
- setDouble,
- setBytes,
- setClob,
- setBlob
- ...

After setting the parameter values, the SQL statement can be executed using any of the
`executeQuery`, `executeUpdate`, or `execute` method based on the SQL type.

{% include lab/exercise.html broj="8.3" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji svim predmetima koji imaju X ESPB bodova, postavlja broj bodova na Y. Nakon toga ispisati broj ažuriranih redova. Brojevi X i Y se učitavaju sa standardnog ulaza." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_3/Main.java, java)

{% include lab/exercise.html broj="8.4" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izdvaja indeks, ime,
prezime i naziv smera svih studenata koji su položili tačno N predmeta, kao i spisak tih
predmeta (naziv i ocena). Broj N se učitava sa standardnog ulaza." %}

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

{% include lab/exercise.html broj="8.5" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izdvaja ime, prezime i datum rođenja za sve studentkinje (pol = 'z') iz tabele `DOSIJE`. Ukoliko datum rođenja nije poznat, ispisati `Nepoznat`." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_5/Main.java, java)

### 8.3.5 Podešavanje kursora

A `ResultSet` maintains a cursor, which points to a row in the result set. It works like a
cursor in database programs. You can scroll the cursor to a specific row in the result set
to access or manipulate the column values for that row. The cursor can point to only one
row at a time. The row to which it points at a point in time is called the current row.
There are different ways to move the cursor of a `ResultSet` to a row in the result set. 

The following three properties of a `ResultSet` need to be discussed before you can look at
an example:

- Scrollability
- Concurrency
- Holdability

Scrollability determines the ability of the `ResultSet` to scroll through the rows. By default,
a `ResultSet` is scrollable only in the forward direction. When you have a forward-only
scrollable `ResultSet`, you can move the cursor starting from the first row to the last row.
Once you move to the last row, you cannot reuse the ResultSet because you cannot scroll
back in a forward-only scrollable `ResultSet`. You can also create a `ResultSet` that can scroll
in the forward as well as the backward direction. We call this `ResultSet` a bidirectional
scrollable `ResultSet`.

A bidirectional scrollable `ResultSet` has another property called update sensitivity. It
determines whether the changes in the underlying database will be reflected in the result
set while you are scrolling through its rows. A scroll sensitive `ResultSet` shows you changes
made in the database, whereas a scroll insensitive one would not show you the changes
made in the database after you have opened the `ResultSet`. The following three constants
in the `ResultSet` interface are used to specify the scrollability of a `ResultSet`:

- `TYPE_FORWARD_ONLY`: Allows a `ResultSet` to scroll only in the forward direction.
- `TYPE_SCROLL_SENSITIVE`: Allows a `ResultSet` to scroll in the forward and backward
directions. It makes the changes in the underlying database made by other transactions or statements in the same transaction visible to the `ResultSet`. This type of
`ResultSet` is aware of the changes made to its data by other means.
- `TYPE_SCROLL_INSENSITIVE`: Allows a `ResultSet` to scroll in the forward and backward
directions. It does not make the changes in the underlying database made by other
transactions or statements in the same transaction visible to the `ResultSet` while
scrolling. This type of `ResultSet` determines its data set when it is open and the
data set does not change if it is updated through any other means except through
this `ResultSet` itself. If you want to get up-to-date data, you must re-execute the
query.

Concurrency refers to the ability of a `ResultSet` to update data. By default, a `ResultSet` is
read-only and it does not let you update its data. If you want to update data in a database
through a `ResultSet`, you need to request an updatable result set from the JDBC driver.
The following two constants in the `ResultSet` interface are used to specify the concurrency
of a `ResultSet`:

- `CONCUR_READ_ONLY`: Makes a result set read-only.
- `CONCUR_UPDATABLE`: Makes a result set updatable.

Holdability refers to the state of a `ResultSet` after a transaction that it is associated with
has been committed. A `ResultSet` may be closed or kept open when the transaction is
committed. The default value of the holdability of a `ResultSet` is dependent on the JDBC
driver. For DB2 driver, the holdability is set to `false`. It is specified using one of the
following two constants defined in the `ResultSet` interface:

- `HOLD_CURSORS_OVER_COMMIT`: Keeps the `ResultSet` open after the transaction is committed.
- `CLOSE_CURSORS_AT_COMMIT`: Closes the `ResultSet` after the transaction is committed.

Da bismo definisali ponašanje `ResultSet` objekta, potrebno je da prosledimo odgovarajuće
opcije metodima `createStatement`, odnosno, `prepareStatement`, koji su definisani nad
objektom interfejsa `Connection`, u zavisnosti od toga da li želimo da naredbu izvršimo kroz
interfejse `Statement` ili `PreparedStatement`, redom.

U slučaju kreiranja objekta `Statement`, na raspolaganju su nam naredna dva preopterećenja metoda `createStatement`:

- Metod sa dva argumenta:
   - `int resultSetType` — Tip skupa rezultata.
   - `int resultSetConcurrency` — Tip konkurentnosti.
- Metod sa tri argumenta:
   - `int resultSetType`
   - `int resultSetConcurrency`
   - `int resultSetHoldability` — Tip zadrživosti kursora prilikom izvršavanja operacije pohranjivanja.

U slučaju kreiranja objekta `PreparedStatement`, na raspolaganju su nam naredna dva
preopterećenja metoda `prepareStatement`, sa istim značenjima parametara kao i u slučaju
metoda `createStatement`:

- Metod sa tri argumenta:
   - `String sql` — Niska koja sadrži tekstualni oblik SQL naredbe. Može sadržati
nula ili više parametarskih oznaka.
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

Postoje dve procedure za ažuriranje informacija u bazi podataka na osnovu `ResultSet`
kursora:

- Korišćenjem pozicionirajuće `UPDATE` naredbe.
- Korišćenjem JDBC metoda `updateXXX` i `updateRow`.

Kao što znamo, možemo koristiti pozicionirajuću `UPDATE` naredbu za menjanje podataka
u bazi podataka, na osnovu tekućeg reda kursora, tako što se na kursor referiše u `WHERE CURRENT OF` 
klauzi naredbe. Da bismo dohvatili naziv `ResultSet` kursora, možemo iskoristiti
metod `ResultSet.getCursorName`, koji vraća nisku sa nazivom kursora koji je vezan
za taj `ResultSet`. Na primer, naredni fragment koda ilustruje kako možemo ažurirati broj bodova za sve predmete iz tabele `PREDMET` korišćenjem pozicionirajuće `UPDATE` naredbe:

```java
String upit =
    "SELECT SIFRA, " +
    "       NAZIV, " + 
    "       BODOVI " +
    "FROM   PREDMET " +
    "FOR    UPDATE OF BODOVI";

Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(upit);

String nazivKursora = rs.getCursorName();
String azuriranje =
    "UPDATE PREDMET " +
    "SET    BODOVI = ? " +
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

Drugi pristup podrazumeva kori\v s\'cenje specifi\v cnih metoda radi a\v zuriranja podataka u kursoru, \v cime se efektivno vr\v si a\v zuriranje u bazi podataka. Naredni koraci opisuju postupak a\v zuriranja postoje\'cih slogova u `ResultSet`:

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
    // ...
}
```
4. Pozvati odgovarajući metod nad objektom `kursor` iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)` za ažuriranje vrednosti kolone tabele sa indeksom `columnIndex` novom vrednošću `x`
5. Ako želimo da poništimo izmene, možemo pozvati metod `kursor.cancelRowUpdates()`
6. Ukoliko želimo da zapravo izvršimo izmene nad slogom u BP, potrebno je da pozovemo metod `kursor.updateRow()`. U suprotnom će, prelaskom na drugi red, sve izmene biti ignorisane
7. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora
8. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

{% include lab/exercise.html broj="8.7" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje sadržaj tabele
`PREDMET` i, u istoj iteraciji, ukoliko je broj bodova jednak X, postavlja se broj bodova na
Y i ispisuje se poruka da je promena izvršena, zajedno sa ispisom novih podataka o
tom predmetu. Brojevi X i Y se učitavaju sa standardnog ulaza." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_7/Main.java, java)

### 8.3.7 Unos redova korišćenjem ResultSet kursora

Ono \v sto nismo imali prilike da vidimo jeste da se pomo\'cu kursora mogu uneti novi slogovi u tabelu. Za unos novog sloga u tabelu koristi se specijalni „slog za unos“. Ovaj specijalni slog zapravo predstavlja bafer za konstruisanje novog sloga koji \'ce biti unet u odgovaraju\'cu tabelu.

Konstrukcija novog sloga se vrši u dva koraka:
1. Potrebno je postaviti vrednosti svih kolona rezultujuće tabele u „slogu za unos“ pozivom odgovarajućih metoda iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)`
2. Kada su svim kolonama u „slogu za unos“ dodeljene odgovaraju\'ce vrednosti, tada je potrebno izvr\v siti unos baferisanog „sloga za unos“ u rezultujuću tabelu.

Unos baferisanog „slogu za unos“ se izvr\v sava pozivom metoda `void ResultSet.insertRow()`. Me\dj utim, da bismo zapravo uspeli da konstrui\v semo ovaj specijalni slog, neophodno je da signaliziramo kursoru da \v zelimo da menjamo ba\v s taj red, a ne teku\'ci red kroz koji se iterira u kursoru. Ovo je mogu\'ce uraditi pozivom metoda `void ResultSet.moveToInsertRow()`. Prilikom poziva ovog metoda, kursor se pozicionira na „slog za unos“ i ujedno se pamti redni broj tekućeg sloga koji se obrađuje u kursoru. Nakon \v sto smo zavr\v sili proceduru za unos opisanu prethodnim koracima, potrebno je pozvati metod `void ResultSet.moveToCurrentRow()` koji \'ce ponovo pozicionirati kursor na prethodno zapamćeni tekući slog koji se obrađuje u kursoru.

Dakle, celokupna procedura za unos novog sloga je data narednim koracima:

1. Kreirati objekat interfejsa `Statement` pozivom metoda 
```java
Statement stmt = con.createStatement(..., ResultSet.CONCUR_UPDATABLE, ...);
```
2. Kreirati objekat interfejsa `ResultSet` pozivom metoda 
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Pozicionirati se na „slog za unos“ pozivom metoda `kursor.moveToInsertRow()`
4. Pozvati odgovarajući metod nad objektom `kursor` iz familije metoda `ResultSet.updateXXX(int columnIndex, XXX x)` za postavljanje vrednosti kolone „sloga za unos“ sa indeksom `columnIndex` novom vrednošću `x`
5. Ukoliko želimo da zapravo unesemo novi slog u BP, potrebno je da pozovemo metod `kursor.insertRow()`. U suprotnom će, prelaskom na drugi red, sve izmene biti ignorisane
6. Pozicionirati se na tekući slog u rezultujućoj tabeli kursora pozivom metoda `kursor.moveToCurrentRow()`
7. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora
8. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

{% include lab/exercise.html broj="8.8" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji:\n
1. Kreira tabelu `UNETI_PREDMETI` \v cije su kolone: (1) identifikator predmeta i (2) broj polo\v zenih ispita za taj predmet. Postaviti odgovaraju\'ce primarne i strane klju\v ceve.\n
2. Za svaki predmet koji nije prethodno obra\dj en (tj. koji se ne nalazi u tabeli `UNETI_PREDMETI`) pronalazi statistiku koja se sastoji od njegovog identifikator i broj polo\v zenih ispita.\n
3. Za svaku prona\dj enu statistiku ispisuje podatke na standardni izlaz i pita korisnika da li \v zeli da unete statistiku u tabelu `UNETI_PREDMETI`. Ukoliko korisnik potvrdi, potrebno je uneti statistiku u datu tabelu i ispisati poruku o uspehu. U suprotnom, ispisati poruku da je korisnik poni\v stio unos." %}

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
    "SELECT SIFRA, " +
    "       NAZIV, " + 
    "       BODOVI " +
    "FROM   PREDMET";

Statement stmt = con.createStatement();
ResultSet rs = stmt.executeQuery(upit);

String nazivKursora = rs.getCursorName();
String brisanje =
    "DELETE FROM ISPIT " +
    "WHERE  CURRENT OF " + nazivKursora;
Statement ps = con.prepareStatement();

while (rs.next()) {
    String nazivPredmeta = rs.getString(2);

    ps.executeUpdate(brisanje);
    System.out.println("Obrisan je predmet " + nazivPredmeta);
}
```

Brisanje sloga iz objekta `ResultSet` je jednostavnije nego a\v zuriranje ili unos sloga. Naredni koraci defini\v su operaciju brisanja sloga iz kursora:

1. Kreirati objekat interfejsa `Statement` pozivom metoda
```java
Statement stmt = con.createStatement(..., ResultSet.CONCUR_UPDATABLE, ...);
```
2. Kreirati objekat interfejsa `ResultSet` pozivom metoda 
```java
ResultSet kursor = stmt.executeQuery(sql);
```
3. Pozicionirati se na slog koji je potrebno obrisati metodima za prolazak kroz kursor. Na primer:
```java
while (kursor.next()) {
    // ...
}
```
4. Pozvati metod `kursor.deleteRow()` koji će obrisati slog u bazi podataka
5. Nakon svih iteracija, pozvati metod `kursor.close()` radi zatvaranja kursora
6. Nakon zatvaranja kursora, pozvati metod `stmt.close()` radi zatvaranja objekta naredbe

The `deleteRow()` method deletes the row from the `ResultSet` and, at the same time, it
deletes the row from the database. There is no way to cancel the delete operation except
by rolling back the transaction. If the auto-commit mode is enabled on the `Connection`,
`deleteRow()` will permanently delete the row from the database.

{% include lab/exercise.html broj="8.9" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji briše sve nepoložene ispite u godini koja se zadaje sa standarnog ulaza. Nakon svakog brisanja ispita, ispisati naredne informacije o njemu na standardni izlaz: indeks, oznaku roka, godinu roka i identifikator predmeta." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_9/Main.java, java)

### 8.3.9 Ugnežđeni kursori

U JDBC aplikacijama se kursori jednostavno ugnežđuju: potrebno je celokupnu obradu
unutrašnjeg kursora smestiti u okviru obrade jednog reda iz spoljašnjeg kursora. Naredni
primer ilustruje upotrebu ugnežđenih kursora.

{% include lab/exercise.html broj="8.10" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji izdvaja indeks, ime, prezime i naziv smera svih studenata koji su položili tačno N predmeta, kao i spisak tih predmeta (naziv i ocena). Broj N se učitava sa standardnog ulaza. Za svakog studenta napraviti posebnu sekciju izveštaja." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/ispiti.sql, sql)
include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/predmeti.sql, sql)
include_source(vezbe/primeri/poglavlje_8/src/zadatak_8_10/Main.java, java)

## 8.4 Zadaci za vežbu

{% include lab/exercise.html broj="8.11" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje podatke za predmet čiji je identifikator `640`." %}

{% include lab/exercise.html broj="8.12" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji ispisuje broj studenata koji su upisali studije u godini koja se unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.13" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji za svaku ocenu od 6 do 10 ispisuje ime i prezime studenta koji je poslednji položio neki ispit sa tom ocenom. U slučaju da ima više takvih studenata, klauzom `LIMIT 1` naredbe `SELECT INTO` se osigurati da rezultat vrati najviše 1 rezultat. (Pomoć: Koristiti `for` petlju za menjanje vrednosti matične promenljive koja sadrži ocenu, pa u svakoj iteraciji dohvatiti informaciju za tekuću vrednost te matične promenljive.)" %}

{% include lab/exercise.html broj="8.14" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se dodaje da je za polaganje predmeta čiji je identifikator `720` uslov da se položi predmet čiji je identifikator `655`." %}

{% include lab/exercise.html broj="8.15" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se u tabelu `NIVO_KVALIFIKACIJE` dodaje novi nivo čiji se identifikator, naziv i stepen unose sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.16" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se svim obaveznim predmetima na smeru `'Informatika'` povećava broj semestra za 1." %}

{% include lab/exercise.html broj="8.17" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se, za sve položene ispite čiji se naziv predmeta unosi sa standardnog ulaza, ocena uvećava za 1." %}

{% include lab/exercise.html broj="8.18" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se u tabeli `USLOVNI_PREDMET` brišu svi redovi koji se odnose na predmete koji su uslovni da bi se položio predmet čiji se identifikator unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.19" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki kojim se brišu svi podaci o ispitima za studenta čiji se indeks unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="8.20" tekst="Napisati Java program u kojem se naredbe izvr\v savaju dinami\v cki koji iz tabele `PREDMET` briše podatak o predmetu čija se šifra unosi sa standardnog ulaza. " %}
