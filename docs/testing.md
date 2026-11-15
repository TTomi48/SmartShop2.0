# Tesztelési útmutató

Az alaptesztek parancsai a [gyökér README-ben](../README.md#development-and-testing) és a [frontend README-ben](../frontend/README.md#testing) találhatók. A mérföldkő eredményeit a [2. mérföldkő dokumentuma](milestone-2.md) foglalja össze.

## Integráció és adatmegőrzés

Előfeltétel: működő Docker Compose, Node.js 24 és telepített frontendfüggőségek (`npm ci` a `frontend` könyvtárban).
Az [élő teszt](../frontend/tests/live-flow.mjs) kizárólag új, elkülönített tesztadatbázison futtatható: adatokat hoz létre, globális jelölés-visszaállítást és megvásárolttétel-törlést is végez.

1. Másold a gyökérben a `.env.example` fájlt `.env.test` néven; adj meg tesztjelszót és egy szabad `SMARTSHOP_PORT` értéket.
2. Válassz egy még nem használt Compose-projektnevet. Az alábbi `smartshop-test` csak példa; minden Compose-parancsnál ugyanazt a nevet használd.
3. A gyökérből, PowerShell alatt futtasd:

```powershell
docker compose --env-file .env.test -p smartshop-test up --build -d --wait --wait-timeout 180
cd frontend
$env:SMARTSHOP_TEST_URL = 'http://localhost:' + (Read-Host 'A .env.test fájlban megadott SMARTSHOP_PORT')
node tests/live-flow.mjs --disposable
cd ..
docker compose --env-file .env.test -p smartshop-test down
docker compose --env-file .env.test -p smartshop-test up -d --wait --wait-timeout 180
cd frontend
node tests/live-flow.mjs --verify-persistence
```

Mindkét szkriptfutás elvárt kilépési kódja 0. A `down` parancshoz ne adj `--volumes` kapcsolót: az ellenőrzés ugyanazon adatbáziskötet megőrzését vizsgálja.
A főfolyamat-teszt újbóli adatfelviteléhez új projekt és üres adatbázis szükséges; a `--verify-persistence` az elmentett állapothoz hasonlít, ezért változatlan tesztadatokon ismételhető.

## Bizonyítékok

- A backend JUnit-jelentései a `backend/target/surefire-reports/` könyvtárban keletkeznek. A 120 sikeres teszt megoszlása: REST 34, üzletválasztás 8, inicializálás 4, tételek 53, bolti elrendezés 21. A mennyiségtesztek létrehozáskor és módosításkor is ellenőrzik az egész, 1–3 és több tizedesjegyes értékeket, a `HALF_UP` kerekítést és a nullára kerekedő bemenet elutasítását.
- A frontend 36 alaptesztjéből 35 a validációt, az API-klienst és a szerkesztő mezőértékeit, egy a React-sor renderelését ellenőrzi. A `1,5`/`1.5` és `1,1255`/`1.1255` azonos numerikus értékként jut a backendhez, kliensoldali kerekítés nélkül; a lista és a visszanyitott űrlap a mentett értéket vesszővel jeleníti meg.
- Az élő teszt valódi HTTP-kérésekkel ellenőrzi a mintaadatokat, új üzleteket/kategóriákat, független sorrendet, duplikációkat, tételműveleteket, vásárlási állapotot és hibás módosítások elutasítását.
- A kerekítés javítása utáni élő futás sikeres: egész, 1–3 és több tizedesjegyes mennyiség, `HALF_UP` felfelé/lefelé, opcionális mennyiség, nullára kerekedő bemenet, létrehozás/módosítás és pontos visszaolvasás ellenőrizve. A konténerek újralétrehozása után a snapshot egyezett, benne az `1.1255` bemenetből mentett `1.126` értékkel. Ez API-szintű eredmény, a módosított böngészős működés kézi visszaellenőrzését a felhasználó ezt követően lezártnak jelezte.
- A `frontend/tests/evidence/live-snapshot.json` tartalmazza az újralétrehozás előtti üzleteket, kategóriákat, elrendezéseket, tételeket és rendezett listát, beleértve a mennyiségeket, vásárlási jelöléseket és a kiválasztott üzletet. A külön ellenőrző futás ennek egyezését vizsgálja.
- A meglévő, opcionális `npm run test:regression` egy szándékosan hibás ideiglenes másolat és a javított API-kliens 204-válaszkezelését hasonlítja össze. Kimenete: `frontend/tests/evidence/regression-204.txt`; korábbi eredménye: hibás másolat exit 1, javított exit 0.

A generált jelentések és az `evidence` fájlok helyi, Gitből kizárt állományok; a dokumentáció átdolgozása ezeket nem törölte. Friss klónozás után a parancsokkal újra előállíthatók. A tesztforrások a repository részei.

## Böngészős ellenőrzések

Előfeltétel: külön tesztalkalmazás a [README](../README.md#running-the-application) szerint elindítva, böngészőben megnyitva. A felhasználó mind az öt fő forgatókönyvet manuálisan végrehajtotta, és a működést megfelelőnek találta. Beszámolója kiterjedt az üzlet- és kategóriakezelésre, tételműveletekre, hibás bemenetekre, sorrendre és figyelmeztetésekre, valamint a vásárlási folyamatra, oldalfrissítésre és Docker Compose újraindítás utáni adatmegőrzésre. Ezek felhasználói manuális eredmények; automatizált böngészős teszt nem futott.

A tesztelés külön hibát tárt fel: az `1,1255` négy tizedesjeggyel is megjelent. A három tizedesjegyes `HALF_UP` korrekció implementálva és unit tesztekkel ellenőrizve; a felhasználó a módosított UI működésének kézi visszaellenőrzését ezt követően lezártnak jelezte. Az alábbi lépéssor a már végrehajtott fő forgatókönyveket őrzi meg; eredményük a beszámoló alapján megfelelő; a külön feltárt mennyiségkerekítési hiba javításának kézi visszaellenőrzése is lezárult.

| Eset | Lépések | Elvárt eredmény |
|---|---|---|
| Üzletek és kategóriák (EF1–EF3) | Categories: hozz létre két kategóriát. Stores: két új üzlet Layout nézetében rendeld hozzá őket fordított sorrendben; próbálj ismét azonos kategóriát hozzáadni. | Új üzlet üres; a kategóriák nem kerülnek bele automatikusan; a sorrend üzletenként független; duplikáció nem hozható létre. |
| Tételműveletek (EF4, EF8) | New item: név, kategória, `1.125`, egyedi egység, 250 karakteres megjegyzés. Vedd fel kétszer; az egyik Edit nézetében ürítsd az opcionális mezőket. Próbálj 0, negatív és szöveges mennyiséget. Jelölj, vond vissza, majd törölj egy tételt. | Külön tételek, pontos mennyiség, törölhető opcionális mezők; hibás bevitel nem menthető; csak a kiválasztott tétel törlődik. |
| Sorrend és hiányzó kategória (EF3, EF5–EF6) | Előbb Tej, majd Kenyér; a bolti elrendezésben Pékáru felülre. Válts üzletet, majd vissza. Távolítsd el a Tej kategóriáját, pipáld a tételt, végül rendeld vissza a kategóriát. | A bolti sorrend érvényesül; a hiányos tétel a végén, piros aláhúzással és figyelmeztetéssel jelenik meg, pipálható; visszahelyezés után a figyelmeztetés eltűnik. |
| Vásárlási műveletek (EF7) | Két tételből egyet jelölj; Remaining, All, New shopping trip megerősítéssel. Ismét jelölj egyet, majd Delete purchased. Végül ellenőrizd a minden megvásárolva és az üres lista állapotát. | A szűrés helyes; az új bevásárlás csak jelöléseket töröl; a megvásároltak törlése csak a jelölteket távolítja el; az üzenetek érthetők. |
| Frissítés és újraindítás (EF1, EF8) | Legyen két tétel, egyik pipálva; válassz üzletet, nyomj F5-öt. Jegyezd fel a listát és elrendezést. Ugyanazon tesztprojekten `down`, majd `up -d --wait`, kötet törlése nélkül; ismét F5. | Az üzletválasztás, tételek, mezőértékek, jelölések, kategóriák és elrendezések megmaradnak. |

### A kerekítési javítás kézi visszaellenőrzése – lezárva

Új tételnél és Edit nézetben is írd be a `1,5`, `1.5`, `1,1255`, `1.1255`, `1,1254`, `0,0009`, `2,9999` értékeket. A szöveges, decimális bevitelt jelző mezőnek mindkét elválasztót el kell fogadnia. Mentés után rendre `1,5`, `1,5`, `1,126`, `1,126`, `1,125`, `0,001`, `3` jelenjen meg a listában és a visszanyitott szerkesztőben. Az üres mennyiség elfogadott; a `0,0001`, nulla, negatív és nem numerikus mennyiség elutasítandó, változatlan tárolt adatokkal. F5 és újraindítás után is a mentett kerekített érték maradjon meg. **Tényleges eredmény: a felhasználó visszajelzése alapján a mennyiségkezelés kézi visszaellenőrzése lezárva, megfelelő eredménnyel. Az ellenőrzést a felhasználó manuálisan végezte, nem automatizált böngészős teszt.**
