SELECT      D.INDEKS,
            IME,
            PREZIME,
            DECIMAL(AVG(OCENA*1.0), 5, 2) PROSEK
FROM        DOSIJE D JOIN
            ISPIT I ON D.INDEKS = I.INDEKS
WHERE       EXISTS (SELECT * FROM ISPIT I2 WHERE I2.INDEKS = I.INDEKS AND OCENA = ?) AND
            OCENA > 5            
GROUP BY    D.INDEKS,
            IME,
            PREZIME            