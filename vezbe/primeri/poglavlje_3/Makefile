# Za prevodjenje svih programa pozvati: make all
# Za porevodjenje konkretnog programa (na primer, zadatak_3_1.sqc) pozvati: make zadatak_3_1
# Za ciscenje artefakta pozvati: make clean

all: zadatak_3_1 zadatak_3_2 zadatak_3_3 zadatak_3_4 zadatak_3_5 zadatak_3_6

zadatak_3_1: zadatak_3_1.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_3_2: zadatak_3_2.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_3_3: zadatak_3_3.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_3_4: zadatak_3_4.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_3_5: zadatak_3_5.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_3_6: zadatak_3_6.sqc
	./prevodjenje $@ stud2020 student abcdef

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f zadatak_3_1
	rm -f zadatak_3_2
	rm -f zadatak_3_3
	rm -f zadatak_3_4
	rm -f zadatak_3_5
	rm -f zadatak_3_6
