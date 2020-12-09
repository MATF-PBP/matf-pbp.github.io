WITH BROJ_POLAGANJA_PREDMETA AS (
    SELECT      ID_PREDMETA,
                GODINA,
                COUNT(*) BROJ_POLAGANJA
    FROM        ISPIT
    GROUP BY    ID_PREDMETA,
                GODINA
)
SELECT      UK.ID_PREDMETA,
            UK.GODINA,
            COUNT(*) BROJ_UPISANIH,
            (
                SELECT  BROJ_POLAGANJA 
                FROM    BROJ_POLAGANJA_PREDMETA BPP 
                WHERE   UK.ID_PREDMETA = BPP.ID_PREDMETA AND
                        UK.GODINA = BPP.GODINA
            ) BROJ_POLAGANJA 
FROM        UPISAN_KURS UK
WHERE       UK.ID_PREDMETA = ?
GROUP BY    UK.ID_PREDMETA,
            UK.GODINA
WITH        RR