---
layout: page
title: 3. Programiranje korišćenjem kursora
under_construction: true
---

Do sada su rezultati naših SQL upita bili kardinalnosti 1. Zbog toga smo mogli da koristimo jednostavnu `SELECT INTO` naredbu za dohvatanje informacija iz rezultata upita. Ukoliko smo sigurni da će rezultat upita biti jedan red, ovakav način programiranja je prihvatljiv. Međutim, ukoliko znamo da se rezultat može sastojati od više redova, potreban nam je drugačiji pristup.

U slučaju da nam je nepoznat broj redova u rezultatu upita, za dohvatanje rezultata možemo iskoristiti mehanizam zasnovan na kursorima. Kursorima je moguće procesirati svaki red rezultata, bez obzira na to koliko redova rezultat sadrži. Kursor je imenovana kontrolna struktura koja se koristi od strane aplikativnog programa da "pokazuje" na specifičan red u okviru uređenog skupa redova.

## 3.1 Rad sa kursorima

Rad sa kursorima se najčešće može opisati kroz naredna četiri koraka:

1. Deklaracija kursora
2. Otvaranje kursora
3. Iteriranje kroz kursor
4. Zatvaranje kursora

### 3.1.1. Deklaracija kursora

Deklaracija kursora se izvodi navođenjem SQL naredbe `DECLARE` čija je sintaksa data u nastavku:

```sql
DECLARE <IME_KURSORA>
CURSOR FOR <UPIT>
[(FOR READ ONLY)|(FOR UPDATE OF <LISTA_ATRIBUTA>)]
[(WITHOUT HOLD)|(WITH HOLD)]
```

Promenljiva `<IME_KURSORA>` mora biti jedinstvena u programu. Vrednost `<UPIT>` predstavlja upit, tj. naredbu `SELECT` za koji se kursor vezuje. Upit ne može da sadrži parametarske oznake, ali može sadržati matične promenljive, s tim da deklaracije matičnih promenljivih koje se koriste u upitu moraju biti pre deklaracije kursora. 

Ukoliko navedemo klauzu `FOR READ ONLY`, time definišemo kursor koji služi samo za čitanje podataka. Ukoliko želimo da se podaci menjaju pomoću kursora, tada se nakon `<UPIT>` navodi klauza `FOR UPDATE OF` za kojom sledi lista imena kolona u rezultatu upita koji se mogu menjati kursorom, odvojeni zapetama.

Postoje tri tipa kursora:

- *\v Citaju\'ci*, koji podr\v zava samo operaciju \v citanja podataka. Ovim kursorom nije mogu\'ce vr\v siti operacije brisanja ili a\v zuriranja.
- *Bri\v su\'ci*, koji podr\v zava operacije \v citanja i brisanja podataka. Ovim kursorom nije mogu\'ce vr\v siti operaciju a\v zuriranja.
- *A\v zuriraju\'ci*, koji podr\v zava sve tri operacije nad podacima.
- *Dvosmisleni*, ukoliko Db2 nije u stanju da na osnovu upita zaklju\v ci da li \'ce se kursor koristiti za \v citanje ili menjanje podataka.

U nastavku slede njihove definicije:

- Za kursor kažemo da je *brišući* ako je svaki od narednih uslova ispunjen:
    - Klauza `FROM` u glavnom delu `<UPIT>` sadrži ta\v cno jednu tabelu ili pogled nad jednom tabelom koji služi za brisanje.
    - `<UPIT>` ne sadrži neku od klauza `DISTINCT` ili `VALUES`.
    - `<UPIT>` ne sadrži `ORDER BY` i klauzu `FOR UPDATE OF` (zajedno).
    - `<UPIT>` ne sadrži agregatne funkcije ili neku od klauza `GROUP BY` ili `HAVING`.
    - `<UPIT>` ne sadrži neki skupovni operator, kao što su `UNION`, `INTERSECT` ili `EXCEPT` sa izuzetkom `UNION ALL`.
    - `<UPIT>` ne sadrži klauzu `FOR READ ONLY`.

- Za kursor kažemo da je *\v citaju\'ci* ukoliko nije bri\v su\'ci. 

- Za kursor kažemo da je *ažurirajući* ukoliko je brišući i kolone koje su proglašene za ažuriranje u klauzi `FOR UPDATE OF` predstavljaju neke od kolona u baznoj tabeli. 

- Za kursor kažemo da je *dvosmisleni* ukoliko je upit dinamički pripremljen i nije navedena nijedna od klauza `FOR READ ONLY` ili `FOR UPDATE OF` i kursor zadovoljava uslove bri\v su\'ceg kursora.

Navođenjem neke od opcionih klauza `WITHOUT HOLD` ili `WITH HOLD` možemo specifikovati da li će se kursor zatvarati ili ne kao posledica operacije *pohranjivanja* (engl. *commit*). Navođenjem klauze `WITHOUT HOLD` kursor se ne sprečava da bude zatvoren, što je podrazumevano ponašanje. Navođenjem klauze `WITH HOLD` kursor održava resurse kroz različite jedinice posla. Kada budemo diskutovali o transakcijama, definisaćemo preciznije ovo ponašanje.

### 3.1.2. Otvaranje kursora

Otvaranje kursora se izvodi navođenjem SQL naredbe `OPEN` čija je sintaksa data u nastavku:

```sql
OPEN <IME_KURSORA>
[USING <LISTA_MATICNIH_PROMENLJIVIH>]
```

Naredbom `OPEN` se vrši otvaranje kursora i njegovo izvršavanje, zarad dohvatanja redova iz rezultujuće tabele. Promenljiva `<IME_KURSORA>` mora biti deklarisana naredbom `DECLARE` pre samog otvaranja kursora. Kada se izvrši naredba `OPEN`, kursor naziva `<IME_KURSORA>` mora biti u zatvorenom stanju (bilo da je eksplicitno zatvoren ili da je samo deklarisan pre otvaranja).

Ukoliko se kursor otvara za pripremljenu SQL naredbu, navođenjem klauze `USING` možemo uvesti vrednosti koje se koriste za zamenu parametarskih oznaka. U slučaju statičke SQL naredbe, pri deklaraciji kursora, klauza `USING` se može koristiti, upravo iz razloga što statičke SQL naredbe ne mogu imati parametarske oznake.

Nakon otvaranja kursor je pozicioniran ispred prvog reda rezultujuće tabele.

### 3.1.3. Iteriranje kroz kursor

Iteriranje kroz kursor se izvodi navođenjem SQL naredbe `FETCH` čija je sintaksa data u nastavku:

```sql
FETCH <IME_KURSORA>
INTO <LISTA_MATICNIH_PROMENLJIVIH>
```

Naredbom `FETCH` se vrši pozicioniranje kursora na naredni red iz rezultujuće tabele i dodeljuju se vrednosti iz tog reda ciljanim promenljivama. Promenljiva `<IME_KURSORA>` mora biti deklarisana naredbom `DECLARE` pre samog dohvatanja podataka. Dodatno, da bi se izvršila naredba `FETCH`, kursor naziva `<IME_KURSORA>` mora biti u otvorenom stanju.

Klauzom `INTO` se prva vrednost dohvaćenog reda smešta u prvu promenljivu koja je navedena u `<LISTA_MATICNIH_PROMENLJIVIH>`, druga vrednost reda u drugu promenljivu, itd. Ako dođe do greške pri bilo kojoj dodeli vrednosti, ta vrednost se ne dodeljuje promenljivoj, kao ni bilo koja vrednost nakon nje. Sve do tada dodeljene vrednosti ostaju dodeljene.

Otvoreni kursor ima tri moguće pozicije: 

1. Može biti pozicioniran ispred prvog reda.
2. Može biti pozicioniran na nekom redu.
3. Može biti pozicioniran nakon poslednjeg reda.

Kursor može biti samo pozicioniran na nekom redu isključivo primenom naredbe `FETCH`. Ako se kursor pozicionira na poslednjem redu rezultujuće tabele ili iza njega, izvršavanje naredbe `FETCH` ima naredne efekte:

- Vrednost koda za dijagnostiku greške (u našim programima, vrednost u koju se razmota makro `SQLCODE`) postavlja se na vrednost `+100`.

- Kursor se pozicionira nakon poslednjeg reda rezultata.

- Vrednosti se ne dodeljuju matičnim promenljivama.

Ako je kursor pozicioniran ispred prvog reda, izvršavanjem naredbe `FETCH`, kursor se pozicionira na prvi red, i vrednosti se dodeljuju matičnim promenljivama klauzom `INTO`. Ako je kursor pozicioniran na redu koji nije poslednji, izvršavanjem naredbe `FETCH`, kursor se pozicionira na naredni red i vrednosti tog reda se dodeljuju matičnim promenljivama klauzom `INTO`.

Ako je kursor pozicioniran na nekom redu, taj red se naziva *tekući red kursora*. Kursor na koji se referiše u pozicionirajućim naredbama `UPDATE` ili `DELETE` mora biti pozicioniran na nekom redu. Moguće je da se, dolaskom do greške, stanje kursora postavi na nepredvidivo.

### 3.1.4. Zatvaranje kursora

Zatvaranje kursora se izvodi navođenjem SQL naredbe `CLOSE` čija je sintaksa data u nastavku:

```sql
CLOSE <IME_KURSORA>
[WITH RELEASE]
```

Naredbom `CLOSE` se vrši zatvaranje kursora. Ukoliko je rezultujuća tabela kreirana kada je kursor otvoren, ta tabela se uništava. Promenljiva `<IME_KURSORA>` mora biti deklarisana naredbom `DECLARE` pre samog dohvatanja podataka. Dodatno, da bi se izvršila naredba `CLOSE`, kursor naziva `<IME_KURSORA>` mora biti u otvorenom stanju.

Ukoliko se navede opciona klauza `WITH RELEASE`, prilikom zatvaranja kursora se pokušava sa oslobađanjem svih katanaca koji su držani od strane kursora. S obzirom da se katanci mogu držati drugim operacijama ili procesima, ne znači da će biti nužno i oslobođeni zatvaranjem kursora. O katancima će biti više reči u kasnijim poglavljima.

Na kraju jedinice posla, svi kursori koji pripadaju procesu aplikacije i koji su deklarisani bez klauze `WITH HOLD` se implicitno zatvaraju.

## 3.2 Korišćenje kursora za čitanje podataka

Sada smo spremni za rešavanje primera koji zahtevaju upotrebu kursora. Počnimo sa narednim zadacima koji ilustruju čitanje rezultata iz kursora.

{% include lab/exercise.html broj="3.1" tekst="Napisati C/SQL program koji ispisuje identifikator, oznaku, naziv, broj semestara i broj bodova za svaki od smerova." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_3/zadatak_3_1.sqc, c)

{% include lab/exercise.html broj="3.2" tekst="Napisati C/SQL program kojim se za uneti broj indeksa studenta ispisuju podaci (naziv predmeta, datum polaganja i ocena) za sve ispite koje je on položio. Nakon toga ispisuje se njegov prosek." %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_3/zadatak_3_2.sqc, c)

## 3.3 Korišćenje kursora za ažuriranje i brisanje podataka

Kao što smo napomenuli, moguće je korišćenje kursora za ažuriranje ili brisanje redova iz tabela. U slučaju ažuriranja redova, potrebno je deklarisati koje kolone se mogu menjati pri deklaraciji kursora, a zatim koristiti `UPDATE` naredbu oblika:

```sql
UPDATE  <TABELA>
SET     <KOLONA_1> = <VREDNOST_1>,
        -- ...
        <KOLONA_N> = <VREDNOST_N>
WHERE   CURRENT OF <IME_KURSORA>
```

{% include lab/definition.html def="SQL naredba `UPDATE` koja ažurira podatke u tabeli na osnovu pozicije nekog kursora naziva se *pozicionirajuća* `UPDATE` naredba. " %}

Očigledno, kolone `<KOLONA_1>`, `...`, `<KOLONA_N>` moraju biti deklarisane u `FOR UPDATE OF` klauzi pri deklaraciji kursora naziva `<IME_KURSORA>`. Takođe, tabela `<TABELA>` mora biti jedina tabela koja se nalazi u `FROM` klauzi kursora. Na ovaj način će upotrebom opisane naredbe `UPDATE` biti ažuriran tekući red kursora.

{% include lab/exercise.html broj="3.3" tekst="Napisati C/SQL program kojim se za svaki od smerova korisniku postavlja pitanje da li želi da uveća broj bodova za 10. Ako je odgovor potvrdan, vrši se odgovarajuća promena." %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_3/zadatak_3_3.sqc, c)

U slučaju brisanja redova pomoću kursora, nije potrebno deklarisati koje kolone se mogu brisati kao što je to bio slučaj sa ažuriranjem. Potrebno je samo da tabela `<TABELA>` bude jedina tabela koja se nalazi u `FROM` klauzi upita kursora naziva `<IME_KURSORA>`, da bi se mogla koristiti `DELETE` naredba oblika:

```sql
DELETE 
FROM    <IME_TABELE>
WHERE   CURRENT OF <IME_KURSORA>
```

{% include lab/definition.html def="SQL naredba `DELETE` koja ažurira podatke u tabeli na osnovu pozicije nekog kursora naziva se *pozicionirajuća* `DELETE` naredba." %}

{% include lab/exercise.html broj="3.4" tekst="Napisati C/SQL program kojim se za sve studente smera Informatika briše prvi položen ispit (ukoliko ima položenih ispita tog studenta)." %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_3/zadatak_3_4.sqc, c)

## 3.4 Ugnežđeni kursori

Do sada smo videli kako možemo koristiti kursore za upravljanje potencijalno višim brojem redova u rezultatu upita. Obrada ovih redova je do sada obuhvatala jednostavno procesiranje podataka, poput ispisivanja na standardni izlaz, uz eventualne transformacije ili ažuriranje ili brisanje podataka na koje pokazuje kursor.

Međutim, šta raditi ukoliko je potrebno da za svaki red rezultata jednog upita izvršimo akciju nad rezultatom nekog drugog upita? Da li nam kursori u ovakvim situacijama mogu pomoći? Odgovor je potvrdan zbog činjenice da je kursore moguće ugnežđavati. Tipičan tok rada podrazumeva naredne korake u slučaju dva kursora od kojih je jedan (unutrašnji) ugnežđen u drugi (spoljašnji):

1. Deklaracija spoljašnjeg kursora.

2. Deklaracija unutrašnjeg kursora.

3. Otvaranje spoljašnjeg kursora.

4. Dohvatanje jednog po jednog reda spoljašnjeg kursora. Za svaki dohvaćeni red u spoljašnjem kursoru:

   1. Obrada dohvaćenih podataka spoljašnjeg kursora.

   2. Otvaranje unutrašnjeg kursora.

   3. Dohvatanje jednog po jednog reda unutrašnjeg kursora. Za svaki dohvaćeni red u unutrašnjem kursoru:

      1. Obrada dohvaćenih podataka unutrašnjeg kursora.

   4. Zatvaranje unutrašnjeg kursora.

5. Zatvaranje spoljašnjeg kursora.

Naredna dva zadatka ilustruju rad sa ugnežđenim kursorima.

{% include lab/exercise.html broj="3.5" tekst="Napisati C/SQL program kojim se formira izveštaj o studentima koji su padali neki ispit koji sadrži sledeće informacije: ime, prezime i broj indeksa. Za svaki smer formirati posebnu sekciju izveštaja sa odgovarajućim zaglavljem. Sadržaj svake sekcije urediti po broju indeksa." %}

Re\v senje:

include_source(vezbe/primeri/poglavlje_3/zadatak_3_5.sqc, c)

{% include lab/exercise.html broj="3.6" tekst="Napisati C/SQL program kojim se za svaki smer pronalazi student koji ima najviše položenih ESPB bodova. Zatim u tabeli ISPIT u napomeni koja se odnosi na poslednji položeni ispit tog studenta zapisuje 'Ovo je student koji ima najvise polozenih kredita na svom smeru'." %}

Re\v senje: U ovom zadatku smo kreirali pomo\'cne funkcije koje upravljaju kursorima i obra\dj uju podatke iz dohva\'cenih rezultata, kako bismo pove\'cali modularnost koda. Ono \v sto je va\v zno primetiti jeste da, bez obzira na organizaciju izvornog koda, neophodno je da se deklaracije kursora u kodu nalaze ispred drugih operacija sa kursorima u kodu. Ukoliko to nije slu\v caj, onda \'ce Db2 pretprocesor prijaviti gre\v sku. Razlog za ovo pona\v sanje jeste zbog toga \v sto Db2 pretprocesor kod \v cita kao tekst, ne uzimaju\'ci u obzir redosled stvarnog izvr\v savanja operacija i poziva funkcija. Na primer, ukoliko bi Db2 pretprocesor prvo nai\v sao na naredbu `OPEN`, pa onda na `DECLARE`, tada bi prijavio gre\v sku zato \v sto naredba `OPEN` referi\v se na naziv kursora za koji Db2 pretprocesor prethodno nije zapamtio da postoji.

include_source(vezbe/primeri/poglavlje_3/zadatak_3_6.sqc, c)

## 3.5 Zadaci za vežbu

{% include lab/exercise.html broj="3.7" tekst="Napisati C/SQL program koji ispisuje sva ženska imena koja postoje među studentima (zajedno sa brojem pojavljivanja) u opadajućem poretku." %}

{% include lab/exercise.html broj="3.8" tekst="Napisati C/SQL program koji ispisuje za svakog studenta ime, prezime, poslednji položeni ispit (naziv predmeta koji je položen), kao i datum polaganja tog ispita." %}

{% include lab/exercise.html broj="3.9" tekst="Napisati C/SQL program koji se za sve studente smera Informatika ažurira u tabeli ISPIT prvi položen ispit (ukoliko ima položenih ispita za tog studenta) tako što povećava ocenu za 1 (ukoliko je ocena bila 5 ili 10 ostavlja je nepromenjenu)." %}

{% include lab/exercise.html broj="3.10" tekst="Napisati C/SQL program koji ispisuje sve napomene koje se nalaze u tabeli ISPIT, navodeći i broj indeksa studenata." %}

{% include lab/exercise.html broj="3.11" tekst="Napisati C/SQL program kojim se sa standardnog ulaza unosi ime smera, a zatim se ispisuje 10 studenata tog smera koji su najviše padali na ispitima tokom studija. Izdvojiti ime, prezime, broj indeksa i broj padova tokom studija." %}

{% include lab/exercise.html broj="3.12" tekst="Napisati C/SQL program koji ispisuje za svakog studenta ime, prezime, poslednji položeni ispit (tj. naziv predmeta koji je položen), kao i datum polaganja tog ispita." %}

{% include lab/exercise.html broj="3.13" tekst="Napisati C/SQL program koji za studenta čiji se broj indeksa zadaje sa standardnog ulaza, ispisuje naziv predmeta, datum polaganja, ocenu, broj bodova na pismenom i broj bodova na usmenom delu ispita za svaki ispit koji je student položio. Nakon toga ispisuje se prosečna ocena studenta." %}

{% include lab/exercise.html broj="3.14" tekst="Napisati C/SQL program kojim se omogućava nastavniku da unese naziv predmeta, godinu roka i oznaku roka. Za svako polaganje datog predmeta u datom ispitnom roku ponuditi nastavniku mogućnost da izmeni ocenu koju je student osvojio. Ispisati informacije o indeksu, imenu i prezimenu studenta kao i ocenu koju je dobio, pa zatražiti od nastavnika novu ocenu. Nakon unosa nove ocene, obavestiti nastavnika o uspešnosti izmene i preći na naredno polaganje (ukoliko ih ima više)." %}

{% include lab/exercise.html broj="3.15" tekst="Napisati C/SQL program kojim se brišu sva uspešna polaganja ispita iz barem trećeg pokušaja za studente koji su upisivali najviše N godina, gde se vrednost za N unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="3.16" tekst="Napisati C/SQL program kojim se prvo unosi identifikator predmeta sa standardnog ulaza. Za taj predmet se zatim korisniku nudi da li želi da obriše uslovne predmete za taj predmet i to tako što se za svaki uslovni predmet nudi da li korisnik hoće da ga obriše ili ne. Odgovori su 'D' ili 'N'. Ukoliko je odgovor 'D', u tabeli `USLOVNI_PREDMET` briše se željeni red koji se odnosi na predmet za koje je pitanje postavljeno." %}

{% include lab/exercise.html broj="3.17" tekst="Napisati C/SQL program kojim se, za svakog studenta koji se upisao u godini koja se učitava sa standardnog ulaza, ispisuju podaci o imenu, prezimenu i prosečnoj oceni, a zatim se ispisuju informacije o položenim ispitima i to: naziv predmeta, ocena i datum polaganja." %}

{% include lab/exercise.html broj="3.18" tekst="Napisati C/SQL program koji ispisuje izveštaj za svaki predmet o njegovim uslovnim predmetima. Za svaki predmet ispisati informacije o identifikatoru predmeta i njegovom nazivu. Svaka sekcija koja ispisuje informacije o uslovnim predmetima treba da izlistava identifikator i naziv uslovnih predmeta za tekući predmet za koji se pravi sekcija, pri čemu se prikazuju informacije samo o onim uslovnim predmetima za koje postoji polaganje sa ocenom 10 na osnovnim studijama čiji je datum polaganja bio pre 2011. godine. Stavke sekcije prikazati razdvojene zapetom, i svaki uslovni predmet prikazati u posebnom redu." %}

{% include lab/exercise.html broj="3.19" tekst="Napisati C/SQL program koji za svaki ispitni rok ispisuje njegov naziv i broj uspešnih polaganja za svaku ocenu u tom ispitnom roku. Nakon ispisivanja informacija o ispitnom roku, ponuditi korisniku da izbriše informacije o polaganim ispitima u tom roku. Ukoliko korisnik želi da obriše te podatke, prvo izlistati podatke o indeksu, identifikatoru predmeta, godini roka, oznaci roka i datumu polaganja (ako postoji) za svako polaganje koje se briše. Na kraju brisanja polaganja u jednom ispitnom roku, ispisati ukupan broj obrisanih redova." %}
    