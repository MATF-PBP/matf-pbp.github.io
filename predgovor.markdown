---
permalink: /predgovor/
layout: page
title: Predgovor
---

Ovaj tekst predstavlja literaturu za časove vežbi za dva kursa "Programiranje baza podataka" na Matematičkom fakultetu Univerziteta u Beogradu - prvi je izborni kurs na 3. i 4. godini smera Informatika, a drugi je obavezni kurs na 4. godini smera Računarstvo i Informatika. Knjiga je dostupna u celosti, besplatno, na zvaničnom sajtu koji se može pronaći na adresi [https://matf-pbp.github.io/](https://matf-pbp.github.io/).

Knjiga je koncipirana na osnovu različitih materijala sa časova vežbi koje su držale mnogobrojne kolege sa Katedre za računarstvo i informatiku Matematičkog fakulteta: [Nina Radojičić Matić](http://www.math.rs/~nina), [Ana Vulović](http://www.math.rs/~aspasic), [Vesna Marinković](http://www.math.rs/~vesnam) i [Saša Malkov](http://www.math.rs/~smalkov), uz razne dopune, ispravke i poboljšanja u materijalima od strane autora [Nikole Ajzenhamera](https://matf.nikolaajzenhamer.rs), [Anje Bukurov](http://www.math.rs/~anja_bukurov) i [Ana Vulović](http://www.math.rs/~aspasic). Knjiga je prateći materijal pre svega studentima koji ovaj kurs slušaju u okviru svojih studija, ali i svima Vama koji biste želeli da se upoznate sa ovom tematikom.

Ova knjiga je podeljena u 5 delova:

- U prvom delu uvodimo čitaoca u pojam programiranja baza podataka koristeći Db2 sistem za upravljanje bazama podataka. Ovaj deo se sastoji od jednog poglavlja u kojem je dat kratak prikaz arhitekture klijent-server na koju se oslanjaju tehnologije u narednim delovima knjige. Takođe, poglavlje 1 sadrži instrukcije za podešavanje operativnog sistema za uspešan razvoj aplikacija. 

- Drugi deo započinjemo osnovnim konceptima za programiranje C/SQL klijentskih aplikacija. Uvodno poglavlje 2 diskutuje o nekim važnim razlikama između C/SQL aplikacija koji imaju ugnežđene statičke, odnosno, dinamičke SQL naredbe. Takođe, u ovom poglavlju demonstriramo proces prevođenja i izvršavanja jednog jednostavnog C/SQL programa. Ovo poglavlje završavamo elementarnim konceptima važnim za kreiranje C/SQL klijentskih aplikacija. Poglavlje 3 uvodi koncept kursora i demonstrira rad sa njima. Poglavlje 4 diskutuje o ispravnosti implementacija transakcionog rada. U poglavlju 5 govorimo o razvoju aplikacija pod veoma realnom pretpostavkom da se aplikacije izvršavaju konkurentno, odnosno, da više korisnika koristi podatke iz iste baze podataka u isto vreme. Poglavlje 6 se tiče se programiranja C/SQL aplikacija u kojima se SQL naredbe izvršavaju dinamički. Poslednje poglavlje u ovom delu, poglavlje 7, govori o povezivanju klijentske aplikacije na više baza podataka.

- Treći deo knjige predstavlja pandan drugom delu, sa razlikom da se koristi programski jezik Java za programiranje klijentskih aplikacija. Poglavlje 8 diskutuje o osnovnim konceptima za konstrukciju Java/SQL aplikacija u kojem se naredbe izvršavaju dinamički. U poglavlju 9 dajemo tehnike za razvoj naprednijih koncepata Java/SQL aplikacija, odnosno, govorimo o transakcionom radu, radu u višekorisničkom okruženju i radu sa više baza podataka.

- Četvrti deo knjige je posvećen razvojnom okruženju Hibernate koji koristi programski jezik Java da bi izvršio objektno-relaciono preslikavanje. Drugim rečima, u pitanju je alat kojim možemo razvijati programe tako da podatke menjamo u memoriji aplikacije, a zatim da se te izmene u memoriji aplikacije oslikavaju u bazi podataka.

- U petom delu knjige predstavljamo Db2 implementaciju standarda SQL/PSM kroz jezik SQL/PL. U pitanju je proširenje SQL standarda za konstruisanje SQL rutina na serverskoj strani arhitekture. U ovom delu diskutujemo o kreiranju ugrađenih procedura, korisnički-definisana funkcija i okidača. Takođe, prikazujemo i načine na koje je moguće pozivati i izvršavati SQL rutine.

Nakon što smo opisali detaljnije sadržaj knjige, trebalo bi da postavimo odgovarajuće pretpostavke pred čitaoce. U ovoj knjizi nisu obrađene naredne teme, te se podrazumeva njihovo poznavanje:

- Programiranje C aplikacija
- Programiranje Java aplikacija
- SQL naredbe za izdvajanje, unošenje, menjanje i brisanje informacija iz relacionih baza podataka.

Ovaj materijal ne može zameniti pohađanje vežbi niti drugu preporučenu literaturu. Ovaj tekst je u ranoj fazi formiranja i nije recenziran. Ukoliko ste pažljivi čitalac ove knjige, i ukoliko uočite bilo kakvu grešku ili propust, možete otvoriti primedbu na [zvaničnom GitHub repozitorijumu kursa](https://github.com/MATF-PBP/matf-pbp.github.io). 

Autori