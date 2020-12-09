SELECT      D.INDEKS, 
            TRIM(IME),
            TRIM(PREZIME), 
            TRIM(NAZIV)
FROM        DOSIJE D JOIN 
            SMER S ON D.ID_SMERA = S.ID_SMERA JOIN 
            ISPIT I ON D.INDEKS = I.INDEKS
WHERE       OCENA > 5 AND 
            STATUS_PRIJAVE = 'o'
GROUP BY    D.INDEKS, 
            IME, 
            PREZIME, 
            NAZIV
HAVING      COUNT(*) = ?