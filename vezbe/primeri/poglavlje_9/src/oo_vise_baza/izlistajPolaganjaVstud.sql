SELECT  INDEKS, 
        P.NAZIV, 
        OCENA, 
        GODINA_ROKA, 
        OZNAKA_ROKA  
FROM    ISPIT I JOIN 
        PREDMET P ON I.ID_PREDMETA = P.ID_PREDMETA  
WHERE   OCENA > 5 AND 
        STATUS_PRIJAVE = 'O' AND 
        NOT EXISTS (  
            SELECT  *  
            FROM    ISPIT I2  
            WHERE   I.INDEKS = I2.INDEKS AND  
                    OCENA < ?  
        )