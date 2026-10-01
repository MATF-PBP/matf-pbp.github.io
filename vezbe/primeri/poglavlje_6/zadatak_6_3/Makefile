# Za prevodjenje programa pozvati: make
# Za pripremu baze pozvati: make db
# Za ciscenje artefakta pozvati: make clean

repeatableRead: repeatableRead.sqc insertIspitniRok
	./prevodjenje repeatableRead stud2020 student abcdef

insertIspitniRok: insertIspitniRok.sqc
	./prevodjenje insertIspitniRok stud2020 student abcdef

.PHONY: clean db

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f repeatableRead
	rm -f insertIspitniRok

db:
	db2 -tf pripremaBaze.sql || true
