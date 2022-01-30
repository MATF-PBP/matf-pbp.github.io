---
layout: page
title: 6. Aplikacije u višekorisničkom okruženju
under_construction: true
---

Ovo poglavlje je posvećeno dizajnu aplikacija koje koriste baze podataka, a za koje se očekuje da će biti izvršavane u višekorisničkom okruženju. Ovo podrazumeva da je potrebno uvesti dodatne mere u slučaju da više aplikacija želi da pristupi nekom objektu u bazi podataka (na primer, određenom redu ili tabeli).

## 6.1 Aplikacioni proces i konkurentnost

Svi SQL-aplikativni programi se izvršavaju kao deo *aplikacionih procesa* ili *agenata*. Aplikacioni proces podrazumeva izvršavanje jednog ili više programa, i predstavlja jedinicu kojoj SUBP alocira resurse i katance. Različiti aplikacioni procesi mogu da povlače sa sobom izvršavanje različitih programa ili različita izvršavanja istog programa.

U višekorisničkom okruženju, osnovna pretpostavka je da više od jednog aplikacionog procesa može da zahteva pristup istim podacima u isto vreme. U tu svrhu, da bi se obezbedio integritet podataka, potrebno je implementirati i koristiti mehanizam poznat pod nazivom zaključavanje.

*Zaključavanje* (engl. *locking*) predstavlja mehanizam zasnovan na katancima koji se koristi za održavanje integriteta podataka pod uslovima višekorisničkog okruženja.

Korišćenjem zaključavanja se može sprečiti da, na primer, dva aplikaciona procesa izvrše ažuriranje istog reda u isto vreme.

SUBP upravlja katancima da bi onemogućio da nepotvrđene izmene napravljene od strane jednog aplikacionog procesa budu vidljive od bilo kog drugog procesa. U trenutku kada se neki proces završi, tada se svi katanci koje je proces dobio od strane RSUBP, i držao ih, oslobađaju. Ipak, aplikacioni proces može da eksplicitno zahteva da se neki katanac oslobodi ranije. Ovo je omogućeno korišćenjem operacije potvrđivanja izmena, koja oslobađa katance koji su dobijeni tokom jedinice posla i koja, takođe, potvrđuje sve izmene koje su obavljene tokom te jedinice posla.

Direktni pozivi DB2 funkcija i ugnežđeni SQL omogućavaju režim konekcije koji se naziva *konkurentne transakcije* (engl. *concurrent transactions*) kojim se omogućavaju višestruke konekcije ka bazama podataka, pri čemu svaka od njih predstavlja nezavisnu transakciju.

Aplikacija može da ima više konkurentnih konekcija ka istoj bazi podataka.

## 6.2 Katanci

Kao što smo rekli, katanci predstavljaju osnovni alat za uspostavljanje mehanizma zaključavanja. Svaki katanac ima odgovarajuće karakteristike:

- *Režim* (engl. *mode*) definiše tip pristupa koji je dozvoljen procesu koji drži ključ, kao i tip pristupa koji je dozvoljen konkurentnim procesima. Ponekad se režim katanca naziva i *stanje* (engl. *state*) katanca.

- *Objekat* (engl. *database object*) definiše resurs nad kojim se primenjuje operacija zaključavanja. Jedini tip objekta koji se može programerski zaključati jeste tabela (za više informacija, videti sekciju [6.4](#64-programersko-zaključavanje-tabela)). SUBP ima pravo da postavi katanac na druge objekte u bazi podataka, kao što su: redovi, prostori tabela, blokovi, particije podataka, i dr.

- *Trajanje* (engl. *lock count*) definiše dužinu vremena tokom kojeg je katanac bio držan od strane procesa. Trajanje katanca se određuje nivoom izolovanosti pod kojim se naredba pokreće (za više informacija o nivoima izolovanosti, videti sekciju [6.3](#63-nivoi-izolovanosti)).

DB2 sistem nudi podršku za različite režime katanaca. Mi ćemo prikazati samo najosnovnije, poređane rastuće po restrikciji koju obezbeđuju:

- *IN* (engl. *intent none*) definiše da vlasnik katanca može da čita sve podatke u tabeli, uključujući i nepotvrđene podatke, ali ne može da ih menja. Druge konkurentne aplikacije mogu da čitaju ili da ažuriraju tabelu. Ne postavljaju se nikakvi katanci na redovima.

- *IS* (engl. *intent share*) definiše da vlasnik katanca može da čita proizvoljan podatak u tabeli ako se *S* katanac može dobiti na redovima ili stranicama od interesa.

- *IX* (engl. *intent exclusive*) definiše da vlasnik katanca može da čita ili menja proizvoljni podatak u tabeli ako važe naredna dva uslova:

   - *X* katanac se može dobiti na redovima ili stranicama koje želimo da menjamo.

   - *S* ili *U* katanac se može dobiti na redovima koje želimo da čitamo.

- *SIX* (engl. *share with intent exclusive*) definiše da vlasnik katanca može da čita sve podatke iz tabele i da menja redove ako može da dobije *X* katanac na tim redovima. Za čitanje se ne dobijaju katanci na redovima. Druge konkurentne aplikacije mogu da čitaju iz tabele. *SIX* katanac se dobija ako aplikacija ima *IX* katanac na tabeli i onda se zahteva *S* katanac ili obratno.

- *S* (engl. *share*) definiše da vlasnik katanca moze da čita proizvoljan podatak u tabeli i neće dobiti katance na redovima ili stranicama.

- *U* (engl. *update*) definiše da vlasnik katanca može da čita proizvoljan podatak u tabeli i može da menja podatke ako se na tabeli može dobiti *X* katanac. Pritom se ne dobijaju nikakvi katanci na redovima ili stranicama.

- *X* (engl. *exclusive*) definiše da vlasnik katanca može da čita i da menja proizvoljan podatak iz tabele. Pritom se ne dobijaju nikakvi katanci na redovima ili stranicama.

- *Z* (engl. *super exclusive*) katanac se zahteva na tabeli u posebnim prilikama, kao što su recimo menjanje strukture tabele ili njeno brisanje. Nijedna druga aplikacija (ni ona sa nivoom izolovanosti "nepotvrđeno čitanje", videti sekciju [6.3](#53-nivoi-izolovanosti)) ne može da čita niti da ažurira podatke u tabeli.

Očigledno, primenom različitih vrsta katanaca, može doći do sukoba u smislu da li SUBP treba da dodeli katanac nekom aplikacionom procesu kada postoji drugi aplikacioni proces koji već drži katanac nad istim objektom. Samo u slučaju kada su katanci *kompatibilni* će SUBP dodeliti drugi katanac.

U slučaju da katanci nisu kompatibilni, nije moguće dodeliti traženi katanac aplikacionom procesu. Tada taj proces mora da pređe u stanje čekanja dok proces ne oslobodi nekompatibilni katanac koji drži, kao i dok se svi ostali nekompatibilni katanci ne oslobode. U nekim slučajevima može doći do *isteka vremena* (engl. *timeout*) dok zatražilac čeka na katanac. O tome će nešto detaljnije biti reči u nastavku, a za sada ćemo prikazati kompatibilnosti između katanaca u narednoj tabeli. Oznaka *N* znači da nad resursom nema katanca, odnosno, da aplikacioni proces ne traži katanac.

!["Tabela kompatibilnosti katanaca."](./Slike/tabela-kompatibilnosti-katanaca.png){:class="ui centered large image"}

### 6.2.1 Konverzija i ekskalacija katanaca

Konverzija katanca se ostvaruje u situacijama kada aplikacioni proces pristupa objektu nad kojim već drži katanac, a taj pristup zahteva restriktivniji katanac od onog koji se već drži. Proces nad objektom može da drži najviše jedan katanac u nekom trenutku, ali može da traži različite katance nad istim objektom indirektno kroz izvršavanje naredbi.

Promena režima katanca koji se već drži se naziva *konverzija katanca* (engl. *lock conversion*).

Neki režimi katanaca se primenjuju samo za tabele, neki samo za redove, blokove ili particije podataka. Za redove ili blokove, konverzija se uglavnom izvršava ukoliko se traži katanac *X*, a već se drži katanac *S* ili *U*.

Katanci *IX* i *S* imaju specijalan odnos — nijedan od ova dva katanca se smatra restriktivnijim u odnosu na drugi. Ukoliko aplikacioni proces drži jedan od ova dva katanca i zahteva drugi, onda će se izvršiti konverzija u katanac *SIX*. Sve ostale konverzije se vrše po narednom principu: **Ukoliko je katanac koji se zahteva restriktivniji u odnosu na katanac koji se drži, onda se katanac koji se drži konvertuje u katanac koji se zahteva.**

Naravno, da bi se uspešno izvršila konverzija katanca, nad objektom nad kojim se zahteva novi katanac ne sme postojati neki drugi katanac koji je nekompatibilan sa njim.

Da bi se uslužio što veci broj aplikacija, menadžer baza podataka omogucava funkciju _ekskalacije katanaca_. Ovaj proces objedinjuje proces dobijanja katanca na tabeli i oslobađanja katanaca na redovima. Željeni efekat  je da se smanje ukupni zahtevi skladištenja za katance od strane menadžera baza podataka. Ovo ́ce omoguciti drugim aplikacijama da dobiju željene katance. Dva konfiguraciona parametra baze podataka imaju direktan uticaj na proces ekskalacije katanaca:

- `locklist`: to je prostor u globalnoj memoriji baze podataka koji se koristi za skladištenje katanaca,
- `maxlocks`: to je procenat ukupne liste katanaca koji je dozvoljeno da drži jedna aplikacija. 

Oba parametra su konfigurabilna.

Ekskalacija katanaca se dešava u dva slučaja:

- Aplikacija zahteva katanac koji bi proizveo da ona prevaziđe procenat ukupne veličine liste katanaca, koji je definisan parametrom `maxlocks`. U ovoj situaciji menadžer baze podataka ́ce pokušati da oslobodi memorijski prostor dobijanjem jednog katanca na tabelu, a oslobađanjem katanaca na redove te tabele.
- Aplikacija ne može da dobije katanac jer je lista katanaca puna. Menadžer baze podataka ́ce pokušati da oslobodi memorijski prostor tako što ́ce dobiti katanac na tabelu, a osloboditi katance na redove te tabele. Primetimo da aplikacija koja pokrece ekskalaciju katanaca može, ali i ne mora da drži značajan broj katanaca.

Ekskalacija katanaca može da ne uspe. Ako se desi greška, aplikacija koja je pokrenula ekskalaciju dobiće vrednost `SQLCODE` -912. Ovaj kod bi trebaloda bude obrađen programski od strane aplikacije.

### 6.2.2 Istek vremena i mrtva petlja

Već smo napomenuli da, ukoliko aplikacija zahteva katanac nad objektom koji je nekompatibilan sa katancem koji već postoji nad istim objektom, ona ulazi u fazu čekanja dok se taj katanac ne oslobodi. Neka, na primer, jedna transakcija čeka na katanac koji drži neka korisnička aplikacija. Ukoliko korisnik koji koristi tu aplikaciju napusti aplikaciju bez da dozvoli potvrđivanje izmena, onda se katanac ne oslobađa nad tim objektom i transakcija može da čeka beskonačno dugo.

SUBP ima poseban mehanizam kojim se može sprečiti da aplikacija beskonačno dugo čeka na oslobađanje katanca i koji se naziva *detekcija isteka vremena* (engl. *lock timeout detection*). Ovaj mehanizam se oslanja na korišćenje `locktimeout` parametra podešavanja baze podataka i predstavlja najduže vreme koje transakcija može da čeka na oslobađanje katanca. Podrazumevana vrednost ovog parametra je `-1`, što znači da je detekcija isteka vremena onemogućena. U nastavku ćemo videti kako možemo postaviti vrednost ovog parametra, kao i koje je ponašanje SUBP u slučaju da dođe do isteka vremena, međutim, prvo ćemo diskutovati o situaciji do koje može doći u slučaju da aplikacije mogu da čekaju beskonačno dugo na oslobađanje katanca.

Pretpostavimo da postoje dve aplikacije A i B koje rade konkurentno i da je mehanizam detekcije isteka vremena onemogućen u SUBP. Aplikacija A se sastoji od dve operacije: prva operacija želi da ažurira red 1 u tabeli 1, a druga operacija želi da ažurira red 2 u tabeli 2. Aplikacija B se takođe sastoji od dve operacije: prva operacija želi da ažurira red 2 u tabeli 2, a druga operacija želi da ažurira red 1 u tabeli 1. Pretpostavimo takođe da se operacije dešavaju u (približno) isto vreme. Redosled izvršavanja ovih operacija i dobijanja katanaca je sledeći (što je ilustrovano i na narednoj slici):

- U trenutku T1:
   - Aplikacija A dobija katanac X nad redom 1 u tabeli 1.
   - Aplikacija B dobija katanac X nad redom 2 u tabeli 2.

- U trenutku T2:
   - Aplikacija A pokušava da dobije katanac X nad redom 2 u tabeli 2. Taj katanac je nekompatibilan sa postojećim katancem X koji drži aplikacija B. Aplikacija A prelazi u stanje čekanja.
   - Aplikacija B pokušava da dobije katanac X nad redom 1 u tabeli 1. Taj katanac je nekompatibilan sa postojećim katancem X koji drži aplikacija A. Aplikacija B prelazi u stanje čekanja.

- U trenutku TN (N > 2):
   - Aplikacija A je u stanju čekanja.
   - Aplikacija B je u stanju čekanja.

!["Ilustracija pojave mrtve petlje prilikom izvršavanja dva aplikaciona procesa."](./Slike/mrtva-petlja.png){:class="ui centered large image"}

Očigledno, počevši od trenutka T3, obe aplikacije će čekati jedna na drugu beskonačno dugo i neće se "nikad" završiti. Ova pojava se naziva mrtva petlja.

*Mrtva petlja* (engl. *deadlock*) predstavlja pojavu kada dve aplikacije (ili više njih) zaključaju podatak koji je neophodan jedan drugoj, što rezultuje u situaciji da se obe aplikacije blokiraju i nijedna ne može da nastavi sa daljim izvršavanjem.

Zbog toga što aplikacije neće "volonterski" osloboditi katance koje drže nad podacima, da bi se prevazišla mrtva petlja, mora se pristupiti procesu prepoznavanja mrtve petlje. Mehanizam prepoznavanja mrtve petlje nadgleda informacije o agentima koji čekaju na katance i pokreće se na interval specifikovan kroz parametar `dlchktime` podešavanja baze podataka.

Ukoliko mehanizam pronađe mrtvu petlju, onda se nasumično bira jedan od procesa u mrtvoj petlji koji se naziva *žrtva* (engl. *victim process*), čije će izmene biti poništene. Aplikacija koja pripada procesu žrtvi se podiže i prosleđuje joj se odgovarajući kod koji je ona dužna da obradi. **SUBP automatski poništava nepotvr\dj ene izmene od strane odabrane aplikacije.** Kada je operacija poni\v stavanja završena, katanci koji su pripadali procesu žrtvi se oslobađaju, kursori se zatvaraju i ostali procesi u mrtvoj petlji mogu da nastave dalje sa radom.

Postoje dve vrste grešaka koje SUBP može prijaviti aplikaciji u slučaju da dođe do isteka vremena, odnosno, ako je aplikacija izabrana za žrtvu prilikom prepoznavanja mrtve petlje:

- Greška -911
   - Značenje: Tekuća transakcija se poništava usled mrtve petlje ili isteka vremena.
   - Uz ovu poruku se prilaže i kod koji preciznije označava razlog zbog kojeg se došlo do greske. Ovih kodova ima više, a mi ćemo prikazati dva najznačajnija:
      - 2: Transakcija je poništena usled mrtve petlje.
      - 68: Transakcija je poništena usled isteka vremena za katanac.

Pokretanjem komande `db2 ? sql911` u terminalu je moguće dobiti više informacija o grešci.

- Greška -913
   - Značenje: Došlo je do neuspeha pri izvršavanju distribuirane transakcije zbog mrtve petlje ili isteka vremena.
   - I uz ovu poruku se prilaže kod koji preciznije označava razlog zbog kojeg se došlo do greske. Ovih kodova ima više, a mi ćemo prikazati dva najznačajnija:
      - 2: Grana transakcije je neuspešna usled mrtve petlje.
      - 68: Grana transakcije je neuspešna usled isteka vremena za katanac.
      - 80: Naredba je neuspešna usled isteka vremena.

Pokretanjem komande `db2 ? sql913` u terminalu je moguće dobiti više informacija o grešci.

Još jedan mogući način da se izborimo sa mrtvom petljom jeste da specifikujemo najduži vremenski period koji aplikacija može da čeka na dobijanje katanca. Specijalni registar `CURRENT LOCK TIMEOUT`, koji je tipa `INTEGER`, sadrži broj sekundi pre nego što SUBP vrati grešku koja indikuje da katanac ne može biti dodeljen aplikacionom procesu. 

Validne vrednosti za ovaj registar su u opsegu `[-1, 32767]`. Dodatno, specijalnom registru se može postaviti vrednost `NULL`. Vrednost `-1` označava da SUBP neće prijavljivati istek vremena, već da aplikacija mora da čeka na zahtevani katanac dok se nekompatibilni katanac ne oslobodi ili dok se ne detektuje mrtva petlja. Vrednost `0` označava da aplikacija neće čekati na katanac, već ukoliko ne može da dobije katanac kada ga zatraži, odmah će dobiti grešku. Vrednost `NULL` označava da će SUBP koristiti vrednost koja je postavljena u parametru `locktimeout` o kojem je bilo reči ranije.

Ukoliko želimo da pročitamo vrednost ovog specijalnog registra, nakon konekcije na bazu podataka potrebno je izvršiti narednu komandu u terminalu:

```shell
db2 values CURRENT LOCK TIMEOUT
```

SQL naredba `SET CURRENT LOCK TIMEOUT` služi za promenu vrednosti specijalnog registra `CURRENT LOCK TIMEOUT`. Izvršavanje ove naredbe nije pod uticajem kontrole transakcije u programu. Zbog toga, ukoliko ovu naredbu koristimo u našim aplikacijama, dobra je praksa vratiti vrednost na podrazumevanu pre završavanja programa. Sintaksa ove naredbe je data u nastavku:

```sql
SET [CURRENT] LOCK TIMEOUT [=]
(WAIT|NOT WAIT|NULL|[WAIT] <CELOBROJNA_KONSTANTA>|<MATIČNA_PROMENLJIVA>)
```

Ovoj naredbi je moguće specifikovati naredne vrednosti:

- Klauzom `WAIT` specifikuje se vrednost `-1`, što znači da SUBP mora da čeka dok se katanac ne oslobodi ili se ne detektuje mrtva petlja.

- Klauzom `NOT WAIT` specifikuje se vrednost `0`, što znači da SUBP ne čeka na katance koji se ne mogu dodeliti aplikacijama, već odmah prijavljuje grešku.

- Vrednošću `NULL` specifikuje se da vrednost u registru `CURRENT LOCK TIMEOUT` nije postavljena, čime se efektivno koristi vrednost parametra `locktimeout` podešavanja baze podataka. Vrednost koja se vraća za ovaj specijalni registar se menja pri promeni vrednosti parametra `locktimeout`.

- Klauzom `[WAIT] <CELOBROJNA_KONSTANTA>` specifikuje se celobrojna vrednost iz intervala `[-1, 32767]`. Ako data vrednost pripada intervalu `[1, 32767]`, onda će SUBP čekati toliko sekundi pre nego što aplikaciji prosledi grešku. Vrednosti `-1` i `0` odgovaraju specifikovanju prethodno opisanih klauza `WAIT` i `NO WAIT`, redom.

- Navođenjem `<MATIČNA PROMENLJIVA>` specifikuje se celobrojna vrednost iz intervala `[-1, 32767]` koja se čita iz vrednosti date matične promenljive. Ako data vrednost pripada intervalu `[1, 32767]`, onda će SUBP čekati toliko sekundi pre nego što aplikaciji prosledi grešku. Vrednosti `-1` i `0` odgovaraju specifikovanju prethodno opisanih klauza `WAIT` i `NO WAIT`, redom.

{% include lab/exercise.html broj="6.1" tekst="Napisati C/SQL program koji za svaki studijski program pita korisnika da li \v zeli da promeni obim ESPB bodova na tom studijskom programu. Ukoliko korisnik odgovori potvrdno, aplikacija zahteva od korisnika da unese novi broj bodova nakon \v cega se vr\v si izmena podataka.

Napisati program tako da može da radi u višekorisničkom okruženju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi.

Pokrenuti dve instance aplikacije. Uveriti se da dok jedna aplikacija obra\dj uje teku\'ci studijski program, druga aplikacija ne mo\v ze da dobije katanac nad odgovaraju\'cim slogom." %}

Re\v senje: Nakon izvr\v savanja svake SQL naredbe koja mo\v ze da zahteva zaklju\v cavanje nekog sloga u tabeli, potrebno je izvr\v siti proveru da li aplikacija mo\v ze da dobije odgovaraju\'ci katanac. Provera se sastoji u ispitivanju vrednosti makroa `SQLCODE` i to pre poziva funkcije za obradu gre\v ske, kako se proces ne bi zavr\v sio pre obrade katanaca. Sama obrada \v cekanja je u ovom slu\v caju vrlo jednostavna - potrebno je ispisati poruku korisniku da je isteklo vreme za \v cekanje trenutnog katanca i nastaviti dalje sa izvr\v savanjem (poni\v stiti eventualne izmene i ponovo otvoriti kursor koji se obra\dj uje).

include_source(vezbe/primeri/poglavlje_6/zadatak_6_1.sqc, c)

## 6.3 Obrada transakcija u višekorisničkom okruženju

Problem sa pristupom obrade gre\v saka u prethodnom zadatku je taj \v sto se pod transakcijom smatra izvr\v savanje celog programa, te ako bilo gde u obradi do\dj e do problema dobijanja katanaca, sve izmene koje su do tada na\v cinjene se poni\v stavaju. Umesto toga, pouzdanija tehnika jeste definisati da jedna transakcija obuhvata obradu jednog podatka. U prethodnom primeru, to bi zna\v cilo da jedna transakcija obuhvata izmenu ta\v cno jednog studijskog programa, a ne svih studijskih programa. Dodatno, \v cesto je po\v zeljno da aplikacija pamti koji podaci su ve\'c obra\dj eni, kako ih ne bi obra\dj ivala dva puta. Ovo je va\v zno pamtiti zbog toga \v sto otvaranjem kursora u obradi \v cekanja, kursor se pozicionira na po\v cetak rezultuju\'ce tabele, a ne tamo gde je obrada stala. (Alternativno, mo\v zemo pamtiti broj obra\dj enih redova u neku mati\v cnu promenljivu `row_count`, pa zatim koristiti klauzu `OFFSET :row_count ROWS` u `SELECT` naredbi.)

Naredni zadatak ilustruje obradu transakcija u višekorisničkom okruženju korišćenjem znanja iz ovog poglavlja.

{% include lab/exercise.html broj="6.2" tekst="Napisati C/SQL program koji za svaki predmet omogućava korisniku da poveća broj ESPB za 1. Nakon svakog ažuriranja, proveriti da li je odgovarajuci red u tabeli izmenjen ponovnim dohvatanjem informacija.

Napisati program tako da može da radi u višekorisničkom okruženju. Obrada jednog predmeta predstavlja jednu transakciju. Postaviti istek vremena za zahtevanje katanaca na 10 sekundi." %}

Rešenje: S obzirom da se ovoga puta obrada katanaca vr\v si kao deo zasebne transakcije, onda je potrebno da poni\v stavamo izmene ukoliko do\dj e do isteka vremena za dobijanje katanaca. Zbog toga, funkciji za obradu \v cekanja dodajemo naredbu `ROLLBACK`. Dodatno, kako \'ce ta naredba zatvoriti sve otvorene kursore u transakciji (pa \v cak i one deklarisane klauzom `WITH HOLD`), potrebno je otvoriti kursor koji se koristio pre nego \v sto se pre\dj e u narednu iteraciju petlje koja obra\dj uje taj kursor. 

Tako\dj e, primetimo da aplikacija \v cuva niz obra\dj enih predmeta tokom svog rada. Prilikom obra\dj ivanja narednog predmeta, aplikacija prvo proverava funkcijom `int isCourseProcessed(sqlint32 id)` i ukoliko jeste, aplikacija ga preska\v ce i prelazi na naredni predmet. U suprotnom, vr\v si se obrada i funkcijom `void setCourseAsProcessed(sqlint32 id)` se predmet ozna\v cava da je obra\dj en za naredne provere.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_2.sqc, c)

## 6.4 Nivoi izolovanosti

U najosnovnijem slučaju, svaka transakcija je u potpunosti izolovana od svih drugih transakcija. Međutim, videli smo da ovakva, potpuna izolovanost, može brzo dovesti do problema poput mrtve petlje u konkurentnom sistemu izvršavanja. Zbog toga, da bi se povećala konkurentnost između aplikacionih procesa, DB2 SUBP definiše različite nivoe izolovanosti na kojima aplikacija može da se izvršava.

*Nivo izolovanosti* (engl. *isolation level*) koji se pridružuje aplikacionom procesu određuje stepen zaključavanja ili izolacije u kojem su podaci kojima taj proces pristupa, u odnosu na druge konkurentne procese.

Drugim rečima, nivo izolovanosti jednog aplikacionog procesa P specifikuje sledeće:

- Stepen u kojem su redovi koji se čitaju ili ažuriraju od strane procesa P dostupni drugim konkurentnim aplikacionim procesima.
- Stepen u kojem ažuriranja od strane drugih konkurentnih aplikacionih procesa mogu da utiču na izvršavanje procesa P.

DB2 definiše naredna četiri nivoa izolacije:

- Ponovljeno čitanje
- Stabilno čitanje
- Stabilni kursor
- Nepotvrđeno čitanje

### 6.4.1 Ponovljeno čitanje

U nivou izolacije *ponovljeno čitanje* (engl. *repeatable read*, skr. *RR*) nijedan podatak koji je pročitan od strane procesa P ne sme se menjati od strane drugih procesa sve do završetka obrade od strane procesa P (čime se obezbeđuje saglasnost podataka u slučaju ponovljenog čitanja, te otuda i naziv nivoa izolacije). Sa druge strane, nijedan red promenjen od strane drugog procesa ne može se čitati od strane procesa P dok se promene u okviru tog procesa na okončaju.

Osim već pomenutih ekskluzivnih katanaca, proces na RR nivou izolovanosti dobija bar deljive katance na svim podacima kojima pristupa, pri čemu se zaključavanje vrši tako da je proces u potpunosti izolovan od strane ostalih procesa.

### 6.4.2 Stabilno čitanje

U nivou izolacije *stabilno čitanje* (engl. *read stability*, skr. *RS*) nijedan podatak koji je pročitan od strane procesa P ne sme se menjati od strane drugih procesa sve do završetka obrade od strane procesa P. Sa druge strane, nijedan red promenjen od strane drugog procesa ne može se čitati dok se promene u okviru tog procesa ne okončaju.

Međutim, za razliku od RR, na ovom nivou izolacije proces koji izvršava isti upit više puta, može dobiti neke nove redove u odgovorima u slučaju da drugi proces unese nove redove koji zadovoljavaju dati upit i potvrdi te izmene. Ovo predstavlja tzv. *problem fantomskih redova* (engl. *fantom rows problem*).

Osim ekskluzivnih katanaca, proces na RS nivou izolovanosti dobija bar deljive katance na svim podacima koji su prihvaćeni. Na primer, neka se u procesu P izvršava naredni upit:

```sql
SELECT  *
FROM    DA.STUDIJSKIPROGRAM
WHERE   IDNIVOA = 1
```

U slučaju RR nivoa izolovanosti izvršavanje ovog upita izazvaće zaključavanje svih redova tabele `STUDIJSKIPROGRAM`, a u slučaju RS nivoa izolovanosti zaključavanje se vrši samo nad onim redovima koji ispunjavaju uslov restrikcije `IDNIVOA = 1`. Zbog toga, ako neki drugi proces unese novi red koji zadovoljava datu restrikciju, u slučaju RR nivoa izolovanosti to neće biti moguće, dok će ponovo izvršavanje upita u slučaju RS nivoa izolovanosti proizvesti prikazivanje i tih novih redova.

### 6.4.3 Stabilni kursor

U nivou izolacije *stabilni kursor* (engl. *cursor stability*, skr. *CS*) nijedan proces ne može da menja podatke iz reda dok je ažurirajući kursor pozicioniran nad tim redom. Dakle, ovaj nivo izolovanosti obezbeđuje da konkurentni procesi ne mogu menjati samo trenutno aktivne redove otvorenih kursora posmatranog procesa P. Redovi koji su ranije pročitani mogu biti menjani. Takodje, nijedan red promenjen od strane drugog procesa ne može se čitati dok se promene u okviru tog procesa na okončaju. 

Takođe, ovaj nivo izolovanosti zaključava sve redove kojima transakcija pristupa dok je kursor pozicioniran na tim redovima. Ako transakcija koristi podatke iz reda za čitanje, onda se ovaj katanac drži sve dok se ne dohvati naredni red u kursoru ili dok se transakcija ne završi. Međutim, ako je izmena izvršena nad nekim redom, onda se nad tim redom drži katanac sve do kraja transakcije.

Očito, i u ovom nivou izolacije moguć je problem fantomskih redova. Međutim, postoji još jedan problem koji se može javiti. Neka prvo proces A pročita red, pa zatim pređe na druge redove. Neka zatim proces B pročita i izmeni isti red i potvrdi svoje izmene. Ukoliko proces A ponovo pročita isti red, videće drugačije vrednosti u odnosu na prethodno čitanje. Ovaj problem se naziva *problem neponovljivih čitanja* (engl. *non-repeatable reads problem*).

Bez obzira na razne ekskluzivne katance, ovaj nivo obezbeđuje bar deljive katance na aktivnim redovima svih kursora.

### 6.4.4 Nepotvrđeno čitanje

I nivou izolacije *nepotvrđeno čitanje* (engl. *uncommitted read*, skr. *UR*) proces P može da pristupi nepotvrđenim izmenama od strane drugih procesa. Dodatno, nivo izolacije UR ne sprečava drugim procesima da pristupe redu koji čita proces P, osim ukoliko taj drugi proces ne pokušava da izmeni strukturu tabele ili da je obriše.

Za naredbe `SELECT INTO` ili `FETCH` nad kursorom koji služi samo za čitanje, ili podupite naredbi `INSERT` ili `UPDATE`, ili upite koji kao rezultat imaju skalarnu vrednost, ovaj nivo izolacije omogućava da:

- Bilo koji red pročitan tokom jedinice obrade bude promenjen od strane drugih procesa.

- Proces može čitati podatke izmenjene od strane drugih korisnika, čak i pre nego što su te izmene potvrđene (ne traži se deljeni katanac za čitanje).

- Za ostale operacije važe pravila nivoa izolovanosti CS.

Pored problema fantomskih redova i problema neponovljivih čitanja, postoji još jedan problem koji se može javiti u ovom nivou izolacije. Ako prvo proces A izmeni neku vrednost, zatim proces B pročita tu vrednost pre nego što je potvrđena, a zatim proces A poništi svoje izmene, onda proces B može raditi nad nekorektnim podacima. Ovaj problem se naziva *problem pristupa nepotvrđenim podacima* (engl. *access to uncommitted data problem*). 

### 6.4.5 Programersko podešavanje nivoa izolovanosti

Moguće je specifikovati koji se nivo izolovanosti koristi i on je u efektu tokom trajanja jedinice posla. Postoji nekoliko načina za podešavanje nivoa izolovanosti:

- Navođenje na nivou jedne naredbe je moguće izvršiti korišćenjem klauze `WITH`[^1], čija je sintaksa data u nastavku:

[^1]: Obratiti pažnju da se ne radi o `WITH` klauzi za kreiranje privremenih tabela u `SELECT` naredbi, već da se ova `WITH` klauza nekad naziva i `isolation-clause`.

```sql
WITH (
    RR [<KLAUZA_ZAKLJUČAVANJA>] |
    RS [<KLAUZA_ZAKLJUČAVANJA>] |
    CS |
    UR
)
```

gde je `<KLAUZA_ZAKLJUČAVANJA>` klauza kojom se specifikuje tip katanca koji aplikacija zahteva od SUBP, a čija je sintaksa:

```sql
USE AND KEEP (SHARE|UPDATE|EXCLUSIVE) LOCKS
```

`WITH` klauza se može koristiti u narednim naredbama: `DECLARE CURSOR`, pretražujuća `UPDATE` naredba, `INSERT`, `SELECT`, `SELECT INTO` i pretražujuća `DELETE` naredba.

- Podešavanjem specijalnog registra `CURRENT ISOLATION`, čiji je tip `CHAR(2)`, a koji sadrži informaciju o nivou izolacije za svaku dinamičku SQL naredbu koja se izvršava u tekućoj sesiji. Moguće vrednosti su: prazna niska, `'RR'`, `'RS'`, `'CS'` i `'UR'`. Promena ove vrednosti se može izvršiti naredbom `SET CURRENT ISOLATION`, čija je sintaksa:

```sql
SET [CURRENT] ISOLATION [=] (UR|CS|RR|RS|RESET)
```

Navođenjem vrednosti `RESET` će vrednost registra biti postavljena na praznu nisku, što znači da se neće koristiti ta vrednost za registar.

- Navođenjem vrednosti za nivo izolacije kao atribut paketa, čime se efektivno koristi ta vrednost za aplikacije koje koriste taj paket. Ovo se može uraditi navođenjem `ISOLATION` opcije prilikom izvršavanja naredbi `PRECOMPILE` ili `BIND`. Ova opcija ima narednu sintaksu:

```sql
ISOLATION (CS|RR|RS|UR)
```

Ukoliko se ne specifikuje nivo izolacije, DB2 će podrazumevano koristiti nivo CS.

Naredni primeri ilustruju različite efekte iste aplikacije pri različitim nivoima izolovanosti. Primetimo da smo u narednim primerima koristili klauzu `WITH` za postavljanje nivoa izolovanosti.

{% include lab/exercise.html broj="6.3" tekst="Napisati: 

- C/SQL program `repeatableRead` koji dva puta ispisuje informacije o godini, oznaci, nazivu i periodu prijavljivanja za svaki ispitni rok u 2021. godini. Omogućiti da se prilikom oba ispisivanja dobijaju iste informacije.
- C/SQL program `insertIspitniRok` koji unosi novi ispitni rok za mesec mart u 2021. godini. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Rešenje: Za potrebe prevo\dj enja i pripremanja podataka u bazi, napravljena je `Makefile` datoteka čiji je sadržaj: 

include_source(vezbe/primeri/poglavlje_6/zadatak_6_3/Makefile, makefile)

Prevo\dj enje programa se vr\v si pozivom alata `make`, a mogu\'ce je i o\v cistiti artefakte prevo\dj enja pozivom `make clean`. Pre ilustracije rada programa `repeatableRead`, potrebno je pozvati `make db` koji će pripremiti informacije o ispitnim rokovima u bazu podataka na osnovu skripta `pripremaBaze.sql` čiji je sadržaj:

include_source(vezbe/primeri/poglavlje_6/zadatak_6_3/pripremaBaze.sql, sql)

Pokrenuti program `repeatableRead` i započeti prvo ispisivanje. U toku prvog ispisivanja ili nakon njega, pokrenuti program `insertIspitniRok`, pa nastaviti sa daljim ispisivanjem ispitnih rokova u programu `repeatableRead`. Uveriti se da program `insertIspitniRok` ne može da izvrši unos sve dok program `repeatableRead` ne završi sa svim ispisivanjima i, takođe, da oba ispisivanja u tom programu imaju iste vrednosti.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_3/repeatableRead.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_3/insertIspitniRok.sqc, c)

{% include lab/exercise.html broj="6.4" tekst="Napisati: 

- C/SQL program `readStability` koji dva puta ispisuje informacije o godini, oznaci, nazivu i periodu prijavljivanja za svaki ispitni rok u 2021. godini. Dozvoljeno je da se prilikom drugog ispisivanja pojave novi redovi, ali ne i da budu vidljive izmene pročitanih redova. 
- C/SQL program `insertIspitniRok` koji unosi novi ispitni rok za mesec mart u 2021. godini. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi.
- C/SQL program `updateIspitniRok` koji za svaki ispitni rok u 2021. godini produžava period prijavljivanja za 3 dana. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Rešenje: Kao i u prethodnom zadatku, napravljena je `Makefile` koja slu\v zi za pozivanje alata `make` na ve\'c opisan na\v cin.

Pokrenuti program `readStability` i započeti prvo ispisivanje. U toku ispisivanja ili nakon njega, pokrenuti program `insertIspitniRok`, pa nastaviti sa daljim ispisivanjem ispitnih rokova. Uveriti se da program `insertIspitniRok` može da izvrši unos novog ispitnog roka tokom rada programa `readStability` i da se novi rok vidi prilikom drugog ispisivanja. Zatim, ponovo pokrenuti program `readStability` (nakon ponovnog pripremanja baze podataka pozivom `make db`), pa u toku prvog ispisivanja ili nakon njega, pokrenuti program `updateIspitniRok`. Uveriti se da program `updateIspitniRok` ne može da promeni informacije o ispitnim rokovima dok program `readStability` radi.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_4/readStability.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_4/insertIspitniRok.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_4/updateIspitniRok.sqc, c)

{% include lab/exercise.html broj="6.5" tekst="Napisati: 

- C/SQL program `cursorStability` koji dva puta ispisuje informacije o godini, oznaci, nazivu i periodu prijavljivanja za svaki ispitni rok u 2021. godini. Dozvoljeno je da se prilikom drugog ispisivanja pojave novi redovi i da budu vidljive izmene pročitanih redova, ali ne i nepotvr\dj ene izmene. 
- C/SQL program `insertIspitniRok` koji unosi novi ispitni rok za mesec mart u 2021. godini. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi.
- C/SQL program `updateIspitniRok` koji za svaki ispitni rok u 2021. godini produžava period prijavljivanja za 3 dana. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Rešenje: Kao i u prethodnom zadatku, napravljena je `Makefile` koja slu\v zi za pozivanje alata `make` na ve\'c opisan na\v cin.

Pokrenuti program `cursorStability` i započeti prvo ispisivanje. U toku ispisivanja ili nakon njega, pokrenuti program `insertIspitniRok`, pa nastaviti sa daljim ispisivanjem ispitnih rokova. Uveriti se da program `insertIspitniRok` može da izvrši unos novog ispitnog roka tokom rada programa `readStability` i da se novi rok vidi prilikom drugog ispisivanja. Zatim, ponovo pokrenuti program `readStability` (nakon ponovnog pripremanja baze podataka pozivom `make db`), pa u toku prvog ispisivanja ili nakon njega, pokrenuti program `updateIspitniRok`. Uveriti se da program `updateIspitniRok` ovoga puta može da promeni informacije o ispitnim rokovima, \v cak i dok program `cursorStability` radi, kao i da \'ce ovoga puta program `cursorStability` videti te izmene prilikom drugog ispisivanja, \v sto nije bio slu\v caj za nivo izolovanosti RS ili stro\v zijim od njega.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_5/cursorStability.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_5/insertIspitniRok.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_5/updateIspitniRok.sqc, c)

{% include lab/exercise.html broj="6.6" tekst="Napisati: 

- C/SQL program `uncommittedRead` koji dva puta ispisuje informacije o godini, oznaci, nazivu i periodu prijavljivanja za svaki ispitni rok u 2021. godini. Dozvoljeno je da se prilikom drugog ispisivanja pojave novi redovi, da budu vidljive izmene pročitanih redova, bilo one potvr\dj ene ili ne. 
- C/SQL program `insertIspitniRok` koji unosi novi ispitni rok za mesec mart u 2021. godini. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi. Zahtevati od korisnika eksplicitno potvr\dj ivanje izmena nakon unosa.
- C/SQL program `updateIspitniRok` koji za svaki ispitni rok u 2021. godini produžava period prijavljivanja za 3 dana. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi. Zahtevati od korisnika eksplicitno potvr\dj ivanje izmena nakon a\v zuriranja." %}

Rešenje: Kao i u prethodnom zadatku, napravljena je `Makefile` koja slu\v zi za pozivanje alata `make` na ve\'c opisan na\v cin.

Pokrenuti program `uncommittedRead` i započeti prvo ispisivanje. U toku ispisivanja ili nakon njega, pokrenuti program `insertIspitniRok`, ali ne potvrditi izmene jo\v s uvek. Zatim, nastaviti sa daljim ispisivanjem ispitnih rokova u programu `uncommittedRead`. Uveriti se da program `insertIspitniRok` može da izvrši unos novog ispitnog roka tokom rada programa `uncommittedRead` i da se novi rok vidi prilikom drugog ispisivanja, bez obzira \v sto izmene u programu `insertIspitniRok` nisu potvr\dj ene. Zatim, ponovo pokrenuti program `readStability` (nakon ponovnog pripremanja baze podataka pozivom `make db`) i uraditi isti postupak sa programom `updateIspitniRok` umesto `insertIspitniRok`. Uveriti se \'ce i ovoga puta program `uncommittedRead` videti nepotvr\dj ene izmene od strane programa `updateIspitniRok`, sli\v cno kao i u slu\v caju programa `insertIspitniRok`. Ovo je pona\v sanje koje se ne\'ce javiti u slu\v caju nivoa izolovanosti CS ili stro\v zijim od njega.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_6/uncommittedRead.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_6/insertIspitniRok.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_6/updateIspitniRok.sqc, c)

Sledi jo\v s jedan zadatak koji ilustruje koncept konkurentnih transakcija u vi\v sekorisni\v ckom okru\v zenju. Za potrebe ovog zadatka je neophodno izvr\v siti naredne SQL naredbe:

```sql
CREATE TABLE DA.DRZAVA (
    IDDRZAVE INTEGER NOT NULL PRIMARY KEY,
    NAZIV VARCHAR(50) NOT NULL
);

CREATE TABLE DA.EKSKURZIJA (
    INDEKS INTEGER NOT NULL,
    IDDRZAVE INTEGER NOT NULL,
    TRENUTAKPRIJAVE TIMESTAMP NOT NULL,
    PRIMARY KEY (INDEKS),
    FOREIGN KEY (INDEKS) REFERENCES DA.DOSIJE,
    FOREIGN KEY (IDDRZAVE) REFERENCES DA.DRZAVA
);

INSERT  INTO DA.DRZAVA
VALUES  (1, 'Spanija'),
        (2, 'Italija'),
        (3, 'Grcka'),
        (4, 'Nemacka'),
        (5, 'Rusija'),
        (6, 'Francuska');
```

{% include lab/exercise.html broj="6.7" tekst="Napisati C/SQL program koji od korisnika zahteva da unese identifikator studijskog programa sa osnovnih studija. Program na osnovu unetog podatka pronalazi naredne informacije o studentima sa datog studijskog programa: (1) broj indeksa, (2) ime, (3) prezime, (4) broj položenih ispita tog studenta, (5) broj položenih ESPB bodova, ali samo ukoliko student ima položeno bar 12 predmeta, ima skupljeno bar 120 bodova i ako se studentov indeks već ne nalazi u tabeli `EKSKURZIJA`.

Za svakog pronađenog studenta, korisnik unosi ceo broj koji predstavlja jedan od identifikatora država iz tabele `DRZAVA` ili 0 (pretpostaviti da je ispravan unos). Ukoliko korisnik unese identifikator, aplikacija unosi informacije u tabelu `EKSKURZIJA`, a ukoliko unese 0, prelazi se na sledećeg studenta. Nakon svaka 3 uneta glasa, ponuditi korisniku mogućnost da prekine sa daljom obradom i napusti program, unošenjem odgovora `'da'`.

Napomene: Obrada jednog studenta predstavlja jednu transakciju. Proveravati greške koje se javljaju prilikom izvršavanja aplikacije u višekorisničkom okruženju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi. Voditi računa da aplikacija ne sme da vidi bilo kakve vrste izmena od strane drugih aplikacija." %}

include_source(vezbe/primeri/poglavlje_6/zadatak_6_7.sqc, c)

## 6.5 Programersko zaključavanje tabela

Programeri mogu sami da zatraže katance od SUBP ukoliko smatraju da im je to neophodno, ali sa ograničenjem da je na ovaj način jedino moguće zaključati tabelu, a ne redove, blokove, i druge objekte. SQL naredba `LOCK TABLE` onemogućava konkurentnoj aplikaciji da koristi ili menja tabelu, u zavinosti od režima u kojem se tabela zaključava. Ovako dobijeni katanac se oslobađa kada se završi jedinica posla u kojem je naredba `LOCK TABLE` izvršena, bilo operacijom potvrđivanja izmena ili njenim završavanjem.

Korisnik koji izvršava naredbu `LOCK TABLE` mora da ima barem jednu od narednih privilegija da bi je uspešno izvršio:

- `SELECT` privilegiju nad tabelom koja se zaključava.
- `CONTROL` privilegiju nad tabelom koja se zaključava.
- `DATAACCESS` privilegiju.

Sintaksa ove naredbe je data u nastavku:

```sql
LOCK TABLE <IME_TABELE> IN [SHARE|EXCLUSIVE] MODE
```

Ovom naredbom se traži odgovarajući katanac nad tabelom `<IME_TABELE>`. U zavisnosti od odabranih vrednosti narednih opcija prilikom deklaracije složene SQL naredbe, ta naredba može imati različite varijante koji utiču na način izvršavanja:

- Klauza `SHARE` onemogućava konkurentnoj aplikaciji da izvrši bilo koju operaciju osim čitanja podataka iz tabele.

- Kluza `EXCLUSIVE` onemogućava konkurentnoj aplikaciji da izvrši bilo koju operaciju nad tabelom. Napomenimo da ovaj režim ne onemogućava da aplikacioni procesi izvršavaju naredbe čitanja podataka iz tabele ukoliko oni rade na nivou izolacije nepotvrđeno čitanje (UR).

{% include lab/exercise.html broj="6.6" tekst="Napisati C/SQL program `shareMode` koji ispisuje identifikator, oznaku, naziv i broj ESPB bodova za svaki studijski program u bazi podataka. Omogućiti da ostale aplikacije ne mogu da menjaju ove podatke tokom ispisivanja podataka.

Napisati C/SQL program `exclusiveMode` koji ispisuje identifikator, oznaku, naziv, broj semestara i broj ESPB bodova za svaki studijski program u bazi podataka. Omogućiti da ostale aplikacije ne mogu da menjaju ove podatke tokom ispisivanja podataka, kao ni da ih čitaju." %}

Rešenje: Isprobati sve kombinacije redosleda izvršavanja programa `exclusiveMode` i `shareMode` i uveriti se da jedino kombinacija `(shareMode, shareMode)` može raditi konkurentno. Primetimo da u ovom zadatku nismo koristili obradu \v cekanja na katance niti smo postavili vreme za istek katanaca, \v sto zna\v ci da \'ce jedna aplikacija \v cekati sve dok druga ne zavr\v si sa radom.

include_source(vezbe/primeri/poglavlje_6/zadatak_6_8/shareMode.sqc, c)

include_source(vezbe/primeri/poglavlje_6/zadatak_6_8/exclusiveMode.sqc, c)

## 6.6 Zadaci za vežbu

{% include lab/exercise.html broj="6.9" tekst="Napisati C/SQL program koji za sve ispitne rokove pronalazi informacije o polaganjima za svaki polo\v zeni predmet u tom ispitnom roku i te podatke unosi u tabelu `ISPITNIROKOVIPOLAGANJA`. Nakon jednog unosa podataka ispisati unete podatke odvojene zapetom. Nakon svih unosa podataka, potrebno je odgovaraju\'com SQL naredbom izra\v cunati broj unetih redova u tabeli `ISPITNIROKOVIPOLAGANJA` i ispisati rezultat na standardni izlaz. Napisati program tako da mo\v ze da radi u vi\v sekorisni\v ckom okru\v zenju. Unos podataka za jedan ispitni rok i jedno polaganje predstavlja jednu transakciju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Napraviti tabelu `ISPITNIROKOVIPOLAGANJA` koja sadr\v zi kolone sa podacima o:
- godini roka
- oznaci roka
- identifikatoru predmeta
- oceni
- broju polo\v zenih ispita na tom predmetu sa tom ocenom
- nazivu roka
- nazivu predmeta

Definisati primarni klju\v c na osnovu prvih 5 kolona i strane klju\v ceve ka odgovaraju\'cim tabelama. 

{% include lab/exercise.html broj="6.10" tekst="Napisati C/SQL program koji za sve studente pronalazi informacije o studijskom programu na kojem studiraju i te podatke unosi u tabelu `STUDIJSKIPROGRAMPSTUDENTI`. Nakon jednog unosa podataka ispisati unete podatke odvojene zapetom. Nakon svih unosa podataka, potrebno je odgovaraju\'com SQL naredbom izra\v cunati broj unetih redova u tabeli `STUDIJSKIPROGRAMPSTUDENTI` i ispisati rezultat na standardni izlaz. Napisati program tako da mo\v ze da radi u vi\v sekorisni\v ckom okru\v zenju. Unos podataka za jednog studenta i jedan studijski program predstavlja jednu transakciju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Napraviti tabelu `STUDIJSKIPROGRAMPSTUDENTI` koja sadr\v zi kolone sa podacima o:
- indeksu studenta
- nazivu studijskog programa
- imenu i prezimenu studenta
- zvanju
- broju bodova koji je potrebno ostvariti na studijskom programu
- broju bodova koje je student trenutno ostvario
- napomeni: vrednost ove kolone je niska `'Polozeni su svi predmeti'` ukoliko je student ostvario sve potrebne poene, a prazna niska u suprotnom

Definisati primarni klju\v c na osnovu prve 2 kolone i strane klju\v ceve ka odgovaraju\'cim tabelama. 

{% include lab/exercise.html broj="6.11" tekst="Napisati C/SQL program koji redom omogu\'cava naredne funkcionalnosti:

1. Student zapo\v cinje prijavljivanje na praksu navo\dj enjem broja indeksa, nakon \v cega mu se nudi spisak numerisanih naziva predmeta za koje se mo\v ze prijaviti da radi praksu. Za prijavljivanje studentu se nude samo predmeti koje je polo\v zio sa ocenom ve\'com od prose\v cne ocene sa polo\v zenih ispita iz tog predmeta.
2. Zatim student (potencijalno vi\v se puta) upisuje identifikator predmeta iz koga \v zeli da radi praksu, ili 0 za kraj. Posle svakog izabranog predmeta studentu se nudi izmenjen spisak predmeta u kome se ne nalaze
ve\'c izabrani predmeti. Odabrani predmet se unosi u tabelu `PRAKSA`.
3. Na kraju se ispisuje izve\v staj koji za svaki prijavljeni predmet sadr\v zi: naziv predmeta, broj do tada prijavljenih studenata za praksu iz tog predmeta; prose\v cnu prolaznost (ukupnu) u poslednjih 5 godina. Izve\v staj urediti u opadaju\'cem redosledu po prolaznosti. Prolaznost ra\v cunati kao koli\v cnik broja polo\v zenih ispita sa ocenom ve\'com od 5 nevezano od statusa prijave i broja izlazaka na ispit (broja unosa u tabelu ispit sa statusom prijave razli\v citim od 'n').

Svako prijavljivanje predstavlja nezavisnu transakciju. Napisati program tako da radi ispravno u vi\v sekorisni\v ckom okru\v zenju. Postaviti istek vremena za zahtevanje katanaca na 5 sekundi." %}

Potrebno je u bazi vstud kreirati naredbu koja pravi tabelu `PRAKSA` koja ima kolone:
- `INDEKS` - indeks studenta;
- `IDPREDMETA` - identifikator predmeta koji student prijavljuje.

Definisati primarni klju\v c u tabeli `PRAKSA` i strane klju\v ceve nad tabelama `DOSIJE` i `PREDMET`. U ovoj tabeli se \v cuvaju podaci o prijavljenim praksama. Jedan student mo\v ze potencijalno prijaviti vi\v se razli\v citih predmeta, a i isti predmet mo\v ze prijaviti vi\v se razli\v citih studenata.

-----
