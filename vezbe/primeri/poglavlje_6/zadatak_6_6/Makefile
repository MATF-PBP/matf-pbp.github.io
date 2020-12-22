# Za prevodjenje programa pozvati: make
# Za pripremu baze pozvati: make db
# Za ciscenje artefakta pozvati: make clean

cursorStability: cursorStability.sqc insertIspitniRok updateIspitniRok
	./prevodjenje cursorStability stud2020 student abcdef

insertIspitniRok: insertIspitniRok.sqc
	./prevodjenje insertIspitniRok stud2020 student abcdef

updateIspitniRok: updateIspitniRok.sqc
	./prevodjenje updateIspitniRok stud2020 student abcdef

.PHONY: clean db

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f cursorStability
	rm -f insertIspitniRok
	rm -f updateIspitniRok

db:
	db2 -tf pripremaBaze.sql || true
