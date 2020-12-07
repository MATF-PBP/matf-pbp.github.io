WITH BROJ_POLOZENIH_ISPITA AS (
    SELECT      INDEKS
    FROM        ISPIT 
    WHERE       OCENA > 5 AND 
                STATUS_PRIJAVE = 'o'
    GROUP BY    INDEKS
    HAVING      COUNT(*) = ?
)
SELECT      D.INDEKS, 
            IME, 
            PREZIME, 
            S.NAZIV, 
            P.NAZIV, 
            I.OCENA
FROM        DOSIJE D JOIN 
            BROJ_POLOZENIH_ISPITA BPI ON D.INDEKS = BPI.INDEKS JOIN
            SMER S ON D.ID_SMERA = S.ID_SMERA JOIN
            ISPIT I ON D.INDEKS = I.INDEKS JOIN
            PREDMET P ON I.ID_PREDMETA = P.ID_PREDMETA
WHERE       I.OCENA > 5 AND
            I.STATUS_PRIJAVE = 'o'
ORDER BY    D.INDEKS,
            IME,
            PREZIME,
            S.NAZIV,
            P.NAZIV