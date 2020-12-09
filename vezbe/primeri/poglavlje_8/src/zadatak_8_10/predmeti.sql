SELECT      TRIM(NAZIV), 
            OCENA 
FROM        ISPIT I JOIN 
            PREDMET P ON I.ID_PREDMETA = P.ID_PREDMETA 
WHERE       I.INDEKS = ? AND 
            OCENA > 5 AND 
            STATUS_PRIJAVE = 'o'
ORDER BY    NAZIV