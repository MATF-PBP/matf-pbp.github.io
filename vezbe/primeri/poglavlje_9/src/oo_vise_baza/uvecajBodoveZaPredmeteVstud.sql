UPDATE  PREDMET P
SET     BODOVI = BODOVI + 1 
WHERE   BODOVI < 10 AND 
        EXISTS (
            SELECT  *
            FROM    ISPIT I 
            WHERE   I.ID_PREDMETA = P.ID_PREDMETA AND 
                    OCENA > 5
                    AND INDEKS = ?
        )