SELECT ID,  
       NAZIV,  
       ESPB 
FROM   DA.PREDMET 
WHERE  ID IN ( 
           SELECT  IDPREDMETA 
           FROM    DA.PREDMETPROGRAMA 
           WHERE   IDPROGRAMA = 103 AND 
           		   VRSTA = 'obavezan'
       );