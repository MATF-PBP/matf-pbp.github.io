# Za prevodjenje svih programa pozvati: make all
# Za porevodjenje konkretnog programa (na primer, zadatak_2_1.sqc) pozvati: make zadatak_2_1
# Za ciscenje artefakta pozvati: make clean

all: zadatak_2_1 zadatak_2_2 zadatak_2_3 zadatak_2_4

zadatak_2_1: zadatak_2_1.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_2_2: zadatak_2_2.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_2_3: zadatak_2_3.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_2_4: zadatak_2_4.sqc
	./prevodjenje $@ stud2020 student abcdef

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f zadatak_2_1
	rm -f zadatak_2_2
	rm -f zadatak_2_3
	rm -f zadatak_2_4
