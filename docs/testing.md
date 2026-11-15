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

- A backend JUnit-jelentései a `backend/target/surefire-reports/` könyvtárban keletkeznek. A korábbi 104 teszt megoszlása: REST 34, üzletválasztás 8, inicializálás 4, tételek 37, bolti elrendezés 21.
- A frontend 26 alaptesztjéből 25 a validációt és az API-klienst, egy a React-sor renderelését ellenőrzi. Az eredmény a `npm test` kimenetében látható.
- Az élő teszt valódi HTTP-kérésekkel ellenőrzi a mintaadatokat, új üzleteket/kategóriákat, független sorrendet, duplikációkat, tételműveleteket, vásárlási állapotot és hibás módosítások elutasítását.
- A `frontend/tests/evidence/live-snapshot.json` tartalmazza az újralétrehozás előtti üzleteket, kategóriákat, elrendezéseket, tételeket és rendezett listát, beleértve a mennyiségeket, vásárlási jelöléseket és a kiválasztott üzletet. A külön ellenőrző futás ennek egyezését vizsgálja.
- A meglévő, opcionális `npm run test:regression` egy szándékosan hibás ideiglenes másolat és a javított API-kliens 204-válaszkezelését hasonlítja össze. Kimenete: `frontend/tests/evidence/regression-204.txt`; korábbi eredménye: hibás másolat exit 1, javított exit 0.

A generált jelentések és az `evidence` fájlok helyi, Gitből kizárt állományok; a dokumentáció átdolgozása ezeket nem törölte. Friss klónozás után a parancsokkal újra előállíthatók. A tesztforrások a repository részei.

## Böngészős ellenőrzések

Előfeltétel: külön tesztalkalmazás a [README](../README.md#running-the-application) szerint elindítva, böngészőben megnyitva. Az alábbi esetek végrehajtása még szükséges; az API-szintű eredmények nem jelentik a UI sikeres ellenőrzését. Használj egyedi tesztneveket, és rögzítsd a tényleges eredményt.

| Eset | Lépések | Elvárt eredmény |
|---|---|---|
| Üzletek és kategóriák (EF1–EF3) | Categories: hozz létre két kategóriát. Stores: két új üzlet Layout nézetében rendeld hozzá őket fordított sorrendben; próbálj ismét azonos kategóriát hozzáadni. | Új üzlet üres; a kategóriák nem kerülnek bele automatikusan; a sorrend üzletenként független; duplikáció nem hozható létre. |
| Tételműveletek (EF4, EF8) | New item: név, kategória, `1.125`, egyedi egység, 250 karakteres megjegyzés. Vedd fel kétszer; az egyik Edit nézetében ürítsd az opcionális mezőket. Próbálj 0, negatív és szöveges mennyiséget. Jelölj, vond vissza, majd törölj egy tételt. | Külön tételek, pontos mennyiség, törölhető opcionális mezők; hibás bevitel nem menthető; csak a kiválasztott tétel törlődik. |
| Sorrend és hiányzó kategória (EF3, EF5–EF6) | Előbb Tej, majd Kenyér; a bolti elrendezésben Pékáru felülre. Válts üzletet, majd vissza. Távolítsd el a Tej kategóriáját, pipáld a tételt, végül rendeld vissza a kategóriát. | A bolti sorrend érvényesül; a hiányos tétel a végén, piros aláhúzással és figyelmeztetéssel jelenik meg, pipálható; visszahelyezés után a figyelmeztetés eltűnik. |
| Vásárlási műveletek (EF7) | Két tételből egyet jelölj; Remaining, All, New shopping trip megerősítéssel. Ismét jelölj egyet, majd Delete purchased. Végül ellenőrizd a minden megvásárolva és az üres lista állapotát. | A szűrés helyes; az új bevásárlás csak jelöléseket töröl; a megvásároltak törlése csak a jelölteket távolítja el; az üzenetek érthetők. |
| Frissítés és újraindítás (EF1, EF8) | Legyen két tétel, egyik pipálva; válassz üzletet, nyomj F5-öt. Jegyezd fel a listát és elrendezést. Ugyanazon tesztprojekten `down`, majd `up -d --wait`, kötet törlése nélkül; ismét F5. | Az üzletválasztás, tételek, mezőértékek, jelölések, kategóriák és elrendezések megmaradnak. |
