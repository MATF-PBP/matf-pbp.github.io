# Za prevodjenje svih programa pozvati: make all
# Za porevodjenje konkretnog programa (na primer, zadatak_4_1.sqc) pozvati: make zadatak_4_1
# Za ciscenje artefakta pozvati: make clean

all: zadatak_4_1 zadatak_4_2 zadatak_4_3 zadatak_4_4

zadatak_4_1: zadatak_4_1.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_4_2: zadatak_4_2.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_4_3: zadatak_4_3.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_4_4: zadatak_4_4.sqc
	./prevodjenje $@ stud2020 student abcdef

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f zadatak_4_1
	rm -f zadatak_4_2
	rm -f zadatak_4_3
	rm -f zadatak_4_4
