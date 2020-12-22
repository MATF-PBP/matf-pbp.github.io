---
layout: page
title: 5. Implementiranje transakcija
under_construction: true
---

Transakcije predstavljaju izuzetno važan alat za svakog programera koji koristi baze podataka u svojim aplikacijama. Svaka iole kompleksnija operacija nad podacima zahteva korišćenje transakcija da bi se takva operacija uspešno implementirala. Pritom, postoji veliki broj pitanja i potencijalnih problema koji se otvaraju prilikom korišćenja  transakcija. U ovoj sekciji ćemo se upoznati sa različitim naredbama za rad sa transakcijama u DB2 sistemu za upravljanje relacionim bazama podataka. Započnimo ovaj deo teksta narednom definicijom.

*Transakcija* (engl. *transaction*) predstavlja logičku jedinicu posla pri radu sa podacima.

Transakcija predstavlja niz radnji koji ne narušava uslove integriteta. Sa stanovišta korisnika, izvršavanje transakcije je atomično. Po izvršenju kompletne transakcije stanje baze treba da bude konzistentno, tj. da su ispunjeni uslovi integriteta. Dakle, posmatrana kao jedinica posla, transakcija prevodi jedno konzistentno stanje baze u drugo takvo stanje baze, dok u međukoracima transakcije konzistentnost podataka može biti i narušena. Transakcija na taj način predstavlja i bezbedno sredstvo za interakciju korisnika sa bazom.

## 5.1 Operacije potvrđivanja i poništavanja izmena u bazi podataka

Pre nego što započnemo detaljnu diskusiju o implementaciji transakcija, obratićemo pažnju na jedan realan primer izvršavanja programa koji implementira prebacivanje novca sa jednog računa na drugi. Ovaj primer će nam služiti kao glavna motivacija zašto je potrebno da razumemo rad sa transakcijama. Pre nego što demonstriramo primer, dajmo narednu definiciju.

*Atomična* (engl. *atomic*) operacija je ona operacija koju nije moguće podeliti na više manjih operacija (tj. u pitanju je nedeljiva operacija).

Premeštaj novca sa jednog računa na drugi se može implementirati narednim nizom atomičnih operacija (radi čuvanja prostora, iz narednog niza eliminišemo razne provere, kao što su provera da li korisnik može da prebaci novac između računa, provera da li ima dovoljno sredstava na prvom računu, itd.):

1. Dohvati red u tabeli koji predstavlja prvi račun.
2. Umanji iznos u tom redu za traženu sumu.
3. Dohvati red u tabeli koji predstavlja drugi račun.
3. Uvećaj iznos u tom redu za traženu sumu.

Pretpostavimo da SUBP operacije iz ovih koraka implementira na takav način da se neposredno nakon njihovog izvršavanja sve izmene trajno upisuju u bazu podataka. Naredna slika ilustruje jedno moguće izvršavanje programa koji implementira ove korake. U tom izvršavanju, program prvo naredbom `SELECT` dohvata red u tabeli koji predstavlja prvi račun (koji je na početku imao 100 000 jedinica valute). Zatim, u drugom koraku, naredbom `UPDATE` umanjuje iznos u tom redu za traženu sumu (10 000 jedinica valute). Iz nekog razloga (nestanak struje, prekid mrežne komunikacije, itd.), nakon 2. koraka, program prijavljuje grešku i operativni sistem ga prekida. Međutim, kao što je prikazano na slici, stanje baze je takvo da je prvom računu umanjen iznos i informacija o tome da novac nije uspešno prebačen na drugi račun se izgubila. Drugim rečima, baza se nalazi u *nekonzistentnom* stanju. Zbog toga, važno je zapamtiti da **SUBP nikada ne implementira operacije izmena tako da njihovi efekti budu trajno upisani u bazu podataka neposredno nakon njihovog izvršavanja**.

![Nepravilna implementacija transakcija može dovesti do kršenja pravila u poslovnom domenu](./Slike/CitanjePrePotvrdjivanja.png){:class="ui centered large image"}

Sada je validno postaviti pitanje - u kom trenutku se informacije o izmenama zaista upisuju u bazu podataka? Db2 baza podataka definiše naredne dve operacije za rad sa izmenama u bazi podataka.

*Potvrđivanje* (engl *commit*) predstavlja trajno upisivanje izmena u bazu podataka koje su do tada bile izvršene nad tom bazom podataka. Sve načinjene izmene se trajno pamte u bazi podataka i svi ostali procesi dobijaju mogućnost da vide načinjene izmene.

*Poništavanje* (engl *rollback*) predstavlja vraćanje stanje baze podataka u ono u kojem se baza podataka našla pre izvršavanja izmena koje su do tada bile izvršene nad tom bazom podataka. Izvršavanjem ove naredbe možemo poništiti sve one akcije koje do trenutka poništavanja nisu prethodno bile potvrđene.

Da bismo dodatno razumeli koje su to izmene u bazi podataka koje se potvrđuju, odnosno, poništavaju ovim operacijama, potrebno je da definišemo pojam jedinice posla.

*Jedinica posla* (engl. *unit of work*, skr. *UOW*) predstavlja nadoknadivu sekvencu operacija u okviru aplikacionog procesa.

Jedinica posla se inicijalizuje prilikom pokretanja aplikacionog procesa ili kada se prethodna jedinica posla završila posledicom operacije koja nije prekid aplikacionog procesa. Jedinica posla se završava operacijom potvrđivanja ili poništavanja izmena ili završetkom aplikacionog procesa. Operacije potvrđivanja i poništavanja izmena utiču samo da one promene u bazi podataka koje su izvršene tokom te jedinice posla koja se završava.

Inicijalizacija i završetak jedinice posla definišu tačke konzistentnosti u okviru aplikacionog procesa. Razmotrimo prethodni primer bankarske transakcije u kojoj se vrši premeštaj sredstava sa jednog računa na drugi račun. Kao što smo rekli, nakon drugog koraka (oduzimanja sredstava) podaci su nekonzistentni. Tek nakon izvršavanja četvrtog koraka (uvećavanje sredstava) konzistentnost je obnovljena, što je prikazano na narednoj slici:

!["Grafički prikaz jedne jedinice posla tokom vremena. Ova jedinica posla se uspešno izvršila i sve izmene koje predstavljaju deo te jedinice posla se uspešno potvrđuju u bazi podataka."](./Slike/uow1.png){:class="ui centered large image"}

Kada se oba koraka izvrše, može se iskoristiti operacija potvrđivanja izmena da bi se završila jedinica posla. Ako dođe do greške pre nego što se jedinica posla uspešno završi, SUBP će poništiti sve nepotvrđene izmene da bi vratio stanje baze podataka u konzistentno, što je prikazano na narednoj slici:

!["Grafički prikaz jedne jedinice posla tokom vremena. U ovoj jedinici posla je došlo do greške, čime je neophodno da se izmene koje su načinjene u bazi podataka ponište."](./Slike/uow2.png){:class="ui centered large image"}

Dakle, rešenje problema prenosa novca bismo implementirali narednim koracima:

1. (Implicitno) Započni novu jedinicu posla (bilo započinjanjem novog procesa, prethodnim izvršavanjem naredba potvrđivanja ili poništavanja, itd.).
2. Dohvati red u tabeli koji predstavlja prvi račun.
3. Umanji iznos u tom redu za traženu sumu.
4. Dohvati red u tabeli koji predstavlja drugi račun.
5. Uvećaj iznos u tom redu za traženu sumu.
6. Potvrdi izmene u bazi podataka.

Naravno, nakon svakog koraka je neophodno izvršiti proveru grešaka. U slučaju greške u bilo kom trenutku, program mora da izvrši poništavanje izmena, čime se stanje baze vraća u ono koje je bilo pre 1. koraka, dakle, u konzistentno stanje.

SQL jezik definiše dve naredbe koje odgovaraju opisanim operacijama:

- Naredba `COMMIT` implementira operaciju potvrđivanja izmena.

- Naredba `ROLLBACK` implementira operaciju poništavanja izmena.

O ovim naredbama i njihovim bočnim efektima ćemo detaljnije govoriti u sekcijama [o potvr\dj ivanju izmena](#54-potvrđivanje-izmena) i [o poni\v stavanju izmena](#55-poništavanje-izmena). Međutim, diskusija koju smo izložili do sada je dovoljna za demonstraciju najosnovnijeg efekta ovih naredbi.

{% include lab/exercise.html broj="5.1" tekst="Napisati C/SQL program koji redom:

1. Pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.
2. Briše studenta sa pronađenim indeksom iz tabele `ISPIT` i ispisuje poruku korisniku o uspešnosti brisanja.
3. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`.
4. Pita korisnika da li želi da potvrdi ili poništi izmene. U zavisnosti od korisnikovog odgovora, aplikacija potvrđuje ili poništava izmene uz ispisivanje poruke korisniku.
5. Ponovo pronalazi i ispisuje najveći indeks iz tabele `ISPIT`." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_5/zadatak_5_1.sqc, c)

Vrlo je bitno primetiti naredne dve stvari u kodu:

- U slučaju uspešnog izvršavanja programa, pre nego što zatvorimo konekciju sa bazom podataka, izvršavamo SQL naredbu `COMMIT` kako bismo potvrdili sve izmene koje je naša aplikacija eventualno izvršila nad bazom podataka.

- U slučaju da dođe do greške prilikom izvršavanja programa, pre nego što izađemo iz programa i prijavimo neuspeh, u funkciji `checkSQL` izvršavamo naredbu `ROLLBACK` kako bismo poništili sve izmene koje je naša aplikacija eventualno izvršila nad bazom podataka.

Ovo je dobra praksa i mi ćemo usvojiti ovaj način rada u našim C/SQL programima nadalje.

## 5.2 Složena SQL naredba

U ovoj sekciji ćemo diskutovati o načinima za izvršavanje više SQL naredbi kao jedne naredbe, tzv. složene SQL naredbe. Složene SQL naredbe predstavljaju osnovu rada sa transakcijama, o čemu će biti detaljnije reči u nastavku teksta.

*Složena SQL naredba* (engl. *compound SQL*) predstavlja sekvencu SQL naredbi ograđenu odgovarajućim ključnim rečima kojim se definiše jedan blok izvršavanja.

Postoje tri tipa složenih SQL naredbi:

1. *Linijske* (engl. *inline*) - Složena SQL linijska naredba je složena SQL naredba koja je umetnuta linijski tokom faze izvršavanja u okviru druge SQL naredbe. Složene SQL linijske naredbe imaju svojstvo atomičnosti; ako izvršavanje bilo koje pojedinačne SQL naredbe podigne grešku, cela naredba se poništava.

2. *Ugnežđene* (engl. *embedded*) - Ovaj tip naredbi kombinuje jednu ili više SQL naredbi (odnosno, podnaredbi) u jedan izvršivi blok. 

3. *Kompilirane* (engl. *compiled*) - Predstavlja sekvencu SQL naredbi koje se izvršavaju sa lokalnim opsegom za promenljive, uslove, kursore i pokazivače na slogove (engl. *handle*).

Mi ćemo u daljem tekstu razmatrati isključivo složene SQL ugnežđene naredbe, tako da podrazumevamo ovaj tip kada kažemo "složena SQL naredba". Sintaksa ovih naredbi je data u nastavku:

```sql
BEGIN COMPOUND (ATOMIC|NOT ATOMIC) STATIC
[STOP AFTER FIRST <MATICNA_PROMENLJIVA> STATEMENTS]

<SQL_NAREDBA>;
<SQL_NAREDBA>;
-- ...
<SQL_NAREDBA>;

END COMPOUND
```

U zavisnosti od odabranih vrednosti narednih opcija prilikom deklaracije složene SQL naredbe, ta naredba može imati različite varijante koji utiču na način izvršavanja:

- Atomičnost - tačno jedna od naredne dve opcije se navode
   
   - ATOMIC - Specifikuje da, ako bilo koja od podnaredbi u okviru složene SQL naredbe bude izvršena neuspešno, onda se sve izmene u bazi podataka koje su nastale efektom bilo koje druge podnaredbe, bilo one uspešne izvršene ili ne, poništavaju.
   
   - NOT ATOMIC - Specifikuje da, bez obzira na neuspešno izvršavanje podnaredbi, složena SQL naredba neće poništiti izmene u bazi podataka koje su nastale efektom bilo koje druge podnaredbe.

- Statičnost - navodi se naredna naredba
   
   - STATIC - Specifikuje da će sve matične promenljive za sve podnaredbe zadržati njihove originalne vrednosti. Na primer, ukoliko se u SQL složenoj naredbi nađe naredba:

```sql
SELECT  MIN(OCENA) 
INTO    :ocena 
FROM    DA.ISPIT
```

koja je praćena naredbom

```sql
DELETE  FROM DA.ISPIT 
WHERE   OCENA = :ocena
```

onda će naredba `DELETE` koristiti vrednost matične promenljive `ocena` koju je ta promenljiva imala na početku bloka koji je definisan složenom SQL naredbom, a ne vrednost koju je naredba `SELECT INTO` upisala u tu mati\v cnu promenljivu. Time je transakcija koja se ostvaruje tom SQL složenom naredbom, za koju bismo možda očekivali da bude korektna, zapravo neispravno implementirana, zato što se oslanja na međuvrednost koju mati\v cna promenljiva `ocena` dobija naredbom `SELECT INTO`. Dodatno, ako se vrednost iste promenljive postavlja od strane više SQL podnaredbi, onda će na kraju bloka ta promenljiva sadržati vrednost koju je postavila poslednja SQL podnaredba.

Napomenimo da u DB2 sistemu nestatičko ponašanje nije podržano. To znači da bi trebalo posmatrati kao da se podnaredbe izvršavaju nesekvencijalno i podnaredbe ne bi trebalo da imaju međuzavisnosti, kao u datom primeru.

- Opcionom klauzom `STOP AFTER FIRST` specifikujemo da će se izvršiti samo određeni broj podnaredbi. Matična promenljiva `<MATICNA_PROMENLJIVA>` tipa `short` sadrži ceo broj *N* kojim se specifikuje koliko prvih *N* podnaredbi će biti izvršeno.

Nakon opisanih opcija, potrebno je navesti nula ili više SQL naredbi `<SQL_NAREDBA>`. SQL naredbe koje je moguće navesti kao deo složenih SQL naredbi su sve izvršive naredbe, osim narednih 14 naredbi:

1. `CALL`
2. `CLOSE`
3. `CONNECT`
4. Složena SQL naredba
5. `DESCRIBE`
6. `DISCONNECT`
7. `EXECUTE IMMEDIATE`
8. `FETCH`
9. `OPEN`
10. `PREPARE`
11. `RELEASE (Connection)`
12. `ROLLBACK`
13. `SET CONNECTION`
14. `SET variable`

Važe i neka dodatna pravila. Na primer, ukoliko se naredba `COMMIT` koristi kao jedna od podnaredbi, onda se ona mora naći kao poslednja podnaredba. Ukoliko je `COMMIT` nađe na ovoj poziciji, onda će ona biti izvršena, čak i u situaciji da klauza `STOP AFTER FIRST` indikuje da se neće sve podnaredbe u okviru složene SQL naredbe izvršiti. Na primer, pretpostavimo da je `COMMIT` poslednja podnaredba u okviru složene SQL naredbe koja ima 100 podnaredbi. Ukoliko se klauzom `STOP AFTER FIRST` specifikuje da se izvršava prvih 50 podnaredbi, onda će `COMMIT` podnaredba biti izvršena kao 51. podnaredba.

Neka dodatne napomene koje treba imati u vidu prilikom rada sa složenih SQL naredbama u DB2 sistemu su sledeće:

- Nije dozvoljeno ugnežđavati kod iz matičnog jezika unutar bloka koji definiše složena SQL naredba.

- Nije dozvoljeno ugnežđavati složene SQL naredbe u okviru drugih složenih SQL naredbi.

- Pripremljena `COMMIT` naredba nije dozvoljena u `ATOMIC` složenoj SQL naredbi.

- Jedna SQLCA struktura se postavlja za celu složenu SQL naredbu. Važe naredna pravila:

   - Vrednosti `SQLCODE` i `SQLSTATE` koje se postavljaju na kraju složene SQL naredbe su podrazumevano postavljene na osnovu poslednje podnaredbe koja se izvršila u okviru složene SQL naredbe, osim u slučaju opisanom narednom tačkom.

   - Ako je sistem signalizirao upozorenje da "nije pronađen podatak" (`SQLSTATE 02000`, odnosno, `SQLCODE +100`), onda se tom upozorenju daje prednost u odnosu na ostala upozorenja da bi se naredbom `WHENEVER NOT FOUND` moglo dejstvovati. U ovoj situaciji se polja iz `SQLCA` strukture koja se eventualno vraćaju aplikaciji postavljaju na osnovu podnaredbe koja je okinula upozorenje da "nije pronađen podatak". Ukoliko u okviru složene SQL naredbe postoji više podnaredbi koje okidaju ovo upozorenje, onda se polja iz SQLCA strukture postavljaju na osnovu poslednje od tih podnaredbi.

Sada slede primeri korišćenja složenih SQL naredbi. Primetimo da smo, kao i u prethodnom zadatku, koristili naredbu `ROLLBACK` u definiciji funkcije `checkSQL` da poništimo izmene u bazi podataka u slučaju da dođe do greške, odnosno, naredbu `COMMIT` za potvrđivanje izmena pre raskidanja konekcije.

{% include lab/exercise.html broj="5.2" tekst="Napisati C/SQL program koji redom:

1. Kreira novi ispitni rok samo za predmete iz prvog semestra u tekućoj godini čija je oznaka `'maj'` i naziv `'Maj GODINA'` u zavisnosti od tekuće godine (na primer, `'Maj 2021'`). Za početak prijavljivanja postaviti datum izvr\v savanja programa i postaviti da prijavljivanje traje 20 dana.
2. Ažurira datum kraja prijavljivanja za prethodno uneti ispitni rok tako što smanjuje trajanje prijavljivanja za 10 dana.

Obezbediti da se navedene operacije izvrše ili sve ili nijedna." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_5/zadatak_5_2.sqc, c)

## 5.3 Tačke čuvanja u okviru transakcija

U prethodnim primerima smo videli da možemo da predstavimo transakcije kao složene SQL naredbe, a zatim da koristimo specifičnosti složenih SQL naredbi da bismo preciznije definisali ponašanje u zavisnosti od toga da li se pojavila greška tokom rada transakcije ili ne. Preciznije, videli smo kako je moguće poništiti efekat celog izvršavanja programa kao jedinice posla u slučaju pojave greške u nekoj od podnaredbi.

Vrlo često nam je neophodno da imamo precizniju kontrolu nad time šta se tačno poništava, na primer, ukoliko želimo da samo jedan deo transakcije bude poništen, umesto cele transakcije. Da bismo postigli takav efekat, potrebne su nam sofisticiranije metode upravljanja transakcijama. Jedan od takvih metoda podrazumeva korišćenje tačke čuvanja.

*Tačka čuvanja* (engl. *savepoint*) predstavlja mehanizam za poništavanje precizno definisanog skupa izvršenih naredbi.

Ukoliko se desi neka greška prilikom izvršavanja transakcije, tačka čuvanja se može koristiti da poništi dejstvo naredbi od trenutka kada je tačka čuvanja započeta do trenutka kada je poništenje akcija zahtevano.

Samim tim, tačka čuvanja omogućava konstruisanje grupe od nekoliko SQL naredbi, grupisanih u jedan izvršivi blok, koji se može izvršavati, kao deo jedne transakcije. Ukoliko se neka od podnaredbi izvrši sa greškom, taj definisani blok će biti poništen.

### 5.3.1 Kreiranje tačke čuvanja

Kreiranje tačke čuvanja u izvornom kodu se može izvršiti pozivanjem naredbe `SAVEPOINT`, čija je sintaksa data sa:

```sql
SAVEPOINT <NAZIV_TACKE_CUVANJA> [UNIQUE]
ON ROLLBACK RETAIN CURSORS
[ON ROLLBACK RETAIN LOCKS]
```

Ovom naredbom se kreira nova tačka čuvanja naziva `<NAZIV_TACKE_CUVANJA>`. Ukoliko specifikujemo opcionu klauzu `UNIQUE`, onda navodimo da aplikacija ne želi da iskoristi ovo ime tačke čuvanja dok je tačka čuvanja aktivna u okviru trenutnog nivoa čuvanja. DB2 sistem će prijaviti grešku ako pokušamo da kreiramo tačku čuvanja kao jedinstvenu ako već postoji tačka čuvanja sa istim imenom, kao i ako pokušamo da kreiramo tačku čuvanja sa imenom koje je prethodno bilo proglašeno za jedinstveno.

Obaveznom klauzom `ON ROLLBACK RETAIN CURSORS` se specifikuje ponašanje sistema tokom operacija poništavanja izmena do ove tačke čuvanja u odnosu na otvorene kursore nakon ove SAVEPOINT naredbe. Ovom klauzom se indikuje da, kada god je to moguće, kursori bivaju van uticaja operacije poništavanja do tačke čuvanja. Za više detalja o tome kako poništavanje izmena utiče na kursore, pogledati sekciju [o poni\v stavanju izmena](#55-poništavanje-izmena).

Opcionom klauzom `ON ROLLBACK RETAIN LOCKS` se specifikuje ponašanje sistema tokom operacija poništavanja izmena do ove tačke čuvanja u odnosu na katance koje je aplikacija dobila nakon ove `SAVEPOINT` naredbe. Ukoliko je navedena, katanci koje je aplikacija dobila neće biti oslobođeni prilikom takve operacije poništavanja.

Neka dodatna pravila i napomene koje treba imati u vidu prilikom kreiranja tačaka čuvanja u DB2 sistemu su sledeće:

- Novi nivo čuvanja se kreira kada se naredni događaji okinu:

   - Nova jedinica posla je započela.

   - Pozvana je procedura koja je definisana klauzom `NEW SAVEPOINT LEVEL`.

   - Započeta je atomična složena SQL naredba.

- Nivo čuvanja se završava kada događaj koji je inicirao njegovo kreiranje je završen ili uklonjen. Kada se nivo čuvanja završi, sve tačke čuvanja koje se nalaze na tom nivou se oslobađaju. Svi otvoreni kursori, DDL akcije ili modifikacije podataka su nasleđeni od strane roditeljskog nivoa čuvanja (odnosno, nivoa čuvanja u okviru kojeg se trenutni nivo čuvanja završio) i pod uticajem su bilo koje naredbe koja se tiče nivoa čuvanja i koja važi u okviru roditeljskog nivoa čuvanja.

## 5.4 Potvrđivanje izmena

Kao što smo videli, jedna od dve osnovne operacije za upravljanje izmenama koje je napravila jedna transakcija jeste *potvrđivanje izmena* (engl. *commit*), koja se izvršava naredbom `COMMIT`. Sintaksa ove naredbe je data u nastavku:

```sql
COMMIT [WORK]
```

Iako naredba pohranjivanja izmena ima jednostavnu sintaksu, njeni efekti su mnogobrojni. Osnovna upotreba naredbe podrazumeva da se jedinica posla, u kojoj je `COMMIT` naredba izvršena, završava i nova jedinica posla se inicira. Sve izmene koje su izvršene nekom od narednih naredbi se potvrđuju u bazi podataka: `ALTER`, `COMMENT`, `CREATE`, `DROP`, `GRANT`, `LOCK TABLE`, `REVOKE`, `SET INTEGRITY`, `SET Variable`, kao i naredbe za izmenu podataka: `INSERT`, `DELETE`, `MERGE`, `UPDATE`, uključujući i one naredbe koje su  ugnežđene u upitima.

Svi katanci koje je jedinica posla dobila nakon njenog iniciranja se oslobađaju, osim neophodnih katanaca za otvorene kursore koji su deklarisani klauzom `WITH HOLD`. Svi otvoreni kursori koji nisu deklarisani klauzom `WITH HOLD` se zatvaraju. Otvoreni kursori koji jesu deklarisani klauzom `WITH HOLD` ostaju otvoreni, i takvi kursori se pozicioniraju ispred narednog logičkog reda rezultujuće tabele (drugim rečima, naredba `FETCH` se mora izvršiti pre nego što se izvrši pozicionirana naredba `UPDATE` ili `DELETE`).

Sve tačke čuvanja postavljene u okviru transakcije se oslobađaju.

Neka dodatna pravila i napomene koje treba imati u vidu prilikom poništavanja izmena u DB2 sistemu su sledeće:

- Snažno se preporučuje da svaki aplikacioni proces eksplicitno završi svoju jedinicu posla pre nego li se završi. Ako se aplikacioni program završi bez `COMMIT` ili `ROLLBACK` naredbe, onda će SUBP sam pokušati da izvrši operaciju pohranjivanja ili poništavanja u zavisnosti od okruženja aplikacije. Iako su SUBP sistemi napredni, ipak se ne bi trebalo osloniti na njih da rade posao programera.

## 5.5 Poništavanje izmena

Ukoliko želimo da poništimo izmene koje je napravila jedna transakcija, možemo koristiti SQL naredbu `ROLLBACK`. Sintaksa ove naredbe je data u nastavku:

```sql
ROLLBACK [WORK]
[TO SAVEPOINT [<IME_TACKE_CUVANJA>]]
```

Efekat ove naredbe je da se prekida jedinica posla u kojoj je izvršena naredba `ROLLBACK` i nova jedinica posla se inicijalizuje. Sve promene koje su se ostvarile u bazi podataka tokom jedinice posla su poništene. U zavisnosti od odabranih vrednosti narednih opcija prilikom deklaracije naredbe `ROLLBACK`, ta naredba može imati različite varijante koje utiču na način izvršavanja:

- Navođenjem opcione klauze `TO SAVEPOINT`, poništavanje se izvršava parcijalno, odnosno, do poslednje tačke čuvanja. Ukoliko nijedna tačka čuvanja nije aktivna na trenutnom nivou čuvanja, podiže se greška (`SQLSTATE 3B502`). Nakon uspešnog poništavanja, navedena tačka čuvanja `<IME_TACKE_CUVANJA>` nastavlja da postoji, ali svaka ugnežđena tačka čuvanja se oslobađa i nadalje ne postoji. Ugnežđene tačke čuvanja, ako postoje, smatraju se za poništene i oslobođene kao deo poništavanja do navedene tačke čuvanja. Ukoliko `<IME_TACKE_CUVANJA>` nije navedeno, onda se poništavanje vrši do poslednje postavljene tačke čuvanja na tekućem nivou čuvanja.
<br>
<br>
Ako se klauza `TO SAVEPOINT` ne postavi, onda naredba `ROLLBACK` poništava čitavu transakciju. Dodatno, sve tačke čuvanja u okviru te transakcije se oslobađaju. Ukoliko se navede `<IME_TACKE_CUVANJA>`, onda će se poništavanje izvršiti do te imenovane tačke čuvanja. Nakon uspešne operacije poništavanja, navedena imenovana tačka čuvanja nastavlja da postoji. Ukoliko ne postoji imenovana tačka čuvanja sa datim nazivom, podiže se greška (`SQLSTATE 3B001`).

Neka dodatna pravila i napomene koje treba imati u vidu prilikom poništavanja izmena u DB2 sistemu su sledeće:

- Svi katanci koji se čuvaju se oslobađaju prilikom izvršavanja naredbe `ROLLBACK` za tu jedinicu posla. Svi otvoreni kursori se zatvaraju.

- Izvršavanje naredbe `ROLLBACK` neće uticati na naredbu `RELEASE`.

- Ukoliko se izvršavanje program ne završi normalno, onda je jedinica posla implicitno poništena.

- Uticaj na kursore koji rezultuje iz naredbe `ROLLBACK TO SAVEPOINT` zavisi od naredbi u okviru tačke čuvanja:
   
   - Ako tačka čuvanja sadrži DDL za koji je kursor zavisan, kursor se označava za nevalidan. Pokušaji da se takav kursor koristi rezultuju podizanjem greške (`SQLSTATE 57007`).

   - Inače:

      - Ako je kursor referenciran u tački čuvanja, kursor ostaje otvoren i biva pozicioniran ispred narednog logičkog reda rezultujuće tabele. U tom slučaju je potrebno  izvršiti naredbu `FETCH` pre nego što se izvrši pozicionirajuća naredba `UPDATE` ili `DELETE`.

      - Inače, kursor ne potpada pod uticaj naredbe `ROLLBACK TO SAVEPOINT` (ostaje otvoren i pozicioniran).

- Naredba `ROLLBACK TO SAVEPOINT` će obrisati sve privremeno kreirane ili deklarisane privremene tabele u okviru tačke čuvanja.

- Svi katanci su sadržani nakon `ROLLBACK TO SAVEPOINT` naredbe.

Naredni primeri ilustruju napredno implementiranje transakcija korišćenjem naredbi `COMMIT` i `ROLLBACK`.

{% include lab/exercise.html broj="5.3" tekst="Napisati C/SQL program kojim se za svaki ispitni rok, za koji postoji makar jedno polaganje, prvo ispisuju informacije o nazivu i godini roka, a zatim se korisnik pita da li želi da obriše sva polaganja za taj ispitni rok. Ukoliko želi, aplikacija izvršava brisanje i prikazuje poruku korisniku. Obrada jednog ispitnog roka predstavlja jednu transakciju." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_5/zadatak_5_3.sqc, c)

Za naredni program je potrebno kreirati tabelu `OBRADJENIPREDMETI` sa narednom strukturom:

```sql
DROP TABLE DA.OBRADJENIPREDMETI;

CREATE TABLE DA.OBRADJENIPREDMETI (
    IDPREDMETA INTEGER NOT NULL,
    PRIMARY KEY (IDPREDMETA),
    FOREIGN KEY (IDPREDMETA) REFERENCES DA.PREDMET
);
```

{% include lab/exercise.html broj="5.4" tekst="Napisati C/SQL program koji za svaki predmet koji se ne nalazi u tabeli OBRADJENIPREDMETI izlistava njegov naziv i ESPB. Korisniku se nudi opcija da poveća broj bodova za 1. Obrada 5 uzastopnih predmeta predstavlja jednu transakciju. Nakon svakog 5. predmeta pitati korisnika da li želi da nastavi sa daljim izmenama. Ukoliko ne želi, program se prekida. U suprotnom, nastaviti sa daljom obradom predmeta." %}

Rešenje: 

include_source(vezbe/primeri/poglavlje_5/zadatak_5_4.sqc, c)

Naredni zadatak ilustruje jednostavan rad sa tačkama čuvanja u okviru transakcija.

Za naredni program je potrebno izvr\v siti naredne SQL naredbe:

```sql
DELETE  FROM DA.ISPITNIROK
WHERE   SKGODINA = 2021;

DELETE  FROM DA.SKOLSKAGODINA
WHERE   SKGODINA = 2021;

INSERT  INTO DA.SKOLSKAGODINA
VALUES  (2021, '01/01/2021', '12/31/2021');
```

{% include lab/exercise.html broj="5.5" tekst="Napisati C/SQL program koji zahteva od korisnika da unese broj obaveznih ispitnih rokova u 2021. godini. Program zatim unosi za svaki mesec, po\v cev\v si od januara 2021. godine, po jedan ispitni rok, pa ispisuje sve ispitne rokove. Program zatim pita korisnika da li želi da poništi unos ispitnih rokova koji nisu obavezni. Ukoliko korisnik odgovori potvrdno, poništiti unos neobaveznih ispitnih rokova. U suprotnom, potvrditi sve izmene. Ispisati sve ispitne rokove ponovo." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_5/zadatak_5_5.sqc, c)

Naredni zadaci ilustruju kompleksniju upotrebu ta\v caka \v cuvanja u okviru transakcija.

Za naredni program je potrebno izvr\v siti naredne SQL naredbe:

```sql
DROP    TABLE DA.STATISTIKAPOLAGANJA;

CREATE  TABLE DA.STATISTIKAPOLAGANJA (
    SKGODINA    SMALLINT NOT NULL,
    OZNAKAROKA  VARCHAR(20) NOT NULL,
    IDPREDMETA  INTEGER NOT NULL,
    USPESNOST   DOUBLE,
    PRIMARY KEY (SKGODINA, OZNAKAROKA, IDPREDMETA),
    FOREIGN KEY (SKGODINA, OZNAKAROKA) 
                REFERENCES DA.ISPITNIROK,
    FOREIGN KEY (IDPREDMETA)
                REFERENCES DA.PREDMET
);
```

{% include lab/exercise.html broj="5.6" tekst="Napisati C/SQL program koji izra\v cunava statistiku polaganja predmeta po ispitnim rokovima i te podatke upisuje u tabelu `STATISTIKAPOLAGANJA`. Program prvo ispisuje procenat polo\v zenih ispita u odnosu na ukupan broj polaganih ispita za predmete po ispitnim rokovima, ali samo za one predmete u ispitnom rokovima koji nemaju statistiku, pa zatim bele\v zi izra\v cunatu statistiku. Nakon unosa polaganja, pitati korisnika da li \v zeli da poni\v sti zabele\v zenu statistiku, ali omogu\'citi da se sa\v cuva informacija o tome da je statistika zabele\v zena (tj. da kolona `USPESNOST` bude `NULL`). Cela obrada jednog predmeta u jednom ispitnom roku predstavlja jednu transakciju." %}

Rešenje:

include_source(vezbe/primeri/poglavlje_5/zadatak_5_6.sqc, c)

Za naredni program je potrebno izvr\v siti naredne SQL naredbe:

```sql
DROP TABLE DA.OBRADJENAPOLAGANJA;

CREATE TABLE DA.OBRADJENAPOLAGANJA (
    INDEKS INTEGER NOT NULL,
    GODINA SMALLINT NOT NULL,
    PRIMARY KEY (INDEKS, GODINA),
    FOREIGN KEY (INDEKS)
        REFERENCES DA.DOSIJE
);

INSERT INTO DA.OBRADJENAPOLAGANJA
VALUES (20180050, 2018);
```

{% include lab/exercise.html broj="5.7" tekst="Napisati C/SQL program kojim se omogu\'cuje da radnik u studentskoj slu\v zbi poni\v stava studentske ispite. Obrada jednog studenta u jednoj godini roka, koja je opisana u nastavku, mora da predstavlja zasebnu transakciju. Transakcija se sastoji od narednih koraka:

1. Aplikacija zahteva od korisnika da unese indeks studenta.
2. Aplikacija na osnovu unetog indeksa ispisuje godine rokova u kojima student ima neke polo\v zene ispite, ali samo ukoliko ve\'c nije prethodno ta godina roka obra\dj ena za tog studenta (ova informacija se \v cuva u tabeli `OBRADJENAPOLAGANJA`).
3. Korisnik bira jednu od ispisanih godina.
4. Aplikacija pronalazi sve polo\v zene ispite za datog studenta u odabranoj godini studija. Za svaki ispit, aplikacija ispisuje naziv polo\v zenog predmeta i ocenu koju je student ostvario. Tako\dj e, aplikacija pita korisnika da li \v zeli da poni\v sti teku\'ci ispit \v cije su informacije ispisane. Ukoliko korisnik odgovori potvrdno, aplikacija poni\v stava teku\'ci ispit. U svakom slu\v caju, aplikacija prelazi na naredni ispit sve do ispisivanja svih ispita.
5. Kada se svi ispiti obrade, aplikacija pita korisnika da potvrdi sve izmene u teku\'coj transakciji. Ukoliko korisnik odgovori odri\v cno, onda je potrebno poni\v stiti sve izmene koje se ti\v cu poni\v stavanja ispita iz koraka 4. Me\dj utim, potrebno je omogu\'citi da, u svakom slu\v caju, teku\'ca godina roka za dati indeks bude obra\dj ena (tj. trajno zapam\'cena u tabeli `OBRADJENAPOLAGANJA`).

Na kraju svake transakcije, aplikacija pita korisnika da li \v zeli da zavr\v si sa radom. Ukoliko korisnik odgovori potvrdno, aplikacija se zavr\v sava. U suprotnom, zapo\v cinje se nova transakcija sa prethodno opisanim koracima." %}

Rešenje: Da bismo lak\v se modulirali na\v se re\v senje, implementirajmo naredne funkcije:

1. Funkcija `void deklarisi_kursor_za_godine(sqlint32 indeks)` vr\v si deklaraciju \v citaju\'ceg kursora sa nazivom `c_godine` koji pronalazi godine onih ispitnih rokova u kojem student, \v ciji je indeks jednak vrednosti parametra `indeks` (koja predstavlja mati\v cnu promenljivu koja je globalno deklarisana), ima polo\v zene ispite.

2. Funkcija `unsigned godine_polozenih_ispita()` ispisuje sve godine iz kursora `c_godine`. Funkcija tako\dj e i vra\'ca broj prona\dj enih godina.

3. Funkcija `void deklarisi_kursor_za_polaganja(sqlint32 indeks, short godina)` vr\v si deklaraciju a\v zuriraju\'ceg kursora sa nazivom `c_polozeni` kojim se mo\v ze a\v zurirati vrednost kolone `STATUS_PRIJAVE` u tabeli `ISPIT` i koji pronalazi naziv predmeta i ocenu za sve polo\v zene ispite za studenta sa indeksom `indeks` u godini roka `godina` (oba parametra predstavljaju mati\v cne promenljive). Ovaj kursor mora biti deklarisan klauzom `WITH HOLD` zato \v sto se koristi kao deo transakcije.

4. Funkcija `void polaganja_za_studenta_u_godini(sqlint32 indeks, short godina)` implementira neophodne operacije koje \v cine deo transakcije za jednog studenta. Funkcija redom:

- Unosi informacije (`INSERT`) o indeksu i godini roka u tabelu `OBRADJENAPOLAGANJA`.
- Kreira ta\v cku \v cuvanja, kako bismo mogli da eventualno poni\v stimo sve izmene, osim unosa u tabelu iz prethodne ta\v cke.
- Prolazi kroz kursor `c_polozeni`. Za svaki red ispisuje informacije iz kursora i ukoliko korisnik potvrdi poni\v stavanje ispita, izvr\v sava odgovaraju\'ce a\v zuriranje (`UPDATE`).
- Postavlja pitanje korisniku da li \v zeli da potvrdi izmene. Ukoliko je odgovor negativan, funkcija poni\v stava sve izmene, ali ne i izmenu koja je u\v cinjena u prvoj ta\v cki (tj. naredbu `INSERT` iznad), po\v sto bez obzira na poni\v stavanje a\v zuriranja, \v zelimo da informacija o obra\dj ivanju bude trajno upisana.

Funkcija `int main()` je sada poprili\v cno jednostavna. Nakon povezivanja na bazu podataka i deklarisanja kursora (pozivom funkcija pod 1 i 3 iznad), zapo\v cinje se iterativni proces koji predstavlja jednu transakciju. Svaka transakcija se sastoji od opisanih operacija iz teksta zadatka, tako \v sto se pozivaju odgovaraju\'ce funkcije (pod 2 i 4 iznad). Naravno, da bi svaka iteracija predstavljala jednu transakciju, potrebno je da se na kraju iteracije izvr\v si naredba `COMMIT`.

include_source(vezbe/primeri/poglavlje_5/zadatak_5_6.sqc, c)

## 5.6 Zadaci za vežbu

{% include lab/exercise.html broj="5.7" tekst="Napisati C/SQL program koji redom:

1. Kreira novi ispitni rok samo za predmete iz prvog semestra u 2019. godini čija je oznaka `'apr'` i naziv `'April 2019'`. Za početak prijavljivanja postaviti današnji datum i postaviti da prijavljivanje traje 10 dana.
2. Ažurira tip za prethodno uneti ispitni rok na tip `'Z'`. 

Obezbediti da se navedene operacije izvrše ili sve ili nijedna." %}

{% include lab/exercise.html broj="5.8" tekst="Napisati C/SQL program koji redom:

1. Kreira novi ispitni rok samo za predmete iz prvog semestra u 2019. godini čija je oznaka `'apr'` i naziv `'April 2019'`. Za početak prijavljivanja postaviti današnji datum i postaviti da prijavljivanje traje 10 dana.
2. Ažurira tip za prethodno uneti ispitni rok na tip `'Z'`. 

Obezbediti da se navedene operacije izvrše zasebno." %}

{% include lab/exercise.html broj="5.9" tekst="Napisati C/SQL program koji omogućava korisniku da unese nove ispitne rokove samo za predmete iz prvog semestra u 2019. godini za svaki mesec od marta do oktobra, sa odgovarajućim oznakama i nazivima. Za svaki ispitni rok postaviti da je datum početka prijavljivanja današnji datum pomeren za odgovarajući broj meseci, kao i da prijavljivanje traje 20 dana.

Omogućiti da korisnik unese broj ispitnih rokova koji želi da kreira. Minimalni broj ispitnih rokova je 0, a maksimalni broj je 6. U zavisnosti od unetog broja, kreirati odgovarajući broj ispitnih rokova. 

Obezbediti da se navedene operacije izvrše zasebno." %}

{% include lab/exercise.html broj="5.10" tekst="Napisati C/SQL program koji za svaki predmet koji je obavezan na smeru čiji je identifikator 201, pita korisnika da li želi da poveća broj bodova za 1. Ukoliko je odgovor korisnika 'da', izvršava se odgovarajuća naredba. Obrada jednog predmeta treba da predstavlja jednu transakciju." %}

{% include lab/exercise.html broj="5.11" tekst="Napisati C/SQL program koji omogućava korisniku da obriše informacije o studentima koji su upisani u godini koja se unosi sa standardnog ulaza. Za svakog studenta, program pita korisnika da li želi da obriše informacije. Ako korisnik potvrdi, obrisati podatke iz tabela `ISPIT`, `UPISANKURS`, `UPISGODINE`, `PRIZNATIISPIT`, `DOSIJEEXT` i `DOSIJE` (tim redosledom) za tekućeg studenta i ispisati poruku o uspešnosti brisanja za svaku tabelu ponaosob. Nakon toga, aplikacija pita korisnika da li želi da izvrši potvrđivanje ili poništavanje dotadašnjih izmena. Korisnik može da bira jednu od tri opcije:
1. Izvršavanje potvrđivanja
2. Izvršavanje poništavanja
3. Bez akcije

U slučaju akcija 1. i 2. potrebno je izvršiti odgovarajuću SQL naredbu i prikazati poruku korisniku o uspešnosti akcije. Takođe ispisati i informaciju o tome za koliko studenata je izvršeno potvrđivanje/poništavanje, na primer: `'TRANSAKCIJA JE ZAVRSENA: POTVRDILI STE BRISANJE 7 STUDENATA'` ili `'TRANSAKCIJA JE ZAVRSENA: PONIŠTILI STE BRISANJE 7 STUDENATA'` (ukoliko je pre tekuće akcije korisnik 7 puta odabrao 3. akciju). 

U slučaju akcije 3. potrebno je samo uvećati broj studenata koji je obrisan u tekućoj jedinici posla. Naravno, prilikom izvršavanja akcije 1. ili 2. ovaj broj se mora postaviti na 0." %}
