# Za prevodjenje programa pozvati: make
# Za pripremu baze pozvati: make db
# Za ciscenje artefakta pozvati: make clean

exclusiveMode: exclusiveMode.sqc shareMode
	./prevodjenje exclusiveMode stud2020 student abcdef

shareMode: shareMode.sqc
	./prevodjenje shareMode stud2020 student abcdef

.PHONY: clean

clean:
	rm -f *.bnd
	rm -f *.o
	rm -f *.c
	rm -f exclusiveMode
	rm -f shareMode
