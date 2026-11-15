# 2. mérföldkő – Első működő verzió

## 1. Áttekintés

A SmartShop egy közös bevásárlólistát kezel, amelynek tételeit a kiválasztott üzlet kategóriasorrendje rendezi. Az első működő verzió React és TypeScript frontendből, Spring Boot backendből és PostgreSQL-adatbázisból áll; Docker Compose-zal együtt indítható.

A fő folyamat megvalósult: üzletválasztás, kategóriaelrendezés beállítása, tételek felvétele és a rendezett lista alapján történő bevásárlás. A fejlesztés a [nyilvános GitHub-repository](https://github.com/TTomi48/SmartShop2.0) commit- és pull request-előzményeiben követhető. A működési ellenőrzések nem helyettesítik a még hátralévő böngészős teszteket.

## 2. Megvalósított funkciók

A funkciók a [specifikáció EF1–EF8 feltételeihez](specification.md) kapcsolódnak:

- **Üzletek (EF1):** új üzlet létrehozása és kiválasztása; üres adatbázison két mintaüzlet eltérő kategóriasorrenddel. Üzletváltáskor a tételek és jelölések megmaradnak.
- **Kategóriák (EF2):** közös kategóriakészlet és új kategóriák létrehozása; az új kategória külön rendelhető az üzletekhez.
- **Bolti elrendezés és sorrend (EF3, EF5):** kategóriák hozzárendelése, eltávolítása és fel/le mozgatása üzletenként; a lista a bolti sorrendet, kategórián belül a hozzáadás sorrendjét követi.
- **Listaelemek (EF4):** létrehozás, módosítás és törlés; opcionális pozitív mennyiség, egyedi mértékegység és legfeljebb 250 karakteres megjegyzés. Azonos nevű tételek külön tárolódnak.
- **Hiányzó kategória (EF6):** a tétel a lista végére kerül, figyelmeztetéssel és aláhúzással; továbbra is megvásároltnak jelölhető.
- **Vásárlási állapot (EF7):** kipipálás és visszavonás, hátralévő tételek nézete, új bevásárlás és megvásárolt tételek törlése.
- **Tárolás és hibakezelés (EF8):** az adatok és az üzletválasztás PostgreSQL-ben maradnak meg; a releváns hibás bemenetek elutasítását backendvalidáció biztosítja.

## 3. Ellenőrzések és teszteredmények

A backend- és frontendtesztek, a lint és a build a dokumentáció átdolgozásakor újra lefutottak. A Docker-, integrációs és adatmegőrzési eredmények a 2026. november 15-i működési ellenőrzésből származnak; ezeket az átdolgozás során nem futtattuk újra.

| Ellenőrzés | Eredmény | Bizonyíték |
|---|---|---|
| Backendtesztek | 104 sikeres; 0 sikertelen, hibás vagy kihagyott | [Service-tesztek](../backend/src/test/java/hu/bme/backend/services/) és [REST-tesztek](../backend/src/test/java/hu/bme/backend/controllers/RestApiTest.java) |
| Frontendtesztek | 26 sikeres; 0 sikertelen | [Validáció és API-kliens](../frontend/tests/frontend.test.mjs), [React-renderelés](../frontend/tests/ui-row.test.mjs) |
| Frontend build és lint | Mindkettő sikeres, kilépési kód: 0 | [Futtatási parancsok](../frontend/README.md#testing) |
| Docker Compose indítás | Sikeres build; frontend, backend és adatbázis egészséges | [Compose-konfiguráció](../compose.yaml), [indítási útmutató](../README.md#running-the-application) |
| API-integráció | A fő folyamat ellenőrzése sikeres, kilépési kód: 0 | [Élő tesztszkript](../frontend/tests/live-flow.mjs), [reprodukálás](testing.md#integráció-és-adatmegőrzés) |
| Adatmegőrzés | A konténerek újralétrehozása után a mentett állapot egyezett | Ugyanazon szkript `--verify-persistence` futása; [ellenőrzött adatok](testing.md#bizonyítékok) |
| Böngészős manuális tesztek | Még nem végrehajtottak | [Öt kézi forgatókönyv és elvárt eredmény](testing.md#böngészős-ellenőrzések) |

A backend egységtesztjei mockolt repositorykat, a REST-tesztek mockolt service-eket használnak. A frontend alaptesztek mockolt HTTP-válaszokat és statikus React-renderelést ellenőriznek; az élő teszt valódi nginx–Spring Boot–PostgreSQL kapcsolaton futott.

## 4. Javított hibák

| Probléma | Megoldás | Ellenőrzés |
|---|---|---|
| Hibás specifikáció- és AI-dokumentum-hivatkozások | Helyes relatív utak és [AI-döntési index](ai-decisions/README.md) | A belső Markdown-hivatkozások célfájljainak ellenőrzése |
| Friss adatbázison hiányzó mintaadatok | Két üzlet és hat kategória inicializálása kizárólag üres adatbázison | [Négy inicializálási teszt](../backend/src/test/java/hu/bme/backend/services/InitialDataServiceTest.java) és élő integráció |
| Hiányzó kategóriánál a terméknév nem volt aláhúzva | A terméknév megkapja a figyelmeztető megjelenítést | [React-renderelési teszt](../frontend/tests/ui-row.test.mjs); a jelző változása élő teszttel ellenőrizve |
| A tizedes mennyiség tároláskor kerekedett | Skálakorlát nélküli PostgreSQL `numeric` oszlop | Az élő tesztben `1.125` és `0.001` pontos visszaolvasása |
| A kis tizedes mennyiség megjelenítése nullára kerekedhetett | Pontosabb számformázás | A renderelési teszt ellenőrzi a `0.0001 kg` szöveget |
| Hiányzott az integráció és adatmegőrzés ismételhető ellenőrzése | Élő tesztszkript és külön [tesztelési útmutató](testing.md) | Sikeres főfolyamat-teszt és újralétrehozás utáni állapot-összehasonlítás |

## 5. Ismert korlátok és további ellenőrzések

- A böngészős űrlapkezelés, navigáció, megerősítő ablakok, vizuális figyelmeztetések és oldalfrissítés ellenőrzése még hátravan. Az API-tesztek ezeket nem igazolják.
- A mennyiségoszlop javítását friss tesztadatbázison ellenőriztük. Meglévő adatbázis sémamódosítása külön ellenőrzést igényel; a korábban lekerekített értékek elveszett számjegyei nem állíthatók vissza.
- A külön, Docker nélküli fejlesztői indítás nincs végigpróbálva; a teljes alkalmazás Docker Compose-os indítása ellenőrzött.

## 6. Indítás és tesztelés

- [Alkalmazás indítása, szükséges eszközök és adatbázis-beállítások](../README.md#running-the-application).
- [Backendtesztek futtatása és helyi fejlesztés](../README.md#development-and-testing).
- [Frontendtesztek, lint és build](../frontend/README.md#testing).
- [Integrációs teszt és adatmegőrzési ellenőrzés](testing.md#integráció-és-adatmegőrzés).
- [Böngészős ellenőrzések](testing.md#böngészős-ellenőrzések).
