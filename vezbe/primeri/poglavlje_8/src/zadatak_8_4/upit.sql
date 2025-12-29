WITH BROJ_POLOZENIH_ISPITA AS (
    SELECT      INDEKS
    FROM        DA.ISPIT 
    WHERE       OCENA > 5 AND 
                STATUS = 'o'
    GROUP BY    INDEKS
    HAVING      COUNT(*) = ?
)
SELECT      D.INDEKS, 
            IME, 
            PREZIME, 
            S.NAZIV, 
            P.NAZIV, 
            I.OCENA
FROM        DA.DOSIJE D JOIN 
            BROJ_POLOZENIH_ISPITA BPI ON D.INDEKS = BPI.INDEKS JOIN
            DA.STUDIJSKIPROGRAM S ON D.IDPROGRAMA = S.ID JOIN
            DA.ISPIT I ON D.INDEKS = I.INDEKS JOIN
            DA.PREDMET P ON I.IDPREDMETA = P.ID
WHERE       I.OCENA > 5 AND
            I.STATUS = 'o'
ORDER BY    D.INDEKS,
            IME,
            PREZIME,
            S.NAZIV,
            P.NAZIV