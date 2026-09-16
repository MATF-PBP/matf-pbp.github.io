SELECT      TRIM(NAZIV), 
            OCENA 
FROM        DA.ISPIT I JOIN 
            DA.PREDMET P ON I.IDPREDMETA = P.ID 
WHERE       I.INDEKS = ? AND 
            OCENA > 5 AND 
            STATUS = 'o'
ORDER BY    NAZIV