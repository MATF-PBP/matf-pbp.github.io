CONNECT TO STUD2020 USER student USING abcdef;

DELETE  FROM DA.ISPITNIROK
WHERE   SKGODINA = 2021;

DELETE  FROM DA.SKOLSKAGODINA
WHERE   SKGODINA = 2021;

INSERT  INTO DA.SKOLSKAGODINA
VALUES  (2021, '01/01/2021', '12/31/2021');

CONNECT RESET;
