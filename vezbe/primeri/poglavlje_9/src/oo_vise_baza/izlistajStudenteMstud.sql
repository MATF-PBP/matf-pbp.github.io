SELECT  INDEKS,
        IME,
        PREZIME  
FROM    DOSIJE D  
WHERE   NOT EXISTS (  
            SELECT  *  
            FROM    PREDMET P  
            WHERE   BODOVI = ? AND 
                    NOT EXISTS (  
                        SELECT  *  
                        FROM    ISPIT I  
                        WHERE   I.INDEKS = D.INDEKS AND 
                                I.ID_PREDMETA = P.ID_PREDMETA AND 
                                I.OCENA > 5  
                    )  
        )