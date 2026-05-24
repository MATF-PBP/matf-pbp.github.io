---
layout: notification
title: Softver za kurs Programiranje baza podataka (ostalo)
---

## Korišćenje Docker kontejnera (umesto VM)

Zvanična dokumentacija: <https://hub.docker.com/r/ibmcom/db2>

### Instalacija

- Instalirati alat Docker: <https://docs.docker.com/get-docker/>
- Iz terminala pokrenuti sledeću naredbu:
```
docker volume create bazepodatakavolume
```
- Iz terminala pokrenuti sledeću naredbu:
```
docker run -itd --name bazepodataka --privileged=true -p 50000:50000 -e LICENSE=accept -e DB2INSTANCE=student -e DB2INST1_PASSWORD=abcdef -e DBNAME=stud2020 -v bazepodatakavolume:/database ibmcom/db2
```
- Kada se preuzme Docker slika, pokrenuti narednu naredbu da biste videli logove iz kontejnera:
```
docker logs -f bazepodataka
```
U trenutku pokretanja ćete najverovatnije dobiti informaciju kako se kreira baza podataka STUD2020.
Sačekajte nekoliko minuta dok se u logu ne pojavi poruka “Setup has completed” (verovatno neće biti poslednja linija u logovima).
Kada se poruka ipak pojavi, pritisnite CTRL + C da prekinete gledanje logova.

Sada je Docker kontejner spreman za rad.

### Podešavanje BP

Proverite da li je kontejner pokrenut naredbom:
```
docker ps
```

Ako ne vidite kontejner, onda je potrebno da ga pokrenete naredbom:
```
docker start bazepodataka
```

Sada možemo pristupiti terminalu iz kontejnera naredbom:
```
docker exec -ti bazepodataka bash -c “su - ${DB2INSTANCE}”
```

Da biste preuzeli i instalirali BP STUD2020 u kontejneru, pokrenite naredne naredbe:
```
pwd
wget http://www.matf.bg.ac.rs/~mirjana/stud2020.zip
unzip stud2020.zip
cd stud2020
chmod +x create.sh
chmod +x Stud2020_Ascii/create.sh
chmod +x Stud2020_utf8/create.sh
./create.sh
```
Prva naredba je tu samo da bi prikazala direktorijum u kojem se nalazite.

Zatvaranje terminala se vrši naredbom:
```
exit
```
Gašenje kontejnera se vrši naredbom:
```
docker stop bazepodataka
```

### Pokretanje C/SQL primera u kontejneru

Proverite da li je kontejner pokrenut naredbom:
```
docker ps
```

Ako ne vidite kontejner, onda je potrebno da ga pokrenete naredbom:
```
docker start bazepodataka
```

Sada možemo pristupiti terminalu iz kontejnera naredbom:
```
docker exec -ti bazepodataka bash -c “su - ${DB2INSTANCE}”
```

Da biste preuzeli C/SQL primere, pokrenite naredne naredbe:
```
wget https://matf-pbp.github.io/resources/primeri.tar.gz
tar -xf primeri.tar.gz
cd vezve/primeri
ls
```

U ovom direktorijumu se nalaze svi primeri. Prevođenje, na primer, zadatka 2.1 se može izvršiti na sledeći način:
```
cd poglavlje_2
chmod +x prevodjenje
./prevodjenje zadatak_2_1 stud2020 student abcdef
```

### Pokretanje Java/JDBC primera u kontejneru

TODO

### Pokretanje Hibernate primera u kontejneru

TODO
