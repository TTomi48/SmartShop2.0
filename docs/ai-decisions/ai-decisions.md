# SmartShop – AI-fejlesztési döntések

## AD-01 – Üzletenkénti kategóriasorrend adatmodellje

- **Dátum:** 2026-11-09
- **Fázis:** követelményelemzés és tervezés
- **Követelmények:** EF1–EF3, EF8
- **Eszköz:** ChatGPT (GPT-6)
- **Állapot:** döntés megszületett; implementáció és ellenőrzés még hátravan

**Probléma és kontextus.** A SmartShopban a kategóriák globálisak, de üzletenként eltérő sorrendben szerepelhetnek. Egy üzletben ugyanaz a kategória csak egyszer helyezhető el, más üzletek sorrendjét pedig nem szabad befolyásolni. Technológiák: Java, Spring Boot, Spring Data JPA, PostgreSQL.

**AI-nak adott feladat.** A felhasználó a SmartShop specifikációja alapján az Entity-k kialakításáról, majd az első AI-döntés lehetséges megoldásainak közös megvitatásáról kérdezett. Az AI három lehetőséget hasonlított össze:

| Megoldás | Értékelés |
|---|---|
| `position` a globális `Category`-ban | Nem kezeli az üzletenként eltérő sorrendet. |
| Külön `StoreCategory(store, category, position)` entitás | Üzletenkénti sorrend, relációs integritás; cserébe plusz entitás. |
| Rendezett kategóriaazonosítók JSON-listája a `Store`-ban | Kevesebb tábla, de nehezebb az egyediség és a hivatkozások ellenőrzése. |

**AI-javaslat.** A `StoreCategory` kapcsolóentitás használata, `Store` és `Category` felé mutató kapcsolatokkal, valamint `UNIQUE(store_id, category_id)` adatbázis-korláttal.

**Saját döntés és indoklás.** **Elfogadom a `StoreCategory` megoldást.** Ez volt az első saját ötletem is, és az alternatívák átolvasása után ezt tartom a legjobb kompromisszumnak: az üzletenkénti sorrend külön tárolható, a duplikált elhelyezés korlátozható, miközben a modell áttekinthető marad.

**Ellenőrzési terv (még nem futtatott tesztek).**
1. Két üzlet ugyanazokkal a kategóriákkal eltérő sorrendet tárol; egyik átrendezése nem érinti a másikat.
2. Ugyanaz a kategória egy üzlethez másodszor nem rendelhető hozzá.
3. Új globális kategória nem kerül automatikusan az üzletekhez.

**Bizonyíték:** a `StoreCategory` entitás, a kapcsolódó repository/service-tesztek és a futtatási eredmények hivatkozásai az implementáció után kerülnek ide. Az adatmodell önmagában még nem igazolja az összes megjelenítési szabályt.
