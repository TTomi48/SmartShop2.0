# SmartShop – Üzletfüggő bevásárlólista

## 1. Cél és technológia
A SmartShop webalkalmazás a bevásárlólista tételeit a kiválasztott üzlet kategóriasorrendje szerint jeleníti meg, ezzel segítve a bolti bevásárlást. A felhasználó üzleteket és globális termékkategóriákat hozhat létre, a kategóriákat üzletenként helyezheti el és rendezheti. A rendezés nem valódi útvonal-optimalizálás.

**Frontend:** React + TypeScript; **backend:** Java + Spring Boot; **adatbázis:** PostgreSQL.

## 2. Szerepkör és fő forgatókönyv
Egy szerepkör van: **vásárló**. Nincs regisztráció; az alkalmazás egy közös bevásárlólistát kezel. A három nézet: **Bevásárlólista**, **Bevásárlási mód**, **Üzletek és kategóriák**. A vásárló üzletet választ, szükség esetén beállítja annak kategóriasorrendjét, termékeket vesz fel, majd a rendezett listán megvásároltnak jelöli őket.

## 3. Elfogadási feltételek

**EF1 – Üzletek.** Induláskor két mintaüzlet érhető el, eltérő kategóriasorrenddel. Új üzlet egyedi, nem üres névvel hozható létre; kezdetben nincs hozzá elhelyezett kategória. Üzletváltáskor a listaelemek és kipipálások megmaradnak, csak a sorrend és a figyelmeztetések változhatnak.

**EF2 – Termékkategóriák.** Induláskor közös, globális kategóriák vannak (pl. Zöldség-gyümölcs, Pékáru, Tejtermékek, Húsáru, Italok, Háztartás). Új kategória létrehozható, de egyik üzletbe sem kerül be automatikusan. Üres vagy már létező kategórianév nem fogadható el, kis- és nagybetűtől függetlenül.

**EF3 – Bolti elrendezés.** Az üzlethez kategóriák rendelhetők, fel/le gombokkal átrendezhetők, illetve onnan eltávolíthatók. Egy kategória egy üzletben legfeljebb egyszer szerepelhet. Az első elem feljebb, illetve az utolsó lejjebb mozgatása nem változtat a sorrenden; más üzletek elrendezése érintetlen marad.

**EF4 – Termékek kezelése.** Kötelező a terméknév és a globális kategória. A pozitív egész vagy tizedes mennyiség, a listából választható vagy egyedileg beírható mértékegység és a legfeljebb 250 karakteres megjegyzés egymástól függetlenül opcionális. A tételek módosíthatók, törölhetők. Azonos nevű termék két felvétele két külön listaelem.

**EF5 – Bevásárlási sorrend.** A még nem megvásárolt tételek a kiválasztott üzlet kategóriasorrendjében, kategóriánként csoportosítva jelennek meg; kategórián belül a hozzáadás sorrendje számít. Ha a Pékáru a Tejtermékek előtt van, a Kenyér a Tej előtt látszik akkor is, ha később vették fel.

**EF6 – Hiányzó kategória.** Ha egy termék kategóriája nincs elhelyezve az adott üzletben, a tétel a lista végére kerül, pirossal aláhúzva, „A kategória nincs elhelyezve ebben az üzletben” figyelmeztetéssel. A termék ekkor is kipipálható. A kategória elhelyezése után a figyelmeztetés eltűnik.

**EF7 – Vásárlási állapot.** A termék kipipálható, a jelölés visszavonható. A megvásárolt termék az alaplistán marad, de a hátralévő nézetből eltűnik. Az „Új bevásárlás” minden jelölést visszaállít; a „Megvásároltak törlése” csak a kipipált tételeket törli. Üres listánál és az összes termék megvásárlásakor megfelelő üzenet jelenik meg.

**EF8 – Hibakezelés és adatmegőrzés.** Üres név, érvénytelen üzlet-/kategóriaazonosító, duplikált üzlet–kategória kapcsolat, hibás (0, negatív, nem numerikus) mennyiség és 250 karakternél hosszabb megjegyzés esetén a művelet elutasítandó, az adatok nem változhatnak. Az üzletek, kategóriák, elrendezések, termékek, kipipálások és a kiválasztott üzlet PostgreSQL-ben tárolódnak, újraindítás után megmaradnak.

## 4. Korlátok és értelmezések
A termék kategóriája globális; a kategória üzletenkénti elhelyezése ettől független. A hiányzó kategória nem jelent készlethiányt. Nem része a feladatnak: üzlet- és kategóriatörlés, automatikus termékfelismerés, árak, valódi bolti alaprajz, GPS, többfelhasználós működés és mobilra optimalizálás. A mennyiség elhagyható, de ha meg van adva, pozitív számnak kell lennie.
