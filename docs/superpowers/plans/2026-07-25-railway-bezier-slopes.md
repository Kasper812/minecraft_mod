# Плавные уклоны кривыми Безье — план фазы 2

Продолжение `2026-07-25-railway-worldgen-phase1.md`. Меняет порядок задач фазы 1:
машинерия задачи 8 (проход при загрузке чанка) нужна раньше задач 6 и 7.

**Причина.** Наклонный путь Create поднимается ровно на блок за блок, то есть 45°.
Пандус из таких блоков выглядит стеной, и это подтверждено в игре. Плавный уклон
в Create делается только кривой Безье. Она же даёт опоры-балки бесплатно
(`hasGirder`), то есть закрывает и просьбу про мосты.

---

## Проверенные факты о путях Create 6.0.8

Добыты чтением jar'а, не догадками. Не перепроверять без нужды.

### Соединение блоков

- Концы стыка прямого пути (`ZO`, `XO`) лежат **в серединах противоположных
  граней**. У диагонального (`PD`, `ND`) — **в противоположных углах**.
  Они не совпадают, поэтому **прямой и диагональный пути, поставленные рядом,
  не соединяются**. В игре это выглядит «ёлочкой» из несвязанных обрубков.
- `PD` — ось `(1, 0, 1)`, `ND` — ось `(-1, 0, 1)`. Проверено по байткоду
  `TrackShape.<clinit>`.
- Поворот в Create — **не блок**. Углового рельса не существует. Поворот и
  плавный уклон это `BezierConnection`, лежащий в данных `TrackBlockEntity`
  на обоих концах.
- Наклонные `AN`/`AS`/`AE`/`AW` поднимаются на блок за блок. Цепочка таких
  блоков связна, но два наклонных блока, встречающихся вершинами, — нет:
  их концы стыка на разной высоте. Отсюда требование, чтобы профиль высоты
  нигде не переходил из подъёма сразу в спуск.

### API

```java
// com.simibubi.create.content.trains.track.BezierConnection
public BezierConnection(Couple<BlockPos> bePositions,
                        Couple<Vec3> starts,
                        Couple<Vec3> axes,
                        Couple<Vec3> normals,
                        boolean primary,
                        boolean girder,
                        TrackMaterial material);
public BezierConnection secondary();   // зеркальная копия для второго конца
public double getLength();
public final boolean hasGirder;

// com.simibubi.create.content.trains.track.TrackBlockEntity
public void addConnection(BezierConnection connection);
public Map<BlockPos, BezierConnection> getConnections();
public void validateConnections();
```

`Couple` — из `net.createmod.catnip.data`, не из Create.

Блок пути: `AllBlocks.TRACK.getDefaultState()`, свойства `TrackBlock.SHAPE`
(enum `TrackShape`) и `TrackBlock.HAS_BE` (boolean). Для кривой **оба конца
обязаны иметь `HAS_BE = true`**, иначе `TrackBlockEntity` не создастся и
связь будет некуда положить.

### Окружение разработки

- Create подключается classifier `slim`, библиотеки объявляются отдельно с
  версиями из `META-INF/jarjar/metadata.json` его jar'а. Толстый `all` в dev
  не работает: ForgeGradle ремапит только внешний jar.
- В `runs` обязательны `mixin.env.remapRefMap = true` и
  `mixin.env.refMapRemappingFile = build/createSrgToMcp/output.srg`. Без них
  миксины Ponder падают на старте.

---

## Архитектура

Worldgen-фича не может создать кривую: она пересекает границы чанков, а
`TrackBlockEntity` второго конца лежит в чужом чанке. Поэтому работа делится:

1. **Worldgen-фича** кладёт только горизонтальные участки, оставляя **разрыв**
   на месте уклона. Оба блока по краям разрыва получают `HAS_BE = true`.
   Фича по-прежнему чистая функция от сида и координат и трогает только
   свой чанк.
2. **Проход при загрузке чанка** создаёт `BezierConnection` через разрывы и
   регистрирует всё в графе через `TrackPropagator.onRailAdded`. Здесь есть
   полноценный `ServerLevel` с загруженными соседями.

Геометрия разрыва детерминирована, поэтому проход не ищет концы перебором, а
вычисляет их из тех же `RailNetwork` и `TrackProfiler`.

---

### Задача 1: разрыв под уклон вместо пандуса

**Files:**
- Modify: `src/main/java/dev/gensh/ironroads/geometry/TrackProfiler.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/TrackBuilder.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`
- Test: `src/test/java/dev/gensh/ironroads/geometry/TrackProfilerTest.java`

**Interfaces:**
- Produces:
  - `record Ramp(int startAlong, int endAlong, int startHeight, int endHeight)`
  - `java.util.Optional<Ramp> TrackProfiler.rampAt(Line line, int along)` —
    уклон, накрывающий эту точку, если она внутри разрыва
  - `boolean TrackProfiler.isGap(Line line, int along)`

- [ ] **Step 1: Заменить пандус на разрыв в `TrackProfiler`**

Сейчас `heightAt` выдаёт цепочку наклонных блоков длиной в перепад. Вместо
этого длина уклона берётся с запасом на плавность: `span = |Δh| * SLOPE_RUN`,
где `SLOPE_RUN = 4` даёт видимый уклон около 1:4 — Безье сгладит его в дугу.
Внутри `[rampStart, rampStart + span]` блоков нет; `heightAt` там возвращать
нечего, поэтому появляется `isGap`.

Разрыв обязан целиком лежать внутри отрезка `SEGMENT` и не касаться его границ,
иначе развязка попадёт в пустоту. При `SEGMENT = 96`, `maxGradeRun = 12` и
`SLOPE_RUN = 4` перепад не превышает 8, span не превышает 32 — влезает.

- [ ] **Step 2: Тесты на разрыв**

Проверить: разрыв не пересекает границу отрезка; узел решётки (кратный 768)
никогда не внутри разрыва; `rampAt` симметричен с обеих сторон разрыва;
концы разрыва лежат на высотах соседних площадок.

- [ ] **Step 3: `TrackBuilder` ставит `HAS_BE` на концах разрыва**

Внутри разрыва не ставить ничего. На блоках, соседних с разрывом, выставить
`TrackBlock.HAS_BE = true`. Наклонные формы `AN`/`AS`/`AE`/`AW` больше не
используются — убрать их из `chooseShape` вместе с проверкой `nextY`.

- [ ] **Step 4: Проверить в игре**

Ожидается: горизонтальные участки пути с аккуратными разрывами там, где
раньше были пандусы 45°. Кривых ещё нет, разрывы пустые — это нормально.

---

### Задача 2: создание кривых при загрузке чанка

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/graph/SlopeLinker.java`
- Create: `src/main/java/dev/gensh/ironroads/graph/RailwayGraphLinker.java`
- Create: `src/main/java/dev/gensh/ironroads/graph/LinkedChunks.java`

Задачу 8 фазы 1 (`RailwayGraphLinker`, `LinkedChunks`) писать здесь же — код
из плана фазы 1 годится как есть, к нему добавляется вызов `SlopeLinker`.

- [ ] **Step 1: `SlopeLinker`**

По координатам чанка вычислить, какие разрывы начинаются в его пределах.
Для каждого: собрать `BezierConnection`.

- `bePositions` — позиции блоков на обоих концах разрыва.
- `starts` — точки стыка в мировых координатах: центр грани блока, обращённой
  внутрь разрыва, плюс полблока по высоте.
- `axes` — направление движения на входе и выходе: `(0,0,±1)` для линии вдоль
  Z, `(±1,0,0)` вдоль X. Знак — по направлению внутрь кривой.
- `normals` — вертикаль `(0,1,0)` для обоих концов: путь не наклонён набок.
- `primary = true`, `girder` — включать, когда полотно висит над рельефом
  выше порога, это и есть мост.
- `material` — `TrackMaterial.ANDESITE` либо материал уложенного блока.

Положить связь в `TrackBlockEntity` первого конца через `addConnection`, а
`secondary()` — во второй. Оба конца могут оказаться в разных чанках, поэтому
обрабатывать разрыв только когда оба чанка загружены; иначе отложить до
загрузки второго.

- [ ] **Step 2: Регистрация в графе**

После создания кривых пройти по чанку и вызвать `TrackPropagator.onRailAdded`
для каждого блока пути, как в задаче 8 плана фазы 1.

- [ ] **Step 3: Проверить в игре — главная проверка**

Поставить станцию, собрать поезд, пустить. Ожидается: поезд едет по
магистрали, плавно проходит уклоны, не останавливается на границах чанков.

---

### Задача 3: мосты и тоннели

После кривых. `girder` на кривых уже даёт часть мостов; отдельно нужны опоры
под длинными пролётами и расширенные тоннели с фонарями — по замечаниям:
тоннели выше и шире, со светом.

Материалы выбраны из Create: `RAILWAY_CASING` под путями,
`ANDESITE_SCAFFOLD` на обочинах, `METAL_GIRDER` на мостах,
`ANDESITE_BARS` на перила.

---

### Задача 4: боковые кривые и разнообразие трассы

Тот же механизм `BezierConnection`, но в горизонтальной плоскости. Только
после этого `JITTER` в `RailwayFeature` можно вернуть с нуля: линия станет
длинными прямыми, соединёнными плавными поворотами. Это и даст просьбу
«иногда прямая, иногда кривая».
