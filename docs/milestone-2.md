# 2. mérföldkő – Első működő verzió

## 1. Áttekintés

A SmartShop egy közös bevásárlólistát kezel, amelynek tételeit a kiválasztott üzlet kategóriasorrendje rendezi. Az első működő verzió React és TypeScript frontendből, Spring Boot backendből és PostgreSQL-adatbázisból áll; Docker Compose-zal együtt indítható.

A fő folyamat megvalósult: üzletválasztás, kategóriaelrendezés beállítása, tételek felvétele és a rendezett lista alapján történő bevásárlás. A fejlesztés a [nyilvános GitHub-repository](https://github.com/TTomi48/SmartShop2.0) commit- és pull request-előzményeiben követhető. A felhasználó az öt fő böngészős forgatókönyvet manuálisan végrehajtotta; az eredmény megfelelő, a közben feltárt mennyiségkerekítési hibát külön kezeljük.

## 2. Megvalósított funkciók

A funkciók a [specifikáció EF1–EF8 feltételeihez](specification.md) kapcsolódnak:

- **Üzletek (EF1):** új üzlet létrehozása és kiválasztása; üres adatbázison két mintaüzlet eltérő kategóriasorrenddel. Üzletváltáskor a tételek és jelölések megmaradnak.
- **Kategóriák (EF2):** közös kategóriakészlet és új kategóriák létrehozása; az új kategória külön rendelhető az üzletekhez.
- **Bolti elrendezés és sorrend (EF3, EF5):** kategóriák hozzárendelése, eltávolítása és fel/le mozgatása üzletenként; a lista a bolti sorrendet, kategórián belül a hozzáadás sorrendjét követi.
- **Listaelemek (EF4):** létrehozás, módosítás és törlés; opcionális pozitív mennyiség, egyedi mértékegység és legfeljebb 250 karakteres megjegyzés. A backend a mennyiséget három tizedesjegyre, `HALF_UP` szabállyal kerekíti; nullára kerekedő érték nem menthető. A bevitel vesszőt és pontot is elfogad, a megjelenítés vesszős, záró nullák nélkül. Azonos nevű tételek külön tárolódnak.
- **Hiányzó kategória (EF6):** a tétel a lista végére kerül, figyelmeztetéssel és aláhúzással; továbbra is megvásároltnak jelölhető.
- **Vásárlási állapot (EF7):** kipipálás és visszavonás, hátralévő tételek nézete, új bevásárlás és megvásárolt tételek törlése.
- **Tárolás és hibakezelés (EF8):** az adatok és az üzletválasztás PostgreSQL-ben maradnak meg; a releváns hibás bemenetek elutasítását backendvalidáció biztosítja.

## 3. Ellenőrzések és teszteredmények

A backend- és frontendtesztek, a lint, a build, valamint az élő API-integráció és az újralétrehozás utáni adatmegőrzés ellenőrzése a mennyiségkerekítés javítása után, 2026. november 15-én sikeresen lefutottak. A felhasználó ezt követően a módosított mennyiségkezelés kézi visszaellenőrzését is lezártnak jelezte.

| Ellenőrzés | Eredmény | Bizonyíték |
|---|---|---|
| Backendtesztek | 120 sikeres; 0 sikertelen, hibás vagy kihagyott | [Service-tesztek](../backend/src/test/java/hu/bme/backend/services/) és [REST-tesztek](../backend/src/test/java/hu/bme/backend/controllers/RestApiTest.java) |
| Frontendtesztek | 36 sikeres; 0 sikertelen | [Validáció és API-kliens](../frontend/tests/frontend.test.mjs), [React-renderelés](../frontend/tests/ui-row.test.mjs) |
| Frontend build és lint | Mindkettő sikeres, kilépési kód: 0 | [Futtatási parancsok](../frontend/README.md#testing) |
| Docker Compose indítás | Sikeres build; frontend, backend és adatbázis egészséges | [Compose-konfiguráció](../compose.yaml), [indítási útmutató](../README.md#running-the-application) |
| API-integráció | A fő folyamat és a kerekítés create/update/read ellenőrzése sikeres, kilépési kód: 0 | [Élő tesztszkript](../frontend/tests/live-flow.mjs), [reprodukálás](testing.md#integráció-és-adatmegőrzés) |
| Adatmegőrzés | A konténerek újralétrehozása után a mentett állapot egyezett | Ugyanazon szkript `--verify-persistence` futása; [ellenőrzött adatok](testing.md#bizonyítékok) |
| Böngészős manuális tesztek | A felhasználó mind az öt fő forgatókönyvet végrehajtotta, megfelelő eredménnyel; a kerekítési javítás kézi visszaellenőrzése is lezárva a visszajelzése alapján | [Felhasználói tesztbeszámoló és forgatókönyvek](testing.md#böngészős-ellenőrzések) |

A backend egységtesztjei mockolt repositorykat, a REST-tesztek mockolt service-eket használnak. A frontend alaptesztek mockolt HTTP-válaszokat és statikus React-renderelést ellenőriznek; az élő teszt valódi nginx–Spring Boot–PostgreSQL kapcsolaton futott.

## 4. Javított hibák

| Probléma | Megoldás | Ellenőrzés |
|---|---|---|
| Hibás specifikáció- és AI-dokumentum-hivatkozások | Helyes relatív utak és [AI-döntési index](ai-decisions/README.md) | A belső Markdown-hivatkozások célfájljainak ellenőrzése |
| Friss adatbázison hiányzó mintaadatok | Két üzlet és hat kategória inicializálása kizárólag üres adatbázison | [Négy inicializálási teszt](../backend/src/test/java/hu/bme/backend/services/InitialDataServiceTest.java) és élő integráció |
| Hiányzó kategóriánál a terméknév nem volt aláhúzva | A terméknév megkapja a figyelmeztető megjelenítést | [React-renderelési teszt](../frontend/tests/ui-row.test.mjs); a jelző változása élő teszttel ellenőrizve |
| Kézi teszteléskor háromnál több tizedesjegy is megjelent (`1,1255`) | Backendoldali `HALF_UP` normalizálás létrehozáskor és módosításkor, változatlan adatbázissémával; a korrekció implementálva | Unit- és élő API-tesztek sikeresek; a felhasználó a javítás kézi visszaellenőrzését is lezártnak jelezte |
| A kis mennyiségek és a tizedeselválasztók kezelése nem volt egységes | Nullára kerekedő érték elutasítása; a frontend csak elválasztót normalizál és a mentett értéket jeleníti meg | Backend- és frontendtesztek: `0.0009` → `0.001`, `0.0001` → hiba; vesszős/pontos bevitel és vesszős megjelenítés |
| Hiányzott az integráció és adatmegőrzés ismételhető ellenőrzése | Élő tesztszkript és külön [tesztelési útmutató](testing.md) | Sikeres főfolyamat-teszt és újralétrehozás utáni állapot-összehasonlítás |

## 5. Ismert korlátok és további ellenőrzések

- Az öt fő manuális forgatókönyv és a mennyiségkezelés javításának kézi visszaellenőrzése a felhasználó visszajelzése alapján lezárult. Ezek manuális eredmények; automatizált böngészős tesztet nem futtattunk.
- Nem készült adatbázis-migráció vagy tömeges adatmódosítás. A régi, háromnál több tizedesjeggyel mentett tételekre az új szabály a következő szerkesztés és mentés alkalmával érvényesül; a korábban elveszett számjegyek nem állíthatók vissza.
- A külön, Docker nélküli fejlesztői indítás nincs végigpróbálva; a teljes alkalmazás Docker Compose-os indítása ellenőrzött.

## 6. Indítás és tesztelés

- [Alkalmazás indítása, szükséges eszközök és adatbázis-beállítások](../README.md#running-the-application).
- [Backendtesztek futtatása és helyi fejlesztés](../README.md#development-and-testing).
- [Frontendtesztek, lint és build](../frontend/README.md#testing).
- [Integrációs teszt és adatmegőrzési ellenőrzés](testing.md#integráció-és-adatmegőrzés).
- [Böngészős ellenőrzések](testing.md#böngészős-ellenőrzések).
