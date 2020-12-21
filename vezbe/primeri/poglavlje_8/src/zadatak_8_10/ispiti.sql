SELECT      D.INDEKS, 
            TRIM(IME),
            TRIM(PREZIME), 
            TRIM(NAZIV)
FROM        DA.DOSIJE D JOIN 
            DA.STUDIJSKIPROGRAM S ON D.IDPROGRAMA = S.ID JOIN 
            DA.ISPIT I ON D.INDEKS = I.INDEKS
WHERE       OCENA > 5 AND 
            STATUS = 'o'
GROUP BY    D.INDEKS, 
            IME, 
            PREZIME, 
            NAZIV
HAVING      COUNT(*) = ?