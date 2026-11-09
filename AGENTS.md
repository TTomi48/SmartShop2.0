# AGENTS.md – SmartShop fejlesztési útmutató AI-asszisztenseknek

## 1. Projekt és elsődleges követelmények
A SmartShop egy üzletfüggő bevásárlólista. A kiválasztott üzlethez rendelt kategóriák sorrendje határozza meg a még meg nem vásárolt tételek megjelenítését; ez nem útvonaltervező.

- **Elsődleges forrás:** `specification.md`, különösen az EF1–EF8 elfogadási feltételek. A követelményeket ne változtasd meg önállóan.
- **Frontend:** React + TypeScript + Vite.
- **Backend:** Java + Spring Boot + Maven + Spring Data JPA + Bean Validation + Lombok.
- **Adatbázis:** PostgreSQL. A tényleges Java-/Spring-verziókat a `backend/pom.xml` határozza meg.
- **Hatókör:** egy közös bevásárlólista, regisztráció nélkül. Ne vezess be autentikációt, árakat, GPS-t, útvonal-optimalizálást vagy egyéb, nem kért funkciót.

Ha egy követelmény nem egyértelmű, sorold fel a lehetséges értelmezéseket, és kérj döntést a megvalósítás előtt. Ne állítsd, hogy egy feladat elkészült, ha nincs ellenőrizve.

## 2. Javasolt kódszerkezet és felelősségek

- `backend/`: `entity`, `repository`, `service`, `controller`, `dto`, `exception` csomagok; tesztek a `src/test` alatt.
- `frontend/`: komponensek és nézetek, API-kommunikáció, TypeScript típusok. Az adatbázist kizárólag a backend éri el.
- Az üzleti szabályok helye a backend service réteg; a controller kérés-/válaszkezeléssel foglalkozzon.
- A REST API-hoz lehetőség szerint DTO-kat használj; JPA entitásokat ne tegyél automatikusan API-válasszá.
- Az entitásoknál a Lombok `@Getter`, `@Setter`, `@NoArgsConstructor` használható; a JPA-kapcsolatok miatt ne alkalmazz automatikusan `@Data`-t minden entitásra.
- Közös `Category` és üzletenkénti `StoreCategory` kapcsolat szükséges a pozíció tárolásához. Egy üzletben egy kategória legfeljebb egyszer fordulhat elő.
- Azonos terméknevek külön listaelemként tárolandók; a kipipálás és az üzletválasztás tartós állapot.
- A frontend figyelmeztetése nem helyettesíti a backend validációt. Hibás művelet ne módosítsa a tárolt adatokat.
- Kövesd a projekt meglévő elnevezési és formázási szabályait; ne adj hozzá új keretrendszert indoklás nélkül.

## 3. Munkamódszer minden feladatnál

1. Olvasd el a releváns EF-követelményt és a kapcsolódó meglévő kódot.
2. Röviden foglald össze, mi változik és mely rétegek érintettek. Jelezd a feltételezéseket.
3. Csak az adott feladathoz szükséges, kicsi és áttekinthető módosítást javasold/valósítsd meg.
4. A lényeges üzleti szabályhoz készíts vagy frissíts automatizált tesztet; normál, határ- és hibás esetet is vizsgálj, ha releváns.
5. Futtasd a rendelkezésre álló ellenőrzéseket, például a `backend/` mappából Windows alatt `.\mvnw.cmd test`, Linux/macOS alatt `./mvnw test`; a `frontend/` mappából `npm run build` és a konfigurált lint parancsot.
6. Válaszodban különítsd el: **módosítás**, **ellenőrzés és eredménye**, **nyitott kérdés/kockázat**. Nem futtatott tesztről ne állítsd, hogy sikeres.
7. A specifikációt vagy meglévő adatokat érintő változás előtt kérj jóváhagyást. Titkokat, jelszavakat és valódi adatbázis-hozzáféréseket ne írj forráskódba vagy commitba.

## 4. Git-commit üzenetek – Conventional Commits

A projektben a megadott **Git Commit Patterns** cikk által ismertetett **Conventional Commits** üzenetformátumot használjuk. Ez *commitüzenet-konvenció*, nem szoftvertervezési minta.

**Forrás:** https://dev.to/hornet_daemon/git-commit-patterns-5dm7

**Formátum:**

```text
<type>(<scope>): <subject>

[optional body]

[optional footer]
```

- A **type kötelező**; a **scope**, a hosszabb törzs és a footer opcionális.
- Megengedett típusok: `feat`, `fix`, `refactor`, `style`, `test`, `chore`, `docs`, `build`, `perf`, `ci`, `revert`.
- A `subject` legyen rövid, konkrét, felszólító módú angol kifejezés (pl. `add`, `validate`, `preserve`), ne múlt idejű; ne legyen általános (`updates`, `changes`).
- Javasolt scope-ok: `backend`, `frontend`, `db`, `docs`, `tests`; szükség esetén pontosíthatók `/` jellel, pl. `backend/store`.
- Egy commit egy összefüggő változást tartalmazzon, és csak **egy** típusa legyen. Funkció (`feat`), hibajavítás (`fix`), teszt (`test`) és dokumentáció (`docs`) lehetőség szerint különüljön el.
- A `build` a buildet/alkalmazásfüggőségeket érintő, a `chore` az általános fejlesztési karbantartási változtatásokat jelöli. Ha bizonytalan a típus, fontold meg a módosítás felbontását.
- Breaking change esetén csak indokoltan használj `!` jelölést, és magyarázd el a változást a commit törzsében vagy footerében.

**Példák:**

```text
feat(backend/store): add store creation endpoint
feat(backend/category): persist store-specific category order
test(backend): cover missing-category sorting
fix(frontend): preserve purchased state after store switch
docs: document EF1-EF8 acceptance criteria
build(backend): update managed dependencies
```

A commitüzenet-javaslatot a tényleges diff alapján készítsd el. **Ne futtass `git commit`, `git push`, `git reset --hard` vagy átíró Git-műveleteket a felhasználó kifejezett kérése nélkül.**

## 5. Tesztelés, mérföldkövek, bizonyítékok

- Minden EF1–EF8 követelményhez legyen egyértelmű ellenőrzési mód: automatikus teszt vagy reprodukálható manuális lépéssor elvárt eredménnyel.
- Kiemelten teszteld: az üzletenként független sorrendet; duplikált üzlet–kategória kapcsolat elutasítását; mennyiség és megjegyzés határeseteit; hiányzó kategóriát; vásárlási állapot megőrzését; újraindítás utáni adatmegőrzést.
- A házi feladatban szükséges legalább egy ismerten hibás verzión elbukó és javított verzión átmenő teszt, **mindkét futás bizonyítékával**. A szándékosan előállított hibát mindig jelöld annak.
- A 2. mérföldkő egy működő fő felhasználói folyamatot, indítási útmutatót, alapteszteket és publikus GitHubon követhető állapotot követel. A rögzített verziót célszerű `milestone-2` taggel megjelölni, de ez nem kötelező Git-formátum.
- A később kapott oktatói változtatási igényt a rögzített 2. mérföldköves változatból követhetően vezesd át, regressziós tesztekkel.

## 6. AI-használat dokumentálása

A `README.md` hivatkozzon a `docs/ai-decisions.md` dokumentumra. Legalább három fejlesztési fázisban (pl. követelményelemzés, implementáció, tesztelés) legyen tényleges és igazolt AI-használat.

Három **valós** érdemi döntési esethez jegyezd fel: (1) probléma és kapcsolódó EF, (2) AI-eszköz/modell, (3) átadott kontextus és fontos prompt, (4) AI-javaslat, (5) saját döntés és szakmai indoklás, (6) kód-/teszt-/futtatási bizonyíték és korlátai. Ne találj ki promptokat, teszteredményeket vagy korábban nem megtörtént döntéseket.

Az AGENTS.md létezése önmagában nem bizonyít használatot: a fejlesztési munka során alkalmazd a szabályait, és dokumentáld az ellenőrizhető eredményeket.
