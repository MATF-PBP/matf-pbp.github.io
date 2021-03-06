---
layout: page
title: 12. Napredno kreiranje upita pomoću JPA Criteria API
under_construction: true
---

U prethodnom poglavlju smo diskutovali o kori\v s\'cenju jezika HQL, specifi\v cnog za potrebe aplikacija sa Hibernate podr\v skom, za izdvajanje skupova podataka iz relacione baze podataka. Videli smo da se HQL konceptualno zasniva za jeziku SQL, sa razlikom da, umesto baratanja tabelama u relacionoj bazi podataka, sve restrikcije, projekcije, spajanja itd. koriste Java klase za postavljanje ograni\v cenja koja rezultati upita moraju da ispunjavaju.

Sli\v can koncept, ali ne\v sto op\v stiji u smislu upotrebe, predstavlja JPA Criteria API. Ideja je da se umesto navo\dj enja imena pod niskom (\v sto je bio slu\v caj u HQL upitima), koriste napredniji koncepti za konstrukciju dinami\v ckih upita. Prednost ovog na\v cina kori\v s\'cenja jeste upravo u tome \v sto se umesto kori\v s\'cenja niski za zadavanje upita, koriste inteligentne programerske tehnike, kao \v sto je ulan\v cavanje metoda. Dodatno, koriste se prednosti programskog jezika Java.

## 12.1 Kratak pregled JPA Criteria API

Zapo\v cnimo ovo poglavlje davanjem primera sa ciljem da ilustrujemo promenu u razmi\v sljanju izme\dj u konstrukcije HQL upita i kori\v s\'cenja JPA Criteria API. Naredni HQL upit izdvaja sve zaposlene u kompaniji \v cije je ime _"John Smith"_:

```java
String hql = "FROM Employee e WHERE e.name = \"John Smith\"";
org.hibernate.query.Query<Employee> upit = 
    session.createQuery(hql, Employee.class);
```

Ekvivalentan rezultat koji se dobija kori\v s\'cenjem JPA Criteria API je:

```java
CriteriaBuilder cb          = session.getCriteriaBuilder();
CriteriaQuery<Employee> c   = cb.createQuery(Employee.class);

Root<Employee> emp = c.from(Employee.class);
c.select(emp)
 .where(cb.equal(emp.get("name"), "John Smith"));
```

O\v cigledno, ovi pristupi se jasno razlikuju. HQL upit se konstrui\v se na isti na\v cin kao i SQL upit - navo\dj enjem odgovaraju\'ce sintakse u cilju konstrukcije niske koja sadr\v zi sve informacije. Za razliku od toga, pristup koji se oslanja na JPA Criteria API koristi metode i refleksiju za konstrukciju uslova koje rezultati upita moraju da zadovolje. Kroz ovo poglavlje \'cemo se detaljno upoznati sa ovim konceptima.

Radi kompletnosti, objasnimo ne\v sto detaljnije prethodni kod. Za po\v cetak prime\'cujemo da se kreira instanca interfejsa `CriteriaBuilder` tako \v sto se nad objektom klase `Session` poziva metod `getCriteriaBuilder()`. Instanca `cb` interfejsa `CriteriaBuilder` sadr\v zi veliki broj metoda koji se koriste za definisanje ograni\v cenja rezultata upita. Na primer, pozivom metoda `createQuery` nad objektom `cb` kreira jedan novi objekat klase `CriteriaQuery` koji reprezentuje kostur JPA Criteria API upita. Drugi primer upotrebe klase `CriteriaBuilder` jeste u kreiranju jednakosnog ograni\v cenja koji odgovara izrazu `e.name = \"John Smith\"` u HQL upitu, pozivom metoda `equal()`. 

Kada imamo kostur novog upita, ostatak koda je manje-vi\v se pravolinijski. Potrebno je ustanoviti koren upita, odnosno, klasu od koje se zapo\v cinje zadavanje ograni\v cenja - u na\v sem primeru to je klasa `Employee` - \v sto se izvr\v sava pozivanjem metoda `from()` nad upitom i prosle\dj ivanjem klase koja predstavlja koren. Ovo je ekvivalentno deklarisanju alijasa `e` u HQL upitu i objekat klase `Root` predstavlja osnovu za izgradnju ostatka upita. Naredni korak jeste izvr\v savanje projekcije pozivom metoda `select()` nad upitom i prosle\dj ivanjem konstruisanog korena upita. Kona\v cno, potrebno je zadati odgovaraju\'ce restrikcije nad upitom, \v sto se izvodi ulan\v cavanjem metoda `where()` nad projekcijom upita i navo\dj enjem ograni\v cenja restrikcije. S obzirom da se vr\v si restrikcija nad poljem `name` klase `Employee`, sve \v sto je potrebno uraditi jeste prona\'ci to ime pozivom metoda `get()` nad korenom upita.

Iako smo napisali vi\v se koda u odnosu na pristup zasnovan na HQL jeziku, primetimo slede\'ce. Ukoliko bismo \v zeleli da dinami\v cki postavljamo uslove restrikcije u upitu, u pristupu koji koristi HQL jezik morali bismo da upravljamo niskama za konstruisanje upita, na primer:

```java
String hql = "FROM Employee e WHERE ";
if (nameRestriction) {
    hql += "e.name = \"John Smith\"";
}
else if (salaryRestriction) {
    hql += "e.salary = 20000";
}
else {
    // ...
}

org.hibernate.query.Query<Employee> upit = 
    session.createQuery(hql, Employee.class);
```

Ovakav pristup za kreiranje dinami\v ckih upita ima veliki broj problema. Umesto toga, mnogo je prirodnije osloniti se na mogu\'cnosti samog programskog jezika u kojem se vr\v si razvoj aplikacije, \v sto nam JPA Criteria API omogu\'cava:

```java
CriteriaBuilder cb = session.getCriteriaBuilder();
CriteriaQuery<Employee> c = cb.createQuery(Employee.class);
Root<Employee> emp = c.from(Employee.class);

String attribute;
String restriction;
if (nameRestriction) {
    attribute = "name";
    restriction = "John Smith";
}
else if (salaryRestriction) {
    attribute = "salary";
    restriction = "20000";
}
else {
    // ...
}

c.select(emp)
 .where(cb.equal(emp.get(attribute), restriction));
```

Na ovaj na\v cin ne moramo da vodimo ra\v cuna da li smo ispravno konstruisali niske od kojih upit zavisi zato \v sto se _na\v cin izdvajanja rezultata u poslednjoj liniji nikad ne menja_!

## 12.2 Konstrukcija JPA Criteria API upita

Kao \v sto smo demonstrirali u prethodnoj sekciji, sr\v z JPA Criteria API-ja je u interfejsu `CriteriaBuilder`, koji se dobija pozivom metoda `getCriteriaBuilder()` nad objektom sesije. Ovaj interfejs je poprili\v cno \v sirok i slu\v zi u razne svrhe u okviru JPA Criteria API-ja. Cilj ovog poglavlja jeste da se upoznamo sa njim.

Interfejs `CriteriaBuilder` nudi tri metoda za kreiranje novog upita, u zavisnosti od \v zeljenog tipa rezultata upita: 
1. Metod `createQuery(Class<T>)` prihvata klasu koja odgovara rezultatu upita.
2. Metod `createQuery()`, bez parametara, odgovara rezultatu tipa klase `Object`. 
3. Metod `createTupleQuery()` koristi se za projekcije ili u slu\v caju kada `SELECT` klauza sadr\v zi vi\v se od jednog izraza i \v zelimo da radimo sa rezultatom u strogo tipiziranom maniru. Zapravo, taj metod predstavlja skra\'cenicu za poziv prvog metoda `createQuery(Tuple.class)`. Primetimo da je `Tuple` interfejs koji sadr\v zi razli\v cite objekte ili podatke i primenuje tipiziranost nad agregatnim delovima. Mo\v ze biti kori\v s\'cen kada je rezultat upita vi\v se podataka, pri \v cemu \v zelimo da ih kombinujemo u jedinstveni tipizirani objekat.

JPA Criteria API se sastoji od velikog broja interfejsa koji rade zajedno u cilju modeliranja strukture JPA upita. Kako budemo prolazili kroz ovo poglavlje, bi\'ce korisno pratiti dijagram na narednoj slici i posmatrati odnose izme\dj u interfejsa.

<div markdown="1" class="text-center">
![Interfejsi u JPA Criteria API](./Slike/criteria-interfejsi.png)
</div>

## 12.3 Osnovna struktura

U prethodnom poglavlju smo videli da u jeziku HQL mo\v zemo koristiti narednih \v sest klauza za konstrukciju klasi\v cnih `SELECT` upita: `SELECT`, `FROM`, `WHERE`, `ORDER BY`, `GROUP BY` and `HAVING`. Svaka od ovih HQL klauza ima ekvivalentan metod nad nekim od JPA Criteria API interfejsa. Naredna tabela sumira ove metode.

| HQL klauza | Interfejs iz JPA Criteria API | Metod kojim se ostvaruje |
| --- | --- | --- |
| `SELECT` | `CriteriaQuery` | `select()` |
| | `Subquery` | `select()` |
| `FROM` | `AbstractQuery` | `from()` |
| `WHERE` | `AbstractQuery` | `where()` |
| `ORDER BY` | `CriteriaQuery` | `orderBy()` |
| `GROUP BY` | `AbstractQuery` | `groupBy()` |
| `HAVING` | `AbstractQuery` | `having()` |

A newly created `CriteriaQuery` object is basically an empty shell. With the exception of defining the result type of the query, no additional content has yet been added to fill out the query. As with HQL queries, the developer is responsible for defining the various clauses of the query necessary to fetch the desired data from the database. Semantically speaking, there is no difference between HQL and Criteria API query definitions. Both have `SELECT`, `FROM`, `WHERE`, `GROUP BY`, `HAVING`, and `ORDER BY` clauses; only the manner of defining them is different. Before we can fill in the various clauses of the query definition, let's first revisit two key concepts and look at the equivalent Criteria API syntax for those concepts.

### 12.3.1 Koreni upita

The first fundamental concept to revisit is the identification variable used in the `FROM` clause of HQL queries to alias declarations that cover entity, embeddable, and other abstract schema types. In HQL, the identification variable takes on a central importance, as it is the key to tying the different clauses of the query together. But with the Criteria API we represent query components with objects and therefore rarely have aliases with which to concern ourselves. Still, in order to define a `FROM` clause, we need a way to express which abstract schema types we are interested in querying against.

Interfejs `AbstractQuery` (roditelj interfejsa `CriteriaQuery`) nudi metod `from()` za definisanje apstraktne sheme koja formira osnovu na\v seg upita. Ovaj metod prihvata tip entiteta kao parametar i dodaje novi koren u upitu. Koren u upitu koresponsira identifikacionoj promenljivoj u HQL-u, koja dalje koresponsira deklaraciji ranga (tj. skupu iz kojeg se podaci izdvajaju) ili izrazu spajanja. Naredni kod ilustruje kako mo\v zemo dobiti koren upita:

```java
CriteriaQuery<Student> c    = cb.createQuery(Student.class);
Root<Student> emp           = c.from(Student.class);
```

Metod `from()` vra\'ca objekat interfejsa `Root` koja odgovara tipu entiteta. Interfejs `Root` je dete interfejsa `From`, koji nam nudi funkcionalnosti spajanja. Interfejs `From` je dete interfejsa `Path`, koji je dalje dete `Expression`, pa `Selection`, kojim se omogu\'cava da se koren koristi u drugim delovima definicije upita. Uloga svih ovih interfejsa \'ce biti detaljnije opisana u kasnijim sekcijama. 

Pozivi metoda `from()` su aditivni. To zna\v ci da svaki poziv dodaje novi koren u upitu, \v sto rezultuje u Dekartovom proizvodu kada je vi\v se od jednog korena definisan, ako nema daljih ograni\v cenja u `WHERE` klauzi. Naredni primer demonstrira vi\v sekoreni upit, koji zamenjuju savremenija spajanja tradacionalnijim SQL pristupom:

```sql
SELECT  DISTINCT s
FROM    Student s, 
        Ispit i
WHERE   s = i.indeks
```

Da bismo pretvorili ovaj upit u JPA Criteria API, potrebno je da pozovemo metod `from()` dvaput, dodaju\'ci entitete `Student` i `Ispit` kao korene upita:

```java
CriteriaQuery<Student> c    = cb.createQuery(Student.class);
Root<Student> stud          = c.from(Student.class);
Root<Ispit> ispit           = c.from(Ispit.class);

c.select(stud)
 .distinct(true)
 .where(cb.equal(stud, ispit.get("indeks")));
```

### 12.3.2 Izrazi nad putanjama

The second fundamental concept to revisit is the path expression. The path expression is the key to the power and flexibility of the HQL language, and it is likewise a central piece of the Criteria API. 

Govorili smo o korenima upita u prethodnoj podsekciji, ali svi koreni zapravo predstavljaju specijalni tip izraza putanje. Posmatrajmo naredni jednostavan HQL upit koji vra\'ca sve studente koji su upisani na studijskom programu Informatika:

```sql
SELECT  s
FROM    Student s
WHERE   s.studijskiProgram.naziv = 'Informatika'
```

Razmi\v sljaju\'ci u terminima JPA Criteria API, koren ovog izraza je entitet `Student` i neka je on predstavljen objektom `stud`. Ovaj upit tako\dj e sadr\v zi izraz putanje u `WHERE` klauzi. Da bismo predstavili ovaj izraz putanje kori\v s\'cenjem JPA Criteria API, koristimo naredni izraz:

```java
stud.get("studijskiProgram").get("naziv")
```

Metod `get()` je izveden iz interfejsa `Path` pro\v sirenog interfejsom `Root` i ekvivalentan je operatoru ta\v cke koji se koristi u HQL izrazima putanje radi navigacije kroz putanju. S obzirom da metod `get()` vra\'ca objekat interfejsa `Path`, pozivi metoda se mogu ulan\v cavati, \v sto \'ce nam biti veoma korisna tehnika pri izgradnji upita, jer time izbegavamo uvo\dj enje privremenih lokalnih promenljivih (koji nam ne slu\v ze prakti\v cno ni\v cemu). Argument metoda `get()` je naziv atributa entiteta za koji smo zainteresovani. Zbog toga \v sto je rezultat konstruisanja izraza putanje objekat interfejsa `Expression` koji mi mo\v zemo da koristimo za izgradnju uslovnih izraza, mo\v zemo prethodni HQL upit izraziti na slede\'ci na\v cin:

```java
CriteriaQuery<Student> c    = cb.createQuery(Student.class);
Root<Student> stud          = c.from(Student.class);

c.select(stud)
 .where(cb.equal(stud.get("studijskiProgram").get("naziv"), "Informatika"));
```

Much like HQL, path expressions may be used throughout the different clauses of the query definition. With the Criteria API, it is necessary to hold onto the root object in a local variable and use it to form path expressions where required. Once again it is worth emphasizing that the `from()` method of `AbstractQuery` should never be invoked more than once for each desired root. Invoking it multiple times will result in additional roots being created and a `Cartesian` product if not careful. Always store the `root` objects locally and refer to them when necessary.

## 12.4 Klauza `SELECT`

Postoji nekoliko formi koje klauza `SELECT` mo\v ze da uzima u upitu. Najjednostavnija forma predstavlja izdvajanje jednog izraza, dok neke druge forme rade sa vi\v se izraza ili koriste konstruktorskih izraza radi kreiranja novih instancnih objekata. Svaka forma je na drugi na\v cin predstavljena u JPA Criteria API.

### 12.4.1 Izdvajanje jednog izraza

Metod `select()` interfejsa `CriteriaQuery` se koristi radi formiranja `SELECT` klauze u JPA Criteria API definicijama upita. Sve forme `SELECT` klauze se mogu predstaviti metodom `select()`, premda postoje bolji na\v cini kori\v s\'cenjem specijalizovanih metoda radi pojednostavljivanja kodiranja. Metod `select()` prihvata argument interfejsa `Selection`, \v sto je zapravo roditelj interfejsa `Expression`, kao i `CompoundSelection` za slu\v caj kada je rezultat upita `Tuple` ili niz rezultata.

Do sada, mi smo prosle\dj ivali koren upita metodu `select()`, \v cime smo indikovali da \v zelimo da odgovaraju\'ci entitet bude rezultat upita. Mogu\'ce je proslediti i izraz koji predstavlja jednu vrednost, poput izdvajanja atributa iz entiteta ili bilo kog kompatibilnog skalarnog izraza. Naredni primer ilustruje ovaj pristup izdvajanjem atributa koji predstavlja ime entiteta `Student`:

```java
CriteriaQuery<String> c = cb.createQuery(String.class);
Root<Student> stud      = c.from(Stud.class);

c.select(stud.<String>get("ime"));
```

Ovakav upit \'ce vratiti sva imena studenata, uklju\v cuju\'ci sve duplikate. O izdvajanju upita bez duplikata \'ce biti re\v ci kasnije.

Primetimo neobi\v cnu sintaksu koju koristimo da bismo deklarisali da atribut `ime` predstavlja instancu tipa `String`. Tip izraza koji se prosle\dj uje metodu `select()` mora biti kompatibilan sa rezultuju\'cim tipom koji se koristi za kreiranje `CriteriaQuery` objekta. Na primer, ako je `CriteriaQuery` objekat kreiran pozivom `createQuery(Ispit.class)` nad objektom interfejsa `CriteriaBuilder`, onda \'ce biti gre\v ska ukoliko bismo poku\v sali da postavimo da se dobija izraz entiteta `Student` kori\v s\'cenjem metoda `select()`. Kada poziv metoda poput `select()` koristi \v sablonsku tipiziranost da bi omogu\'cio ograni\v cenja kompatibilnosti, onda mo\v zemo prefiksovati tip imenu metoda kako bismo ga kvalifikovali u slu\v cajevima kada tip ne mo\v ze biti automatski zaklju\v cen. U ovom slu\v caju, moramo koristiti ovaj pristup s obzirom da je metod `select()` deklarisan kao:

```java
CriteriaQuery<T> select(Selection<? extends T> selection);
```

Argument metoda `select()` mora biti tipa koji je kompatibilan rezultuju\'cim tipom definicije upita. Metod `get()` vra\'ca objekat `Path`, ali taj objekat je uvek tipa `Path<Object>` zato \v sto kompilator ne mo\v ze da zaklju\v ci odgovaraju\'ci tip samo na osnovu imena atributa. Zbog toga, da bismo deklarisali da je atribut `ime` u entitetu `Student` tipa `String`, moramo da preciziramo poziv metoda kao \v sto smo videli.

This syntax has to be used whenever the `Path` is being passed as an argument for which the parameter has been strongly typed, such as the argument to the `select()` method and certain `CriteriaBuilder` expression methods. 
We have not had to use them so far in our examples because we have been using them in methods like `equal()`, where the parameter was declared to be of type `Expression<?>`. 
Because the type is wildcarded, it is valid to pass in an argument of type `Path<Object>`. 
Later in the chapter, we look at the strongly typed versions of the Criteria API methods that remove this requirement.

### 12.4.2 Izdvajanje vi\v se izraza

Kada defini\v semo `SELECT` klauzu koja sadr\v zi izdvajanje vi\v se od jednog izraza, onda \'cemo koristiti razli\v cite pristupe u zavisnosti od na\v cina na koji je definicija upita kreirana:

- Ako je rezultuju\'ci tip `Tuple`, onda moramo metodu `select()` proslediti objekat `CompoundSelection<Tuple>`.

- Ako je rezultuju\'ci tip neperzistentna klasa koja \'ce biti kreirana kori\v s\'cenjem konstruktorskih izraza, tada argument mora biti objekat `CompoundSelection<[T]>`, gde je `[T]` tip te neperzistentne klase.

- Kona\v cno, ako je rezultuju\'ci tip niz objekata, onda se metodu prosle\dj uje objekat `CompoundSelection<Object[]>`.

Ovi objekti se kreiraju pozivima metoda `tuple()`, `construct()` i `array()` nad objektom interfejsa `CriteriaBuilder`, redom. Neka je potrebno izdvojiti godine i oznake svih ispitnih rokova, \v sto se izvodi narednim HQL upitom:

```sql
SELECT  ir.godina,
        ir.oznaka
FROM    IspitniRok ir
```

Naredni primer demonstrira kako mo\v zemo imati vi\v sestruke izraze kori\v s\'cenjem upita nad `Tuple`:

```java
CriteriaQuery<Tuple> c  = cb.createTupleQuery();
Root<IspitniRok> ir     = c.from(IspitniRok.class);

c.select(cb.tuple(ir.get("godina"), 
                  ir.get("oznaka")));
```

Kao pogodnost, metod `multiselect()` iz interfejsa `CriteriaQuery` mo\v ze se tako\dj e koristiti za ispunjavanje istog zahteva. Metod `multiselect()` \'ce kreirati odgovaraju\'ci tip argumenata za dati rezultuju\'ci tip upita, odnosno, zavisno od na\v cina kreiranja upita.

Prva forma je za upite koje imaju `Object` ili `Object[]` kao njihov rezultuju\'ci tip. Metodu `multiselect()` se u ovom slu\v caju mo\v ze proslediti lista izraza koje predstavljaju svaki od izraza:

```java
CriteriaQuery<Object[]> c   = cb.createQuery(Object[].class);
Root<IspitniRok> ir         = c.from(IspitniRok.class);

c.multiselect(ir.get("godina"), 
              ir.get("oznaka"));
```

Note that, if the query result type is declared as `Object` instead of `Object[]`, the behavior of `multiselect()` in this form changes slightly. The result is always an instance of `Object`, but if multiple arguments are passed into `multiselect()` then the result must be cast to `Object[]` in order to access any particular value. If only a single argument is passed into `multiselect()`, then no array is created and the result may be cast directly from `Object` to the desired type. In general, it is more convenient to be explicit about the query result type. If you want to work with an array of results, then declaring the query result type to be `Object[]` avoids casting later and makes the shape of the result more explicit if the query is invoked separately from the code that creates it.

Druga forma je sli\v cna prvoj formi, samo se odnosi na upite koji rezultuju tipom `Tuple`. Ponovo, metodu `multiselect()` se u ovom slu\v caju mo\v ze proslediti lista izraza koje predstavljaju svaki od izraza:

```java
CriteriaQuery<Tuple> c  = cb.createTupleQuery();
Root<IspitniRok> ir   = c.from(IspitniRok.class);

c.multiselect(ir.get("godina"), 
              ir.get("oznaka"));
```

Tre\'ca i kona\v cna forma je za upite sa konstruktorskih izrazima \v ciji rezultat je neperzistentnog tipa. Metod `multiselect()` se ponovo poziva sa listom izraza, ali koristi tip upita da odredi i automatski kreira odgovaraju\'ci konstruktorski izraz, u ovom slu\v caju primarni klju\v c klase `IspitniRokId`:

```java
CriteriaQuery<IspitniRokId> c   = cb.createQuery(IspitniRokId.class);
Root<IspitniRok> ir             = c.from(IspitniRok.class);

c.multiselect(ir.get("godina"), 
              ir.get("oznaka"));
```

Ovo je ekvivalentno narednom kodu:

```java
CriteriaQuery<IspitniRokId> c   = cb.createQuery(IspitniRokId.class);
Root<IspitniRok> ir             = c.from(IspitniRok.class);

c.select(cb.construct(IspitniRokId.class, // Izdvoji primarni kljuc na osnovu kolona:
         ir.get("godina"),                // 1. godina
         ir.get("oznaka")));              // 2. godina
```

Me\dj utim, iako je metod `multiselect()` izuzetno pogodan za upotrebu konstruktorskih izraza, postoje slu\v cajevi kada je neophodno kori\v s\'cenje metoda `construct()` iz interfejsa `CriteriaBuilder`. Na primer, ukoliko je potrebno da upit izra\v cuna i naziv ispitnih rokova pored godine i oznake rokova. U tom slu\v caju, ispitni rokovi koji se konstrui\v su predstavljaju samo deo rezultata, tako da rezultuju\'ci tip upita mora biti tipa `Object[]` i onda on mora da sadr\v zi konstruktorski izraz za konstruisanje instanci klase `IspitniRokId`:

```java
CriteriaQuery<Object[]> c   = cb.createQuery(Object[].class);
Root<IspitniRok> ir         = c.from(IspitniRok.class);

c.multiselect(ir.get("naziv"), // Izdvoji naziv roka
              cb.construct(IspitniRokId.class, // Izdvoji primarni kljuc na osnovu kolona:
                           ir.get("godina"),   // 1. godina
                           ir.get("oznaka"))); // 2. oznaka
```

### 12.4.3 Kori\v s\'cenje alijasa

Like HQL, aliases may also be set on expressions in the `SELECT` clause, which will then be included in the resulting SQL statement. They are of little use from a programming perspective as we construct the `ORDER BY` clause through the use of the `Selection` objects used to construct the `SELECT` clause.

Aliases are useful when the query has a result type of `Tuple`. The aliases will be available through the resulting `Tuple` objects. To set an alias, the `alias()` method of the `Selection` interface (parent to `Expression`) must be invoked. The following example demonstrates this approach:

```java
CriteriaQuery<Tuple> c  = cb.createTupleQuery();
Root<Student> stud      = c.from(Student.class);
c.multiselect(stud.get("indeks").alias("indeks"), 
              stud.get("mesto_rodjenja").alias("mesto"));
```

This example actually demonstrates two facets of the `alias()` method. The first is that it returns itself, so it can be invoked as part of the call to `select()` or `multiselect()`. The second is, once again, that it returns itself, and is therefore mutating what should be an otherwise immutable object. The `alias()` method is an exception to the rule that only the query definition interfaces—`CriteriaQuery` and `Subquery`—contain mutating operations. Invoking `alias()` changes the original `Selection` object and returns it from the method invocation. It is invalid to set the alias of a `Selection` object more than once.

Using the alias when iterating over the query results is as simple as requesting the expression by name. Executing the previous query would allow it to be processed as follows:

```java
TypedQuery<Tuple> q = session.createQuery(c);
for (Tuple t : q.getResultList()) {
    Integer indeks  = t.get("indeks", Integer.class);
    String mesto    = t.get("mesto", String.class);
    // ...
}
```

## 12.5 Klauza `FROM`

U sekciji 12.3.1, diskutovali smo o metodu `from()` iz interfejsa `AbstractQuery` kao i o ulogama korenova upita u formiranju definicije upita. Sada \'cemo pro\v siriti tu diskusiju i prikazati na\v cine na koje se izrazi spajanja izra\v zavaju u JPA Criteria API.

### 12.5.1 Unutra\v snja i spolja\v snja spajanja

Izrazi spajanja se kreiraju kori\v s\'cenjem metoda `join()` iz interfejsa `From` koji predstavlja roditelj interfejsa `Root`, o kojem smo diskutovali ranije, kao i interfejsa `Join`, koji predstavlja tip objekata koji se vra\'caju kreiranjem izraza spajanja. Ovo zna\v ci da proizvoljan koren upita mo\v ze da se spaja, kao i da spajanja mogu da se ulan\v cavaju. Metod `join()` zahteva izraz putanje kao argument i opciono, argument kojim se specifikuje tip spajanja: `JoinType.INNER` ili `JoinType.LEFT`, za unutra\v snja ili spolja\v snja spajanja, redom.

Kada se vr\v se spajanja unakrst kolekcijskih tipova (sa izuzetkom `Map`), spajanje \'ce imati dva parametarska tipa: tip izvora i tip cilja. Ovim se odr\v zava sigurnost tipova sa obe strane spajanja i 

Metod `join()` je aditivan, \v sto zna\v ci da svaki poziv rezultuje novim spajanjem koji se kreira. Zbog toga, instanca tipa `Join` koja se vra\'ca kao povratna vrednost metoda bi trebalo biti sa\v cuvana kao lokalna promenljiva radi formiranja izraza putanja kasnije.

Naredni primer ilustruje spolja\v snje spajanje od `Student` ka `Ispit`:

```java
Root<Student> stud          = c.from(Student.class);
Join<Student,Ispit> ispit   = stud.join("indeks", JoinType.LEFT);
```

Da smo izostavili argument `JoinType.LEFT`, spajanje bi podrazumevano bilo unutra\v snje. Poput HQL-a, vi\v sestruka spajanja se mogu asocirati za istu `From` instancu. Na primer, za navigaciju kroz `Ispit` i `StudijskiProgram`, potrebno je koristiti naredni kod, koji pretpostavlja unutra\v snja spajanja:

```java
Root<Student> stud          = c.from(Student.class);
Join<Student,Ispit> ispit   = stud.join("indeks");
Join<Student,StudijskiProgram> studijskiProgram     = stud.join("studijskiProgram");
```

Spajanja se tako\dj e mogu ulan\v cavati u jednu naredbu. Rezultuju\'ce spajanje \'ce biti tipizirano tipom izvora i tipom cilja poslednjeg spajanja u naredbi:

```java
Root<StudijskiProgram> studijskiProgram             = c.from(StudijskiProgram.class);
Join<Student,Ispit> ispiti  = studijskiProgram.join("studenti").join("ispiti");
```

Joins across collection relationships that use `Map` are a special case. HQL uses the `KEY` and `VALUE` keywords to extract the key or value of a `Map` element for use in other parts of the query. In the JPA Criteria API, these operators are handled by the `key()` and `value()` methods of the `MapJoin` interface. Consider the following example assuming a `Map` join across the phones relationship of the `Employee` entity:

```sql
SELECT  e.name, 
        KEY(p), 
        VALUE(p)
FROM    Employee e JOIN 
        e.phones p
```

To create this query using the JPA Criteria API, we need to capture the result of the join as a `MapJoin`, in this case using the `joinMap()` method. The `MapJoin` object has three type parameters: the source type, key type, and value type. It can look a little more daunting, but makes it explicit what types are involved.

```java
CriteriaQuery<Object> c                 = cb.createQuery();
Root<Employee> emp                      = c.from(Employee.class);
MapJoin<Employee,String,Phone> phone    = emp.joinMap("phones");

c.multiselect(emp.get("name"), 
              phone.key(), 
              phone.value());
```

We need to use the `joinMap()` method in this case because there is no way to overload the `join()` method to return a `Join` object or `MapJoin` object when all we are passing in is the name of the attribute. `Collection`, `Set`, and `List` relationships are likewise handled with the `joinCollection()`, `joinSet()`, and `joinList()` methods for those cases where a specific join interface must be used. The strongly typed version of
the `join()` method, which we demonstrate later, is able to handle all join types though the single `join()` call.

## 12.6 Klauza `WHERE`

Kao \v sto smo videli u nekim primerima, klauza `WHERE` u JPA Criteria API upitu se postavlja metodom `where()` iz interfejsa `AbstractQuery`. Metod `where()` ima dve varijante u zavisnosti od toga \v sta prihvata: 

- Nula ili vi\v se `Predicate` objekata 
- Jedan argument tipa `Expression<Boolean>`

Svaki poziv metoda `where()` \'ce poni\v stiti sve druge `WHERE` klauze koje su postavljene prethodnim pozivima metoda `where()` i konstruisa\'ce nov skup ograni\v cenja na osnovu novoprosle\dj enih kriterijuma.

Definicija `WHERE` klauze je data u nastavku:

```
where_clause ::= WHERE conditional_expression
```

Sada \'cemo videti na koje na\v cine je mogu\'ce konstruisati ograni\v cenja koja se prosle\dj uju metodu `where()`.

### 12.6.1 Konstrukcija izraza

Klju\v c za konstruisanje izraza u okviru JPA Criteria API je interfejs `CriteriaBuilder`. Ovaj interfejs sadr\v zi metode za sve predikate, izraze i funkcije koje HQL podr\v zava kao i druge karakteristike specifi\v cne za JPA Criteria API. Naredne tabele sumiraju preslikavanja izme\dj u HQL operatora, izraza i funkcija u njihove ekvivalentne metode interfejsa `CriteriaBuilder`. Primetimo da u nekim slu\v cajevima ne postoji direktna jednakost izme\dj u HQL operatora i metoda, ve\'c je neophodno koristiti kombinaciju metoda iz `CriteriaBuilder` kako bismo ostvarili isti rezultat. U nekim drugim slu\v cajevima, ekvivalentan metod za JPA Criteria API se nalazi u interfejsu koji nije `CriteriaBuilder`.

HQL to CriteriaBuilder Predicate Mapping:

| HQL Operator | CriteriaBuilder Method |
| --- | --- |
| AND | and() |
| OR | or() |
| NOT | not() |
| = | equal() |
| <> | notEqual() |
| > | greaterThan(),gt() |
| >= | greaterThanOrEqualTo(),ge() |
| < | lessThan(),lt() |
| <= | lessThanOrEqualTo(),le() |
| BETWEEN | between() |
| IS NULL | isNull() |
| IS NOT NULL | isNotNull() |
| EXISTS | exists() |
| NOT EXISTS | not(exists()) |
| IS EMPTY | isEmpty() |
| IS NOT EMPTY | isNotEmpty() |
| MEMBER OF | isMember() |
| NOT MEMBER OF | isNotMember() |
| LIKE | like() |
| NOT LIKE | notLike() |
| IN | in() |
| NOT IN | not(in()) |

HQL to CriteriaBuilder Scalar Expression Mapping:

| HQL Expression | CriteriaBuilder Method |
| --- | --- |
| ALL | all() |
| ANY | any() |
| SOME | some() |
| - | neg(),diff() |
| + | sum() |
| * | prod() |
| / | quot() |
| COALESCE | coalesce() |
| NULLIF | nullif() |
| CASE | selectCase() |

HQL to CriteriaBuilder Function Mapping:

| HQL Function | CriteriaBuilder Method |
| --- | --- |
| ABS | abs() |
| CONCAT | concat() |
| CURRENT_DATE | currentDate() |
| CURRENT_TIME | currentTime() |
| CURRENT_TIMESTAMP | currentTimestamp() |
| LENGTH | length() |
| LOCATE | locate() |
| LOWER | lower() |
| MOD | mod() |
| SIZE | size() |
| SQRT | sqrt() |
| SUBSTRING | substring() |
| UPPER | upper() |
| TRIM | trim() |

HQL to CriteriaBuilder Aggregate Function Mapping:

| HQL Aggregate Function | CriteriaBuilder Method |
| --- | --- |
| AVG | avg() |
| SUM | sum(),sumAsLong(),sumAsDouble() |
| MIN | min(),least() |
| MAX | max(),greatest() |
| COUNT | count() |
| COUNT DISTINCT | countDistinct() |

Pored direktnog prevo\dj enja HQL operatora, izraza i funkcija, postoje neke tehnike koje su specifi\v cne za JPA Criteria API koje je potrebno uzeti u obzir prilikom konstrukcije izraza. Slede\'ce sekcije detaljno razmatraju ove tehnike.

### 12.6.2 Predikati

U narednom primeru, prosledili smo niz objekata `Predicate` metodu `and()`. Ovim se dobija pona\v sanje koje kombinuje sve izraze operatorom `AND`. Ekvivalentno pona\v sanje, samo za operatom `OR`, ostvaruje se metodom `or()`. Postoji pre\v cica koja radi za `AND` operator, a to je da se svi izrazi proslede kao argumenti metodu `where()`. Ovaj pristup \'ce implicitno kombinovati izraze kori\v s\'cenjem semantike operatora `AND`.

```java
List<Predicate> criteria = new ArrayList<Predicate>();
if (name != null) {
    ParameterExpression<String> p = cb.parameter(String.class, "name");
    criteria.add(cb.equal(emp.get("name"), p));
}
if (deptName != null) {
    ParameterExpression<String> p = cb.parameter(String.class, "dept");
    criteria.add(cb.equal(emp.get("dept").get("name"), p));
}

if (criteria.size() == 0) {
    throw new RuntimeException("no criteria");
} else if (criteria.size() == 1) {
    c.where(criteria.get(0));
} else {
    c.where(cb.and(criteria.toArray(new Predicate[0])));
}

TypedQuery<Employee> q = em.createQuery(c);
if (name != null) { q.setParameter("name", name); }
if (deptName != null) { q.setParameter("dept", deptName); }

// ...
```

The Criteria API also offers a different style of building `AND` and `OR` expressions for those who wish to build things incrementally rather than as a list. The `conjunction()` and `disjunction()` methods of the `CriteriaBuilder` interface create `Predicate` objects that always resolve to `true` and `false`, respectively. Once obtained, these primitive predicates can then be combined with other predicates to build up nested conditional expressions in a tree-like fashion. The following example rewrites the predication construction portion of the previous example using the `conjunction()` method. Note how each conditional statement is combined with its predecessor using an `and()` call.

```java
Predicate criteria = cb.conjunction();
if (name != null) {
    ParameterExpression<String> p =
        cb.parameter(String.class, "name");
    criteria = cb.and(criteria, cb.equal(emp.get("name"), p));
}
if (deptName != null) {
    ParameterExpression<String> p =
        cb.parameter(String.class, "dept");
    criteria = cb.and(criteria,
                      cb.equal(emp.get("dept").get("name"), p));
}

// ...
```

\v Sto se ti\v ce ostalih predikata, u tabelama za predikate iznad treba napomenuti da postoje dva skupa metoda koji su dostupni za relaciona pore\dj enja. Na primer, postoje metodi `greaterThan()` i `gt()`. Metodi koji imaju naziv od dva karaktera su specifi\v cni za numeri\v cke vrednosti i strogo su tipizirani da rade sa brojevima. Metodi koji pripadaju skupu sa du\v zim nazivom se mogu koristiti u svim ostalim slu\v cajevima.

### 12.6.3 Doslovne vrednosti

Literal values may require special handling when expressed with the JPA Criteria API. In all the cases encountered so far, methods are overloaded to work with both `Expression` objects and Java literals. However, there may be some cases where only an `Expression` object is accepted (in cases where it is assumed you would never pass in a literal value or when any of a number of types would be acceptable). If you encounter this situation then, to use these expressions with Java literals, the literals must be wrapped using the `literal()` method. 

`NULL` doslovne vrednosti se kreiraju pozivom metoda `nullLiteral()`, koji prihvata tip klase kao parametar i proizvodi tipiziranu verziju vrednosti `NULL` za datu prosle\dj enu klasu. Ovo je neophodno radi pro\v sirivanja stroge tipiziranosti API-ja na `NULL` vrednosti.

### 12.6.4 Parametri (parametarske oznake)

Upravljanje parametrima u JPA Criteria API upitima se razlikuje od pristupa u HQL-u. Ako se prisetimo, u HQL-u upitima se parametri navode dvota\v ckom koju prati naziv parametra. U JPA Criteria API-u ovo ne funkcioni\v se. Umesto toga, moramo da eksplicitno kreiramo `ParameterExpression` korektnog tipa koji se mo\v ze koristiti u uslovnim izrazima. Ovo se izvodi metodom `parameter()` interfejsa `CriteriaBuilder`. Ovaj metod zahteva tip klase (kako bi postavio tip objekta `ParameterExpression`) i opciono ime koje \'ce koristiti u slu\v caju imenovanih parametara. Neka je potrebno izdvojiti podatke narednim upitom:

```sql
SELECT  s
FROM    Student s
WHERE   s.studijskiProgram.naziv = :ns
```

Naredni kod ilustruje kori\v s\'cenje ovog metoda:

```java
CriteriaQuery<Student> c    = cb.createQuery(Student.class);
Root<Student> stud          = c.from(Student.class);

c.select(stud);

ParameterExpression<String> nazivStudijskogPrograma =
    cb.parameter(String.class, "ns");

c.where(cb.equal(stud.get("studijskiProgram").get("naziv"), 
                 nazivStudijskogPrograma));
```

Ako se parametar ne\'ce koristiti u drugim delovima upita, onda ga je mogu\'ce ugnezditi direktno u predikatskom izrazu da bismo u\v cinili ceo upit konciznijim. Naredni kod ilustruje ovu tehniku:

```java
CriteriaQuery<Student> c    = cb.createQuery(Student.class);
Root<Student> stud           = c.from(Employee.class);

c.select(stud)
 .where(cb.equal(stud.get("studijskiProgram").get("naziv"),
                 cb.parameter(String.class, "ns")));
```

Da bismo izvr\v sili ovaj upit, o\v cigledno, neophodno je da postavimo vrednost datog parametra. U prvom pristupu, mo\v zemo koristiti preoptere\'cenje koje prihvata `ParameterExpression` objekat:

```java
TypedQuery<Student> query = session.createQuery(c);
query.setParameter(nazivStudijskogPrograma, "Informatika");
List<Student> results = query.getResultList();
```

U drugom pristupu, mo\v zemo koristiti preoptere\'cenje koje prihvata `String` koji sadr\v zi naziv imenovanog parametra:

```java
TypedQuery<Student> query = session.createQuery(c);
query.setParameter("ns", "Informatika");
List<Student> results = query.getResultList();
```

### 12.6.5 Podupiti

The `AbstractQuery` interface provides the `subquery()` method for creation of subqueries. Subqueries may be correlated (meaning that they reference a root, path, or join from the parent query) or non-correlated. The JPA Criteria API supports both correlated and non-correlated subqueries, again using query roots to tie the various clauses and expressions together. The argument to `subquery()` is a class instance representing the result type of the subquery. The return value is an instance of `Subquery`, which is itself an extension of `AbstractQuery`. With the exception of restricted methods for constructing  clauses, the `Subquery` instance is a complete query definition like `CriteriaQuery` that may be used to create both simple and complex queries.

To demonstrate subquery usage, let’s look at a more significant example which uses subqueries to eliminate duplicates. The `Employee` entity has relationships with four other entities: single-valued relationships with `Department` and `Address`, and collection-valued relationships with `Phone` and `Project`. Whenever we join across a collection-valued relationship, we have the potential to return duplicate rows; therefore, we need to change the criteria expression for `Project` to use a subquery. The following code fragment shows the required code:

```java
CriteriaBuilder cb          = em.getCriteriaBuilder();
CriteriaQuery<Employee> c   = cb.createQuery(Employee.class);
Root<Employee> emp = c.from(Employee.class);

c.select(emp);

// ...

if (projectName != null) {
    Subquery<Employee> sq           = c.subquery(Employee.class);
    Root<Project> project           = sq.from(Project.class);
    Join<Project,Employee> sqEmp    = project.join("employees");

    sq.select(sqEmp)
      .where(cb.equal(project.get("name"),
                      cb.parameter(String.class, "project")));
    criteria.add(cb.in(emp).value(sq));
}

// ...
```

We introduced a new non-correlated subquery against `Project`. Because the subquery declares its own root and does not reference anything from the parent query, it runs independently and is therefore non-correlated. The equivalent HQL query with only `Project` criteria would be:

```sql
SELECT  e
FROM    Employee e
WHERE   e IN (
            SELECT  emp
            FROM    Project p JOIN 
                    p.employees emp
            WHERE   p.name = :project
        )
```

Whenever we write queries that use subqueries, there is often more than one way to achieve a particular result. For example, we could rewrite the previous example to use `EXISTS` instead of `IN` and shift the conditional expression into the `WHERE` clause of the subquery:

```java
if (projectName != null) {
    Subquery<Project> sq            = c.subquery(Project.class);
    Root<Project> project           = sq.from(Project.class);
    Join<Project,Employee> sqEmp    = project.join("employees");

    sq.select(project)
      .where(cb.equal(sqEmp, emp),
             cb.equal(project.get("name"),
                      cb.parameter(String.class,"project")));
    criteria.add(cb.exists(sq));
}
```

By referencing the `Employee` root from the parent query in the `WHERE` clause of the subquery, we now have a correlated subquery. This time the query takes the following form in HQL:

```sql
SELECT  e
FROM    Employee e
WHERE   EXISTS (
            SELECT  p
            FROM    Project p JOIN 
                    p.employees emp
            WHERE   emp = e AND 
                    p.name = :name
        )
```

We can still take this example further and reduce the search space for the subquery by moving the reference to the `Employee` root to the `FROM` clause of the subquery and joining directly to the list of projects specific to that employee. In HQL, we would write this as follows:

```sql
SELECT  e
FROM    Employee e
WHERE   EXISTS (
            SELECT  p
            FROM    e.projects p
            WHERE   p.name = :name
        )
```

In order to re-create this query using the JPA Criteria API, we are confronted with a dilemma. We need to base the query on the `Root` object from the parent query but the `from()` method only accepts a persistent class type. The solution is the `correlate()` method from the `Subquery` interface. It performs a similar function to the `from()` method of the `AbstractQuery` interface, but does so with `Root` and `Join` objects from the parent query. The following example demonstrates how to use `correlate()` in this case:

```java
if (projectName != null) {
    Subquery<Project> sq            = c.subquery(Project.class);
    Root<Employee> sqEmp            = sq.correlate(emp);
    Join<Employee,Project> project  = sqEmp.join("projects");

    sq.select(project)
      .where(cb.equal(project.get("name"),
             cb.parameter(String.class,"project")));
    criteria.add(cb.exists(sq));
}
```

Before we leave subqueries in the JPA Criteria API, there is one more corner case with correlated subqueries to explore: referencing a join expression from the parent query in the `FROM` clause of a subquery. Consider the following example that returns projects containing managers with direct reports earning an average salary higher than a userdefined threshold:

```sql
SELECT  p
FROM    Project p JOIN 
        p.employees e
WHERE   TYPE(p) = DesignProject AND
        e.directs IS NOT EMPTY AND
        (SELECT AVG(d.salary)
         FROM   e.directs d) >= :value
```

When creating the Criteria API query definition for this query, we must correlate the employees attribute of `Project` and then join it to the direct reports in order to calculate the average salary. This example also demonstrates the use of the `type()` method of the `Path` interface in order to do a polymorphic comparison of types:

```java
CriteriaQuery<Project> c        = cb.createQuery(Project.class);
Root<Project> project           = c.from(Project.class);
Join<Project,Employee> emp      = project.join("employees");
Subquery<Number> sq             = c.subquery(Number.class);
Join<Project,Employee> sqEmp    = sq.correlate(emp);
Join<Employee,Employee> directs = sqEmp.join("directs");

c.select(project)
  .where(cb.equal(project.type(), DesignProject.class),
         cb.isNotEmpty(emp.<Collection>get("directs")),
         cb.ge(sq.select(cb.avg(directs.get("salary"))),
               cb.parameter(Number.class, "value")));
```

### 12.6.6 Izraz `IN`

Za razliku od drugih operatora, operator `IN` zahteva specijalno upravljanje u JPA Criteria API. Metod `in()` interfejsa `CriteriaBuilder` prihvata samo jedan argument-jednovrednosni izraz koji \'ce biti testiran nad skupom vrednosti u `IN` izrazu. Da bismo postavili skup vrednosti, potrebno je da koristimo `CriteriaBuilder.In` objekat koji se dobija kao povratna vrednost metoda `in()`. Posmatrajmo naredni HQL upit:

```sql
SELECT  s
FROM    Student s
WHERE   s.studijskiProgram.naziv IN ('Informatika', 'Matematika', 'Astronomija')
```

Da bismo pretvorili ovaj upit u JPA Criteria API upit, potrebno je da pozovemo metod `value()` iz interfejsa `CriteriaBuilder.In` da bismo postavili skup vrednosti za nazive studijskih programa za koje smo zainteresovani, kao u narednom kodu:

```java
CriteriaQuery<Student> c   = cb.createQuery(Student.class);
Root<Student> stud         = c.from(Student.class);

c.select(stud)
 .where(cb.in(stud.get("studijskiProgram").get("naziv"))
          .value("Informatika").value("Matematika").value("Astronomija"));
```

Primetimo ulan\v cane pozive metoda `value()` kako bismo postavili skup vrednosti za `IN` izraz. Argument metoda `in()` je izraz koji \'ce biti upore\dj en sa vrednostima iz skupa vrednosti koje su postavljene metodom `value()`.

Specijalno, u slu\v cajevima kada postoji veliki broj poziva `value()` metoda od kojih su sve vrednosti istog tipa, interfejsa `Expression` omogu\'cava pre\v cicu za kreiranje `IN` izraza. Metod `in()` iz ovog interfejsa dozvoljava jednu ili vi\v se vrednosti da budu postavljene jednim pozivom, \v sto naredni kod ilustruje:

```java
CriteriaQuery<Student> c   = cb.createQuery(Student.class);
Root<Student> stud         = c.from(Student.class);

c.select(stud)
 .where(stud.get("studijskiProgram").get("naziv")
            .in("Informatika", "Matematika", "Astronomija"));
```

U ovom pristupu, poziv metoda `in()` predstavlja ulan\v cani sufiks nad izrazom umesto prefiksni, kao \v sto je to bilo u prethodnom primeru. Primetimo razliku u tipovima argumenata izme\dj u dve verzije metoda `in()` iz interfejsa `CriteriaBuilder` i `Expression`. Verzija iz interfejsa `Expression` prihvata skup vrednosti, a ne izraz koji \'ce biti upore\dj en. Sa druge strane, verzija iz interfejsa `CriteriaBuilder` dozvoljava vi\v se tipizirani pristup. Odabir izme\dj u ova dva pristupa je uglavnom stvar preference ili konvencije koju kompanija koristi.

`IN` expressions that use subqueries are written using a similar approach. For a more complex example, in the previous chapter, we demonstrated a HQL query using an `IN` expression in which the department of an employee is tested against a list generated from a subquery. The example is reproduced here.

```sql
SELECT  e
FROM    Employee e
WHERE   e.department IN (
            SELECT  DISTINCT d
            FROM    Department d JOIN 
                    d.employees de JOIN 
                    de.project p
            WHERE   p.name LIKE 'QA%'
        )
```

We can convert this example to the JPA Criteria API, as shown in the following code:

```java
CriteriaQuery<Employee> c       = cb.createQuery(Employee.class);
Root<Employee> emp              = c.from(Employee.class);
Subquery<Department> sq         = c.subquery(Department.class);
Root<Department> dept           = sq.from(Department.class);
Join<Employee,Project> project  = dept.join("employees").join("projects");

sq.select(dept.<Integer>get("id"))
  .distinct(true)
  .where(cb.like(project.<String>get("name"), 
                 "QA%"));

c.select(emp)
 .where(cb.in(emp.get("dept").get("id"))
          .value(sq));
```

The subquery is created separately and then passed into the `value()` method as the expression to search for the `Department` entity. This example also demonstrates using an attribute expression as a value in the search list.

### 12.6.7 Izraz `CASE`

Like the `IN` expression, building `CASE` expressions with the Criteria API requires the use of a helper interface. We begin with the general form of the `CASE` expression, the most powerful but also the most complex.

```sql
SELECT  p.name,
        CASE 
            WHEN TYPE(p) = DesignProject THEN 'Development'
            WHEN TYPE(p) = QualityProject THEN 'QA'
            ELSE 'Non-Development'
        END
FROM    Project p
WHERE   p.employees IS NOT EMPTY
```

The `selectCase()` method of the `CriteriaBuilder` interface is used to create the `CASE` expression. For the general form, it takes no arguments and returns a `CriteriaBuilder.Case` object that we may use to add the conditional expressions to the `CASE` statement. The following example demonstrates this approach:

```java
CriteriaQuery<Object[]> c   = cb.createQuery(Object[].class);
Root<Project> project       = c.from(Project.class);

c.multiselect(project.get("name"),
              cb.selectCase()
                .when(cb.equal(project.type(), DesignProject.class),
                      "Development")
                .when(cb.equal(project.type(), QualityProject.class),
                      "QA")
                .otherwise("Non-Development"))
 .where(cb.isNotEmpty(project.<List<Employee>>get("employees")));
```

The `when()` and `otherwise()` methods correspond to the `WHEN` and `ELSE` keywords from HQL. Unfortunately, `else` is already a keyword in Java, so `otherwise` must be used as a substitute.

The next example simplifies the previous example down to the simple form of the `CASE` statement.

```sql
SELECT  p.name,
        CASE TYPE(p)
            WHEN DesignProject THEN 'Development'
            WHEN QualityProject THEN 'QA'
            ELSE 'Non-Development'
        END
FROM    Project p
WHERE   p.employees IS NOT EMPTY
```

In this case, we pass the primary expression to be tested to the `selectCase()` method and use the `when()` and `otherwise()` methods of the `CriteriaBuilder.SimpleCase` interface. Rather than a predicate or boolean expression, these methods now accept single-valued expressions that are compared to the base expression of the `CASE` statement:

```java
CriteriaQuery<Object[]> c   = cb.createQuery(Object[].class);
Root<Project> project       = c.from(Project.class);

c.multiselect(project.get("name"),
              cb.selectCase(project.type())
                .when(DesignProject.class, "Development")
                .when(QualityProject.class, "QA")
                .otherwise("Non-Development"))
 .where(cb.isNotEmpty(project.<List<Employee>>("employees")));
```

### 12.6.8 Izraz `COALESCE`

The next example we cover in this subsection concerns the HQL `COALESCE` expression:

```sql
SELECT  COALESCE(d.name, d.id)
FROM    Department d
```

Building a `COALESCE` expression with the JPA Criteria API requires a helper interface like the other examples we have looked at in this section, but it is closer in form to the `IN` expression than to the `CASE` expressions. Here we invoke the `coalesce()` method without arguments to get back a `CriteriaBuilder.Coalesce` object that we then use the `value()` method of to add values to the `COALESCE` expression. The following example demonstrates this approach:

```java
CriteriaQuery<Object> c     = cb.createQuery();
Root<Department> dept       = c.from(Department.class);

c.select(cb.coalesce()
           .value(dept.get("name"))
           .value(dept.get("id")));
```

Convenience versions of the `coalesce()` method also exist for the case where only two expressions are being compared.

```java
CriteriaQuery<Object> c     = cb.createQuery();
Root<Department> dept       = c.from(Department.class);

c.select(cb.coalesce(dept.get("name"),
                     dept.get("id")));
```

A final note about case expressions is that they are another exception to the rule that the `CriteriaBuilder` methods are non-mutating. Each `when()` method causes another conditional expression to be added incrementally to the case expression, and each `value()` method adds an additional value to the coalesce list.

### 12.6.9 Izrazi sa funkcijama

Not to be confused with the built-in functions of HQL, criteria function expressions are the JPA Criteria API equivalent of the `FUNCTION` keyword in HQL. They allow native SQL stored functions to be mixed with other JPA Criteria API expressions. They are intended for cases where a limited amount of native SQL is required to satisfy some requirement but you don’t want to convert the entire query to SQL.

Function expressions are created with the `function()` method of the `CriteriaBuilder` interface. It requires as arguments the database function name, the expected return type, and a variable list of arguments, if any, that should be passed to the function. The return type is an `Expression`, so it can be used in many other places within the query. The following example invokes a database function to capitalize the first letter of each word in a department name:

```java
CriteriaQuery<String> c     = cb.createQuery(String.class);
Root<Department> dept       = c.from(Department.class);

c.select(cb.function("initcap", 
                     String.class, 
                     dept.get("name")));
```

As always, developers interested in maximizing the portability of their applications should be careful in using function expressions. Unlike native SQL queries, which are clearly marked, function expressions are a small part of what otherwise looks like a normal portable JPA query that is actually tied to database-specific behavior.

### 12.6.10 Klauza `ORDER BY`

Metod `orderBy()` interfejsa `CriteriaQuery` postavlja ure\dj enje u definiciji upita. Ovaj metoda prihvata jedan ili vi\v se `Order` objekata, koji se kreiraju pozivom metoda `asc()` ili `desc()` interfejsa `CriteriaBuilder`, za kreiranje rastu\'cih ili opadaju\'cih ure\dj enja, redom. Naredni primer ilustruje metod `orderBy()`:

```java
CriteriaQuery<Tuple> c  = cb.createQuery(Tuple.class);
Root<Student> stud      = c.from(Student.class);
Join<Student,StudijskiProgram> studijskiProgram = stud.join("studijskiProgram");

c.multiselect(studijskiProgram.get("naziv"), 
              stud.get("ime"),
              stud.get("prezime"));
c.orderBy(cb.desc(studijskiProgram.get("naziv")),
          cb.asc(stud.get("prezime")));
```

Argumenti metoda `asc()` i `desc()` moraju biti jednovrednosni izrazi, tipi\v cno formirani na osnovu atributa entiteta. Redosled u kojem se argumenti prosle\dj uju metodu `orderBy()` defini\v su generisani SQL. Ekvivalentan HQL upit za prethodni primer je:

```sql
SELECT      sp.naziv
            s.ime,
            s.prezime
FROM        Student s JOIN 
            s.studijskiProgram sp
ORDER BY    sp.naziv DESC, 
            s.prezime
```

### 12.6.11 Klauze `GROUP BY` i `HAVING`

Metodu `groupBy()` i `having()` interfejsa `AbstractQuery` predstavlja JPA Criteria API ekvivalente klauzama `GROUP BY` i `HAVING` u HQL-u, redom. Oba metoda prihvataju jedan ili vi\v se izraza koji se koriste za grupisanje i filtriranje podataka.

Pogledajmo kako je `GROUP BY` klauza definisana:

```
groupby_clause  ::= GROUP BY groupby_item {, groupby_item}*
groupby_item    ::= single_valued_path_expression | identification_variable
```

Pogledajmo kako je `HAVING` klauza definisana:

```
having_clause   ::= HAVING conditional_expression
```

Do ovog trenutka u poglavlju, trebalo bi da \v citaocu bude intuitivan na\v cin kori\v s\'cenja ovih metoda. Pogledajmo naredni HQL upit: 

```sql
SELECT      s.indeks, 
            COUNT(i)
FROM        Student s JOIN 
            s.ispiti i
GROUP BY    s.indeks
HAVING      COUNT(i) BETWEEN 20 AND 30
```

Da bismo rekreirali ovaj primer u JPA Criteria API, potrebno je da koristimo i agregatne funkcije i metode za grupisanje. Naredni primer demonstrira ovu konverziju:

<!-- 
select      student0_.indeks as col_0_0_, 
            count(ispiti1_.id_predmeta) as col_1_0_ 
from        dosije student0_ inner join 
            ispit ispiti1_ on student0_.indeks=ispiti1_.indeks 
group by    student0_.indeks 
having      count(ispiti1_.id_predmeta) between 20 and 30 
-->

```java
CriteriaQuery<Tuple> c      = cb.createTupleQuery();
Root<Student> stud          = c.from(Student.class);
Join<Student,Ispit> ispit   = stud.join("ispiti");

c.multiselect(stud.get("indeks"), cb.count(ispit))
 .groupBy(stud)
 .having(cb.between(cb.count(ispit), 
                    cb.literal(new Long(20)), 
                    cb.literal(new Long(30))));
```

{% include lab/exercise.html broj="12.1" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate i JPA Criteria API-ja izdvaja podatke o studentima bez duplikata \v cija imena ili prezimena po\v cinju slovom `P` i koji su ro\dj eni u Beogradu ili Kragujevcu. Rezultat urediti po mestu ro\dj enja opadaju\'ce, pa po imenu i prezimenu rastu\'ce. Ispisati mesto stanovanja, indeks, ime i prezime za date studente." %}

Re\v senje: Dajemo dva re\v senja. Prvo od njih koristi pristup sa metodom `select()` u kojem se izdvajaju objekti klase `Student`. Drugo od njih koristi pristup sa metodom `multiselect()` da izdvoji samo informacije od zna\v caja.

include_source(vezbe/primeri/poglavlje_12/src/zadatak_12_1/Main.java, java)

{% include lab/exercise.html broj="12.2" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate i JPA Criteria API-ja izdvaja nazive svih polo\v zenih predmeta za studenta \v ciji se indeks u\v citava sa standardnog ulaza. Ispisati prvo ime i prezime studenta, pa zatim spisak njegovih polo\v zenih ispita." %}

Re\v senje: Dodajemo implementaciju u vidu klase `Predmet` za tabelu `PREDMET` kako bismo mogli da pristupimo nazivu predmeta. Potrebno je dopuniti i klasu `Ispit` radi ostvarivanja veze izme\dj u ove dve klase. Kona\v cno, dodajemo klasu `Predmet` u listu anotiranih klasa.

include_source(vezbe/primeri/poglavlje_12/src/zadatak_12_2/Predmet.java, java)
include_source(vezbe/primeri/poglavlje_12/src/zadatak_12_2/Ispit.java, java)
include_source(vezbe/primeri/poglavlje_12/src/zadatak_12_2/HibernateUtil.java, java)
include_source(vezbe/primeri/poglavlje_12/src/zadatak_12_2/Main.java, java)



## 12.7 Zadaci za vežbu

{% include lab/exercise.html broj="12.3" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate i JPA Criteria API-ja izdvaja sve podatke o studentima koji su upisali fakultet godine koja se unosi sa standardnog ulaza." %}

{% include lab/exercise.html broj="12.4" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate i JPA Criteria API-ja za svaki predmet izdvaja oznaku i naziv, a zatim i spisak uslovnih predmeta. Za svaki uslovni predmet izdvojiti oznaku, naziv i broj ESPB bodova." %}

{% include lab/exercise.html broj="12.5" tekst="Napisati Java aplikaciju koja kori\v s\'cenjem biblioteke Hibernate i JPA Criteria API-ja za studenta, čiji se indeks unosi sa standardnog ulaza, pravi izveštaj o položenim ispitima i uspehu po upisanim školskim godinama. Za svaku upisanu godinu ispisati naziv predmeta, datum polaganja i ocenu, a zatim prosečnu ocenu." %}
