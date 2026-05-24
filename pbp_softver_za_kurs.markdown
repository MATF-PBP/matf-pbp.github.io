---
layout: notification
title: Softver za kurs Programiranje baza podataka
---

## Spisak alata

Za praktični deo kursa Programiranje baza podataka koristi se:
- sistem IBM Db2 za upravljanje bazama podataka;
- alat IBM Data Studio za rad sa bazama podataka;
- studentska baza STUD2020;
- biblioteka Hibernate ORM 5.4: <https://hibernate.org/orm/>.

## Baza podataka STUD2020

Za bolje upoznavanje sa bazom podataka STUD2020 pogledajte opis na adresi
<https://matf-pbp.github.io/vezbe/poglavlja/1/#14-baza-podataka-stud2020>.

Skriptovi za pravljenje baze podataka mogu se preuzeti sa linka
<http://www.matf.bg.ac.rs/~mirjana/stud2020.zip>.

Baza podataka STUD2020 sadrži dve sheme:
- shemu `DA`, u kojoj su podaci zapisani ASCII kodnom šemom;
- shemu `DB`, u kojoj su podaci zapisani UTF-8 kodnom šemom.

## Virtualna mašina BazePodataka2020

Napravljena je virtualna mašina BazePodataka2020 za potrebe kursa Relacione baze podataka i Programiranje baza podataka.
Na VM je instalirana verzija IBM Db2 11.5 i Data Studio 4.1.3.

Podaci za studentski nalog su:
- korisnik: `student`;
- lozinka: `abcdef`.

Lokacije za preuzimanje VM:
- <http://enastava.matf.bg.ac.rs/~mirjana/BazePodataka2020_nova.zip>
(pročitati `uputstvo.txt`)

Na VM, ako želite da obrišete postojeću i napravite novu (čistu) bazu podataka STUD2020, potrebno je da se u terminalu pozicionirate u direktorijum:
```
~/Desktop/materijali/baze
```
i odatle pokrenete skript `./create.sh` (što traje nekoliko minuta).

Na VM je nakon logovanja uvek potrebno pokrenuti Db2 SUBP pozivom `db2start` u terminalu, a pre izvršavanja bilo kojih drugih SQL naredbi.

Komande za Db2 se na OS Linux izvršavaju u terminalu, a na OS Windows u DB2 Command Window (ili Command Prompt, pa otkucajte “db2cmd”, ali moraju biti podešene odgovarajuće promenljive okruženja, kao što je PATH da bi mogle biti pronađene odgovarajuće Db2 naredbe).
Instrukcije za pravljenje studentske baze podataka na Vašim host operativnim sistemima:

- nakon preuzimanja i raspakivanja materijala za baze podataka (videti iznad), pozicionirajte se preko terminala ili `db2cmd` u raspakovani direktorijum `stud2020`
- na OS Linux pokrenite skript `create.sh`
- na OS Windows pokrenite `create.cmd`

Napomena: Ako koristite stariju verziju VMware plejera i dobijate grešku pri pokretanju VM, potrebno je da otvorite .vmx datoteku i promenite vrednost za virtualHW.version na Vašu verziju plejera, npr. ako je 15 postavite:
```
virtualHW.version = “15”
```

## Lokalna instalacija (umesto VM)

Ukoliko ne želite da koristite VM, sa IBM sajta možete preuzeti IBM Db2 Community Edition, kao i alat Data Studio.
Preporuka je da napravite nalog na IBM sajtu korišćenjem studentske adrese.
Detaljnije informacije o instalaciji potrebnog softvera možete pronaći na adresi
<https://matf-pbp.github.io/vezbe/poglavlja/1/#13-podešavanje-okruženja-za-rad>.

