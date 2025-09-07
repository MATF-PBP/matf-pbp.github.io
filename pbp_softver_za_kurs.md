# Spisak alata

Za praktičan deo kursa Programiranje baze podataka koristi se:

* IBM Db2 sistem za upravljanje bazama podataka  
* IBM Data Studio alat za rad sa bazama podataka  
* studentska baza STUD2020   
* biblioteka Hibernate ORM 5.4: [https://hibernate.org/orm/](https://hibernate.org/orm/) 

# Baza podataka STUD2020

Za bolje upoznavanje sa bazom podataka STUD2020 pogledajte opis na adresi [https://matf-pbp.github.io/vezbe/poglavlja/1/\#14-baza-podataka-stud2020](https://matf-pbp.github.io/vezbe/poglavlja/1/#14-baza-podataka-stud2020).

Skriptovi za pravljenje baze podataka mogu se preuzeti sa linka [http://www.matf.bg.ac.rs/\~mirjana/stud2020.zip](http://www.matf.bg.ac.rs/~mirjana/stud2020.zip) 

Baza podataka STUD2020 sadrži dve sheme:

* **DA** u kojoj su podaci zapisani ASCII kodnom šemom  
* **DB** u kojoj su podaci zapisani UTF-8 kodnom šemom.

# Virtualna mašina BazePodataka2020

Napravljena je virtualna mašina BazePodataka2020 za potrebe kursa Relacione baze podataka i Programiranje baza podataka. Na VM je instalirana verzija IBM Db2 11.5 i Data Studio 4.1.3. 

Podaci za studentski nalog su:

* korisnik: **student**  
* lozinka: **abcdef**

   
Lokacije za preuzimanje VM: 

* [https://1drv.ms/u/s\!Agf67w2RxBDSgdpS2uiKbVfMkGBhCw?e=rYbr1X](https://1drv.ms/u/s!Agf67w2RxBDSgdpS2uiKbVfMkGBhCw?e=rYbr1X) (pročitati **uputstvo.txt**) 

Na VM, ako želite da obrišete postojeću i napravite novu (čistu) bazu podataka STUD2020, potrebno je da se u terminalu pozicionirate u direktorijum "\~/Desktop/materijali/baze" i odatle pokrenete skript "./create.sh" (što traje nekoliko minuta).

Na VM je nakon logovanja uvek potrebno pokrenuti Db2 SUBP pozivom "db2start" u terminalu, a pre izvršavanja bilo kojih drugih SQL naredbi.

Komande za Db2 se na OS Linux izvršavaju u terminalu, a na OS Windows u DB2 Command Window (ili Command Prompt, pa otkucajte “db2cmd”, ali moraju biti podešene odgovarajuće promenljive okruženja, kao što je PATH da bi mogle biti pronađene odgovarajuće Db2 naredbe). Instrukcije za pravljenje studentske baze podataka na Vašim host operativnim sistemima:

* nakon preuzimanja i raspakivanja materijala za baze podataka (videti iznad), pozicionirajte se preko terminala ili db2cmd u raspakovani direktorijum stud2020  
* na OS Linux pokrenite skript “./create.sh”  
* na OS Windows pokrenite “create.cmd”

Napomena: Ako koristite stariju verziju VMware plejera i dobijate grešku pri pokretanju VM, potrebno je da otvorite .vmx datoteku i promenite vrednost za virtualHW.version na Vašu verziju plejera, npr. ako je 15 postavite

virtualHW.version \= "15"

# Lokalna instalacija (umesto VM)

Ukoliko ne želite da koristite VM, sa IBM sajta možete preuzeti IBM Db2 Community Edition, kao i alat Data Studio. Preporuka je da napravite nalog na IBM sajtu korišćenjem studentske adrese. Detaljnije informacije o instalaciji potrebnog softvera možete pronaći na adresi [https://matf-pbp.github.io/vezbe/poglavlja/1/\#13-podešavanje-okruženja-za-rad](https://matf-pbp.github.io/vezbe/poglavlja/1/#13-podešavanje-okruženja-za-rad). 

# Korišćenje Docker kontejnera (umesto VM)

Zvanična dokumentacija: [https://hub.docker.com/r/ibmcom/db2](https://hub.docker.com/r/ibmcom/db2) 

## Instalacija

* Instalirati alat Docker: [https://docs.docker.com/get-docker/](https://docs.docker.com/get-docker/)   
* Iz terminala pokrenuti sledeću naredbu:

  **docker volume create bazepodatakavolume**

* Iz terminala pokrenuti sledeću naredbu:

  **docker run \-itd \--name bazepodataka \--privileged=true \-p 50000:50000 \-e LICENSE=accept \-e DB2INSTANCE=student \-e DB2INST1\_PASSWORD=abcdef \-e DBNAME=stud2020 \-v bazepodatakavolume:/database ibmcom/db2**

* Kada se preuzme Docker slika, pokrenuti narednu naredbu da biste videli logove iz kontejnera:

  **docker logs \-f bazepodataka**

  U trenutku pokretanja ćete najverovatnije dobiti informaciju kako se kreira baza podataka STUD2020. Sačekajte nekoliko minuta dok se u logu ne pojavi poruka “Setup has completed” (verovatno neće biti poslednja linija u logovima). Kada se poruka ipak pojavi, pritisnite CTRL \+ C da prekinete gledanje logova.

Sada je Docker kontejner spreman za rad. 

## Podešavanje BP

Proverite da li je kontejner pokrenut naredbom: 

**docker ps**

Ako ne vidite kontejner, onda je potrebno da ga pokrenete naredbom:

**docker start bazepodataka**

Sada možemo pristupiti terminalu iz kontejnera naredbom:

**docker exec \-ti bazepodataka bash \-c "su \- ${DB2INSTANCE}"**

Da biste preuzeli i instalirali BP STUD2020 u kontejneru, pokrenite naredne naredbe:

**pwd**  
**wget [http://www.matf.bg.ac.rs/\~mirjana/stud2020.zip](http://www.matf.bg.ac.rs/~mirjana/stud2020.zip)**  
**unzip stud2020.zip**  
**cd stud2020**  
**chmod \+x create.sh**  
**chmod \+x Stud2020\_Ascii/create.sh**  
**chmod \+x Stud2020\_utf8/create.sh**  
**./create.sh**

Prva naredba je tu samo da bi prikazala direktorijum u kojem se nalazite.

Zatvaranje terminala se vrši naredbom: 

**exit**

Gašenje kontejnera se vrši naredbom: 

**docker stop bazepodataka**

## Pokretanje C/SQL primera u kontejneru

Proverite da li je kontejner pokrenut naredbom: 

**docker ps**

Ako ne vidite kontejner, onda je potrebno da ga pokrenete naredbom:

**docker start bazepodataka**

Sada možemo pristupiti terminalu iz kontejnera naredbom:

**docker exec \-ti bazepodataka bash \-c "su \- ${DB2INSTANCE}"**

Da biste preuzeli C/SQL primere, pokrenite naredne naredbe:

**wget [https://matf-pbp.github.io/resources/primeri.tar.gz](https://matf-pbp.github.io/resources/primeri.tar.gz)**  
**tar \-xf primeri.tar.gz**  
**cd vezve/primeri**  
**ls**

U ovom direktorijumu se nalaze svi primeri. Prevođenje, na primer, zadatka 2.1 se može izvršiti na sledeći način:

**cd poglavlje\_2**  
**chmod \+x prevodjenje**  
**./prevodjenje zadatak\_2\_1 stud2020 student abcdef**

## Pokretanje Java/JDBC primera u kontejneru

TODO

## Pokretanje Hibernate primera u kontejneru

TODO