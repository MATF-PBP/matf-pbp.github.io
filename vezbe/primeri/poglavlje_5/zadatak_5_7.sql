CONNECT TO STUD2020 USER student USING abcdef;

DROP TABLE DA.OBRADJENAPOLAGANJA;

CREATE TABLE DA.OBRADJENAPOLAGANJA (
    INDEKS INTEGER NOT NULL,
    GODINA SMALLINT NOT NULL,
    PRIMARY KEY (INDEKS, GODINA),
    FOREIGN KEY (INDEKS)
        REFERENCES DA.DOSIJE
);

INSERT INTO DA.OBRADJENAPOLAGANJA
VALUES (20180050, 2018);

CONNECT RESET; 
