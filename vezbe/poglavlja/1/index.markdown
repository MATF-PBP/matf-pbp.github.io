---
layout: page
title: 1. Uvod u programiranje baza podataka
under_construction: true
---

DB2 je sistem za upravljanje relacionim bazama podataka (RSUBP) koji nudi veoma moćne softverske alate za programiranje baza podataka. Ovi alati su korisni kako administratorima baza podataka, tako i programerima aplikacija koje koriste mogućnosti relacionih baza podataka. Pod programiranjem baza podataka možemo smestiti naredne dve aktivnosti:

1. Programiranje na serveru, odnosno, programiranje SQL rutina.
2. Programiranje na klijentu kroz više programske jezike.

Programiranje na klijentu podrazumeva korišćenje viših programskih jezika kao što su C, C++, Java, PHP i mnogih drugih. Često se ovi jezici nazivaju i *matični jezici* (engl. *host language*). Korišćenjem nekih od razvojnih okruženja, ti programski jezici se koriste za pristupanje interfejsu za programiranje aplikacija (engl. *application programming interface*, skr. *API*). Kroz ovaj interfejs se zatim, preko drajvera koji je dostupan programerima, aplikacija povezuje sa RSUBP i izvršava SQL upite. Ovo povezivanje sa bazom podataka se izvršava kroz mogućnosti operativnog sistema na kojem se klijentska aplikacija izvršava. 

Sa druge strane, programiranje na serveru obuhvata programiranje tzv. *SQL rutina* (engl. *SQL routine*). SQL rutina može biti: *ugrađena procedura* (engl. *stored procedure*), *korisnički-definisana funkcija* (engl. *user-defined function*) ili *okidač* (engl. *trigger*). Svi ovi tipovi SQL rutina podrazumevaju programiranje operacija koje se čuvaju u samoj bazi podataka. Programeri zatim imaju mogućnost poziva ovih sačuvanih procedura, odn. njihovog izračunavanja.

!["Dva načina za programiranje baza podataka: programiranje na klijentu (levo) i programiranje na serveru (desno)."](./Slike/klijent-server-arhitektura.png "Dva načina za programiranje baza podataka: programiranje na klijentu (levo) i programiranje na serveru (desno)."){:class="ui centered huge image"}

## 1.1 Opis razvoja klijentskih aplikacija

Prilikom razvijanja klijentskih aplikacija važno je odrediti viši programski jezik koji će se koristiti. Odabir konkretnog višeg programskog jezika u određenoj meri diktira proces razvoja u kontekstu API-ja koji je dostupan za dati viši programski jezik. Međutim, bez obzira koji se viši programski jezik koristi, koncepti koji se koriste za rad sa bazama podataka su isti. Ono što dodatno olakšava ovaj proces jeste postojanje različitih elemenata softverskog paketa koji se zove *Db2 Data Server Client*. Neki od elemenata ovog paketa sa kojima ćemo se susreti u ovoj knjizi su:

- Prekompilatori za C/C++, COBOL i Fortran.
- Podrška za ugnežđene SQL naredbe u višim programskim jezicima u vidu programerskih biblioteka, zaglavlja za uključivanje u izvorni kod i primere izvornih kodova.
- Podrška za aplikacije zasnovane na ODBC i Db2 Call Level Interface (Db2 CLI) standardima.
- IBM Data Server Driver za JDBC i SQLJ aplikacije.
- IBM Data Studio softversko okruženje za razvoj aplikacija i radom sa Db2 bazama podataka.

Opišimo sada proces razvoja klijentskih aplikacija na primeru aplikacije sa ugnežđenim SQL naredbama u programskom jeziku C. Ovaj proces će biti opisan na dovoljno apstraktnom nivou da ne ulazi u detalje programskog jezika C i detalja procesa vezanim za njega, ali će biti i dovoljno konkretan da čitalac stekne opšti utisak u način razvoja klijentskih aplikacija za rad sa Db2 bazama podataka.

Nakon prepoznavanja problema i izvođenja njegovog rešenja u poslovnom modelu, pristupa se konstrukciji baze podataka i definisanju potrebnih SQL naredbi koje će se koristiti u njegovom rešavanju. Baza podataka se zatim instalira na Db2 serveru baze podataka, a SQL naredbe se koriste u izvornom kodu višeg programskog jezika C prateći odgovarajući C/SQL Db2 API. Aplikacija se kodira na klijentu koji ima instaliran Db2 Data Server Client softverski paket sa alatima za razvoj aplikacija. S obzirom da kod predstavlja "mešavinu" C naredbi i SQL naredbi, potrebno je izvesti iz tog koda nova dva međukoda: jedan koji ima "čiste" C naredbe i drugi koji ima "čiste" SQL naredbe. Prvi međukod se podvrgava procesu prevođenja kao i svaki drugi C program, dok se drugi kod proverava nad bazom podataka nad kojom su definisane SQL naredbe koje se koriste. Ukoliko nema grešaka, SQL naredbe se ugrađuju u bazu podataka, a od izvornog C koda se formira izvršni program. 

Izvršni program se zatim instalira na odgovarajućim uređajima gde će se koristiti (pri čemu je potrebno da ti sistemi imaju odgovarajuće Db2 drajvere za komunikaciju sa Db2 serverom baze podataka - ovo je po pravilu lakše instalirati od celog Db2 Data Server Client softverskog paketa). Prilikom pokretanja programa, aplikacija izvršava C naredbe kao i svaki drugi C program, a kada naiđe na deo koda koji upravlja bazom podataka, program vrši komunikaciju sa datom bazom podataka preko odgovarajućeg Db2 drajvera, korišćenjem mogućnosti operativnog sistema i mreže, čime se prethodno ugrađene SQL naredbe izvršavaju. Rezultati izvršavanja tih SQL naredbi se šalju nazad programu, uz eventualno izveštavanje o greškama ili upozorenjima do koje je došlo u sistemu za upravljanje bazom podataka.

!["Konceptualni prikaz razvoja C/SQL klijentskih aplikacija sa ugnežđenim SQL naredbama."](./Slike/razvoj-klijentskih-aplikacija.png "Konceptualni prikaz razvoja C/SQL klijentskih aplikacija sa ugnežđenim SQL naredbama."){:class="ui centered huge image"}

## 1.2 Opis razvoja serverskih aplikacija

## 1.3 Podešavanje okruženja za rad

Za početak, neophodno je da instaliramo Db2 sistem za upravljanje bazom podataka na našem operativnom sistemu.

### 1.3.1 Instalacija Db2 SUBP

Pakete za instalaciju je moguće preuzeti narednim koracima:

1. Otvoriti stranicu koja se nalazi na adresi [https://www.ibm.com/products/db2-database/developers](https://www.ibm.com/products/db2-database/developers).
2. Odabrati opciju "Try the free edition now".
3. Kreirati IBM nalog i prijaviti se na sistem.
4. Moguće je da sistem zatraži od Vas da odaberete opcije vezane za privatnost. Odaberite opcije i nastavite dalje.
5. Nakon toga bi trebalo da se otvori stranica sa naslovom "IBM Db2 Download Center", gde se može pronaći lista Db2 proizvoda za različite operativne sisteme.
6. Preuzeti odgovarajuću verziju "IBM Db2 v11.5.5" klikom na "Download" pored odgovaraju\'ceg operativnog sistema.
7. \v Stiklirati "I Agree", pa kliknuti na "Confirm" i "Continue".

Ukoliko koristite Linux OS, instalacija se vrši na sledeći način:

1. Otpakovati arhivu i u terminalu se pozicionirati u direktorijum koji se kreirao prilikom otpakivanja
2. Pokrenuti komandu:
```shell
sudo ./db2setup
```
Ako pri instaliranju dobijete poruku da nedostaje biblioteka `libaio.so.1`, pokrenite komandu
```shell
sudo apt-­get install libaio1
```
ili koristite odgovarajući upravljač paketima za Vaš sistem.

3. Pratiti uputstva u instalacionom prozoru: 
    1. Obratiti pažnju da se proizvod instalira na lokaciju `/opt/ibm/db2/V11.5` (ovo je bitno zbog skripta za prevođenje programa). 
    2. Obraditi pažnju da se odabere "Custom Install" i označiti sve opcije da se instaliraju.
    3. Obratiti pažnju na korisnički nalog koji se kreira kao "Instance owner" (podrazumevano je "db2inst1", što će biti korišćeno u nastavku). Zapamtite šifru koju ste ukucali za ovog korisnika.
    4. Obeležiti opciju "Autostart the instance at system startup" da bi se server sam podizao prilikom pokretanja sistema. 
    
4. Za podešavanje da neki drugi korisnik na sistemu može da koristi DB2 RSUBP, potrebno je izvršiti naredne dodatne korake:
    1. Kreirati novog korisnika. U nastavku će to biti korisnik `student` sa lozinkom `abcdef`. Preporučuje se da koristite iste kredencijale da bi Vam kodovi radili bez da ih menjate.
    2. Kreirati novu grupu (u nastavku ce to biti grupa `pbp_db2_admin`):
```shell
sudo groupadd pbp_db2_admin
```

    3. Dodati grupi `pbp_db2_admin` korisnika `student`:
```shell
sudo usermod -­a -­G pbp_db2_admin student
```
    4. Restartovati sistem i ulogovati se kao korisnik `db2inst1` sa lozinkom koju ste ukucali pri instalaciji.

    5. Izvršiti narednu komandu:
```shell
db2 update dbm cfg using SYSADM_GROUP pbp_db2_admin
```
    6. Ulogovati se kao korisnik `student` sa lozinkom `abcdef`
    7. Iz datoteke `/home/db2inst1/.profile` prekopirati tri linije koda koje se u toj datoteci nalaze nakon naredne linije:
```
# The following three lines have been added by IBM DB2 instance utilities.
```
    8. Zalepiti prekopirane linije na kraj datoteke `/home/student/.profile`
    9. Restartovati sistem i ulogovati se kao `student`
    10. Pokrenuti komande:
```shell
db2start
db2
```
    11. Ukoliko Vam se uspešno pokrenuo upravljač bazom podataka i db2 komandna linija, onda ste uspešno instalirali Db2 sistem. (Iz db2 komandne linije se izlazi naredbom `quit`).

Instalacija za Windows operativni sistem podrazumeva sli\v cnu proceduru (nakon otpakivanja arhive, pokrenuti `setup.exe`), te je ne\'cemo detaljnije izlagati ovde.

Nakon instalacije Db2 SUBP na Windows sistemu, na raspolaganju nam je dostupna skript datoteka "DB2 Command Window - Administrator" za podešavanje okruženja komandne linije.

Skript se mo\v ze prona\'ci na narednim lokacijama (lokacije se mogu razlikovati na Vasem sistemu):

- Iz Start menija:
```
Start > IBM DB2 DB2COPY1 (Default) > DB2 Command Window - Administrator
```

- Na lokaciji:
```
C:\ProgramData\Microsoft\Windows\Start Menu\Programs\IBM DB2 DB2COPY1 (Default)\
```

Ovaj skript služi za inicijalizaciju Db2 okru\v zenja.

### 1.3.2 Kori\v s\'cenje Docker kontejnera

Ukoliko ne \v zelite da instalirate SUBP, na raspolaganju je i Docker kontejner koji se mo\v ze prona\'ci na adresi [https://hub.docker.com/r/ibmcom/db2](https://hub.docker.com/r/ibmcom/db2). Detaljno uputstvo za upotrebu se nalazi u opisu Docker kontejnera na datoj adresi.

### 1.3.3 Instalacija IBM Data Studio alata

1. Otvoriti stranicu koja se nalazi na adresi [https://www.ibm.com/products/ibm-data-studio](https://www.ibm.com/products/ibm-data-studio).
2. Kliknuti na "Download product".
3. Prijaviti se na sistem sa kreiranim IBM nalogom u prethodnim instrukcijama.
4. Prihvatiti licencu.
5. Nakon toga bi trebalo da se otvori stranica sa naslovom "IBM Data Studio client".
6. Kliknuti na tab "Download using http".
7. Preuzeti verziju alata za odgovaraju\'ci operativni sistem klikom na "Download" pored naziva verzije.

U slu\v caju Linux operativnog sistema:

1. Otpakovati arhivu i u terminalu se pozicionirati u direktorijum koji se kreirao prilikom otpakivanja.
2. Pokrenuti komandu:
```shell
sudo ./launcher.sh
```
3. Ukoliko se žali da ne može da pronađe odgovarajući veb pregledač, onda umesto prethodne komande pokrenuti narednu komandu (ignorisati eventualne greske) i malo sačekati da se pokrene "IBM Installation Manager":
```shell
sudo ./imLauncherLinux.sh
```

U slu\v caju Windows operativnog sistema:

1. Pokrenuti instalacionu datoteku `launchpad.exe` iz otpakovane arhive pod administratorskim privilegijama.

Nakon \v sto se otvori prozor za instalaciju:

1. Kliknuti na "Install".
2. Prvo će se instalirati "IBM Installation Manager", a zatim će se instalirati "IBM Data Studio".
3. Prilikom instalacije "IBM Data Studio" softvera, obavezno odabrati data "Data Studio Full client" (najbolje je da štiklirate sve što Vam je ponuđeno da se instalira), da biste mogli da razvijate Java aplikacije.
4. Potencijalno restartovati računar nakon instalacije.
5. Pokrenuti Data Studio da biste proverili instalaciju.

### 1.3.4 Instaliranje C/C++ kompilatora za Windows

Ve\'cina Linux operativnih sistema ve\'c dolazi sa preinstaliranim alatima za razvoj C/C++ aplikacija. Za Windows operativne sisteme ovo nije slu\v caj, pa je potrebno instalirati [Visual Studio okruženje za razvoj](https://visualstudio.microsoft.com/downloads/). Alternativno, moguće je instalirati samo [Visual C/C++ alate za razvoj iz komandne linije](https://visualstudio.microsoft.com/downloads/) (tražite "Build Tools for Visual Studio" na stranici). 

Nakon instalacije proizvoda, na raspolaganju nam je dostupna skript datoteka za podešavanje okruženja komandne linije (lokacija se mo\v ze razlikovati ako ste odabrali druga\v ciju putanju prilikom instalacije):

```
C:\Program Files (x86)\Microsoft Visual Studio\2017\Community\VC\Auxiliary\Build\vcvars64.bat
```

Skript se mo\v ze prona\'ci i:

- Iz Start menija:
```
Start > Visual Studio 2017 > x64 Native Tools Command Prompt for VS 2017
```
- Na lokaciji:
```
C:\ProgramData\Microsoft\Windows\Start Menu\Programs\Visual Studio 2017\Visual Studio Tools\VC\
```

Ovaj skript služi za inicijalizaciju Visual C/C++ alata za razvoj. Radi lak\v seg pristupa, pretpostavimo da ste napravili pre\v cicu do ovog skripta sa nazivom `x64 Native Tools Command Prompt for VS 2017.lnk` nalaze na `Desktop`u. Da bismo mogli da kompiliramo SQC datoteke, potrebno je da uradimo sledeće:

1. Otvoriti "DB2 Command Window - Administrator" sa administratorskim privilegijama.
2. U terminalu koji se otvorio, pokrenuti naredne komande (zameniti `username` Vašim korisničkim nalogom): 
```bat
cd /D C:\Users\username\Desktop\
"x64 Native Tools Command Prompt for VS 2017.lnk"
cd /D C:\Users\username\Desktop\
```

Sada možemo da koristimo DB2 alate i Visual C/C++ alate. Na primer, ako se na `Desktop`u nalazi datoteka `aplikacija.sqc` koju želimo da prevedemo, onda koristeći skript `prevodjenje.bat` koji je naveden ispod, prevođenje se vrši na sledeći način:

```bat
prevodjenje.bat aplikacija stud2020
```

Trebalo bi da vidimo poruku sli\v cnu narednoj:

```
   Database Connection Information

 Database server        = DB2/NT64 11.5.1.1
 SQL authorization ID   = USERNAME...
 Local database alias   = STUD2020


LINE    MESSAGES FOR aplikacija.sqc
------  --------------------------------------------------------------------
        SQL0060W  The "C" precompiler is in progress.
        SQL0091W  Precompilation or binding was ended with "0"
                  errors and "0" warnings.

LINE    MESSAGES FOR aplikacija.bnd
------  --------------------------------------------------------------------
        SQL0061W  The binder is in progress.
        SQL0091N  Binding was ended with "0" errors and "0" warnings.
Microsoft (R) C/C++ Optimizing Compiler Version 19.16.27025.1 for x64
Copyright (C) Microsoft Corporation.  All rights reserved.

aplikacija.c
Microsoft (R) Incremental Linker Version 14.16.27025.1
Copyright (C) Microsoft Corporation.  All rights reserved.
```

Prilikom uspešnog prevođenja, skript generiše izvršnu datoteku aplikacija.exe koju je moguće pokrenuti:

```bat
aplikacija.exe
```

Skript za prevođenje `prevodjenje.bat` dat je u nastavku:

```bat
@echo off
 
set INCLUDE=%DB2PATH%\include;%INCLUDE%
 
db2 connect to %2
db2 precompile %1.sqc bindfile
db2 bind %1.bnd
 
cl /Zi /Od /c /W3 %1.c
 
link -debug -out:%1.exe %1.obj "%DB2PATH%\lib\db2api.lib"
```

### 1.3.5 Uputstvo za prevođenje SQLJ aplikacija iz terminala

Ukoliko se primer sastoji od samo jedne SQLJ datoteke, npr. `Primer.sqlj`, onda je dovoljno pokrenuti skript za prevođenje:

```
./bldsqlj Primer
```

Ako se primer sastoji od više `SQLJ` datoteka, na primer, `Primer.sqlj` (koji sadrži metod `main` i predstavlja glavni program) i `Iterator.sqlj` (koji sadrži deklaraciju iteratora koji se koristi u `Primer.sqlj`), onda je potrebno prvo transpilirati pomoćne datoteke pozivanjem:

```
sqlj Iterator.sqlj
```

a zatim prevesti glavnu datoteku:

```
./bldsqlj Primer
```

Napomena: Potrebno je da se u istom direktorijumu gde se nalazi kod, smeste i skriptovi za prevođenje: `bldsqlj` i `db2customize2`.

Pokretanje programa se vrši skriptom `pokreni`:

```
./pokreni Primer
```

Skriptovi za prevođenje i pokretanje se nalaze u nastavku:

Skript `bldsqlj`:

```shell
#!/bin/sh
 
# To hardcode user ID (USER) and password (PSWD)
# Replace "NULL" with the correct values in quotes
USER="student"
PSWD="abcdef"
# You can replace the defaults for each of the following
# with a new value. Note that the PORTNUM number cannot
# be one already used by another process.
SERVER=bp
PORTNUM=50001
DB="vstud"
 
 
# Translate and compile the SQLJ source file
# and bind the package to the database.
 if ( [ $# -eq 1 ]  && [ $USER != "NULL" ] && [ $PSWD != "NULL" ] ) || ( [ $# -ge 3 ]  && [ $# -le 6 ] )
 then
    # Remove .sqlj extension
    progname=${1%.sqlj}
 
    sqlj "${progname}.sqlj"
 
    if [ $# -eq 1 ]
    then
       ./db2sqljcustomize2 -url jdbc:db2://$SERVER:$PORTNUM/$DB \
       -user $USER -password $PSWD "${progname}_SJProfile0"
    elif [ $# -eq  3 ]
    then
       ./db2sqljcustomize2 -url jdbc:db2://$SERVER:$PORTNUM/$DB -user $2 -password $3 \
       "${progname}_SJProfile0"
    elif [ $# -eq 4 ]
    then
       ./db2sqljcustomize2 -url jdbc:db2://$4:$PORTNUM/$DB -user $2 -password $3 \
       "${progname}_SJProfile0"
    elif [ $# -eq 5 ]
    then
       ./db2sqljcustomize2 -url jdbc:db2://$4:$5/$DB -user $2 -password $3 \
       "${progname}_SJProfile0"
    else
       ./db2sqljcustomize2 -url jdbc:db2://$4:$5/$6 -user $2 -password $3 \
       "${progname}_SJProfile0"
    fi
 else
    echo 'Usage: bldsqlj prog_name (requires hardcoding user ID and password)'
    echo '       bldsqlj prog_name userid password'
    echo '       bldsqlj prog_name userid password server_name'
    echo '       bldsqlj prog_name userid password server_name port_number'
    echo '       bldsqlj prog_name userid password server_name port_number db_name'
    echo ''
    echo '       Defaults:'
    echo '         userid      = '$USER
    echo '         password    = '$PSWD
    echo '         server_name = '$SERVER
    echo '         port_number = '$PORTNUM
    echo '         db_name     = '$DB
 fi
```

Skript `db2customize2`:

```shell
#!/bin/sh
 
java com.ibm.db2.jcc.sqlj.Customizer "$@"
```

Skript `pokreni`:

```shell
#!/bin/sh
 
java -Djava.library.path="/opt/ibm/db2/V10.5/lib32" $1  
```

### 1.3.6 Uputstvo za pravljenje JDBC Java projekta u Data Studio alatu

1. Otvoriti “Java" perspektivu:
    1. Window > Open Perspective > Other
    2. Štiklirati “Show all"
    3. Odabrati “Java" i pritisnuti OK
    4. Ukoliko se prikaže prozor “Confirm Enablement", pritisnuti OK
2. U “Package Explorer" pogledu napraviti novi Java projekat:
    1. Desni klik na “Package Explorer"
    2. New > Java Project
    3. Dodeliti projektu naziv 
    4. Pritisnuti “Finish"
3. Napravljenom projektu dodati DB2 JDBC drajvere:
    1. Desni klik na projekat 
    2. Iz padajućeg menija izabrati “Properties"
    3. Iz levog menija izabrati “Java Build Path" i zatim tab “Libraries"
    4. Izabrati opciju “Add External JARs…" i dodati `db2jcc4.jar` i `db2jcc_license_cu.jar` koje se nalaze na lokaciji:
        - Linux: `/opt/ibm/db2/V10.5/java`
        - Windows: `C:\Program Files\IBM\IBM\SQLLIB\java`
    5. Izabrati OK
4. Kreirati novi paket u projektu:
    1. Desni klik na projekat
    2. Iz padajućeg menija izabrati New > Package
    3. Dodeliti paketu naziv 
    4. Pritisnuti “Finish"
5. Kreirati novu klasu u paketu i dodati joj main metod:
    1. Desni klik na paket
    2. Iz padajućeg menija izabrati New > Class
    3. Dodeliti klasi naziv 
    4. Štiklirati “public static void main(String[] args)"
    5. Pritisnuti “Finish"
6. Iskucati željeni kod
7. Pokrenuti JDBC aplikaciju:
    1. Desni klik na klasu
    2. Iz padajućeg menija izabrati Run as > Java Application

### 1.3.7 Uputstvo za uvoženje JDBC projekta u Data Studio

1. Kreirati novi projekat
    1. Odabrati File > New > Java Project
    2. Dodeliti naziv projektu
    3. Odabrati opciju “Create separate folders for sources and class files"
    4. Pritisnuti “Finish"
2. Uvesti fajl strukturu postojećeg projekta u novokreirani projekat
    1. Odabrati File > Import…
    2. Odabrati General > File System
    3. U polju “From directory" navesti putanju do postojećeg projekta ili odabrati “Browse…", pa potražiti postojeći projekat na računaru, 
    na primer: `~\Downloads\cas10`
    4. Označiti direktorijum koji se pojavi u levom prozoru
    5. U polju “Into folder" navesti direktorijum novokreiranog projekta ili ga potražiti u listi klikom na “Browse…"
    6. Odabrati “Finish"
3. Napravljenom projektu dodati DB2 JDBC drajvere, kao u prethodnom uputstvu

## 1.4 Baza podataka `STUD2020`

U zadacima u ovom tekstu se koristi baza podataka `STUD2020`. To je pojednostavljen model baze podataka informacionog sistema fakulteta. Podaci su nalik na podatke iz baze podataka Matematičkog fakulteta, ali sadržaj ne odgovara stvarnim podacima.

Baza podataka ima dve skoro identične kopije organizovane u dve sheme:

- U shemi `DA` su tabele sa ASCII niskama, koje su lakše za rad u komandom režimu i u alatima koji ne rade dobro sa Unicode niskama.

- U shemi `DB` su skoro iste tabele, ali sa Unicode sadržajem.

Svaka od ove dve kopije baze ima po 16 tabela i može da se posmatra kao celovita baza podataka. 

### 1.4.1 Kreiranje baze podataka `STUD2020`

1. Preuzeti [ovu arhivu sa skriptovima](/baze/stud2020/stud2020.zip) za kreiranje baze podataka `STUD2020` i popunjavanje ove baze podacima. 
2. Otpakivovati arhivu.
3. Otvoriti terminal (na Linux sistemu), odnosno, "DB2 - Command Window Administrator" (na Windows sistemu) i u njemu se pozicionirati u otpakovani direktorijum. 
3. Iz ove lokacije pokrenuti skript `create.sh` za Linux sistem, odnosno, `create.cmd` za Windows sistem. 

### 1.4.2 Opis baze podataka `STUD2020`

Na narednoj slici je prikazan dijagram baze podataka. Dijagram baze podataka ćemo predstaviti u notaciji nalik na UML. U svakoj tabeli su označeni atributi koji pripadaju primarnom ključu. Strani ključevi i jedinstveni ključevi su navedeni u drugom odeljku, odvojeno od atributa. Strani ključevi su ilustrovani i strelicama od zavisne tabele prema baznoj tabeli.

![Dijagram baze podataka STUD2020](/baze/stud2020/dijagram.png){:class="ui centered huge image"}

Dajmo sada ne\v sto detaljnije opise tabela koje postoje u ovoj bazi podataka. Uz tekstualni opis dajemo i strukturu tabele koju \v cine nazivi kolona i njihove karakteristike kao \v sto su: tip, veli\v cina, da li mo\v ze sadr\v zati nedostaju\'ce vrednosti itd.

- Tabela `DA.NIVOKVALIFIKACIJE` sadrži osnovne podatke o nivoima studija:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
ID                              SYSIBM    SMALLINT                     2     0 No
NAZIV                           SYSIBM    VARCHAR                    100     0 No

  2 record(s) selected.
```

- Tabela `DA.STUDIJSKIPROGRAM` sadrži osnovne podatke o studijskim programima. Svaki studijski program ima oznaku, naziv, nivo, obim u ESPB, odgovarajuće zvanje i opis:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
ID                              SYSIBM    INTEGER                      4     0 No
OZNAKA                          SYSIBM    VARCHAR                     10     0 No
NAZIV                           SYSIBM    VARCHAR                     50     0 No
IDNIVOA                         SYSIBM    SMALLINT                     2     0 No
OBIMESPB                        SYSIBM    SMALLINT                     2     0 No
ZVANJE                          SYSIBM    VARCHAR                    100     0 No
OPIS                            SYSIBM    LONG VARCHAR             32700     0 Yes

  7 record(s) selected.
```

- Tabela `DA.PREDMET` sadrži podatke o svim predmetima koji postoje na fakultetu. Svaki predmet ima skraćenu oznaku, naziv i broj ESPB koje nosi:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
ID                              SYSIBM    INTEGER                      4     0 No
OZNAKA                          SYSIBM    VARCHAR                     20     0 No
NAZIV                           SYSIBM    VARCHAR                    150     0 No
ESPB                            SYSIBM    SMALLINT                     2     0 No

  4 record(s) selected.
```

- Tabela `DA.PREDMETPROGRAMA` sadrži podatke o obaveznim i izbornim predmetima na studijskim programima.

Da bi student diplomirao, mora da položi sve obavezne predmete studijskog programa i još onoliko izbornih predmeta koliko je potrebno do ukupnog obima studijskog programa u ESPB. Radi jednostavnosti, smatraćemo da se ostali predmeti, koji nisu ni obavezni ni izborni na upisanom studijskom programu, ne računaju u savladan obim predmeta.

Ako je predmet obavezan, onda postoji i podatak o semestru u kome je uobičajeno da se sluša i polaže:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
IDPREDMETA                      SYSIBM    INTEGER                      4     0 No
IDPROGRAMA                      SYSIBM    INTEGER                      4     0 No
VRSTA                           SYSIBM    VARCHAR                     10     0 No
SEMESTAR                        SYSIBM    SMALLINT                     2     0 Yes

  4 record(s) selected.
```

- Tabela `DA.USLOVNIPREDMET` sadrži podatke o parovima uslovnhm predmeta. Uslovnosti se definišu na nivou studijskog programa. Na studijskom programu `IDPROGRAMA` va\v zi da je za polaganje predmeta `IDPREDMETA` neophodno da se prvo položi predmet `IDUSLOVNOGPREDMETA`:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
IDPROGRAMA                      SYSIBM    INTEGER                      4     0 No
IDPREDMETA                      SYSIBM    INTEGER                      4     0 No
IDUSLOVNOGPREDMETA              SYSIBM    INTEGER                      4     0 No

  3 record(s) selected.
```

- Tabela `DA.STUDENTSKISTATUS` sadrži osnovne podatke o statusima koje studenti mogu imati tokom studiranja. Status ima ID i naziv i podatak o tome da li student trenutno studira.

Atribut `STUDIRA` je 1 akko je status aktivan:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
ID                              SYSIBM    SMALLINT                     2     0 No
NAZIV                           SYSIBM    VARCHAR                     50     0 No
STUDIRA                         SYSIBM    SMALLINT                     2     0 No

  3 record(s) selected.
```

- Tabela `DA.DOSIJE` sadrži podatke o studentima. Od ličnih podataka tu su ime, prezime, mesto rođenja i pol. Od podataka o studiranju imamo broj indeksa, upisan studijski program, trenutni status, datum upisa i datum diplomiranja:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
IDPROGRAMA                      SYSIBM    INTEGER                      4     0 No
IME                             SYSIBM    VARCHAR                     50     0 No
PREZIME                         SYSIBM    VARCHAR                     50     0 No
POL                             SYSIBM    CHARACTER                    1     0 Yes
MESTORODJENJA                   SYSIBM    VARCHAR                     50     0 Yes
IDSTATUSA                       SYSIBM    SMALLINT                     2     0 No
DATUPISA                        SYSIBM    DATE                         4     0 No
DATDIPLOMIRANJA                 SYSIBM    DATE                         4     0 Yes

  9 record(s) selected.
```

- Tabela `DA.DOSIJEEXT` sadrži dodatne opisne podatke o studentima. Namenjena je za rad sa velikim i strukturiranim podacima tipa `BLOB`, `CLOB` i `XML`:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
FOTOGRAFIJA                     SYSIBM    BLOB                  10485760     0 Yes
PODACICLOB                      SYSIBM    CLOB                  10485760     0 Yes
PODACIXML                       SYSIBM    XML                          0     0 Yes

  4 record(s) selected.
```

- Tabela `DA.SKOLSKAGODINA` sadrži podatke o školskim godinama:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
DATPOCETKA                      SYSIBM    DATE                         4     0 No
DATKRAJA                        SYSIBM    DATE                         4     0 No

  3 record(s) selected.
```

- Tabela `DA.UPISGODINE` sadrži podatke o upisanim školskim godinama. Svaki upis godine sadrži podatke o tome koji student je koju školsku godinu upisao kog dana i u kom statusu. Status može i naknadno da se promeni, na primer zbog mirovanja:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
DATUPISA                        SYSIBM    DATE                         4     0 No
IDSTATUSA                       SYSIBM    SMALLINT                     2     0 No

  4 record(s) selected.
```

- Tabela `DA.SEMESTAR` sadrži podatke o semestrima:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
SEMESTAR                        SYSIBM    SMALLINT                     2     0 No

  2 record(s) selected.
```

- Tabela `DA.KURS` sadrži podatke o kursevima. Kurs je jedno držanje nastave iz jednog predmeta. Za svaki kurs se vodi koji predmet se drži u kom semestru koje školske godine. Jedan predmet može da se drži u oba semestra jedne školske godine:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
SEMESTAR                        SYSIBM    SMALLINT                     2     0 No
IDPREDMETA                      SYSIBM    INTEGER                      4     0 No

  3 record(s) selected.
```

- Tabela `DA.UPISANKURS` sadrži podatke o kursevima koje studenti upisuju u upisanim školskim godinama. Postoje podaci o tome koji student je u kojoj skolskoj godini i kom semestru upisao koji predmet. Jedan student ne može da upiše dva kursa iz istog predmeta u jednoj školskoj godini:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
SEMESTAR                        SYSIBM    SMALLINT                     2     0 No
IDPREDMETA                      SYSIBM    INTEGER                      4     0 No

  4 record(s) selected.
```

- Tabela `DA.ISPITNIROK` sadrži podatke o ispitnim rokovima. Za svaki ispitni rok imamo oznaku i naziv, datume početka i kraja, kao i školsku godinu na koju se odnosi:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
OZNAKAROKA                      SYSIBM    VARCHAR                     20     0 No
NAZIV                           SYSIBM    VARCHAR                     30     0 No
DATPOCETKA                      SYSIBM    DATE                         4     0 No
DATKRAJA                        SYSIBM    DATE                         4     0 No

  5 record(s) selected.
```

- Tabela `DA.ISPIT` sadrži podatke o prijavljenim i održanim ispitima. Za svaki prijavljen i/ili položen ispit se vode podaci o školskoj godini, oznaci roka, predmetu, indeksu studenta, statusu prijave, datumu polaganja, broju osvojenih poena (0-100) i oceni (5-10). Status prijave može da bude:

    - p - ispit je prijavljen i još nije održan (broj poena i ocena nisu uneseni);
    - n - student nije izašao (broj poena i ocena nisu uneseni);
    - o - student je polagao ispit (broj poena i ocena su uneseni);
    - d - student je diskvalifikovan (broj poena i ocena su uneseni, ocena=5);
    - s - student je odustao (broj poena i ocena su uneseni, ocena=5);
    - x - ispit je poništen (broj poena i ocena su uneseni).

Samo redovi sa statusom "o" i pozitivnom ocenom označavaju da je ispit položen:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
SKGODINA                        SYSIBM    SMALLINT                     2     0 No
OZNAKAROKA                      SYSIBM    VARCHAR                     20     0 No
INDEKS                          SYSIBM    INTEGER                      4     0 No
IDPREDMETA                      SYSIBM    INTEGER                      4     0 No
STATUS                          SYSIBM    CHARACTER                    1     0 No
DATPOLAGANJA                    SYSIBM    DATE                         4     0 Yes
POENI                           SYSIBM    SMALLINT                     2     0 Yes
OCENA                           SYSIBM    SMALLINT                     2     0 Yes

  8 record(s) selected.
```

- Tabela `DA.PRIZNATISPIT` sadrži podatke o ispitima koji su studentima priznati. Ispiti koji se priznaju su položeni na drugom fakultetu ili na drugom (možda starijem) studijskom programu. Radi jednostavnosti, ne čuvaju se detaljni podaci o položenim prdmetima već samo oni koji su neophodni za njihovo vrednovanje: naziv, obim u ESPB i ocena. Svi priznati ispit se vrednuju kao da su na upisanom studijskom programu, a mogu imati nedefinisanu ocenu, ako se predmet nije ocenjivao:

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
NAZIVPREDMETA                   SYSIBM    VARCHAR                    200     0 No
ESPB                            SYSIBM    SMALLINT                     2     0 No
OCENA                           SYSIBM    SMALLINT                     2     0 Yes

  4 record(s) selected.
```

## 1.5 Alat `db2`

Prilikom instalacije Db2 SUBP, na raspolaganju nam je alat komandne linije `db2` kojim mo\v zemo izvr\v savati razne operacije sa bazama podataka. Ilustrujmo neke elementarne naredbe ovog alata pri radu sa bazom podataka `STUD2020`.

Pre nego \v sto izvr\v simo bilo kakvu naredbu, neophodno je da napravimo konekciju ka bazi podataka:

```shell
db2 "CONNECT TO STUD2020"
```

```
   Database Connection Information

 Database server        = DB2/NT64 11.5.1.1
 SQL authorization ID   = DB2INST1
 Local database alias   = STUD2020
```

Ukoliko ne navedemo dodatne argumente, `db2` alat \'ce se na bazu podataka povezati sa korisni\v ckim imenom i lozinkom trenutno prijavljenog korisnika na sistemu. Mogu\'ce je specifikovati konkretnog korisnika sa kojim \'ce se kreirati konekcija:

```shell
db2 "CONNECT TO STUD2020 USER student"
```

```
Enter current password for student: ******

   Database Connection Information

 Database server        = DB2/NT64 11.5.1.1
 SQL authorization ID   = STUDENT
 Local database alias   = STUD2020
```

Kao \v sto vidimo, alat od nas zahteva da unesemo lozinku pre prijavljivanja. Kona\v cno, mo\v zemo specifikovati lozinku prilikom povezivanja:

```shell
db2 "CONNECT TO STUD2020 USER student USING abcdef"
```

Narednom naredbom mo\v zemo prikazati sve tabele u odgovaraju\'coj shemi:

```shell
db2 "LIST TABLES FOR SCHEMA DA"
```

```
Table/View                      Schema          Type  Creation time
------------------------------- --------------- ----- --------------------------
DOSIJE                          DA              T     2020-12-24-17.57.25.149001
DOSIJEEXT                       DA              T     2020-12-24-17.57.25.204001
ISPIT                           DA              T     2020-12-24-17.57.25.827001
ISPITNIROK                      DA              T     2020-12-24-17.57.25.775001
KURS                            DA              T     2020-12-24-17.57.25.473001
NIVOKVALIFIKACIJE               DA              T     2020-12-24-17.57.24.534001
PREDMET                         DA              T     2020-12-24-17.57.25.005001
PREDMETPROGRAMA                 DA              T     2020-12-24-17.57.25.036000
PRIZNATISPIT                    DA              T     2020-12-24-17.57.25.387001
SEMESTAR                        DA              T     2020-12-24-17.57.25.442001
SKOLSKAGODINA                   DA              T     2020-12-24-17.57.25.417001
STUDENTSKISTATUS                DA              T     2020-12-24-17.57.25.121001
STUDIJSKIPROGRAM                DA              T     2020-12-24-17.57.24.875001
UPISANKURS                      DA              T     2020-12-24-17.57.25.692001
UPISGODINE                      DA              T     2020-12-24-17.57.25.529001
USLOVNIPREDMET                  DA              T     2020-12-24-17.57.25.091001

  16 record(s) selected.
```

Izlistavanje informacija o nekoj tabeli, na primer, `DA.DOSIJE`, dobijamo pozivom naredbe:

```shell
db2 "DESCRIBE TABLE DA.DOSIJE"
```

```
                                Data type                     Column
Column name                     schema    Data type name      Length     Scale Nulls
------------------------------- --------- ------------------- ---------- ----- ------
INDEKS                          SYSIBM    INTEGER                      4     0 No
IDPROGRAMA                      SYSIBM    INTEGER                      4     0 No
IME                             SYSIBM    VARCHAR                     50     0 No
PREZIME                         SYSIBM    VARCHAR                     50     0 No
POL                             SYSIBM    CHARACTER                    1     0 Yes
MESTORODJENJA                   SYSIBM    VARCHAR                     50     0 Yes
IDSTATUSA                       SYSIBM    SMALLINT                     2     0 No
DATUPISA                        SYSIBM    DATE                         4     0 No
DATDIPLOMIRANJA                 SYSIBM    DATE                         4     0 Yes

  9 record(s) selected.
```

Alatom `db2` mo\v zemo izvr\v savati proizvoljne SQL naredbe, na primer, upite:

```shell
db2 "SELECT * FROM DA.NIVOKVALIFIKACIJE"
```

```
ID     NAZIV
------ ----------------------------------------------------------------------------------------------------
     1 Osnovne akademske studije
     3 Doktorske akademske studije
     2 Master akademske studije

  3 record(s) selected.
```

SQL naredbe je mogu\'ce izvr\v savati i iz datoteka. Na primer, neka je u radnom direktorijumu na raspolaganju datoteka `upit.sql` sa narednim sadr\v zajem:

```sql
-- Datoteka "upit.sql": 
SELECT      SKGODINA,
            OZNAKAROKA,
            IDPREDMETA,
            SUM(
                CASE 
                    WHEN OCENA > 5 AND STATUS='o' THEN 1.0 
                    ELSE 0.0 
                END
            ) / COUNT(*) * 100.0 AS USPESNOST
FROM        DA.ISPIT I
GROUP BY    SKGODINA,
            OZNAKAROKA,
            IDPREDMETA
ORDER BY    IDPREDMETA ASC,
            SKGODINA ASC,
            OZNAKAROKA DESC
FETCH       FIRST 10 ROWS ONLY;
```

Izvr\v savanje ovog SQL upita se mo\v ze izvr\v siti naredbom:

```shell
db2 -t -f upit.sql
```

```
SKGODINA OZNAKAROKA           IDPREDMETA  USPESNOST
-------- -------------------- ----------- ---------------------------------
    2015 sep1                        1578                             10.00
    2015 jun1                        1578                             40.00
    2015 jan2                        1578                             10.00
    2015 jan1                        1578                             80.00
    2016 sep1                        1578                             20.00
    2016 jun1                        1578                             50.00
    2016 jan2                        1578                             10.00
    2016 jan1                        1578                             60.00
    2017 sep1                        1578                             20.00
    2017 jun1                        1578                             40.00

  10 record(s) selected.
```

Opcija:

- `-t` defini\v se da se krajem jedne SQL naredbe smatra karakter `;` umesto novog reda;
- `-f DATOTEKA` defini\v se da se SQL naredba pro\v cita iz datoteke `DATOTEKA`;
- `-v` ispisuje SQL naredbu koja se izvr\v sava pre nego \v sto se ispi\v se rezultat.

Ukoliko SQL naredba proizvede gre\v sku, ta gre\v ska \'ce biti prikazana u konzoli. Na primer:

```shell
db2 "INSERT INTO DA.SKOLSKAGODINA VALUES (2020, '09/23/2019', '05/18/2020')"
```

```
DB21034E  The command was processed as an SQL statement because it was not a
valid Command Line Processor command.  During SQL processing it returned:
SQL0803N  One or more values in the INSERT statement, UPDATE statement, or
foreign key update caused by a DELETE statement are not valid because the
primary key, unique constraint or unique index identified by "1" constrains
table "DA.SKOLSKAGODINA" from having duplicate values for the index key.
SQLSTATE=23505
```

U ovom slu\v caju je prijavljena gre\v ska `SQL0803N`, odnosno, gre\v ska \v ciji je kod `-803`. Detaljne informacije o tome \v sta uzrokuje neku gre\v sku mo\v zemo dobiti izvr\v savanjem naredbe:

```shell
db2 "? SQL-803"
```

```
SQL0803N  One or more values in the INSERT statement, UPDATE statement,
      or foreign key update caused by a DELETE statement are not valid
      because the primary key, unique constraint or unique index
      identified by "<index-id>" constrains table "<table-name>" from
      having duplicate values for the index key.

Explanation:

The INSERT or UPDATE object table "<table-name>" is
constrained by one or more UNIQUE indexes to have unique values in
certain columns or groups of columns. Alternatively, a DELETE statement
on a parent table caused the update of a foreign key in a dependent
table "<table-name>" that is constrained by one or more UNIQUE
indexes. Unique indexes might support primary keys or unique constraints
defined on a table. The statement cannot be processed because completing
the requested INSERT, UPDATE or DELETE statement would result in
duplicate column values. If the index is on an XML column, the duplicate
values for the index key may be generated from within a single XML
document.

...

sqlcode: -803

sqlstate: 23505


   Related information:
   Troubleshooting data source connection errors
```

Izlaz iz konzole iznad je skra\'cen radi \v cuvanja prostora.

Na kraju, kada smo zavr\v sili rad sa bazom podataka, potrebno je da raskinemo konekciju:

```shell
db2 "CONNECT RESET"
```

```
DB20000I  The SQL command completed successfully.
```
