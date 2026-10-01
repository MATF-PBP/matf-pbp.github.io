# Za prevodjenje svih programa pozvati: make all
# Za porevodjenje konkretnog programa (na primer, zadatak_5_1.sqc) pozvati: make zadatak_5_1
# Za ciscenje artefakta pozvati: make clean

all: zadatak_5_1 zadatak_5_2 zadatak_5_3 zadatak_5_4 zadatak_5_5 zadatak_5_6 zadatak_5_7

zadatak_5_1: zadatak_5_1.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_2: zadatak_5_2.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_3: zadatak_5_3.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_4: zadatak_5_4.sqc zadatak_5_4.sql
	db2 -tf $@.sql || true
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_5: zadatak_5_5.sqc zadatak_5_5.sql
	db2 -tf $@.sql || true
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_6: zadatak_5_6.sqc zadatak_5_6.sql
	db2 -tf $@.sql || true
	./prevodjenje $@ stud2020 student abcdef

zadatak_5_7: zadatak_5_7.sqc zadatak_5_7.sql
	db2 -tf $@.sql || true
	./prevodjenje $@ stud2020 student abcdef

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f zadatak_5_1
	rm -f zadatak_5_2
	rm -f zadatak_5_3
	rm -f zadatak_5_4
	rm -f zadatak_5_5
	rm -f zadatak_5_6
	rm -f zadatak_5_7
