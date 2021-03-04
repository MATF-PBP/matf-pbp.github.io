---
layout: page
title: 2. Osnovni koncepti programiranja C/SQL aplikacija
under_construction: true
---

U ovom poglavlju ćemo se detaljnije upustiti u diskusiju konstrukcije aplikacija, napisanih u programskom jeziku C, koje koriste Db2 RSUBP. S obzirom da preprostavljamo da imamo podešeno okruženje, govorićemo o prevođenju od izvornog koda do izvršne aplikacije i uvešćemo elemente neophodne za kompilaciju naših programa, kao što su Db2 preprocesorske naredbe, upravljanje greškama, ugnežđavanje najrazličitijih vrsta upita i druge. Sve ovo će biti demonstrirano kroz veliki broj primera koji podrazumevaju da postoji baza podataka STUD2020.

## 2.1 Prevođenje programa

Pre nego što započnemo diskusiju o načinu konstrukcije C/SQL aplikacija, neophodno je da se upoznamo sa specifičnostima prevođenja C/SQL aplikacija. Za početak, potrebno je da razumemo da Db2 očekuje da naši programi budu napisani u datotekama čije su ekstenzije `.sqc`. U ovim datotekama se, dakle, nalazi izvorni kod napisan u programskom jeziku C u kojem su ugnežđeni SQL upiti. Izvorni kodovi napisani na ovaj način definišu jednu SQL-ugnežđenu aplikaciju.

Za aplikaciju kažemo da je *SQL-ugnežđena* (engl. *SQL-embedded*) ukoliko postoji barem jedna SQL naredba koja je ugnežđena u matični jezik, kao što su C, C++, Java, i dr.

Proces prevođenja C programa sa ugnežđenim SQL naredbama obično podrazumeva korake koji su dati u nastavku. Ovi koraci pretpostavljaju da u terminalu iz kojeg se pozivaju postoji definisana _promenljiva okru\v zenja_ (engl. _environment variable_) \v ciji je identifikator `DB2PATH`, a \v cija je vrednost putanja na kojoj je instaliran Db2 SUBP. Pretpostavimo da je na na\v sem operativnom sistemu ova lokacija `/opt/ibm/db2/V11.5/`. Dodatno, pretpostavimo da je na\v s sistem 64-bitni. U slu\v caju da nije, onda je potrebno zameniti sva pojavljivanja niske `lib64` niskom `lib32` u koracima ispod:

1. Ostvarivanje konekcije na bazu podataka. Ovo podrazumeva pokretanje naredne naredbe iz terminala:

```shell
db2 connect to IMEBAZE user KORISNIK using LOZINKA
```

{:start="2"}
2. Preprocesiranje aplikacije koju izvodi specijalan program koji se naziva db2 preprocesor. Ovaj program kao ulaz prihvata napisanu `.sqc` datoteku, a kao izlaz konstruiše dve datoteke: 

- Datoteku sa ekstenzijom `.c` koja sadrži "čisti" izvorni C kod. U ovoj datoteci su sve naredbe Db2 API-ja zamenjene pozivima C funkcija, dok je kod napisan od strane razvijača prepisan u izvornoj formi.

- Datoteku sa ekstenzijom `.bnd` koja sadrži samo SQL upite. 

Ovo podrazumeva pokretanje naredne naredbe iz terminala:

```shell
db2 PRECOMPILE IMEDATOTEKE.sqc BINDFILE
```

{:start="3"}
3. Ako je kreirana `IMEDATOTEKE.bnd` datoteka (korišćenjem `BINDFILE` opcije u `PRECOMPILE` naredbi u koraku 2), vrši se vezivanje te datoteke sa bazom podataka da bi se kreirao aplikacioni paket. 

*Vezivanje* predstavlja proces kojim se od datoteke sa ekstenzijom `.bnd` kreira paket koji se čuva u bazi podataka.

*Paket* sadrži izvršivu formu svakog SQL upita iz izvornog koda aplikacije. Za svaku SQL naredbu se čuvaju razne informacije, kao što su: koji se postojeći indeksi koriste, načini njihovog korišćenja, pristupi tabelama i dr. U suštini, paket predstavlja pristupni plan podacima i igra ključnu ulogu u komunikaciji klijentske aplikacije i Db2 servera.

Ovo podrazumeva pokretanje naredne naredbe iz terminala:

```shell
db2 BIND IMEDATOTEKE.bnd
```

{:start="4"}
4. Kompiliranje modifikovanog aplikacionog izvornog koda i izvornih datoteka koji ne sadrže ugnežđeni SQL da bi se kreirale objektne datoteke (jedna ili više njih, zavisno od modularnosti koda; kod nas će uglavnom biti jedna objektna datoteka po primeru). Potrebno je kompilatoru specifikovati gde se nalaze zaglavlja koje nam Db2 sistem nudi. Ovo podrazumeva pokretanje naredne naredbe iz terminala:

```shell
cc -I$DB2PATH/include -c IMEDATOTEKE.c
```

{:start="5"}
5. Linkovanje aplikacionih objektnih datoteka sa Db2 bibliotekama i bibliotekama matičnog jezika da bi se kreirao izvršni program. Ovo podrazumeva pokretanje naredne naredbe iz terminala:

```shell
cc -o IMEDATOTEKE IMEDATOTEKE.o -Wl,-rpath,$DB2PATH/lib64 -L$DB2PATH/lib64 -ldb2
```

{:start="5"}
6. Raskidanje konekcije sa bazom podataka:

```shell
db2 connect reset
```

S obzirom da je ovaj proces identičan za svaku aplikaciju koju ćemo pisati (uz jednocifren broj izuzetaka), dobro bi bilo automatizovati ga. U tu svrhu, kreirali smo skript `prevodjenje` čiji je sadržaj dat u nastavku.

include_source(vezbe/primeri/poglavlje_2/prevodjenje, shell)

Ovaj skript u svojoj osnovnoj varijanti zahteva 4 argumenta:

1. Naziv datoteke koji se prevodi bez ekstenzije `.sqc` (u prethodnim koracima vrednost `IMEDATOTEKE`).

2. Naziv baze podataka na koju se vrši konekcija (u prethodnim koracima vrednost
`IMEBAZE`).

3. Naziv korisnika sa kojim se pristupa bazi podataka (u prethodnim koracima vrednost `KORISNIK`).

4. Lozinka za pristup bazi podataka za korisnika `KORISNIK` iz koraka 3 (u prethodnim koracima vrednost `LOZINKA`).

## 2.2 Osnovni elementi programiranja C/SQL aplikacija

Kao što smo videli do sada, prilikom prevođenja naših C/SQL programa, očekujemo da je prvi korak procesiranje izvornog koda od strane Db2 preprocesora. Tokom ovog procesa, DB2 preprocesor prolazi kroz izvorni kod .sqc datoteke i izvršava određene akcije kada naiđe na određene DB2 naredbe. Ovakvih naredbi ima dosta, a mi ćemo objasniti neke od njih.

Sve DB2 procesorske naredbe počinju ključnim rečima `EXEC SQL`, za kojima slede SQL naredbe. U daljem tekstu ćemo podrazumevati postojanje ovih ključnih reči ukoliko se ne navedu eksplicitno.

### 2.2.1 Naredba `INCLUDE`

Naredba `INCLUDE` služi za uključivanje zaglavlja u izvorni kod programa.

Razlikuje se od standardne C preprocesorske direktive `#include` po tome što se izvršava tokom preprocesiranja od strane DB2 preprocesora. Za razliku od nje, direktiva `#include` se izvršava tokom prevođenja C programa. Ovo je ključna razlika jer želimo da neka zaglavlja budu dostupna pre nego što se dođe do faze prevođenja C programa.

Da bismo mogli da radimo sa SQL upitima, potrebno je da uključimo zaglavlje `SQLCA.h` (*SQL Communication Area*) i to u fazi DB2 preprocesiranja, što se čini naredbom:

```c
EXEC SQL INCLUDE SQLCA;
```

Primetimo da se ne navodi ekstenzija datoteke. Zaglavlje `SQLCA.h` predstavlja kolekciju promenljivih koje se ažuriraju pri izvršavanju svakog SQL upita. Kako se budemo susretali sa raznim zahtevima, tako ćemo objašnjavati svaku od neophodnih promenljivih koje su nam dostupne uključivanjem ovog zaglavlja.

### 2.2.2 Naredbe `BEGIN DECLARE SECTION` i `END DECLARE SECTION`

Vrlo često će nam biti neophodno da koristimo promenljive da bismo smeštali rezultate izvršavanja SQL naredbi ili da bismo rezultate nekih izračunavanja u matičnom jeziku koristili kao ulaz SQL naredbi. Međutim, za razliku od regularnih promenljivih u višim programskim jezicima, programiranje SQL naredbi koje zahtevaju korišćenje promenljivih zahteva uvođenje pojma matične promenljive.

*Matične promenljive* su promenljive koje su definisane u višem programskom jeziku, a koje se ugnežđavaju u SQL naredbe da bi se iskoristile njihove vrednosti ili da bi se rezultati SQL naredbi smestili u njih.

U programskom jeziku C, da bismo deklarisali ovakve promenljive, potrebno je da njihove deklaracije smestimo između para naredbi `BEGIN DECLARE SECTION` i `END DECLARE SECTION`. Na primer:

```c
EXEC SQL BEGIN DECLARE SECTION;
double hAverageGrade;
EXEC SQL END DECLARE SECTION;
```

Par ovih naredbi je moguće smestiti na bilo koje mesto u kodu gde bismo mogli da deklarišemo i standardne C promenljive. U našem slučaju, mi ćemo deklarisati matične promenljive u globalnom opsegu, ali, naravno, to ne mora biti slučaj.

Postoje neka pravila koja se moraju ispoštovati pri korišćenju ovih naredbi. Naredbe `BEGIN DECLARE SECTION` i `END DECLARE SECTION` moraju biti uparene, i nije ih je moguće ugnežđavati. Takođe, nije moguće ugnežđavati SQL naredbe između para ovih naredbi. Promenljive koje se deklarišu izvan sekcija definisanih parom ovim naredbama ne smeju imati isti identifikator kao i matične promenljive u tim sekcijama. Naravno, sve promenljive koje se koriste u SQL upitima moraju biti deklarisane u sekciji definisanoj nekim parom ovih naredbi.

Kako se sada ovako definisane matične promenljive koriste? Matičnu promenljivu možemo koristiti u izračunavanjima u matičnom jeziku kao i bilo koje druge promenljive, na primer:

```c
printf("Prosecna ocena studenata je %lf\n", hAverageGrade);
```

Ukoliko želimo da iskoristimo matičnu promenljivu u SQL naredbi, onda ispred identifikatora promenljive moramo staviti karakter dvotačke, na primer:

```c
EXEC SQL 
    SELECT  AVG(CAST(OCENA AS DOUBLE)) 
    INTO    :hAverageGrade 
    FROM    DA.ISPIT;
```

### 2.2.3 DB2 tipovi promenljivih

Kao što znamo, programski jezik C definiše određeni broj osnovnih tipova. Neki od ovih tipova korespondiraju sa DB2 tipovima kolona u tabelama. U narednoj tabeli prikazan je pregled nekih najčešćih tipova kolona u DB2, kao i odgovarajući C tipovi. Ovi tipovi se mogu koristiti za deklaraciju matičnih promenljivih. Kada DB2 preprocesor naiđe na deklaraciju matične promenljive, on određuje prikladni SQL tip. RSUBP zatim koristi ovu vrednost za konverziju podataka koji se razmenjuju između aplikacije i njega. Napomenimo da se tipovi označeni zvezdicom preporučuju u odnosu na druge zbog kompatibilnosti između operativnih sistema.

| DB2&nbsp;tip&nbsp;kolone | C tip | Opis DB2 tipa |
| -------------- | ----- | ------------- |
| `SMALLINT` | `short` <br> `short int` <br> `sqlint16` | 16-bitni označeni ceo broj |
| `INTEGER` | `int` <br> `long` <br> `long int` <br> `sqlint32`* | 32-bitni označeni ceo broj |
| `BIGINT` | `long long` <br> `long` <br> `__int64` <br> `sqlint64`* | 64-bitni označeni ceo broj |
| `REAL` | `float` | Broj u pokretnom zarezu jednostruke preciznosti |
| `DOUBLE` | `double` | Broj u pokretnom zarezu dvostruke preciznosti |
| `DECIMAL(p,s)` | Nema precizan ekvivalentan tip; koristiti `double` | Pakovan decimalni zapis |
| `CHAR(1)` | `char` | Jedan karakter |
| `CHAR(n)` | Nema precizan ekvivalentan tip; koristiti `char[n+1]` gde je `n` dovoljno veliko da sadrži podatak, tj. `1 <= n <= 254` | Niska nepromenljive dužine |
| `VARCHAR(n)` | <code>struct tag {<br>&nbsp;&nbsp;&nbsp;&nbsp;short int; <br>&nbsp;&nbsp;&nbsp;&nbsp;char[n]; <br>}</code><br>gde je `1 <= n <= 32672` | Niska promenljive dužine bez terminirajuće nule sa 2-bajtnim indikatorom dužine |
| `DATE` | Nula-terminirajuća karakterna forma | Dopustiti barem 11 karaktera da bi se smestio terminirajući karakter |
| `DATE` | `VARCHAR` strukturna forma | Dopustiti barem 10 karaktera |
| `TIME` | Nula-terminirajuća karakterna forma | Dopustiti barem 9 karaktera da bi se smestio terminirajući karakter |
| `TIME` | `VARCHAR` strukturna forma | Dopustiti barem 8 karaktera |
| `TIMESTAMP(p)` | Nula-terminirajuća karakterna forma | Dopustiti barem 20-33 karaktera da bi se smestio terminirajući karakter |
| `TIMESTAMP(p)` | `VARCHAR` strukturna forma | Dopustiti barem 19-32 karaktera |

### 2.2.4 Naredba SELECT INTO

U ovom poglavlju ćemo demonstrirati najjednostavniji slučaj dohvatanja podataka iz baze podataka - u pitanju je slučaj kada *rezultat SQL upita sadrži najviše 1 red*. Da bismo izvršili ovakve upite, potrebno je da koristimo naredbu `SELECT INTO`. Njena sintaksa odgovara regularnoj `SELECT` naredbi, sa time da se za svaku kolonu u jednom redu rezultata navodi u koju se matičnu promenljivu taj rezultat smešta. Na primer, neka su definisane naredne dve matične promenljive:

```c
EXEC SQL BEGIN DECLARE SECTION;
char hFirstExamDate[11];
char hLastExamDate[11];
EXEC SQL END DECLARE SECTION;
```

Ukoliko želimo da iz tabele `ISPIT` dohvatimo datume o prvom i poslednjem ispitu, to možemo uraditi narednom naredbom:

```c
EXEC SQL 
    SELECT  MIN(DATPOLAGANJA), 
            MAX(DATPOLAGANJA) 
    INTO    :hFirstExamDate,
            :hLastExamDate
    FROM    DA.ISPIT;
```

### 2.2.5 Indikatorske promenljive

Jedno pitanje koje se prirodno postavlja u radu sa SQL podacima jeste kako se rukuje podacima za koje znamo da mogu imati nedostajuće vrednosti u bazi podataka. Na primer, u tabeli `DOSIJE` kolona `MESTORODJENJA` mo\v ze imati nedostajuće vrednosti. Potrebno je da se na neki način indikuje ukoliko se naiđe na vrednost `NULL`. Za to nam služe tzv. _indikatorske promenljive_.

Kako su ove promenljive deljene između RSUBP-a i višeg programskog jezika, to je i njih potrebno deklarisati kao matične promenljive. Ove promenljive uzimaju SQL tip vrednosti `SMALLINT`, odnosno, možemo koristiti tip `short` u SQL/C kodu.

Indikatorska promenljiva se u SQL naredbi navodi odmah nakon matične promenljive. S obzirom da je i ona sama matična promenljiva, moramo je prefiksovati karakterom dvotačke. Na primer:

```c
EXEC SQL BEGIN DECLARE SECTION;
char hBirthPlace[51];
short hIndBirthPlace;
EXEC SQL END DECLARE SECTION;

// ...

EXEC SQL
    SELECT  MESTORODJENJA
    INTO    :hBirthPlace :hIndBirthPlace
    FROM    DA.DOSIJE
    WHERE   INDEKS = ...;
```

Ispitivanje da li je neka vrednost `NULL` ili ne, može se izvršiti proverom vrednosti indikatorske promenljive. Ako je njena vrednost negativan broj, dohvaćena vrednost je `NULL` i odgovarajuću matičnu promenljivu ne bi trebalo koristiti. U suprotnom, matična promenljiva sadrži odgovarajuću vrednost iz tabele. RSUBP neće promeniti vrednost matične promenljive u slučaju da je dohvaćena vrednost `NULL`. Primer provere nedostajuće vrednosti se može ilustrovati narednim delom koda:

```c
if (hIndBirthPlace < 0) {
    printf("Podatak ne postoji!\n");
} else {
    printf("Dohvacen je podatak: %s\n", hBirthPlace);
}
```

### 2.2.6 Obrada grešaka

Pri radu sa Db2 SQL bazom podataka, možemo očekivati da će se potencijalno pojaviti tri vrste grešaka:

1. Upozorenjem `NOT FOUND` se signalizira da upitom nije pronađen nijedan red u tabeli koji je zadovoljavajući.

2. Upozorenjem `SQLWARNING` se signalizira da postoji neka akcija koja nije očekivana ili da potencijalno postoji nekakav propust.

3. Greškom `SQLERROR` se signalizira da postoji veliki problem koji je neophodno rešiti.

DB2 sistem nam nudi razne načine za obradu grešaka. U osnovi svih ovih načina nalazi se zaglavlje `SQLCA.h` i razne promenljive, strukture i funkcije koje ona nudi. Komunikacija sa DB2 sistemom je moguća zato što, prilikom preprocesiranja programa, Db2 preprocesor umeće deklaracije raznih matičnih promenljivih na mesto `INCLUDE` naredbe. Sistem zatim komunicira sa našim programom koristeći promenljive za postavljanje zastavica prilikom upozorenja, kodova za greške i drugih informacija za dijagnozu.

Nakon izvršavanja svake SQL naredbe, sistem daje povratni kod kroz C makroe `SQLCODE` i `SQLSTATE`. `SQLCODE` predstavlja makro koji se razvija u celi broj koji na neki način sumira izvršavanje SQL naredbe, dok je `SQLSTATE` makro koji se razvija u nisku od 5 karaktera, koja detaljnije opisuje česte kodove za greške među različitim softverskim rešenjima vezane za relacione baze od strane IBM-a. Mi ćemo se najčešće oslanjati na makro `SQLCODE` prilikom obrade grešaka.

U zavisnosti od vrednosti u koju se razvije makro `SQLCODE`, razlikujemo naredna tri slučaja:

- `SQLCODE = 0`: Naredba se uspešno izvršila i nije došlo do greške.

- `SQLCODE > 0`: Došlo je do nekakvog upozorenja od strane RSUBP, ali naredba je svakako izvršena. Specijalno, ukoliko je kod upozorenja `+100`, to znači da nije pronađen željeni podatak u bazi (u slučaju upita koji vraćaju jednu vrednost) ili da se došlo do kraja čitanja (u slučaju upita koji vraćaju više vrednosti).

- `SQLCODE < 0`: Došlo je do nekakve greške i naredba nije izvršena.

Da bismo otkrili šta zna\v ci određena greška, potrebno je da u komandnoj liniji izvršimo komandu:

```shell
db2 "? sql<KOD GRESKE>"
```

Na primer, ukoliko želimo da vidimo šta znači greška čiji je kod `-502`, potrebno je da izvršimo:

```shell
db2 "? sql-502"
```

### 2.2.7 Tok pisanja programa

Ovo poglavlje završavamo sekcijom koja opisuje koji su to osnovni koraci u programiranju C/SQL aplikacija na jednostavnom uvodnom primeru. 

Pisanje C programa sa ugnežđenim SQL naredbama obično podrazumeva naredne korake:

1. Uključivanje potrebnih zaglavlja
2. Deklaracija matičnih promenljivih
3. Povezivanje na bazu podataka
4. Izvršavanje SQL naredbi
5. Obrada SQL grešaka
6. Ostvarivanje transakcija
7. Prekidanje konekcije sa bazom podataka

{% include lab/exercise.html broj="2.1" tekst="Napisati C/SQL program koji ispisuje maksimalni indeks iz tabele `ISPIT`." %}

Rešenje. Potrebno je da pratimo prethodno opisane korake za pisanje C programa. U ovom primeru, ignorisaćemo korak 6. Na njega ćemo posvetiti posebnu pažnju u poglavljima koji slede. Dakle, krenimo redom:

**Uključivanje potrebnih zaglavlja**

Ovaj korak bi trebalo da nam bude jasan do sada:

```c
#include <stdio.h>

EXEC SQL INCLUDE SQLCA;
```

**Deklaracija matičnih promenljivih**

Kako je indeks definisan tipom `INTEGER`, možemo birati neki od tipova `int`, `long`, `long int` ili `sqlint32`. Pošto je preporučeno koristiti tip `sqlint32` zbog kompatibilnosti između operativnih sistema različite bit-nosti, matičnu promenljivu koja će sadržati traženi indeks deklarišemo na sledeći način:

```c
EXEC SQL BEGIN DECLARE SECTION;
sqlint32 hMaxIndex;
EXEC SQL END DECLARE SECTION;
```

Ostale korake ćemo izvršavati u funkciji `int main()`.

**Povezivanje na bazu podataka**

Da bismo se povezali na bazu podataka, koristićemo SQL naredbu oblika 

```sql
CONNECT TO imeBP USER imeKorisnika USING korisnickaLozinka;
```

U našem slučaju:

```c
EXEC SQL CONNECT TO stud2020 USER student USING abcdef;
```

**Izvršavanje SQL naredbi**

U ovom koraku je potrebno da napišemo odgovarajući SQL upit za izračunavanje najvećeg indeksa iz tabele `ISPIT` i da rezultat smestimo u prethodno definisanu matičnu promenljivu `hMaxIndex`. Zatim, potrebno je da ispišemo rezultat. To se može uraditi narednim delom koda:

```c
EXEC SQL 
    SELECT  MAX(INDEKS) 
    INTO    :hMaxIndex 
    FROM    DA.ISPIT;

printf("Najveci indeks je %d\n", hMaxIndex);
```

**Obrada SQL grešaka**

Naredni deo koda ilustruje najjednostavniji način provere da li je došlo do greške. Definišemo funkciju `checkSQL` koja će izvršavati proveru grešaka i njihovu obradu. Ova funkcija se poziva nakon svake SQL naredbe koja dolazi do izražaja u fazi izvršavanja. Funkcija kao argument prihvata nisku koja sadrži opis SQL naredbe nakon koje se poziva. Ovo ima smisla zbog toga što, u slučaju da dođe do greške, možemo vrlo jednostavno videti u kom delu izvornog koda je došlo do greške.

```c
#include <stdio.h>
#include <stdlib.h>

EXEC SQL INCLUDE SQLCA;

// Definicija funkcije za obradu gresaka
void checkSQL(const char *str) {
    if(SQLCODE < 0) {
        // Ispisujemo kod greske na standardni izlaz za greske, zajedno sa porukom koju smo prosledili
        fprintf(stderr, "Greska %d: %s\n", SQLCODE, str);

        // Zatvaramo konekciju sa bazom podataka i zavrsavamo program sa neuspehom
        EXEC SQL CONNECT RESET;
        exit(EXIT_FAILURE);
    }
}

int main() {
    EXEC SQL CONNECT TO stud2020 USER student USING abcdef;
    checkSQL("Konekcija na bazu podataka");

    // ...

    EXEC SQL CONNECT RESET;
    checkSQL("Prekidanje konekcije sa bazom podataka");

    return 0;
}
```

U svakom na\v sem programu \'cemo definisati funkciju `checkSQL`, koja je prikazana iznad. Dodatno, nakon svake `EXEC SQL` naredbe (koja \'ce se pozvati u fazi izvr\v savanja programa, dakle, sve naredbe osim `EXEC SQL INCLUDE`, `EXEC SQL BEGIN DECLARE SECTION` i `EXEC SQL END DECLARE SECTION`) neophodno je da pozovemo funkciju `checkSQL`, pri \v cemu \'cemo joj proslediti nisku koja sadrži opis te SQL naredbe, kako bismo lak\v se znali na kom mestu u programu je do\v slo do problema.

**Prekidanje konekcije sa bazom podataka**

Prekidanje konekcije je vrlo jednostavno. Dovoljno je izvršiti SQL naredbu `CONNECT RESET`:

```c
EXEC SQL CONNECT RESET;
```

Time smo ispunili zahtev zadatka. U nastavku je dato celokupno rešenje.

include_source(vezbe/primeri/poglavlje_2/zadatak_2_1.sqc, c)

Prevođenje rešenja zadatka 2.1 se vrši pomoću naredne naredbe koja se poziva u terminalu:

```shell
./prevodjenje zadatak_2_1 stud2020 student abcdef
```

Tokom ovog procesa možemo videti da li je došlo do nekih grešaka u fazi prevođenja. Ukoliko je sve prošlo kako treba, kreirana je izvršna datoteka `zadatak_2_1` koja se pokreće kao i svaki drugi izvršni C program:

```shell
./zadatak_2_1
```

{% include lab/exercise.html broj="2.2" tekst="Napisati C/SQL program koji ispisuje indeks, ime, prezime, mesto ro\dj enja (ukoliko je navedeno u bazi) i datum diplomiranja (ukoliko je navedeno u bazi) za studenta sa maksimalnim indeksom iz tabele `ISPIT`." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_2/zadatak_2_2.sqc, c)

{% include lab/exercise.html broj="2.3" tekst="Napisati C/SQL program koji ispisuje indeks, ime, prezime, mesto ro\dj enja (ukoliko je navedeno u bazi) i datum diplomiranja (ukoliko je navedeno u bazi) za studenta čiji se broj indeksa učitava sa standardnog ulaza." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_2/zadatak_2_3.sqc, c)

### 2.2.8 Naredbe `INSERT`, `UPDATE` i `DELETE`

Do sada smo diskutovali o programima u kojima smo dohvatali tačno jedan red u tabeli (ili, eventualno, nijedan), korišćenjem naredbe `SELECT INTO`. U nastavku sledi zadatak u kojem ćemo demonstrirati upotrebu naredbe `INSERT`, zatim pretražujućih varijanti naredbi `UPDATE` i `DELETE`. 

Naredbe `UPDATE` ili `DELETE` zovemo *pretražujućim* ukoliko se podaci koji se ažuriraju, odnosno, brišu pronalaze na osnovu nekih uslova restrikcije u klauzi `WHERE`.

Ove naredbe se jednostavno implementiraju njihovim navođenjem nakon `EXEC SQL`. Za razliku od `SELECT INTO` naredbe, nije va\v zno koliko redova \'ce biti dodato, a\v zurirano ili obrisano naredbama `INSERT`, `UPDATE` i `DELETE`, redom. Dakle, ovim naredbama mo\v zemo promeniti proizvoljan broj redova.

{% include lab/exercise.html broj="2.4" tekst="Napisati naredne funkcije:

- Napisati funkciju `void insertNewCourse()` koja sa standardnog ulaza učitava podatke o identifikatoru, oznaci, nazivu i bodovima za novi predmet na fakultetu. Potrebno je uneti te podatke u tabelu `PREDMET`. Nakon toga, odgovarajućom naredbom proveriti da li su podaci dobro uneti i ispisati ih.

- Napisati funkciju `void updateNewCourse()` koja izvršava ažuriranje podataka za novouneseni predmet, tako što se broj bodova duplo povećava i nakon čega se podaci izlistavaju ponovo.

- Napisati funkciju `void deleteNewCourse()` koja briše novouneseni predmet iz baze podataka.

Napisati i C/SQL program koji testira napisane funkcije."%}

Rešenje: S obzirom da se operacija ispisivanja podataka ponavlja u prve dve funkcije, onda \'cemo pretragu i ispis podataka o novom predmetu izdvojiti u pomo\'cnu funkciju `printNewCourseInfo`, koja \'ce biti pozvana u tim funkcijama.

include_source(vezbe/primeri/poglavlje_2/zadatak_2_4.sqc, c)

## 2.5 Zadaci za vežbu

{% include lab/exercise.html broj="2.5" tekst="Napisati C/SQL program koji ispisuje podatke za predmet čiji je identifikator `1693`." %}

{% include lab/exercise.html broj="2.6" tekst="Napisati C/SQL program koji ispisuje broj studenata koji su upisali studije u godini koja se unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="2.7" tekst="Napisati C/SQL program koji za svaku ocenu od 6 do 10 ispisuje ime i prezime studenta koji je poslednji položio neki ispit sa tom ocenom. U slučaju da ima više takvih studenata, klauzom `LIMIT 1` naredbe `SELECT INTO` se osigurati da rezultat vrati najviše 1 rezultat. (Pomoć: Koristiti `for` petlju za menjanje vrednosti matične promenljive koja sadrži ocenu, pa u svakoj iteraciji dohvatiti informaciju za tekuću vrednost te matične promenljive.)" %}

{% include lab/exercise.html broj="2.8" tekst="Napisati C/SQL program kojim se dodaje da je za polaganje predmeta čiji je identifikator `2343` uslov da se položi predmet čiji je identifikator `2327`, na studijskom programu sa identifikatorom `103`." %}

{% include lab/exercise.html broj="2.9" tekst="Napisati C/SQL program kojim se dodaje novi studijski program prvog stepena čiji se podaci unose sa standardnog ulaza." %}

{% include lab/exercise.html broj="2.10" tekst="Napisati C/SQL program kojim se svim obaveznim predmetima na studijskom programu `'Informatika'` povećava broj bodova za 2." %}

{% include lab/exercise.html broj="2.11" tekst="Napisati C/SQL program kojim se, za sve položene ispite čiji se naziv predmeta unosi sa standardnog ulaza, ocena uvećava za 1." %}

{% include lab/exercise.html broj="2.12" tekst="Napisati C/SQL program kojim se brišu svi podaci o ispitima za studenta čiji se indeks unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="2.13" tekst="Napisati C/SQL program kojim se ukida uslovnost svih predmeta koji su uslovni da bi se položio predmet čiji se identifikator unosi sa standardnog ulaza." %}