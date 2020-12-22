---
layout: page
title: 4. Aplikacije sa dinamičkim SQL naredbama
under_construction: true
---

Do sada smo konstruisali aplikacije sa ugnežđenim SQL-om koje su koristile statičke SQL naredbe. Da se podsetimo, u pitanju su bile naredbe čiji je plan izvršavanja moguće izračunati u fazi pretprocesiranja. Ipak, kreiranje statičkih SQL aplikacija nam nije dovoljno ukoliko su nam neki elementi SQL naredbi nepoznati u fazi prevođenja programa. Štaviše, nekada je potrebno konstruisati aplikaciju u kojoj nijedna SQL naredba nije unapred poznata (sa izuzetkom naredba povezivanja na bazu podataka, prekidanja konekcije i sl.). 

Dizajniranje složenijih SQL aplikacija zahteva nešto moćnije alate za rad koji se oslanjaju na rad sa dinamičkim SQL naredbama, čime se kreiraju dinamičke SQL aplikacije. Samim tim što je programiranje dinamičkih SQL aplikacija moćnije, to je i složenije jer, pored velikog broja novih načina za pisanje aplikacija, otvara i nove teme, pre svega poređenje složenosti, performansi i upotrebljivosti u odnosu na statičke SQL aplikacije. Međutim, u ovom poglavlju, mi ćemo se baviti isključivo programiranjem dinamičkih SQL aplikacija, ne i pitanjima njihovog razvoja.

Pre nego što započnemo sa poglavljem, napomenimo da ukoliko se koriste struktuirani podaci ili podaci koji su nekog od tipa `LOB` (`BLOB`, `CLOB`, `DBLOB`, itd.), onda je za neke od naredbi potrebno izvršiti dodatna procesiranja. Zbog toga ćemo pretpostaviti da naše aplikacije neće baratati takvim podacima.

## 4.1 Naredbe za ugnežđavanje dinamičkih SQL naredbi

Da bismo mogli da izvršimo SQL naredbu dinamički, prvo je neophodno skladištiti je u matičnu promenljivu čiji je tip niska. Dinamička SQL naredba podrazumeva da matična
promenljiva-niska sadrži tekstualnu formu SQL naredbe. Ova naredba, koja je zadata kao tekst, biće procesirana dinamički, odnosno, tek u fazi izvršavanja programa.

Naredna slika prikazuje deo programskog koda koji procesira dinamičku SQL naredbu `SELECT`. Na početku, dinamička SQL naredba je zadata kao niska. Prvi korak se sastoji u procesiranju tog tekstualnog oblika SQL naredbe. Rezultat ove naredbe je pripremljena SQL naredba, odnosno, njen izvršivi oblik. Dodatno, moguće je ovom naredbom upisati informacije o samoj dinamičkoj SQL naredbi u odgovarajuće strukture podataka. Nakon toga, drugi korak se sastoji u samom izvršavanju pripremljene naredbe. Prilikom izvršavanja se informacije koje su bile nepoznate zamenjuju konkretnim vrednostima, naravno, ako je dinamička SQL naredba imala nepoznate informacije.

!["Proces izvršavanja dinamičkih SQL naredbi u dva koraka"](./Slike/dinamicke_naredbe.png){:class="ui centered huge image"}

Tekstualni oblik naredbe neće biti procesiran prilikom prekompilacije aplikacije. Zapravo, sam tekstualni sadrži ne mora ni da postoji u fazi pretprocesiranja. Umesto toga, o dinamičkoj SQL naredbi možemo razmišljati kao matičnoj promenljivoj tokom faze prekompilacije na koju će se referisati prilikom faze izvršavanja.

Sadržaj dinamičkih SQL naredbi odgovara istoj sintaksi kao i za statičke SQL naredbe. Ipak, postoje neki izuzeci nametnuti od strane DB2 SUBP:

- Tekstualni oblik naredbe ne sme počinjati ključnim rečima `EXEC SQL`.
- Tekstualni oblik naredbe se ne sme završavati karakterom za označavanje kraja naredbe (karakter tačka-zapeta (`;`)). Jedini izuzetak od ovog pravila jeste naredba `CREATE TRIGGER`.

Da bismo tekstualni oblik SQL naredbe transformisali u njegov izvršivi oblik, potrebno je izvršiti odgovarajuće korake, odnosno, naredbe za podršku dinamičkom SQL-u. Ove naredbe operišu nad matičnim promenljivama tako što procesiraju tekstualni oblik naredbe, proveravajući sintaksu naredbe i druga pravila koja ta naredba mora da zadovolji. Ovih naredbi ima više, a mi ćemo prikazati svaku od njih u nastavku poglavlja. Za sada, dajmo kratak opis naredbi kako bismo ih imali na umu tokom njihove detaljnije diskusije:

- Naredba `EXECUTE IMMEDIATE` vrši pripremu i izvršavanje dinamičkih SQL naredbi u jednom koraku.

- Naredba `PREPARE` vrši prvu fazu izvršavanja dinamičke SQL naredbe: od tekstualnog oblika naredbe kreira njen izvršivi oblik, dodaje joj naziv i, opciono, upisuje informacije o dinamičkoj SQL naredbi u specijalnim SQLDA strukturama.

- Naredba `EXECUTE` vrši drugu fazu izvršavanja dinamičke SQL naredbe: izvršava prethodno pripremljenu dinamičku SQL naredbu.

- Naredba `DESCRIBE` upisuje informacije o dinamičkoj SQL naredbi u specijalnim SQLDA strukturama.

### 4.1.1 Prikazivanje detalja greške u programima

S obzirom da se dinami\v cke SQL naredbe procesiraju tek u fazi izvr\v savanja programa, Db2 pretprocesor nije u stanju da analizira upit i da nam u fazi prevo\dj enja javi ukoliko je do\v slo do gre\v ske. Ovo nam mo\v ze predstavljati problem po\v sto na\v sa funkcija za obradu gre\v ske samo ispisuje njen kod. Zna\v cilo bi nam da imamo neke dodatne informacije ispisane pored samog koda.

Srećom po nas, ovo je moguće i veoma je jednostavno za uraditi. Ukoliko uključimo zaglavlje `sql.h`, na raspolaganju će nam biti funkcija `sqlaintp`, koju možemo da iskoristimo da dohvatimo tekstualni opis greške koja se dogodila. Ova funkcija ima naredna četiri argumenta:

1. `char ∗pBuffer` - Karakterni bafer u koji će biti smešten tekstualni opis greške. Ukoliko je veličina bafera manja nego što je dužina opisa, opis će biti skraćen na veličinu bafera i biće postavljena terminirajuća nula na kraju bafera.
2. `short BufferSize` - Veličina bafera koji se prosleđuje kao prvi argument.
3. `short LineWidth` - Najveća veličina svake linije u tekstu opisa. Linije će biti razbijene odgovarajućim karakterom za novi red tako da reči u datoj liniji ostanu cele. Ukoliko prosledimo vrednost 0, poruka neće sadržati karaktere za novi red.
4. `struct sqlca ∗pSqlca` - Pokazivač na strukturu `sqlca` na osnovu koje će funkcija zaključiti do koje greške je došlo. S obzirom da mi koristimo globalnu strukturu koja se dobija uklju\v civanjem zaglavlja `SQLCA` Db2 pretprocesorskom direktivom `INCLUDE`, a koja se koristi za dohvatanje informacije o gre\v sci, onda \'cemo ovde prosle\dj ivati upravo tu globalno-definisanu strukturu.

Na\v sa funkcija za obradu gre\v ske \'ce sada izgledati:

```c
#include <sql.h> // Za funkciju sqlaintp()

// Podsetnik: Naredna Db2 pretprocesorka direktiva ce ucitati zaglavlje `sqlca.h` 
// i definisati globalnu `sqlca` strukturu koja se koristi u celom programu.
EXEC SQL INCLUDE SQLCA;

void checkSQL(const char *str)
{
    // `SQLCODE` je makro koji se razvija u `sqlca.sqlcode`
    if(sqlca.sqlcode < 0)
    {
        char Buffer[1024];
        short BufferSize = sizeof(Buffer);
        short LineWidth = 50;
        sqlaintp(Buffer, BufferSize, LineWidth, &sqlca);

        printf("Greska %d: %s\n", sqlca.sqlcode, str);
        printf("%s\n", Buffer);
        exit(EXIT_FAILURE);
    }
}
```

## 4.2 Naredba `EXECUTE IMMEDIATE`

Naredba `EXECUTE IMMEDIATE` priprema izvršivi oblik dinamičke SQL naredbe od njegovog tekstualnog oblika i odmah zatim ga i izvršava. Može biti korišćena za pripremu i izvršavanje SQL naredbi koje ne sadrže ni matične promenljive ni parametarske oznake.

Sintaksa ove naredbe je data u nastavku:

```sql
EXECUTE IMMEDIATE <IZRAZ>
```

Vrednost `<IZRAZ>` predstavlja izraz koji sadrži naredbu koja će se izvršiti. Izraz mora izračunavati nisku čija je najveća veličina 2 MB. Deo spiska naredbi koje mogu biti dinamički izvršene korišćenjem ove SQL naredbe je dat u nastavku:

- `ALTER`
- `CALL`
- `COMMIT`
- Složena SQL naredba (linijska)
- Složena SQL naredba (kompilirana)
- `CREATE`
- `DELETE`
- `DROP`
- `GRANT`
- `INSERT`
- `LOCK TABLE`
- `MERGE`
- `ROLLBACK`
- `SAVEPOINT`
- `SET CURRENT LOCK TIMEOUT`
- `SET <PROMENLJIVA>`
- `UPDATE`

Niska koja sadrži naredbu ne sme imati parametarske oznake ili reference na matične promenljive i ne sme počinjati ključnim rečima `EXEC SQL`. Takođe, onda ne sme sadržati operator završavanja naredbe sa izuzetkom složenih SQL naredbi koje mogu sadržati tačku-zapetu (`;`) za razdvajanje naredbi u bloku izvršavanja.

Kada se naredba `EXECUTE IMMEDIATE` izvrši, specifikovana naredba se parsira i proveravaju se greške. Ako SQL naredba nije validna, ona se neće izvršiti i uslov koji je doveo do greške se izveštava kroz SQLCA. Ako je SQL naredba validna, ali njeno izvršavanje dovodi do greške, onda se ta greška takođe izveštava kroz SQLCA.

{% include lab/exercise.html broj="4.1" tekst="Napisati: 

- C/SQL program u kojem se naredbe izvršavaju dinamički koji čita SQL naredbu iz datoteke čiji se naziv zadaje kao prvi argument komandne linije. SQL naredba se čita do pojave karaktera `;` ili do kraja datoteke, ispisuje se korisniku, a potom se izvršava. Pretpostaviti da korisnik neće uneti naredbu `SELECT`, kao ni da neće sadržati parametarske oznake. Pretpostaviti da nareda koja se čita iz datoteke nije duža od 255 karaktera.

- Datoteku koja sadr\v zi SQL naredbu koja pove\'cava ESPB bodove svim predmetima za 1. Izvr\v siti naredbu iz ove datoteke napisanim programom." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_4/zadatak_4_1.sqc, c)

include_source(vezbe/primeri/poglavlje_4/zadatak_4_1.sql, sql)

**Pokretanje**: ./zadatak_4_1 zadatak_4_1.sql

## 4.3 Naredba `PREPARE`

Naredba `PREPARE` se koristi od strane aplikativnog programa da dinamički pripremi SQL naredbu za izvršenje. Ova naredba kreira izvršivu SQL naredbu koja se naziva *pripremljena naredba* (engl. *prepared statement*) na osnovu tekstualnog oblika naredbe koji se naziva *niska naredbe* (engl. *statement string*).

Sintaksa ove naredbe data je u nastavku:

```sql
PREPARE <NAZIV_NAREDBE>
[[OUTPUT] INTO <NAZIV_OPISIVAČA_IZLAZA>]
[INPUT INTO <NAZIV_OPISIVAČA_ULAZA>]
FROM (<MATIČNA_PROMENLJIVA>|<IZRAZ>)
```

Specifikovanjem vrednosti za `<NAZIV_NAREDBE>` se imenuje pripremljena naredba. Ako se isti naziv koristi za identifikaciju već postojeće pripremljene naredbe, onda se ta, prethodno pripremljena naredba, uništava. Ipak, postoji izuzetak od ovog pravila: `<NAZIV_NAREDBE>` ne sme već idenfitikovati pripremljenu naredbu koja predstavlja `SELECT` naredbu u otvorenom kursoru.

Pripremljena naredba se izračunava na osnovu niske naredbe koja se nalazi u vrednosti u `FROM` klauzi. Može se navesti bilo matična promenljiva čiji je identifikator `<MATIČNA_PROMENLJIVA>` i tip niska ili izraz `<IZRAZ>` koji vraća nisku.

U zavisnosti od korišćenja narednih klauza, ova naredba ima sledeće efekte:

- Klauzom `OUTPUT INTO` se informacije o izlaznim parametarskim oznakama u pripremljenoj naredbi upisuju u SQLDA promenljivu koja se specifikuje kao `<NAZIV_OPISIVAČA_IZLAZA>`.

- Klauzom `INPUT INTO` se informacije o ulaznim parametarskim oznakama u pripremljenoj naredbi upisuju u SQLDA promenljivu koja se specifikuje kao `<NAZIV_OPISIVAČA_ULAZA>`. Ulazne parametarske oznake se uvek smatraju za *potencijalno nedostajuće* (engl. *nullable*), bez obzira na njihovo korišćenje.

Umesto ovih klauza se može koristiti naredba `DESCRIBE` o kojoj će biti reči u kasnijem tekstu.

Naredbe koje se mogu pripremiti na ovaj način su iste one naredbe kao za naredbu `EXECUTE IMMEDIATE`, uz dodatak `SELECT` naredbe.

### 4.3.1 Parametarske oznake

Iako dinamička SQL naredba koja se priprema ne može da sadrži reference na matične promenljive, ona ipak može da koristi *parametarske oznake* (engl. *parameter marker*). Parametarske oznake mogu biti zamenjene vrednostima iz matičnih promenljivih u trenutku izvršavanja pripremljene naredbe.

Postoje dva načina za specifikovanje parametarskih oznaka:

- *Neimenovana* parametarska oznaka se navodi korišćenjem karaktera upitnika (`?`).

- *Imenovana* parametarska oznaka se navodi karakterom dvotačke (`:`) iza koje sledi identifikator parametarske oznake. Na primer, `:GODINA_UPISA`.

Oba opisana načina se koriste na istom mestu gde bi se koristile matične promenljive u slučaju da je SQL naredba statička. Iako imenovane parametarske oznake imaju istu sintaksu kao i matične promenljive, njihova upotrebna vrednost je svakako drugačija. Matična promenljiva sadrži vrednost u memoriji i koristi se direktno u statičkoj SQL naredbi. Imenovana parametarska oznaka predstavlja tek *mesto za zamenu* (engl. *placeholder*) za vrednost u dinamičkoj SQL naredbi i njena vrednost se navodi prilikom  izvršavanja pripremljene naredbe.

Takođe, parametarske oznake imaju i dva tipa:

- *Netipizirana* parametarska oznaka se navodi bez tipa njenog rezultata i ima neimenovanu formu. Sam tip vrednosti netipizirane parametarske oznake se izvodi iz konteksta u kojem se upotrebljava. Na primer, u SQL naredbi

```sql
UPDATE  DA.ISPIT
SET     OCENA = 10
WHERE   INDEKS = ?
```

koristi se netipizirana parametarska oznaka kao predikat restrikcije, čiji će tip biti implicitno postavljen na onaj tip kojim je definisana kolona `INDEKS` u tabeli `DA.ISPIT`. Netipizirane parametarske oznake se mogu koristiti u dinamičkim SQL naredbama sve dok se tip parametarske oznake može izvesti iz konteksta upotrebe. U suprotnom, SUBP prijavljuje `SQLSTATE` vrednost `42610` (`SQLCODE -418`).

- *Tipizirana* parametarska oznaka se navodi zajedno sa tipom rezultata. Sintaksa koja se koristi u ovom slučaju je

```sql
CAST(? AS <TIP_PODATAKA>)
```

gde je `<TIP_PODATAKA>` odgovarajući Db2 tip. Ova notacija ne predstavlja poziv funkcije ili eksplicitnu konverziju, već tek "obećanje" da će tip vrednosti, koja će se koristiti umesto navedene tipizirane parametarske oznake kada se naredba bude izvr\v savala, odgovarati tipu koji je naveden ili da se makar može konvertovati u njega. Na primer, u SQL naredbi

```sql
UPDATE  DA.DOSIJE
SET     PREZIME = TRANSLATE(CAST(? AS VARCHAR(50)))
WHERE   INDEKS = 20150050
```

vrednost argumenta funkcije `TRANSLATE` biće navedena tokom faze izvršavanja. Očekuje se da je tip te vrednosti bilo `VARCHAR(50)` ili tip koji se može konvertovati u njega. Tipizirane parametarske oznake se mogu koristiti u dinamičkim SQL naredbama gde god se očekuje matična promenljiva i tip podataka je kompatibilan sa "obećanjem" navedenim u `CAST` funkciji.

Kada se naredba `PREPARE` izvrši, specifikovana naredba se parsira i proveravaju se greške. Ako SQL naredba nije validna, ona se neće izvršiti i uslov koji je doveo do greške se izveštava kroz SQLCA strukturu. Svako naredno izvršavanje naredbi `EXECUTE` ili `OPEN` koji referišu na ovu naredbu će takođe dobiti istu grešku, osim ukoliko se prethodno ne ispravi.

Pripremljena naredba može biti referisana u narednim naredbama, sa odgovarajućim ograničenjima navedenim u zagradama:

- `DESCRIBE` (može biti proizvoljna naredba)
- `DECLARE CURSOR` (može biti isključivo `SELECT` naredba)
- `EXECUTE` (ne sme biti `SELECT` naredba)

Prednost korišćenja pripremljenih naredbi je u činjenici da se one mogu izvršavati više puta, sa potencijalno razli\v citim vrednostima parametarskih oznaka. Dakle, jednom pripremljenu naredbu nema potrebe pripremati opet. Ipak, ukoliko naredba koja nije `SELECT` ne sadrži parametarske oznake i izvršava se samo jednom, onda se preporučuje korišćenje `EXECUTE IMMEDIATE` naredbe umesto korišćenja kombinacije naredbi `PREPARE` i `EXECUTE`.

## 4.4 Naredba `EXECUTE`

Naredba `EXECUTE` izvršava prethodno pripremljenu SQL naredbu koja nije `SELECT`. Sintaksa ove naredbe je data u nastavku:

```sql
EXECUTE <NAZIV_NAREDBE>
[INTO DESCRIPTOR <NAZIV_OPISIVAČA_IZLAZA>]
[USING (<LISTA_MATIČNIH_PROMENLJIVIH> | DESCRIPTOR <NAZIV_OPISIVAČA_ULAZA>)]
```

Ova naredba izvršava prethodno pripremljenu naredbu koja se identifikuje pomoću `<NAZIV_NAREDBE>`. Ta vrednost mora odgovarati nekoj prethodno pripremljenoj naredbi. Pripremljena naredba se može izvršiti više puta. Ukoliko pripremljena naredba sadr\v zi parametarske oznake, onda mo\v zemo menjati vrednosti parametarskih oznaka svaki put kada izvr\v savamo naredbu, ukoliko za time ima potrebe.

U zavisnosti od korišćenja narednih klauza, ova naredba ima sledeće efekte:

- Klauzom `INTO DESCRIPTOR` se identifikuje izlazna SQLDA struktura `<NAZIV_OPISIVAČA_IZLAZA>` koja mora sadržati validne opise matičnih promenljivih. Videti sekciju 4.6 za dodatne napomene. Umesto ovoga se mo\v ze koristiti naredba `DESCRIBE OUTPUT`.

- Klauzom `USING` se idenfitikuje lista matičnih promenljivih ili izraza koje će se koristiti prilikom zamene vrednosti za ulazne parametarske oznake u pripremljenoj naredbi. Ako se makar jedna ulazna parametarska oznaka nalazi u pripremljenoj naredbi, onda se mora specifikovati klauza `USING`. U suprotnom, SUBP prijavljuje `SQLSTATE` vrednost `07004`. Vrednosti se mogu specifikovati na dva načina:

   - `<LISTA_MATIČNIH_PROMENLJIVIH>` predstavlja listu matičnih promenljivih koje su razdvojene karakterom zapete (`,`). Broj matičnih promenljivih u listi mora biti jednak broju ulaznih parametarskih oznaka u pripremljenoj naredbi. Dodatno, *n*-ta matična promenljiva u listi odgovara *n*-toj parametarskoj oznaci u pripremljenoj naredbi.

   - `DESCRIPTOR <NAZIV_OPISIVAČA_ULAZA>` identifikuje ulaznu SQLDA strukturu koja mora sadržati validne opise matičnih promenljivih. Videti sekciju 4.6 za dodatne napomene.

Pre samog izvršavanja pripremljene naredbe, svaka ulazna parametarska oznaka se zamenjuje vrednošću odgovarajuće promenljive ili izraza. Za tipizirane parametarske oznake, atributi ciljne promenljive ili izraza su oni koji su navedeni u `CAST` specifikaciji. Za netipizirane parametarske oznake, atributi ciljne promenljive ili izraza se izvode iz konteksta upotrebe parametarske oznake.

{% include lab/exercise.html broj="4.2" tekst="Napisati C/SQL program u kojem se naredbe izvršavaju dinamički kojim se omogućava da korisnik unese podatke o novom ispitnom roku. Uneti novi ispitni rok u bazu podataka sa unetim podacima. Obratiti pažnju da se naziv roka može sadržati od više reči." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_4/zadatak_4_2.sqc, c)

## 4.5 Naredba `DECLARE CURSOR` sa dinami\v ckim SQL naredbama

Kao što smo rekli, moguće je pripremiti dinamičku SQL naredbu `SELECT` naredbom `PREPARE`. Međutim, ovakvu naredbu nije moguće izvršiti naredbom `EXECUTE`. Umesto toga, koristimo isti pristup rada sa kursorima, sa razlikom da se prilikom deklaracije kursora koristi pripremljena naredba umesto kursora, kao i da se prilikom otvaranja kursora navode matične promenljive ili izrazi koji će biti zamenjeni umesto parametarskih oznaka u `SELECT` naredbi, ako ih takva naredba sadrži. Naredni zadatak ilustruje rad sa dinamičkom SQL naredbom `SELECT`.

{% include lab/exercise.html broj="4.3" tekst="Napisati C/SQL program u kojem se naredbe izvršavaju dinamički. Izdvojiti naredne podatke o studijskim programima: identifikator, oznaka i naziv, za svaki studijski program čiji se identifikator nivoa unosi sa standardnog ulaza." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_4/zadatak_4_3.sqc, c)

## 4.6 SQL prostor za opisivanje (SQLDA)

*SQL prostor za opisivanje* (engl. *SQL Description Area*, skr. *SQLDA*) predstavlja kolekciju promenljivih koje su neophodne za dohvatanje informacija o dinamičkim SQL naredbama. Da bismo mogli da ga koristimo, zaglavlje je potrebno uključiti pomoću naredbe `INCLUDE`:

```c
EXEC SQL INCLUDE SQLDA;
```

Promenljive deklarisane u SQLDA zaglavlju predstavljaju podešavanja koja se mogu koristiti u naredbama poput `PREPARE`, `OPEN`, `FETCH` i `EXECUTE`. SQLDA komunicira sa dinamičkom SQL naredbom i ima nekoliko upotrebnih vrednosti. Značenje informacija u SQLDA zavisi od njenog korišćenja. U naredbama `PREPARE` i `DESCRIBE`, koristi se kao izvor informacija ka aplikaciji o pripremljenoj naredbi. U naredbama `OPEN`, `EXECUTE` i `FETCH`, SQLDA nosi informacije o matičnim promenljivama.

U nastavku sledi opis strukture SQLDA, a grafički prikaz se nalazi na narednoj slici.

!["Struktura SQLDA."](./Slike/sqlda.png){:class="ui centered large image"}

Zaglavlje strukture se sastoji od narednih informacija:

- Polje `sqldaid`:
   - Tip: `CHAR(8)`
   - Značenje u `DESCRIBE` i `PREPARE`: Sedmi bajt ovog polja se naziva `SQLDOUBLED` i postavlja se od strane SUBP na karakter `2` ukoliko se za svaku kolonu koriste dve `SQLVAR` vrednosti.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Ako se bilo koja od matičnih promenljivih opisuje kao struktuirani tip ili neka od `LOB` vrednosti, onda `SQLDOUBLED` mora biti postavljen na karakter `2`.

- Polje `sqldabc`:
   - Tip: `INTEGER`
   - Značenje u `DESCRIBE` i `PREPARE`: Veličina SQLDA strukture u bajtovima.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Isto.

- Polje `sqln`:
   - Tip: `SMALLINT`
   - Značenje u `DESCRIBE` i `PREPARE`: Nepromenjena od strane SUBP. Mora biti postavljena na vrednost veću ili jednaku nuli pre izvršavanja naredbe `DESCRIBE`. Indikuje ukupan broj pojavljivanja `SQLVAR`.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Ukupan broj pojavljivanja `SQLVAR` u SQLDA. `SQLN` vrednost mora biti postavljena na vrednost veću ili jednaku nuli.

- Polje `sqld`:
   - Tip: `SMALLINT`
   - Značenje u `DESCRIBE` i `PREPARE`: Broj kolona u rezultujućoj tabeli ili broj parametarskih oznaka, postavljen od strane SUBP.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Broj matičnih promenljivih opisanih pojavljivanjima SQLVAR.

Zaglavlje je praćeno proizvoljnim brojem pojavljivanja struktura koje se nazivaju `SQLVAR`. U naredbama `OPEN`, `FETCH` i `EXECUTE` svako pojavljivanje `SQLVAR` opisuje matičnu promenljivu. U naredbama `DESCRIBE` i `PREPARE` svako pojavljivanje `SQLVAR` opisuje kolonu rezultujuće tabele ili parametarsku oznaku. Svaka `SQLVAR` struktura ima naredna polja:

- Polje `sqltype`:
   - Tip: `SMALLINT`
   - Značenje u `DESCRIBE` i `PREPARE`: Indikuje tip kolone ili parametarske oznake, kao i informaciju o tome da li je potencijalno nedostajuća (pri čemu smo napomenuli da se parametarske oznake uvek smatraju za potencijalno nedostajuće). Tabela u nastavku prikazuje dozvoljene vrednosti i njihova značenja.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Isto kao u prethodnom slučaju, samo primenjeno na matične promenljive.

- Polje `sqllen`:
   - Tip: `SMALLINT`
   - Značenje u `DESCRIBE` i `PREPARE`: Dužina atributa kolone ili parametarske oznake.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Dužina atributa matične promenljive.

- Polje `sqldata`:
   - Tip: pokazivač
   - Značenje u `DESCRIBE` i `PREPARE`: Za različite varijante tipa niske sadrži informaciju o preslikavanju karaktera u tačke kodova. Za ostale tipove je nedefinisan.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Adresa matične promenljive u kojoj će biti smešteni dohvaćeni podaci.

- Polje `sqlind`:
   - Tip: pokazivač
   - Značenje u `DESCRIBE` i `PREPARE`: Za različite varijante tipa niske sadrži informaciju o preslikavanju karaktera u tačke kodova. Za ostale tipove je nedefinisan.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Adresa indikatorske promenljive za odgovarajuću matičnu promenljivu, ako postoji. Inače, ne koristi se. Ako je vrednost `sqltype` paran broj, ovo polje se ignoriše.

- Polje `sqlname`:
   - Tip: `VARCHAR(30)`
   - Značenje u `DESCRIBE` i `PREPARE`: Sadrži naziv kolone ili parametarske oznake.
   - Značenje u `FETCH`, `OPEN` i `EXECUTE`: Različito značenje za različit tip baze podataka (relacione ili XML).

Naredna tabela opisuje SQL tipove kolona i njihove odgovarajuće `SQLTYPE` tipove u SQLDA. Primetimo da svaki simbolički naziv počinje imenom `SQL_TYP_` ukoliko vrednost nije potencijalno nedostajuća, a imenom `SQL_TYP_N` ukoliko jeste.

| SQL tip kolone | SQLTYPE numerička vrednost | SQLTYPE simboličko ime |
| ----- | ----- | ----- |
| DATE | 384/385 | SQL_TYP_DATE / SQL_TYP_NDATE |
| TIME | 388/389 | SQL_TYP_TIME / SQL_TYP_NTIME |
| TIMESTAMP | 392/393 | SQL_TYP_STAMP / SQL_TYP_NSTAMP |
| n/a | 400/401 | SQL_TYP_CGSTR / SQL_TYP_NCGSTR |
| BLOB | 404/405 | SQL_TYP_BLOB / SQL_TYP_NBLOB |
| CLOB | 408/409 | SQL_TYP_CLOB / SQL_TYP_NCLOB |
| DBCLOB | 412/413 | SQL_TYP_DBCLOB / SQL_TYP_NDBCLOB |
| VARCHAR | 448/449 | SQL_TYP_VARCHAR / SQL_TYP_NVARCHAR |
| CHAR | 452/453 | SQL_TYP_CHAR / SQL_TYP_NCHAR |
| LONG VARCHAR | 456/457 | SQL_TYP_LONG / SQL_TYP_NLONG |
| n/a | 460/461 | SQL_TYP_CSTR / SQL_TYP_NCSTR |
| VARGRAPHIC | 464/465 | SQL_TYP_VARGRAPH / SQL_TYP_NVARGRAPH |
| GRAPHIC | 468/469 | SQL_TYP_GRAPHIC / SQL_TYP_NGRAPHIC |
| LONG VARGRAPHIC | 472/473 | SQL_TYP_LONGRAPH / SQL_TYP_NLONGRAPH |
| FLOAT | 480/481 | SQL_TYP_FLOAT / SQL_TYP_NFLOAT |
| REAL | 480/481 | SQL_TYP_FLOAT / SQL_TYP_NFLOAT |
| DECIMAL | 484/485 | SQL_TYP_DECIMAL / SQL_TYP_DECIMAL |
| INTEGER | 496/497 | SQL_TYP_INTEGER / SQL_TYP_NINTEGER |
| SMALLINT | 500/501 | SQL_TYP_SMALL / SQL_TYP_NSMALL |
| n/a | 804/805 | SQL_TYP_BLOB_FILE / SQL_TYPE_NBLOB_FILE |
| n/a | 808/809 | SQL_TYP_CLOB_FILE / SQL_TYPE_NCLOB_FILE |
| n/a | 812/813 | SQL_TYP_DBCLOB_FILE / SQL_TYPE_NDBCLOB_FILE |
| n/a | 960/961 | SQL_TYP_BLOB_LOCATOR / SQL_TYP_NBLOB_LOCATOR |
| n/a | 964/965 | SQL_TYP_CLOB_LOCATOR / SQL_TYP_NCLOB_LOCATOR |
| n/a | 968/969 | SQL_TYP_DBCLOB_LOCATOR / SQL_TYP_NDBCLOB_LOCATOR |
| XML | 988/989 | SQL_TYP_XML / SQL_TYP_XML |

### 4.6.1 Efekat naredbe `EXECUTE` na SQLDA strukturu

Pre nego što se naredba `EXECUTE` izvrši, korisnik mora da postavi naredna polja u ulaznoj SQLDA strukturi:

- `SQLN` za indikovanje broja pojavljivanja SQLVAR.
- `SQLDABC` za indikovanje broja bajtova alociranih za SQLDA.
- `SQLD` za indikovanje broja promenljivih koje se koriste u SQLDA prilikom procesiranja naredbe.
- `SQLVAR` pojavljivanja za indikovanje atributa promenljivih.

Pritom, SQLDA mora imati dovoljno alociranog prostora da sadrži sva pojavljivanja `SQLVAR`.

Procedura za alociranje dovoljne veličine prostora glasi:

- Zaglavlje sadrži fiksnu veličinu od 16 bajtova.
- Niz promenljive dužine `SQLVAR` struktura za svaki element sadrži 44 bajtova na 32-bitnim, odnosno, 56 bajtova na 64-bitnim sistemima.

Drugim rečima, količina prostora koju treba alocirati je (`SQLD` je odgovarajući broj matičnih promenljivih opisanih pojavljivanjima `SQLVAR`):

```c
16 + (SQLD * sizeof(struct sqlvar))
```

Na raspolaganju nam je makro `SQLDASIZE` koji će izračunati ovu vrednost za nas, uzimajući u obzir specifičnosti platforme. Njegov obavezni argument je upravo vrednost `SQLD`. Dodatno, `SQLD` mora biti postavljen na vrednost veću ili jednaku nuli i manju od ili jednaku `SQLN`.

### 4.6.2 Efekat naredbe `PREPARE` na SQLDA strukturu

Prilikom izvršavanja naredbe `PREPARE INPUT`, SUBP uvek postavlja `SQLD` na broj ulaznih parametarskih oznaka u naredbi.

Prilikom izvršavanja naredbe `PREPARE OUTPUT`, SUBP uvek postavlja `SQLD` na broj kolona u rezultujućoj tabeli ili na broj izlaznih parametarskih oznaka.

## 4.7 Naredba `DESCRIBE`

Naredba `DESCRIBE` dohvata informacije o pripremljenoj naredbi. Postoje dva tipa informacija koja se mogu dobiti ovom naredbom, i svaki od njih ćemo posebno opisati.

### 4.7.1 Naredba `DESCRIBE INPUT`

Ova naredba dohvata informacije o ulaznim parametarskim oznakama u pripremljenoj naredbi. Ova informacija se smešta u SQLDA strukturu.

Sintaksa ove naredbe je:

```sql
DESCRIBE INPUT <NAZIV_NAREDBE> INTO <NAZIV_OPISIVAČA>
```

Ova naredba dohvata informacije iz prethodno pripremljene naredbe koja se identifikuje imenom `<NAZIV_NAREDBE>`. Klauza `INTO <NAZIV_OPISIVAČA>` identifikuje SQLDA strukturu za unos informacija.

Prilikom izvršavanja naredbe `DESCRIBE INPUT`, SUBP uvek postavlja `SQLD` na broj ulaznih parametarskih oznaka u naredbi.

#### Priprema SQLDA strukture za izvršavanje naredbe

Pre izvršavanja `DESCRIBE INPUT` naredbe, korisnik mora da alocira prostor za SQLDA strukturu i da postavi vrednost promenljive `SQLN` na broj `SQLVAR` pojavljivanja u okviru SQLDA strukture. Ova vrednost mora biti veća od nule ili jednaka nuli, pre nego što se naredba izvrši.

Postoje tri tehnike za alociranje SQLDA strukture:

1. Alocirati prostor za SQLDA strukturu sa dovoljnim brojem `SQLVAR` promenljivih da sadrži bilo koju listu informacija koju aplikacija može da procesira. Najveći dozvoljen broj informacija iznosi 255. Ova tehnika koristi veliku količinu prostora koja u praktičnim primenama neće biti iskorišćena.

2. Ponavljati naredna dva koraka:

- Izvršiti naredbu `DESCRIBE INPUT` sa SQLDA koja nema pojavljivanja `SQLVAR`, odnosno, u kojoj je `SQLN` vrednost postavljena na nulu. Vrednost koja je postavljena u `SQLD` nakon izvršavanja naredbe predstavlja broj kolona u rezultujućoj tabeli. SUBP u ovom slučaju postavlja `SQLCODE` na vrednost `+236`.

- Alocirati prostor za SQLDA strukturu sa dovoljnim brojem `SQLVAR` pojavljivanja na osnovu postavljene vrednosti za `SQLD` iz prethodnog koraka. Zatim izvršiti naredbu `DESCRIBE` ponovo, korišćenjem novoalocirane SQLDA. Ova tehnika ima bolji mehanizam rukovanja memorijom od prve tehnike, ali se broj izvršavanja naredbi `DESCRIBE` udvostručava.

{:start="3"}
3. Alocirati prostor za SQLDA strukturu koja je dovoljno velika da sadrži veliki broj `SQLVAR` pojavljivanja, ako ne i sve liste informacija koju aplikacija može da procesira, ali je takođe i dovoljno mala da ne bude previše viška memorije utrošeno. Ukoliko je ovo dovoljna količina prostora, nastaviti sa izvršavanjem. U suprotnom, alocirati novi prostor sa dovoljnim brojem `SQLVAR` pojavljivanja i izvršiti naredbu `DESCRIBE` ponovo. Ova tehnika predstavlja kompromis između prethodne dve tehnike i oslanja se na heuristiku izbora veličine prostora koja je dovoljno velika, a u isto vreme i dovoljno mala. 

#### Efekat izvršenja naredbe

Nakon izvršavanja naredbe `DESCRIBE INPUT`, SUBP dodeljuje vrednosti promenljivama strukture SQLDA na sledeći način:

- U polju `SQLDAID` prvih 6 bajtova se postavlja na nisku `'SQLDA '`, a sedmi bajt (`SQLDOUBLED`) postavlja se na karakter `'2'` ili na razmak. Osmi bajt se postavlja na razmak.

- Polje `SQLDABC` se postavlja na dužinu SQLDA strukture u bajtovima.

- Polje `SQLD` se postavlja na broj ulaznih parametara procedure.

- Za svako pojavljivanje `SQLVAR`: ako je vrednost `SQLD` jednaka nuli ili veća od `SQLN`, ne postavljaju se vrednosti za pojavljivanja SQLVAR. Ako je vrednost `SQLD` jednaka *n*, gde je *n > 0* i *n <= `SQLN`*, onda se vrednosti dodeljuju prvih *n* pojavljivanja `SQLVAR`. Ove vrednosti opisuju parametarske oznake za ulazne parametre prodecure, redom.

### 4.7.2 Naredba `DESCRIBE OUTPUT`

Ova naredba dohvata informacije o pripremljenoj naredbi ili informacije o listi kolona u pripremljenoj `SELECT` naredbi. Ova informacija se smešta u SQLDA strukturu.

Sintaksa ove naredbe je:

```sql
DESCRIBE [OUTPUT] <NAZIV_NAREDBE> INTO <NAZIV_OPISIVAČA>
```

Ova naredba dohvata informacije iz perthodno pripremljene naredbe koja se identifikuje imenom `<NAZIV_NAREDBE>`. Ako je pripremljena naredba `SELECT` ili `VALUES INTO`, onda informacija koja se dohvata predstavlja informaciju o kolonama rezultujuće tabele. Klauza `INTO <NAZIV_OPISIVAČA>` identifikuje SQLDA strukturu za unos informacija.

Prilikom izvršavanja naredbe `DESCRIBE OUTPUT`, SUBP uvek postavlja `SQLD` na broj kolona u rezultujućoj tabeli ili na broj izlaznih parametarskih oznaka.

Pre izvršavanja `DESCRIBE OUTPUT` naredbe, korisnik mora da alocira prostor za SQLDA strukturu i da postavi vrednost promenljive `SQLN` na broj `SQLVAR` pojavljivanja u okviru SQLDA strukture. Ova vrednost mora biti veća od nule ili jednaka nuli, pre nego što se naredba izvrši.

Postoje tri tehnike za alociranje SQLDA strukture:

1. Alocirati prostor za SQLDA strukturu sa dovoljnim brojem `SQLVAR` promenljivih da sadrži bilo koju listu informacija koju aplikacija može da procesira. Najveći dozvoljen broj informacija iznosi 255. Ova tehnika koristi veliku količinu prostora koja u praktičnim primenama neće biti iskorišćena.

2. Ponavljati naredna dva koraka:

- Izvršiti naredbu `DESCRIBE OUTPUT` sa SQLDA koja nema pojavljivanja `SQLVAR`, odnosno, u kojoj je `SQLN` vrednost postavljena na nulu. Vrednost koja je postavljena u `SQLD` nakon izvršavanja naredbe predstavlja broj kolona u rezultujućoj tabeli. SUBP u ovom slučaju postavlja `SQLCODE` na vrednost `+236`.

- Alocirati prostor za SQLDA strukturu sa dovoljnim brojem `SQLVAR` pojavljivanja na osnovu postavljene vrednosti za `SQLD` iz prethodnog koraka. Zatim izvršiti naredbu `DESCRIBE` ponovo, korišćenjem novoalocirane SQLDA. Ova tehnika ima bolji mehanizam rukovanja memorijom od prve tehnike, ali se broj izvršavanja naredbi `DESCRIBE` udvostručava.

{:start="3"}
3. Alocirati prostor za SQLDA strukturu koja je dovoljno velika da sadrži veliki broj `SQLVAR` pojavljivanja, ako ne i sve liste informacija koju aplikacija može da procesira, ali je takođe i dovoljno mala da ne bude previše viška memorije utrošeno. Ukoliko je ovo dovoljna količina prostora, nastaviti sa izvršavanjem. U suprotnom, alocirati novi prostor sa dovoljnim brojem `SQLVAR` pojavljivanja i izvršiti naredbu `DESCRIBE` ponovo. Ova tehnika predstavlja kompromis između prethodne dve tehnike i oslanja se na heuristiku izbora veličine prostora koja je dovoljno velika, a u isto vreme i dovoljno mala. 

#### Efekat izvršenja naredbe

Nakon izvršavanja naredbe `DESCRIBE OUTPUT`, SUBP dodeljuje vrednosti promenljivama strukture SQLDA na sledeći način:

- U polju `SQLDAID` prvih 6 bajtova se postavlja na nisku `'SQLDA '`, a sedmi bajt (`SQLDOUBLED`) postavlja se na karakter `'2'` ili na razmak. Osmi bajt se postavlja na razmak.

- Polje `SQLDABC` se postavlja na dužinu SQLDA strukture u bajtovima.

- Polje `SQLD` se postavlja na broj kolona u rezultujućoj tabeli, ako je pripremljena naredba `SELECT`. Inače, `SQLD` se postavlja na 0.

- Za svako pojavljivanje `SQLVAR`: ako je vrednost `SQLD` jednaka nuli ili veća od `SQLN`, ne postavljaju se vrednosti za pojavljivanja SQLVAR. Ako je vrednost `SQLD` jednaka *n*, gde je *n > 0* i *n <= `SQLN`*, onda se vrednosti dodeljuju prvih *n* pojavljivanja `SQLVAR`. Ove vrednosti opisuju parametarske oznake za ulazne parametre prodecure, redom.

Naredni zadatak ilustruje sve opisane tehnike za dizajn programa koji može da izvrši proizvoljnu dinamičku SQL naredbu.

{% include lab/exercise.html broj="4.4" tekst="Napisati C/SQL program u kojem se naredbe izvršavaju dinamički. Omogućiti izvršavanje proizvoljne naredbe. Pretpostaviti da su jedini tipovi podataka koji se koriste numerički, tekstualni i datumi. Pretpostaviti da naredba koja se unosi sa standardnog ulaza nije duža od 512 karaktera." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_4/zadatak_4_4.sqc, c)

## 4.8 Zadaci za vežbu

{% include lab/exercise.html broj="4.5" tekst="Napisati C/SQL program u kojem se naredbe izvršavaju dinamički. Izdvojiti naziv predmeta, prosečnu ocenu i procenat studenata iz tog predmeta u školskoj godini koja se unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="4.6" tekst="Uraditi zadatke za ve\v zbu iz poglavlja 2 i 3 kori\v s\'cenjem dinami\v ckih SQL naredbi." %}
