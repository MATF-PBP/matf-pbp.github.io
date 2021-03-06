WITH BROJPOLAGANJAPREDMETA AS (
    SELECT      IDPREDMETA,
                SKGODINA,
                COUNT(*) BROJPOLAGANJA
    FROM        DA.ISPIT
    GROUP BY    IDPREDMETA,
                SKGODINA
)
SELECT      UK.IDPREDMETA,
            UK.SKGODINA,
            COUNT(*) BROJUPISANIH,
            (
                SELECT  BROJPOLAGANJA 
                FROM    BROJPOLAGANJAPREDMETA BPP 
                WHERE   UK.IDPREDMETA = BPP.IDPREDMETA AND
                        UK.SKGODINA = BPP.SKGODINA
            ) BROJPOLAGANJA 
FROM        DA.UPISANKURS UK
WHERE       UK.IDPREDMETA = ?
GROUP BY    UK.IDPREDMETA,
            UK.SKGODINA
WITH        RR