# Za prevodjenje svih programa pozvati: make all
# Za porevodjenje konkretnog programa (na primer, zadatak_6_1.sqc) pozvati: make zadatak_6_1
# Za ciscenje artefakta pozvati: make clean

all: zadatak_6_1 zadatak_6_2 zadatak_6_3 zadatak_6_4 zadatak_6_5 zadatak_6_6 zadatak_6_7 zadatak_6_8
	make -B zadatak_6_4
	make -B zadatak_6_5
	make -B zadatak_6_6
	make -B zadatak_6_7
	make -B zadatak_6_8

zadatak_6_1: zadatak_6_1.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_6_2: zadatak_6_2.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_6_3: zadatak_6_3.sqc
	./prevodjenje $@ stud2020 student abcdef

zadatak_6_4: 
	cd zadatak_6_4 && make db && make && cd ..

zadatak_6_5: 
	cd zadatak_6_5 && make db && make && cd ..

zadatak_6_6: 
	cd zadatak_6_6 && make db && make && cd ..

zadatak_6_7: 
	cd zadatak_6_7 && make db && make && cd ..
	
zadatak_6_8: 
	cd zadatak_6_8 && make && cd ..

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f zadatak_6_1
	rm -f zadatak_6_2
	rm -f zadatak_6_3
	cd zadatak_6_4 && make clean && cd ..
	cd zadatak_6_5 && make clean && cd ..
	cd zadatak_6_6 && make clean && cd ..
	cd zadatak_6_7 && make clean && cd ..
	cd zadatak_6_8 && make clean && cd ..
