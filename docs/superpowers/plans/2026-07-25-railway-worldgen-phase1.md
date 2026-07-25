# Бесконечная ЖД сеть для Create — план реализации, фаза 1

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Мод для Minecraft 1.20.1 Forge, генерирующий бесконечную сеть путей Create — двухпутные магистрали по сетке с джиттером, насыпи, эстакады, тоннели, мосты, ответвления к станциям — с корректной регистрацией в `TrackGraph`.

**Architecture:** Два этапа. Worldgen-фича ставит только блоки, будучи чистой функцией от сида и координат. Пост-процессор на `ChunkEvent.Load` сшивает граф путей через `TrackPropagator.onRailAdded`. Вся геометрия (`RailNetwork`, `TrackProfiler`) вынесена в классы без зависимостей от Minecraft и покрыта юнит-тестами.

**Tech Stack:** Java 17, Forge 47.x (MC 1.20.1), ForgeGradle 6, Create 0.5.1.f, JUnit 5.

## Global Constraints

- Minecraft 1.20.1, Forge 47.2.0, Java 17. Ни одна задача не меняет эти версии.
- Mod id: `ironroads`. Базовый пакет: `dev.gensh.ironroads`.
- Create 0.5.1.f — единственная compile-зависимость на сторонний мод. Steam 'n' Rails в фазе 1 **не** подключается: вся используемая функциональность (`TrackBlock`, `TrackShape`, `TrackPropagator`) принадлежит Create. Мод обязан оставаться совместимым с SnR в рантайме.
- Классы `RailNetwork`, `TrackProfiler`, `HeightSampler`, `BranchPlanner` не импортируют ничего из `net.minecraft` и `com.simibubi`. Это условие тестируемости, нарушать нельзя.
- Шаг сетки 768, амплитуда джиттера 48, максимальный уклон 1:12, шаг опор 8, порог эстакада→мост 24, минимальная глубина тоннеля 6. Значения по умолчанию, выносятся в конфиг в задаче 10.
- Вся геометрия детерминирована: одинаковый сид и координаты дают одинаковый результат, независимо от порядка генерации чанков.

---

### Task 1: Каркас проекта и точка входа

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `gradle.properties`
- Create: `src/main/java/dev/gensh/ironroads/IronRoads.java`
- Create: `src/main/resources/META-INF/mods.toml`
- Create: `src/main/resources/pack.mcmeta`
- Create: `.gitignore`

**Interfaces:**
- Consumes: ничего
- Produces: `IronRoads.MOD_ID` (`String`, значение `"ironroads"`), `IronRoads.LOGGER` (`org.slf4j.Logger`), рабочая команда сборки `gradlew build`

- [ ] **Step 1: Скачать Forge MDK 1.20.1**

Скачать `forge-1.20.1-47.2.0-mdk.zip` с https://files.minecraftforge.net/ и распаковать в корень проекта. Понадобятся только `gradlew`, `gradlew.bat`, `gradle/wrapper/`. Файлы `src/main/java/com/example/`, `LICENSE.txt`, `changelog.txt` из MDK удалить.

- [ ] **Step 2: Написать `settings.gradle`**

```groovy
pluginManagement {
    repositories {
        gradlePluginPortal()
        maven { url = 'https://maven.minecraftforge.net/' }
        maven { url = 'https://maven.parchmentmc.org' }
    }
}
plugins {
    id 'org.gradle.toolchains.foojay-resolver-convention' version '0.8.0'
}
rootProject.name = 'ironroads'
```

- [ ] **Step 3: Написать `gradle.properties`**

```properties
org.gradle.jvmargs=-Xmx3G
org.gradle.daemon=false
org.gradle.parallel=true

minecraft_version=1.20.1
forge_version=47.2.0
parchment_version=2023.09.03
mod_id=ironroads
mod_version=0.1.0
mod_group_id=dev.gensh.ironroads
create_version=0.5.1.f-36
flywheel_version=0.6.10-7
registrate_version=MC1.20-1.3.3
```

- [ ] **Step 4: Написать `build.gradle`**

```groovy
plugins {
    id 'eclipse'
    id 'net.minecraftforge.gradle' version '[6.0,6.2)'
    id 'org.parchmentmc.librarian.forgegradle' version '1.+'
}

version = mod_version
group = mod_group_id
base { archivesName = mod_id }

java.toolchain.languageVersion = JavaLanguageVersion.of(17)

minecraft {
    mappings channel: 'parchment', version: "${parchment_version}-${minecraft_version}"
    copyIdeResources = true
    runs {
        client {
            workingDirectory project.file('run')
            property 'forge.logging.console.level', 'debug'
            mods { "${mod_id}" { source sourceSets.main } }
        }
        server {
            workingDirectory project.file('run-server')
            property 'forge.logging.console.level', 'debug'
            mods { "${mod_id}" { source sourceSets.main } }
        }
    }
}

repositories {
    maven { url = 'https://maven.tterrag.com/' }        // Create, Flywheel, Registrate
    maven { url = 'https://maven.blamejared.com/' }     // JEI, транзитивно у Create
    mavenCentral()                                       // JUnit
}

dependencies {
    minecraft "net.minecraftforge:forge:${minecraft_version}-${forge_version}"

    implementation fg.deobf("com.simibubi.create:create-${minecraft_version}:${create_version}:slim") {
        transitive = false
    }
    implementation fg.deobf("com.jozufozu.flywheel:flywheel-forge-${minecraft_version}:${flywheel_version}")
    implementation fg.deobf("com.tterrag.registrate:Registrate:${registrate_version}")

    testImplementation platform('org.junit:junit-bom:5.10.2')
    testImplementation 'org.junit.jupiter:junit-jupiter'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

test {
    useJUnitPlatform()
    testLogging { events 'passed', 'skipped', 'failed' }
}

tasks.named('jar') {
    manifest {
        attributes([
            'Specification-Title'   : mod_id,
            'Specification-Vendor'  : 'gensh',
            'Specification-Version' : '1',
            'Implementation-Title'  : project.name,
            'Implementation-Version': project.jar.archiveVersion,
            'Implementation-Vendor' : 'gensh'
        ])
    }
    finalizedBy 'reobfJar'
}

tasks.withType(JavaCompile).configureEach {
    options.encoding = 'UTF-8'
}
```

Отдельная обвязка для тестов не нужна: `RailNetwork` и `TrackProfiler` не зависят от Minecraft, поэтому обычный `src/test/java` работает без MDK-специфики.

- [ ] **Step 5: Написать точку входа**

`src/main/java/dev/gensh/ironroads/IronRoads.java`:

```java
package dev.gensh.ironroads;

import com.mojang.logging.LogUtils;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;

@Mod(IronRoads.MOD_ID)
public class IronRoads {
    public static final String MOD_ID = "ironroads";
    public static final Logger LOGGER = LogUtils.getLogger();

    public IronRoads() {
        LOGGER.info("IronRoads loaded");
    }
}
```

- [ ] **Step 6: Написать `mods.toml`**

`src/main/resources/META-INF/mods.toml`:

```toml
modLoader = "javafml"
loaderVersion = "[47,)"
license = "MIT"

[[mods]]
modId = "ironroads"
version = "0.1.0"
displayName = "Iron Roads"
description = "Бесконечная железнодорожная сеть на путях Create."

[[dependencies.ironroads]]
    modId = "forge"
    mandatory = true
    versionRange = "[47,)"
    ordering = "NONE"
    side = "BOTH"

[[dependencies.ironroads]]
    modId = "minecraft"
    mandatory = true
    versionRange = "[1.20.1,1.21)"
    ordering = "NONE"
    side = "BOTH"

[[dependencies.ironroads]]
    modId = "create"
    mandatory = true
    versionRange = "[0.5.1,)"
    ordering = "AFTER"
    side = "BOTH"
```

`ordering = "AFTER"` обязателен: наша фича обращается к блокам Create, они должны быть зарегистрированы раньше.

- [ ] **Step 7: Написать `pack.mcmeta`**

`src/main/resources/pack.mcmeta`:

```json
{
  "pack": {
    "description": "Iron Roads resources",
    "pack_format": 15
  }
}
```

- [ ] **Step 8: Написать `.gitignore`**

```
run/
run-server/
build/
.gradle/
.idea/
*.iml
eclipse/
```

- [ ] **Step 9: Собрать проект**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`, в `build/libs/` появился `ironroads-0.1.0.jar`.

Первый запуск скачивает и декомпилирует Minecraft — это занимает 5–15 минут. Это нормально.

- [ ] **Step 10: Инициализировать репозиторий и закоммитить**

```bash
git init
git add -A
git commit -m "chore: Forge 1.20.1 scaffold with Create dependency"
```

---

### Task 2: `RailNetwork` — геометрия сети

Чистая математика: где именно проходят линии. Ни одного импорта Minecraft.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/geometry/Line.java`
- Create: `src/main/java/dev/gensh/ironroads/geometry/RailNetwork.java`
- Test: `src/test/java/dev/gensh/ironroads/geometry/RailNetworkTest.java`

**Interfaces:**
- Consumes: ничего (задача независима от задачи 1 по коду, но требует собранного проекта)
- Produces:
  - `enum Line.Axis { X, Z }`
  - `record Line(Line.Axis axis, int index)`
  - `RailNetwork(long seed, int spacing, int jitter)`
  - `int RailNetwork.spacing()`
  - `int RailNetwork.perpendicularAt(Line line, int along)`
  - `java.util.List<Line> RailNetwork.linesNear(int blockX, int blockZ, int radius)`
  - `boolean RailNetwork.isCrossing(int blockX, int blockZ)`

Соглашение: для `Axis.Z` линия тянется вдоль Z, параметр `along` — это Z, а `perpendicularAt` возвращает X. Для `Axis.X` — зеркально.

- [ ] **Step 1: Написать `Line.java`**

```java
package dev.gensh.ironroads.geometry;

/** Одна линия сети. Для Axis.Z линия тянется вдоль Z, её изменяемая координата — X. */
public record Line(Line.Axis axis, int index) {
    public enum Axis { X, Z }
}
```

- [ ] **Step 2: Написать падающий тест**

`src/test/java/dev/gensh/ironroads/geometry/RailNetworkTest.java`:

```java
package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class RailNetworkTest {

    private static final long SEED = 123456789L;
    private static final int SPACING = 768;
    private static final int JITTER = 48;

    private RailNetwork net() {
        return new RailNetwork(SEED, SPACING, JITTER);
    }

    @Test
    void sameSeedAndPositionGiveSameResult() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int along = -2000; along <= 2000; along += 37) {
            assertEquals(net().perpendicularAt(line, along),
                         net().perpendicularAt(line, along),
                         "перпендикуляр должен быть детерминирован");
        }
    }

    @Test
    void linesPassExactlyThroughLatticeCrossings() {
        Line line = new Line(Line.Axis.Z, 3);
        for (int m = -3; m <= 3; m++) {
            int along = m * SPACING;
            assertEquals(3 * SPACING, net().perpendicularAt(line, along),
                         "в точке пересечения джиттер обязан быть нулевым");
        }
    }

    @Test
    void jitterStaysWithinAmplitude() {
        Line line = new Line(Line.Axis.Z, -2);
        int base = -2 * SPACING;
        for (int along = -5000; along <= 5000; along++) {
            int perp = net().perpendicularAt(line, along);
            assertTrue(Math.abs(perp - base) <= JITTER,
                       "смещение " + (perp - base) + " превысило амплитуду на along=" + along);
        }
    }

    @Test
    void pathNeverTurnsSharperThanFortyFiveDegrees() {
        Line line = new Line(Line.Axis.Z, 1);
        RailNetwork n = net();
        int prev = n.perpendicularAt(line, -5000);
        for (int along = -4999; along <= 5000; along++) {
            int cur = n.perpendicularAt(line, along);
            assertTrue(Math.abs(cur - prev) <= 1,
                       "скачок " + (cur - prev) + " на along=" + along
                       + ": путь нельзя уложить прямыми и диагоналями");
            prev = cur;
        }
    }

    @Test
    void differentSeedsGiveDifferentPaths() {
        Line line = new Line(Line.Axis.Z, 0);
        RailNetwork a = new RailNetwork(1L, SPACING, JITTER);
        RailNetwork b = new RailNetwork(2L, SPACING, JITTER);
        boolean anyDifference = false;
        for (int along = 100; along < 700; along++) {
            if (a.perpendicularAt(line, along) != b.perpendicularAt(line, along)) {
                anyDifference = true;
                break;
            }
        }
        assertTrue(anyDifference, "разные сиды обязаны давать разные трассы");
    }

    @Test
    void linesNearFindsBothAxesAroundOrigin() {
        List<Line> found = net().linesNear(0, 0, 64);
        assertTrue(found.contains(new Line(Line.Axis.Z, 0)));
        assertTrue(found.contains(new Line(Line.Axis.X, 0)));
    }

    @Test
    void linesNearIgnoresDistantCorridors() {
        List<Line> found = net().linesNear(SPACING / 2, SPACING / 2, 16);
        assertTrue(found.isEmpty(), "в середине ячейки сетки линий быть не должно");
    }

    @Test
    void crossingDetectedAtLatticePoint() {
        assertTrue(net().isCrossing(SPACING, -SPACING));
        assertFalse(net().isCrossing(SPACING / 2, -SPACING));
    }
}
```

- [ ] **Step 3: Запустить тест и убедиться, что он падает**

Run: `./gradlew test --tests '*RailNetworkTest*'`
Expected: FAIL — компиляция не проходит, класс `RailNetwork` не существует.

- [ ] **Step 4: Написать `RailNetwork.java`**

```java
package dev.gensh.ironroads.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Геометрия сети магистралей. Класс без состояния сверх параметров конструктора
 * и без единой зависимости от Minecraft — это условие тестируемости.
 *
 * Линии образуют сетку с шагом spacing. Каждая линия виляет на величину до
 * jitter блоков по детерминированному шуму, но в точках решётки (кратных
 * spacing) джиттер плавно гасится до нуля — так пересечения всегда попадают
 * ровно в узел решётки и обе линии сходятся без подгонки.
 */
public final class RailNetwork {

    /** Период шума. Больше значение — длиннее и плавнее изгибы. */
    private static final int JITTER_PERIOD = 400;

    /** Длина зоны гашения джиттера по обе стороны от узла решётки. */
    private static final int CROSSING_FLAT = 64;

    private final long seed;
    private final int spacing;
    private final int jitter;

    public RailNetwork(long seed, int spacing, int jitter) {
        if (spacing < 4 * CROSSING_FLAT) {
            throw new IllegalArgumentException(
                "spacing должен быть не меньше " + (4 * CROSSING_FLAT));
        }
        if (jitter < 0) {
            throw new IllegalArgumentException("jitter не может быть отрицательным");
        }
        this.seed = seed;
        this.spacing = spacing;
        this.jitter = jitter;
    }

    public int spacing() {
        return spacing;
    }

    /** Перпендикулярная координата линии в точке along вдоль неё. */
    public int perpendicularAt(Line line, int along) {
        int base = Math.multiplyExact(line.index(), spacing);
        if (jitter == 0) {
            return base;
        }
        double noise = noise1D(lineSeed(line), (double) along / JITTER_PERIOD);
        double offset = noise * jitter * crossingTaper(along);
        return base + (int) Math.round(offset);
    }

    /**
     * Все линии, чей коридор попадает в квадрат радиуса radius вокруг точки.
     * Коридор шире номинала на jitter, потому что линия внутри него виляет.
     */
    public List<Line> linesNear(int blockX, int blockZ, int radius) {
        List<Line> result = new ArrayList<>(4);
        int reach = radius + jitter;
        collect(result, Line.Axis.Z, blockX, reach);
        collect(result, Line.Axis.X, blockZ, reach);
        return result;
    }

    private void collect(List<Line> out, Line.Axis axis, int perpendicular, int reach) {
        int first = Math.floorDiv(perpendicular - reach, spacing);
        int last = Math.floorDiv(perpendicular + reach, spacing);
        for (int index = first; index <= last; index++) {
            int base = index * spacing;
            if (Math.abs(base - perpendicular) <= reach) {
                out.add(new Line(axis, index));
            }
        }
    }

    /** Точка решётки, где магистрали пересекаются. */
    public boolean isCrossing(int blockX, int blockZ) {
        return Math.floorMod(blockX, spacing) == 0
            && Math.floorMod(blockZ, spacing) == 0;
    }

    /**
     * Коэффициент 0..1: гасит джиттер вблизи узлов решётки, чтобы линия
     * приходила в пересечение строго по базовой координате.
     */
    private double crossingTaper(int along) {
        int distance = Math.floorMod(along, spacing);
        distance = Math.min(distance, spacing - distance);
        if (distance >= CROSSING_FLAT) {
            return 1.0;
        }
        double t = (double) distance / CROSSING_FLAT;
        return t * t * (3.0 - 2.0 * t);
    }

    private long lineSeed(Line line) {
        return mix(seed ^ (line.axis() == Line.Axis.X ? 0x9E3779B9L : 0x7F4A7C15L)
                        ^ ((long) line.index() * 0x165667B1L));
    }

    /** Значение-шум с кубической интерполяцией, результат в [-1, 1]. */
    private static double noise1D(long lineSeed, double t) {
        int cell = (int) Math.floor(t);
        double f = t - cell;
        double a = unitAt(lineSeed, cell);
        double b = unitAt(lineSeed, cell + 1);
        double smooth = f * f * (3.0 - 2.0 * f);
        return (a + (b - a) * smooth) * 2.0 - 1.0;
    }

    private static double unitAt(long lineSeed, int cell) {
        long h = mix(lineSeed ^ ((long) cell * 0x27D4EB2FL));
        return (h >>> 11) * 0x1.0p-53;
    }

    /** splitmix64 — быстрый и хорошо перемешивающий финализатор. */
    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
```

Про тест на 45°: амплитуда 48 при периоде 400 даёт непрерывную крутизну шума около `1.5 · 48 / 400 ≈ 0.18` блока на блок. После округления до целых координат дискретный шаг получается 0 или 1 — то есть путь укладывается прямыми и диагоналями, а шага в 2 блока не возникает никогда. Проверено прогоном: максимальный наблюдаемый шаг равен 1. Если конфиг увеличит `jitter` или уменьшит период настолько, что непрерывная крутизна приблизится к 1, появятся шаги в 2 блока и тест сломается первым — это правильно.

- [ ] **Step 5: Запустить тесты**

Run: `./gradlew test --tests '*RailNetworkTest*'`
Expected: PASS, 8 тестов.

- [ ] **Step 6: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/geometry src/test/java/dev/gensh/ironroads/geometry
git commit -m "feat: deterministic rail network geometry with crossing-anchored jitter"
```

---

### Task 3: `TrackProfiler` — продольный профиль высоты

Самая ответственная математика плана. Профиль обязан быть **позиционно-локальным**: значение в точке не должно зависеть от того, с какого места мы начали считать, иначе на границах чанков появятся ступени.

Наивное «идём вдоль пути и ограничиваем уклон на ходу» этому условию не удовлетворяет — результат зависит от точки старта. Вместо этого берётся нижняя огибающая Липшица:

```
h(t) = min по d из [-W, W] от ( smooth(t + d) + |d| / G )
```

Это наибольшая функция, не превышающая сглаженный рельеф и нигде не нарушающая уклон `1/G`. Она вычисляется независимо в каждой точке — значит, одинакова из любого чанка.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/geometry/HeightSampler.java`
- Create: `src/main/java/dev/gensh/ironroads/geometry/TrackProfiler.java`
- Test: `src/test/java/dev/gensh/ironroads/geometry/TrackProfilerTest.java`

**Interfaces:**
- Consumes: `Line`, `RailNetwork.perpendicularAt(Line, int)` из задачи 2
- Produces:
  - `interface HeightSampler { int heightAt(int x, int z); }`
  - `TrackProfiler(RailNetwork network, int maxGradeRun, HeightSampler terrain)`
  - `int TrackProfiler.heightAt(Line line, int along)`

Источник рельефа передаётся в конструктор, а не в метод: профайлер кэширует
сглаженную высоту по узлам, и экземпляр, обслуживающий два разных рельефа,
раздавал бы чужие значения.

- [ ] **Step 1: Написать `HeightSampler.java`**

```java
package dev.gensh.ironroads.geometry;

/**
 * Источник высоты поверхности. В игре за ним стоит ChunkGenerator,
 * в тестах — синтетический рельеф. Благодаря этому интерфейсу
 * TrackProfiler не знает про Minecraft.
 */
@FunctionalInterface
public interface HeightSampler {
    int heightAt(int x, int z);
}
```

- [ ] **Step 2: Написать падающий тест**

`src/test/java/dev/gensh/ironroads/geometry/TrackProfilerTest.java`:

```java
package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class TrackProfilerTest {

    private static final int GRADE = 12;
    private static final Line LINE = new Line(Line.Axis.Z, 0);

    private RailNetwork network() {
        return new RailNetwork(42L, 768, 48);
    }

    private TrackProfiler profiler() {
        return new TrackProfiler(network(), GRADE);
    }

    @Test
    void flatTerrainGivesFlatProfile() {
        HeightSampler flat = (x, z) -> 70;
        TrackProfiler p = profiler();
        for (int along = -500; along <= 500; along += 13) {
            assertEquals(70, p.heightAt(flat, LINE, along));
        }
    }

    @Test
    void gradeNeverExceedsLimit() {
        // Обрыв в 60 блоков — профиль обязан размазать его в пологий уклон.
        HeightSampler cliff = (x, z) -> z < 0 ? 60 : 120;
        TrackProfiler p = profiler();
        int prev = p.heightAt(cliff, LINE, -800);
        for (int along = -799; along <= 800; along++) {
            int cur = p.heightAt(cliff, LINE, along);
            int rise = Math.abs(cur - prev);
            assertTrue(rise <= 1,
                       "подъём " + rise + " блоков за один блок пути на along=" + along);
            prev = cur;
        }
        // И на дистанции G блоков — не больше одного блока подъёма.
        for (int along = -700; along <= 700; along += 5) {
            int a = p.heightAt(cliff, LINE, along);
            int b = p.heightAt(cliff, LINE, along + GRADE);
            assertTrue(Math.abs(b - a) <= 1,
                       "уклон круче 1:" + GRADE + " на отрезке от " + along);
        }
    }

    @Test
    void profileIsPositionLocalNotPathDependent() {
        // Один и тот же along, посчитанный из разных экземпляров, обязан
        // совпадать — иначе на границах чанков будут ступени.
        HeightSampler hilly = (x, z) -> 80 + (int) (25 * Math.sin(z / 90.0));
        TrackProfiler p = profiler();
        for (int along = -300; along <= 300; along += 7) {
            int direct = p.heightAt(hilly, LINE, along);
            int again = new TrackProfiler(network(), GRADE).heightAt(hilly, LINE, along);
            assertEquals(direct, again, "профиль зависит от контекста вызова");
        }
    }

    @Test
    void profileNeverRisesAboveTerrain() {
        HeightSampler hilly = (x, z) -> 80 + (int) (25 * Math.sin(z / 90.0));
        TrackProfiler p = profiler();
        for (int along = -300; along <= 300; along += 3) {
            int terrain = hilly.heightAt(0, along);
            assertTrue(p.heightAt(hilly, LINE, along) <= terrain + 1,
                       "профиль ушёл выше рельефа на along=" + along);
        }
    }

    @Test
    void narrowValleyIsBridgedNotFollowed() {
        // Узкое ущелье в 40 блоков глубиной: путь должен пройти поверху.
        HeightSampler gorge = (x, z) -> (z > -20 && z < 20) ? 40 : 90;
        TrackProfiler p = profiler();
        assertTrue(p.heightAt(gorge, LINE, 0) > 70,
                   "профиль нырнул в ущелье вместо того, чтобы его перелететь");
    }
}
```

- [ ] **Step 3: Запустить тест и убедиться, что он падает**

Run: `./gradlew test --tests '*TrackProfilerTest*'`
Expected: FAIL — класс `TrackProfiler` не существует.

- [ ] **Step 4: Написать `TrackProfiler.java`**

```java
package dev.gensh.ironroads.geometry;

/**
 * Продольный профиль высоты пути.
 *
 * Считается в два шага. Сначала рельеф сглаживается скользящим средним, чтобы
 * убрать мелкие неровности. Затем берётся нижняя огибающая Липшица:
 *
 *     h(t) = min по d из [-W, W] от ( smooth(t + d) + |d| / G )
 *
 * Это наибольшая функция, которая не выше сглаженного рельефа и нигде не
 * нарушает уклон 1/G. Главное её свойство — позиционная локальность:
 * значение в точке не зависит от того, откуда мы начали считать, поэтому
 * профиль сходится на границах чанков без ступеней.
 */
public final class TrackProfiler {

    /** Полуширина окна сглаживания. */
    private static final int SMOOTH_RADIUS = 48;

    /**
     * Полуширина окна огибающей. Должна покрывать максимальный перепад,
     * умноженный на maxGradeRun, иначе крутой обрыв не успеет размазаться
     * в допустимый уклон.
     */
    private static final int ENVELOPE_RADIUS = 768;

    /**
     * Шаг разрежения при обходе окон.
     * ponytail: шаг 8 при радиусе 384 даёт 96 сэмплов на точку вместо 768;
     * если профиль окажется угловатым на резком рельефе — уменьшать шаг,
     * а не радиус.
     */
    private static final int STRIDE = 8;

    private final RailNetwork network;
    private final int maxGradeRun;

    public TrackProfiler(RailNetwork network, int maxGradeRun) {
        if (maxGradeRun < 1) {
            throw new IllegalArgumentException("maxGradeRun должен быть положительным");
        }
        this.network = network;
        this.maxGradeRun = maxGradeRun;
    }

    /** Высота полотна пути в точке along вдоль линии. */
    public int heightAt(HeightSampler terrain, Line line, int along) {
        double best = Double.MAX_VALUE;
        for (int d = -ENVELOPE_RADIUS; d <= ENVELOPE_RADIUS; d += STRIDE) {
            double candidate = smoothedAt(terrain, line, along + d)
                             + (double) Math.abs(d) / maxGradeRun;
            if (candidate < best) {
                best = candidate;
            }
        }
        return (int) Math.round(best);
    }

    /** Скользящее среднее рельефа вдоль линии. */
    private double smoothedAt(HeightSampler terrain, Line line, int along) {
        double sum = 0;
        int count = 0;
        for (int d = -SMOOTH_RADIUS; d <= SMOOTH_RADIUS; d += STRIDE) {
            sum += sampleTerrain(terrain, line, along + d);
            count++;
        }
        return sum / count;
    }

    /** Рельеф в точке линии — с учётом того, что линия виляет. */
    private int sampleTerrain(HeightSampler terrain, Line line, int along) {
        int perpendicular = network.perpendicularAt(line, along);
        return line.axis() == Line.Axis.Z
             ? terrain.heightAt(perpendicular, along)
             : terrain.heightAt(along, perpendicular);
    }
}
```

- [ ] **Step 5: Запустить тесты**

Run: `./gradlew test --tests '*TrackProfilerTest*'`
Expected: PASS, 5 тестов.

Если падает `narrowValleyIsBridgedNotFollowed` — увеличить `SMOOTH_RADIUS`. Если падает `gradeNeverExceedsLimit` — увеличить `ENVELOPE_RADIUS`, а не ослаблять проверку: обрыв в 60 блоков при уклоне 1:12 размазывается на 720 блоков, поэтому радиус 384 упирался в собственный край. Актуальное значение 768 проверено прогоном.

- [ ] **Step 6: Запустить весь набор тестов**

Run: `./gradlew test`
Expected: PASS, 13 тестов.

- [ ] **Step 7: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/geometry src/test/java/dev/gensh/ironroads/geometry
git commit -m "feat: chunk-seam-free longitudinal height profile via Lipschitz envelope"
```

---

### Task 4: Мост к Minecraft — полотно в мире

Первая задача, касающаяся мира. Кладёт полосу гравия по трассе, рельсов ещё нет. Цель — убедиться, что геометрия из задач 2–3 рисует в игре связную сеть без швов.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/worldgen/ChunkGeneratorHeightSampler.java`
- Create: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`
- Create: `src/main/java/dev/gensh/ironroads/worldgen/ModFeatures.java`
- Create: `src/main/resources/data/ironroads/worldgen/configured_feature/railway.json`
- Create: `src/main/resources/data/ironroads/worldgen/placed_feature/railway.json`
- Create: `src/main/resources/data/ironroads/forge/biome_modifier/railway.json`
- Modify: `src/main/java/dev/gensh/ironroads/IronRoads.java`

**Interfaces:**
- Consumes: `RailNetwork`, `TrackProfiler`, `HeightSampler`, `Line` из задач 2–3
- Produces:
  - `ChunkGeneratorHeightSampler(ChunkGenerator, LevelHeightAccessor, RandomState)` реализует `HeightSampler`
  - `ModFeatures.RAILWAY` — `RegistryObject<Feature<NoneFeatureConfiguration>>`
  - `ModFeatures.register(IEventBus bus)`
  - `RailwayFeature.place(FeaturePlaceContext<NoneFeatureConfiguration>)` возвращает `boolean`

- [ ] **Step 1: Написать `ChunkGeneratorHeightSampler.java`**

```java
package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.HeightSampler;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Высота рельефа без генерации чанков. getBaseHeight считает шумовую функцию
 * ландшафта в произвольной точке — именно поэтому профиль пути может смотреть
 * на сотни блоков вперёд, не нарушая правило "фича трогает только свой чанк".
 */
public final class ChunkGeneratorHeightSampler implements HeightSampler {

    private final ChunkGenerator generator;
    private final LevelHeightAccessor heightAccessor;
    private final RandomState randomState;

    public ChunkGeneratorHeightSampler(ChunkGenerator generator,
                                       LevelHeightAccessor heightAccessor,
                                       RandomState randomState) {
        this.generator = generator;
        this.heightAccessor = heightAccessor;
        this.randomState = randomState;
    }

    @Override
    public int heightAt(int x, int z) {
        return generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG,
                                       heightAccessor, randomState);
    }
}
```

- [ ] **Step 2: Написать `RailwayFeature.java`**

```java
package dev.gensh.ironroads.worldgen;

import com.mojang.serialization.Codec;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;

/**
 * Ставит блоки железной дороги в пределах одного чанка.
 * Ни одного обращения к соседним чанкам: всё, что нужно знать о трассе,
 * вычисляется из сида и координат.
 */
public class RailwayFeature extends Feature<NoneFeatureConfiguration> {

    private static final int SPACING = 768;
    private static final int JITTER = 48;
    private static final int MAX_GRADE_RUN = 12;

    /** Полуширина полотна: 3 блока между путями плюс обочины. */
    static final int BED_HALF_WIDTH = 4;

    public RailwayFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    @Override
    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        WorldGenLevel level = context.level();
        BlockPos origin = context.origin();

        long seed = level.getSeed();
        RailNetwork network = new RailNetwork(seed, SPACING, JITTER);
        TrackProfiler profiler = new TrackProfiler(network, MAX_GRADE_RUN, terrain);
        HeightSampler terrain = new ChunkGeneratorHeightSampler(
                context.chunkGenerator(), level,
                level.getLevel().getChunkSource().randomState());

        int chunkMinX = origin.getX() & ~15;
        int chunkMinZ = origin.getZ() & ~15;

        boolean placedAnything = false;
        for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, 8)) {
            placedAnything |= placeSegment(level, network, profiler, terrain,
                                           line, chunkMinX, chunkMinZ);
        }
        return placedAnything;
    }

    private boolean placeSegment(WorldGenLevel level, RailNetwork network,
                                 TrackProfiler profiler, HeightSampler terrain,
                                 Line line, int chunkMinX, int chunkMinZ) {
        boolean placed = false;
        int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;

        for (int step = 0; step < 16; step++) {
            int along = alongStart + step;
            int perpendicular = network.perpendicularAt(line, along);
            int y = profiler.heightAt(line, along);

            for (int offset = -BED_HALF_WIDTH; offset <= BED_HALF_WIDTH; offset++) {
                int x = line.axis() == Line.Axis.Z ? perpendicular + offset : along;
                int z = line.axis() == Line.Axis.Z ? along : perpendicular + offset;

                if (x < chunkMinX || x >= chunkMinX + 16
                 || z < chunkMinZ || z >= chunkMinZ + 16) {
                    continue;  // соседний чанк построит это сам
                }

                level.setBlock(new BlockPos(x, y, z),
                               Blocks.GRAVEL.defaultBlockState(), 2);
                placed = true;
            }
        }
        return placed;
    }
}
```

Проверка границ чанка — не оптимизация, а требование корректности: запись за пределы своего чанка вызывает каскадную генерацию и дедлоки.

- [ ] **Step 3: Написать `ModFeatures.java`**

```java
package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.IronRoads;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public final class ModFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(ForgeRegistries.FEATURES, IronRoads.MOD_ID);

    public static final RegistryObject<Feature<NoneFeatureConfiguration>> RAILWAY =
            FEATURES.register("railway",
                    () -> new RailwayFeature(NoneFeatureConfiguration.CODEC));

    public static void register(IEventBus bus) {
        FEATURES.register(bus);
    }

    private ModFeatures() {}
}
```

Импорт `ForgeRegistries` — именно `net.minecraftforge.registries.ForgeRegistries`. Если IDE подставит `net.minecraft.core.registries.Registries`, сборка упадёт.

- [ ] **Step 4: Подключить регистрацию в `IronRoads.java`**

Заменить конструктор на:

```java
public IronRoads() {
    IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
    ModFeatures.register(bus);
    LOGGER.info("IronRoads loaded");
}
```

Добавить импорты:

```java
import dev.gensh.ironroads.worldgen.ModFeatures;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
```

- [ ] **Step 5: Написать datapack-файлы**

`src/main/resources/data/ironroads/worldgen/configured_feature/railway.json`:

```json
{
  "type": "ironroads:railway",
  "config": {}
}
```

`src/main/resources/data/ironroads/worldgen/placed_feature/railway.json`:

```json
{
  "feature": "ironroads:railway",
  "placement": []
}
```

Пустой `placement` намеренный: фича сама решает, проходит ли через чанк трасса. Любой ванильный placement-модификатор внёс бы случайность и разорвал бы сеть.

`src/main/resources/data/ironroads/forge/biome_modifier/railway.json`:

```json
{
  "type": "forge:add_features",
  "biomes": "#minecraft:is_overworld",
  "features": "ironroads:railway",
  "step": "top_layer_modification"
}
```

- [ ] **Step 6: Собрать и проверить в игре**

Run: `./gradlew runClient`
Создать новый мир с сидом `1`, режим креатива, `/tp 0 100 0`, лететь вдоль оси Z.

Expected: сплошная полоса гравия шириной 9 блоков тянется без разрывов, плавно виляет, повторяет сглаженный рельеф. На `/tp 768 100 0` — соседняя магистраль. На `/tp 0 100 768` — перекрёсток.

Что проверять в первую очередь: **разрывы и ступени на границах чанков**. Их быть не должно — это проверка задачи 3 в бою.

- [ ] **Step 7: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads src/main/resources/data
git commit -m "feat: worldgen feature laying continuous railway bed along network"
```

---

### Task 5: Укладка путей Create

Заменяет гравийную полосу настоящими путями: два пути магистрали с корректными `TrackShape` — прямые, диагонали на изгибах, подъёмы на уклонах.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/worldgen/TrackBuilder.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`

**Interfaces:**
- Consumes: `RailNetwork`, `TrackProfiler`, `Line`, `HeightSampler` из задач 2–4
- Produces:
  - `TrackBuilder(RailNetwork network, TrackProfiler profiler)`
  - `void TrackBuilder.layTrack(WorldGenLevel level, HeightSampler terrain, Line line, int along, int lateralOffset, int chunkMinX, int chunkMinZ)`

- [ ] **Step 1: Написать `TrackBuilder.java`**

```java
package dev.gensh.ironroads.worldgen;

import com.simibubi.create.AllBlocks;
import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackShape;
import dev.gensh.ironroads.geometry.HeightSampler;
import dev.gensh.ironroads.geometry.Line;
import dev.gensh.ironroads.geometry.RailNetwork;
import dev.gensh.ironroads.geometry.TrackProfiler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Кладёт один путь Create вдоль линии. Форма блока выбирается по тому, как
 * трасса меняется на этом шаге: ровно — прямая, со сдвигом вбок — диагональ,
 * с подъёмом — ascending.
 */
public final class TrackBuilder {

    private final RailNetwork network;
    private final TrackProfiler profiler;

    public TrackBuilder(RailNetwork network, TrackProfiler profiler) {
        this.network = network;
        this.profiler = profiler;
    }

    /**
     * @param lateralOffset смещение пути от осевой линии: -2 и +2 для двух
     *                      путей магистрали, 0 для однопутного ответвления
     */
    public void layTrack(WorldGenLevel level, HeightSampler terrain, Line line,
                         int along, int lateralOffset,
                         int chunkMinX, int chunkMinZ) {

        int perpendicular = network.perpendicularAt(line, along) + lateralOffset;
        int y = profiler.heightAt(line, along);

        int x = line.axis() == Line.Axis.Z ? perpendicular : along;
        int z = line.axis() == Line.Axis.Z ? along : perpendicular;

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;
        }

        int nextPerpendicular = network.perpendicularAt(line, along + 1) + lateralOffset;
        int nextY = profiler.heightAt(line, along + 1);

        TrackShape shape = chooseShape(line, perpendicular, nextPerpendicular, y, nextY);

        BlockState state = AllBlocks.TRACK.getDefaultState()
                .setValue(TrackBlock.SHAPE, shape);
        level.setBlock(new BlockPos(x, y, z), state, 2);
    }

    private TrackShape chooseShape(Line line, int perpendicular, int nextPerpendicular,
                                   int y, int nextY) {
        boolean alongZ = line.axis() == Line.Axis.Z;

        if (nextY > y) {
            // Подъём Create — 1:1. Профиль ставит такие блоки редко, между ними
            // длинные горизонтальные полки.
            return alongZ ? TrackShape.AS : TrackShape.AE;
        }
        if (nextY < y) {
            return alongZ ? TrackShape.AN : TrackShape.AW;
        }

        int drift = nextPerpendicular - perpendicular;
        if (drift == 0) {
            return alongZ ? TrackShape.ZO : TrackShape.XO;
        }
        return drift > 0 ? TrackShape.PD : TrackShape.ND;
    }
}
```

`AllBlocks.TRACK` — константа Registrate в Create с типом `BlockEntry<TrackBlock>`, `getDefaultState()` даёт её `BlockState`. Если IDE не находит поле под этим именем, открыть `AllBlocks` переходом к определению и найти константу нужного типа.

- [ ] **Step 2: Добавить обработку перекрёстков в `TrackBuilder`**

Без этого шага в узле решётки два перпендикулярных пути перезапишут друг друга и сеть порвётся на каждой развязке.

Добавить поле и метод в `TrackBuilder`:

```java
/** Полуширина зоны развязки: покрывает оба пути двухпутной магистрали. */
private static final int CROSSING_REACH = 3;

/**
 * Точка внутри развязки. В узле решётки джиттер обеих линий погашен,
 * поэтому пути проходят строго по базовым координатам и пересекаются
 * в предсказуемом квадрате вокруг узла.
 */
private boolean insideCrossing(int x, int z) {
    int spacing = network.spacing();
    int dx = Math.floorMod(x, spacing);
    int dz = Math.floorMod(z, spacing);
    dx = Math.min(dx, spacing - dx);
    dz = Math.min(dz, spacing - dz);
    return dx <= CROSSING_REACH && dz <= CROSSING_REACH;
}
```

Вставить проверку в начало `chooseShape`, изменив её сигнатуру так, чтобы она получала мировые координаты:

```java
private TrackShape chooseShape(Line line, int x, int z,
                               int perpendicular, int nextPerpendicular,
                               int y, int nextY) {
    if (insideCrossing(x, z)) {
        return TrackShape.CR_O;  // ортогональное пересечение двух путей
    }

    boolean alongZ = line.axis() == Line.Axis.Z;
    // ... остальное тело без изменений
```

И соответственно поправить вызов в `layTrack`:

```java
TrackShape shape = chooseShape(line, x, z, perpendicular, nextPerpendicular, y, nextY);
```

Порядок записи блоков внутри развязки перестаёт иметь значение: обе линии кладут туда один и тот же `CR_O`.

- [ ] **Step 3: Переписать `placeSegment` в `RailwayFeature`**

Заменить тело метода на:

```java
private boolean placeSegment(WorldGenLevel level, RailNetwork network,
                             TrackProfiler profiler, HeightSampler terrain,
                             TrackBuilder trackBuilder,
                             Line line, int chunkMinX, int chunkMinZ) {
    boolean placed = false;
    int alongStart = line.axis() == Line.Axis.Z ? chunkMinZ : chunkMinX;

    for (int step = 0; step < 16; step++) {
        int along = alongStart + step;
        int perpendicular = network.perpendicularAt(line, along);
        int y = profiler.heightAt(line, along);

        for (int offset = -BED_HALF_WIDTH; offset <= BED_HALF_WIDTH; offset++) {
            int x = line.axis() == Line.Axis.Z ? perpendicular + offset : along;
            int z = line.axis() == Line.Axis.Z ? along : perpendicular + offset;

            if (x < chunkMinX || x >= chunkMinX + 16
             || z < chunkMinZ || z >= chunkMinZ + 16) {
                continue;
            }

            level.setBlock(new BlockPos(x, y - 1, z),
                           Blocks.GRAVEL.defaultBlockState(), 2);
            clearGauge(level, x, y, z);
            placed = true;
        }

        trackBuilder.layTrack(level, terrain, line, along, -2, chunkMinX, chunkMinZ);
        trackBuilder.layTrack(level, terrain, line, along, +2, chunkMinX, chunkMinZ);
    }
    return placed;
}

/** Вырубает пространство над полотном: 4 блока по высоте. */
private void clearGauge(WorldGenLevel level, int x, int y, int z) {
    for (int dy = 0; dy < 4; dy++) {
        BlockPos pos = new BlockPos(x, y + dy, z);
        if (!level.getBlockState(pos).isAir()) {
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
        }
    }
}
```

В `place()` создать builder и передать его в вызов:

```java
TrackBuilder trackBuilder = new TrackBuilder(network, profiler);

for (Line line : network.linesNear(chunkMinX + 8, chunkMinZ + 8, 8)) {
    placedAnything |= placeSegment(level, network, profiler, terrain, trackBuilder,
                                   line, chunkMinX, chunkMinZ);
}
```

- [ ] **Step 4: Собрать и проверить в игре**

Run: `./gradlew runClient`
Новый мир, сид `1`, `/tp 0 100 0`.

Expected: два пути Create на гравийном полотне, тянутся непрерывно, на изгибах — диагональные блоки, над путями расчищено 4 блока. На `/tp 0 100 768` — развязка на блоках `CR_O`, ни одна из четырёх ветвей не обрывается.

Известное ограничение этого шага: граф ещё не зарегистрирован, поезд поставить нельзя. Это задача 8.

- [ ] **Step 5: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/worldgen
git commit -m "feat: lay Create track blocks with shape derived from path curvature"
```

---

### Task 6: Насыпи, эстакады и мосты

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/worldgen/SupportBuilder.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`

**Interfaces:**
- Consumes: `HeightSampler` из задачи 3
- Produces:
  - `SupportBuilder(int supportSpacing, int viaductMaxHeight)`
  - `void SupportBuilder.support(WorldGenLevel level, int x, int y, int z, int terrainY, int along, int chunkMinX, int chunkMinZ)`

- [ ] **Step 1: Написать `SupportBuilder.java`**

```java
package dev.gensh.ironroads.worldgen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;

/**
 * Всё, что держит путь над землёй: насыпь, эстакада на брёвнах, мост.
 * Решение принимается по разнице между высотой полотна и рельефом.
 */
public final class SupportBuilder {

    private static final BlockState FILL = Blocks.GRAVEL.defaultBlockState();
    private static final BlockState PILLAR = Blocks.OAK_LOG.defaultBlockState()
            .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    private static final BlockState DECK = Blocks.STONE_BRICKS.defaultBlockState();

    private final int supportSpacing;
    private final int viaductMaxHeight;

    public SupportBuilder(int supportSpacing, int viaductMaxHeight) {
        this.supportSpacing = supportSpacing;
        this.viaductMaxHeight = viaductMaxHeight;
    }

    /**
     * @param y        высота полотна (блок под рельсом)
     * @param terrainY высота поверхности земли в этой точке
     * @param along    позиция вдоль линии — по ней определяется, ставить ли опору
     */
    public void support(WorldGenLevel level, int x, int y, int z, int terrainY,
                        int along, int chunkMinX, int chunkMinZ) {

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;
        }

        int rise = y - terrainY;
        if (rise <= 0) {
            return;  // выемка — задача 7
        }

        if (rise <= 3) {
            fillColumn(level, x, y - 2, z, terrainY - 1, FILL);
            return;
        }

        // Настил эстакады есть всегда, опоры — через каждые supportSpacing блоков.
        level.setBlock(new BlockPos(x, y - 1, z), DECK, 2);

        if (Math.floorMod(along, supportSpacing) != 0) {
            return;
        }

        int bottom = (rise > viaductMaxHeight || isSubmerged(level, x, terrainY, z))
                   ? terrainY - 2
                   : terrainY;
        fillColumn(level, x, y - 2, z, bottom, PILLAR);
    }

    private boolean isSubmerged(WorldGenLevel level, int x, int terrainY, int z) {
        return level.getFluidState(new BlockPos(x, terrainY + 1, z)).getType()
             == Fluids.WATER;
    }

    private void fillColumn(WorldGenLevel level, int x, int fromY, int z, int toY,
                            BlockState state) {
        for (int y = fromY; y >= toY; y--) {
            if (y < level.getMinBuildHeight()) {
                return;
            }
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) {
                level.setBlock(pos, state, 2);
            }
        }
    }
}
```

- [ ] **Step 2: Подключить в `RailwayFeature`**

Внутри цикла по `offset`, после `clearGauge`, добавить:

```java
int terrainY = terrain.heightAt(x, z);
supportBuilder.support(level, x, y, z, terrainY, along, chunkMinX, chunkMinZ);
```

Создать в `place()` и передать в `placeSegment` дополнительным параметром:

```java
SupportBuilder supportBuilder = new SupportBuilder(8, 24);
```

- [ ] **Step 3: Проверить в игре**

Run: `./gradlew runClient`
Новый мир, сид `1`. Лететь вдоль магистрали до первой долины и до первого водоёма.

Expected: над впадинами — эстакада на бревенчатых опорах с шагом 8, над водой — опоры уходят до дна, на пологих участках — гравийная насыпь. Ни одна опора не висит в воздухе.

- [ ] **Step 4: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/worldgen
git commit -m "feat: embankments, log-pillar viaducts and water crossings"
```

---

### Task 7: Выемки и тоннели

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/worldgen/TunnelBuilder.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`

**Interfaces:**
- Consumes: `Line` из задачи 2
- Produces:
  - `TunnelBuilder(int tunnelMinDepth, int halfWidth)`
  - `void TunnelBuilder.carve(WorldGenLevel level, Line line, int x, int y, int z, int terrainY, int chunkMinX, int chunkMinZ)`

- [ ] **Step 1: Написать `TunnelBuilder.java`**

```java
package dev.gensh.ironroads.worldgen;

import dev.gensh.ironroads.geometry.Line;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Прорезает путь сквозь рельеф. Небольшое превышение земли над полотном
 * снимается открытой выемкой, значительное — закрытым тоннелем со сводом.
 */
public final class TunnelBuilder {

    private static final BlockState LINING = Blocks.STONE_BRICKS.defaultBlockState();
    private static final BlockState LIGHT = Blocks.LANTERN.defaultBlockState();
    private static final int CLEARANCE = 4;
    private static final int LIGHT_SPACING = 12;

    private final int tunnelMinDepth;
    private final int halfWidth;

    public TunnelBuilder(int tunnelMinDepth, int halfWidth) {
        this.tunnelMinDepth = tunnelMinDepth;
        this.halfWidth = halfWidth;
    }

    public void carve(WorldGenLevel level, Line line, int x, int y, int z,
                      int terrainY, int chunkMinX, int chunkMinZ) {

        if (x < chunkMinX || x >= chunkMinX + 16
         || z < chunkMinZ || z >= chunkMinZ + 16) {
            return;
        }

        int depth = terrainY - y;
        if (depth <= 0) {
            return;  // насыпь — задача 6
        }

        boolean closed = depth >= tunnelMinDepth;
        int top = closed ? y + CLEARANCE : terrainY + 1;

        for (int cy = y; cy <= top; cy++) {
            BlockPos pos = new BlockPos(x, cy, z);
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2);
            }
        }

        if (!closed) {
            return;
        }

        level.setBlock(new BlockPos(x, top + 1, z), LINING, 2);

        int along = line.axis() == Line.Axis.Z ? z : x;
        if (isEdgeColumn(line, x, z) && Math.floorMod(along, LIGHT_SPACING) == 0) {
            level.setBlock(new BlockPos(x, top, z), LIGHT, 2);
        }
    }

    /** Крайняя колонка габарита — туда вешаются фонари. */
    private boolean isEdgeColumn(Line line, int x, int z) {
        int perpendicular = line.axis() == Line.Axis.Z ? x : z;
        return Math.floorMod(perpendicular, 2 * halfWidth) == 0;
    }
}
```

- [ ] **Step 2: Подключить в `RailwayFeature`**

Сразу за вызовом `supportBuilder.support(...)`:

```java
tunnelBuilder.carve(level, line, x, y, z, terrainY, chunkMinX, chunkMinZ);
```

Создать в `place()` и передать в `placeSegment`:

```java
TunnelBuilder tunnelBuilder = new TunnelBuilder(6, BED_HALF_WIDTH);
```

- [ ] **Step 3: Проверить в игре**

Run: `./gradlew runClient`
Новый мир, сид `1`, лететь вдоль магистрали до горного участка.

Expected: пологие холмы срезаны открытой выемкой, крупные горы пройдены закрытым тоннелем с каменным сводом и фонарями через каждые 12 блоков. Тоннель не залит водой и не обрывается на границах чанков.

- [ ] **Step 4: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/worldgen
git commit -m "feat: cuttings and lit tunnels through terrain"
```

---

### Task 8: Сшивание графа путей

Кульминация фазы 1: до этой задачи по путям нельзя ехать.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/graph/LinkedChunks.java`
- Create: `src/main/java/dev/gensh/ironroads/graph/RailwayGraphLinker.java`

**Interfaces:**
- Consumes: блоки путей, поставленные задачей 5
- Produces:
  - `LinkedChunks.contains(ChunkPos)`, `LinkedChunks.add(ChunkPos)` — package-private
  - `RailwayGraphLinker.onChunkLoad(ChunkEvent.Load)` — обработчик Forge-события, регистрируется аннотацией

- [ ] **Step 1: Написать `LinkedChunks.java`**

```java
package dev.gensh.ironroads.graph;

import net.minecraft.world.level.ChunkPos;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Какие чанки уже прошли сшивание графа в текущем запуске сервера.
 *
 * ponytail: набор в памяти, не персистентный. Повторное сшивание после
 * перезапуска безвредно — TrackPropagator идемпотентен, — но стоит лишнего
 * прохода по чанку. Если профилировщик покажет, что это заметно, переносить
 * пометку в SavedData, а не наращивать логику здесь.
 */
final class LinkedChunks {

    private static final int MAX_TRACKED = 20_000;

    private static final Set<ChunkPos> SEEN =
            Collections.synchronizedSet(new LinkedHashSet<>());

    static boolean contains(ChunkPos pos) {
        return SEEN.contains(pos);
    }

    static void add(ChunkPos pos) {
        synchronized (SEEN) {
            SEEN.add(pos);
            if (SEEN.size() > MAX_TRACKED) {
                Iterator<ChunkPos> oldest = SEEN.iterator();
                oldest.next();
                oldest.remove();
            }
        }
    }

    private LinkedChunks() {}
}
```

- [ ] **Step 2: Написать `RailwayGraphLinker.java`**

```java
package dev.gensh.ironroads.graph;

import com.simibubi.create.content.trains.track.TrackBlock;
import com.simibubi.create.content.trains.track.TrackPropagator;
import dev.gensh.ironroads.IronRoads;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraftforge.event.level.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Регистрирует сгенерированные пути в графе Create.
 *
 * Worldgen-фича не может этого сделать: связь между двумя путями живёт в
 * BlockEntity обоих концов, а соседний чанк во время генерации трогать нельзя.
 * Здесь же есть полноценный ServerLevel с загруженными соседями, поэтому
 * TrackPropagator сшивает участки через границы чанков сам.
 */
@Mod.EventBusSubscriber(modid = IronRoads.MOD_ID)
public final class RailwayGraphLinker {

    @SubscribeEvent
    public static void onChunkLoad(ChunkEvent.Load event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        ChunkAccess chunk = event.getChunk();
        if (LinkedChunks.contains(chunk.getPos())) {
            return;
        }

        int linked = linkTracks(level, chunk);
        LinkedChunks.add(chunk.getPos());

        if (linked > 0) {
            IronRoads.LOGGER.debug("Сшито {} путей в чанке {}", linked, chunk.getPos());
        }
    }

    private static int linkTracks(ServerLevel level, ChunkAccess chunk) {
        ChunkPos pos = chunk.getPos();
        int count = 0;

        for (int sectionIndex = 0; sectionIndex < chunk.getSectionsCount(); sectionIndex++) {
            LevelChunkSection section = chunk.getSection(sectionIndex);
            if (section.hasOnlyAir()) {
                continue;
            }
            int sectionMinY = chunk.getMinBuildHeight() + sectionIndex * 16;

            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    for (int z = 0; z < 16; z++) {
                        BlockState state = section.getBlockState(x, y, z);
                        if (!(state.getBlock() instanceof TrackBlock)) {
                            continue;
                        }
                        BlockPos worldPos = new BlockPos(
                                pos.getMinBlockX() + x,
                                sectionMinY + y,
                                pos.getMinBlockZ() + z);
                        TrackPropagator.onRailAdded(level, worldPos, state);
                        count++;
                    }
                }
            }
        }
        return count;
    }

    private RailwayGraphLinker() {}
}
```

- [ ] **Step 3: Собрать**

Run: `./gradlew build`
Expected: `BUILD SUCCESSFUL`.

- [ ] **Step 4: Проверить в игре — главная проверка фазы**

Run: `./gradlew runClient`
Новый мир, сид `1`, `/tp 0 100 0`, режим креатива.

1. Найти путь, поставить рядом `Train Station` из Create и привязать к пути.
2. Собрать простейший поезд: `Train Casing` на пути плюс паровой котёл, либо `Creative Motor`-состав.
3. Задать расписание и пустить поезд.

Expected: поезд едет по магистрали, проходит границы чанков без остановки, проезжает эстакады и тоннели.

Если поезд встаёт на границе чанка — граф не сшился. Смотреть лог на сообщения «Сшито N путей»: если их нет, проверить, что `ChunkEvent.Load` вообще приходит, и что блоки действительно `TrackBlock`.

- [ ] **Step 5: Закоммитить**

```bash
git add src/main/java/dev/gensh/ironroads/graph
git commit -m "feat: register generated tracks into Create's track graph on chunk load"
```

---

### Task 9: Ответвления и станции

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/geometry/Branch.java`
- Create: `src/main/java/dev/gensh/ironroads/geometry/BranchPlanner.java`
- Create: `src/main/java/dev/gensh/ironroads/worldgen/StationBuilder.java`
- Test: `src/test/java/dev/gensh/ironroads/geometry/BranchPlannerTest.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`

**Interfaces:**
- Consumes: `RailNetwork`, `Line`, `TrackBuilder`, `SupportBuilder`, `TunnelBuilder`, `TrackProfiler`
- Produces:
  - `record Branch(Line source, int departureAlong, int departureX, int departureZ, int endpointX, int endpointZ)`
  - `BranchPlanner(RailNetwork network, long seed)`
  - `java.util.List<Branch> BranchPlanner.branchesNear(int blockX, int blockZ, int radius)`
  - `void StationBuilder.build(WorldGenLevel level, Branch branch, int y, int chunkMinX, int chunkMinZ)`

- [ ] **Step 1: Написать падающий тест**

`src/test/java/dev/gensh/ironroads/geometry/BranchPlannerTest.java`:

```java
package dev.gensh.ironroads.geometry;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BranchPlannerTest {

    private static final int SPACING = 768;

    private BranchPlanner planner() {
        return new BranchPlanner(new RailNetwork(7L, SPACING, 48), 7L);
    }

    @Test
    void branchesAreDeterministic() {
        List<Branch> first = planner().branchesNear(0, 0, 400);
        List<Branch> second = planner().branchesNear(0, 0, 400);
        assertEquals(first, second);
    }

    @Test
    void branchesDepartFromHalfGridNodes() {
        List<Branch> branches = planner().branchesNear(0, 0, SPACING);
        assertFalse(branches.isEmpty(), "рядом с началом координат ветки быть обязаны");
        for (Branch branch : branches) {
            assertEquals(0, Math.floorMod(branch.departureAlong(), SPACING / 2),
                         "ответвление обязано отходить от узла полусетки");
        }
    }

    @Test
    void branchesNeverDepartFromCrossings() {
        for (Branch branch : planner().branchesNear(0, 0, SPACING)) {
            assertNotEquals(0, Math.floorMod(branch.departureAlong(), SPACING),
                            "ветка не должна отходить прямо с развязки");
        }
    }

    @Test
    void branchLengthStaysWithinBudget() {
        for (Branch branch : planner().branchesNear(0, 0, SPACING)) {
            int dx = Math.abs(branch.endpointX() - branch.departureX());
            int dz = Math.abs(branch.endpointZ() - branch.departureZ());
            assertTrue(dx + dz <= SPACING / 2,
                       "ответвление длиннее половины ячейки сетки");
        }
    }
}
```

- [ ] **Step 2: Запустить тест и убедиться, что он падает**

Run: `./gradlew test --tests '*BranchPlannerTest*'`
Expected: FAIL — классы `Branch` и `BranchPlanner` не существуют.

- [ ] **Step 3: Написать `Branch.java`**

```java
package dev.gensh.ironroads.geometry;

/** Однопутное ответвление от магистрали к станции. */
public record Branch(Line source, int departureAlong,
                     int departureX, int departureZ,
                     int endpointX, int endpointZ) {
}
```

- [ ] **Step 4: Написать `BranchPlanner.java`**

```java
package dev.gensh.ironroads.geometry;

import java.util.ArrayList;
import java.util.List;

/**
 * Где от магистралей отходят ответвления и куда они ведут.
 *
 * Точки отправления стоят на полусетке — посередине между узлами пересечений,
 * подальше от развязок. Сторона и длина выбираются детерминированно по сиду.
 */
public final class BranchPlanner {

    private static final int MIN_LENGTH = 96;

    private final RailNetwork network;
    private final long seed;

    public BranchPlanner(RailNetwork network, long seed) {
        this.network = network;
        this.seed = seed;
    }

    public List<Branch> branchesNear(int blockX, int blockZ, int radius) {
        List<Branch> result = new ArrayList<>();
        int half = network.spacing() / 2;

        for (Line line : network.linesNear(blockX, blockZ, radius + half)) {
            int along = line.axis() == Line.Axis.Z ? blockZ : blockX;
            int first = Math.floorDiv(along - radius - half, half);
            int last = Math.floorDiv(along + radius + half, half);

            for (int node = first; node <= last; node++) {
                int departureAlong = node * half;
                if (Math.floorMod(departureAlong, network.spacing()) == 0) {
                    continue;  // это узел пересечения, там развязка
                }
                result.add(makeBranch(line, departureAlong, half));
            }
        }
        return result;
    }

    private Branch makeBranch(Line line, int departureAlong, int maxLength) {
        int perpendicular = network.perpendicularAt(line, departureAlong);
        int departureX = line.axis() == Line.Axis.Z ? perpendicular : departureAlong;
        int departureZ = line.axis() == Line.Axis.Z ? departureAlong : perpendicular;

        long h = mix(seed ^ ((long) departureX * 0x9E3779B9L)
                          ^ ((long) departureZ * 0x85EBCA6BL));
        int side = (h & 1L) == 0 ? -1 : 1;
        int length = MIN_LENGTH + (int) Math.floorMod(h >>> 1, maxLength - MIN_LENGTH);

        int endpointX = line.axis() == Line.Axis.Z ? departureX + side * length : departureX;
        int endpointZ = line.axis() == Line.Axis.Z ? departureZ : departureZ + side * length;

        return new Branch(line, departureAlong, departureX, departureZ,
                          endpointX, endpointZ);
    }

    private static long mix(long z) {
        z += 0x9E3779B97F4A7C15L;
        z = (z ^ (z >>> 30)) * 0xBF58476D1CE4E5B9L;
        z = (z ^ (z >>> 27)) * 0x94D049BB133111EBL;
        return z ^ (z >>> 31);
    }
}
```

- [ ] **Step 5: Запустить тесты**

Run: `./gradlew test --tests '*BranchPlannerTest*'`
Expected: PASS, 4 теста.

- [ ] **Step 6: Написать `StationBuilder.java`**

```java
package dev.gensh.ironroads.worldgen;

import com.simibubi.create.AllBlocks;
import dev.gensh.ironroads.geometry.Branch;
import dev.gensh.ironroads.geometry.Line;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Станция в конце ответвления: перрон с навесом и блок Station из Create.
 * Достаточно, чтобы к сети можно было привязать расписание.
 */
public final class StationBuilder {

    private static final BlockState PLATFORM = Blocks.SMOOTH_STONE.defaultBlockState();
    private static final BlockState ROOF = Blocks.SPRUCE_PLANKS.defaultBlockState();
    private static final BlockState POST = Blocks.SPRUCE_FENCE.defaultBlockState();

    private static final int PLATFORM_LENGTH = 12;
    private static final int ROOF_HEIGHT = 4;

    public void build(WorldGenLevel level, Branch branch, int y,
                      int chunkMinX, int chunkMinZ) {

        // Ответвление идёт перпендикулярно своей магистрали, значит перрон
        // тянется вдоль оси самой магистрали.
        boolean platformAlongZ = branch.source().axis() == Line.Axis.Z;

        for (int step = 0; step < PLATFORM_LENGTH; step++) {
            int x = platformAlongZ ? branch.endpointX() + 2 : branch.endpointX() + step;
            int z = platformAlongZ ? branch.endpointZ() + step : branch.endpointZ() + 2;
            placePlatformColumn(level, x, z, y, step, chunkMinX, chunkMinZ);
        }

        placeStationBlock(level, branch.endpointX() + 1, y, branch.endpointZ(),
                          chunkMinX, chunkMinZ);
    }

    private void placePlatformColumn(WorldGenLevel level, int x, int z, int y,
                                     int step, int chunkMinX, int chunkMinZ) {
        if (outside(x, z, chunkMinX, chunkMinZ)) {
            return;
        }
        level.setBlock(new BlockPos(x, y, z), PLATFORM, 2);

        if (step % 4 != 0) {
            return;
        }
        for (int dy = 1; dy < ROOF_HEIGHT; dy++) {
            level.setBlock(new BlockPos(x, y + dy, z), POST, 2);
        }
        level.setBlock(new BlockPos(x, y + ROOF_HEIGHT, z), ROOF, 2);
    }

    private void placeStationBlock(WorldGenLevel level, int x, int y, int z,
                                   int chunkMinX, int chunkMinZ) {
        if (outside(x, z, chunkMinX, chunkMinZ)) {
            return;
        }
        level.setBlock(new BlockPos(x, y + 1, z),
                       AllBlocks.TRACK_STATION.getDefaultState(), 2);
    }

    private boolean outside(int x, int z, int chunkMinX, int chunkMinZ) {
        return x < chunkMinX || x >= chunkMinX + 16
            || z < chunkMinZ || z >= chunkMinZ + 16;
    }
}
```

Имя `AllBlocks.TRACK_STATION` проверить переходом к определению: нужна константа `BlockEntry<StationBlock>` в классе `AllBlocks` Create.

- [ ] **Step 7: Подключить ответвления в `RailwayFeature`**

В `place()`, после цикла по магистральным линиям:

```java
BranchPlanner branchPlanner = new BranchPlanner(network, seed);
StationBuilder stationBuilder = new StationBuilder();

for (Branch branch : branchPlanner.branchesNear(chunkMinX + 8, chunkMinZ + 8, 8)) {
    placedAnything |= placeBranch(level, profiler, terrain, trackBuilder,
                                  supportBuilder, tunnelBuilder, stationBuilder,
                                  branch, chunkMinX, chunkMinZ);
}
```

Добавить метод `placeBranch`:

```java
private static final int BRANCH_HALF_WIDTH = 2;

private boolean placeBranch(WorldGenLevel level, TrackProfiler profiler,
                            HeightSampler terrain, TrackBuilder trackBuilder,
                            SupportBuilder supportBuilder, TunnelBuilder tunnelBuilder,
                            StationBuilder stationBuilder, Branch branch,
                            int chunkMinX, int chunkMinZ) {

    // Ветка идёт перпендикулярно магистрали: если магистраль вдоль Z,
    // ветка меняет X, и наоборот.
    boolean branchAlongX = branch.source().axis() == Line.Axis.Z;
    int from = branchAlongX ? branch.departureX() : branch.departureZ();
    int to = branchAlongX ? branch.endpointX() : branch.endpointZ();
    int direction = Integer.signum(to - from);

    // Ветка представлена как линия противоположной оси, проходящая через точку
    // отправления — этого достаточно, чтобы переиспользовать профиль и укладку.
    Line branchLine = new Line(branchAlongX ? Line.Axis.X : Line.Axis.Z, 0);

    boolean placed = false;
    for (int along = from; along != to + direction; along += direction) {
        int y = profiler.heightAt(branchLine, along);

        for (int offset = -BRANCH_HALF_WIDTH; offset <= BRANCH_HALF_WIDTH; offset++) {
            int perpendicular = (branchAlongX ? branch.departureZ() : branch.departureX())
                              + offset;
            int x = branchAlongX ? along : perpendicular;
            int z = branchAlongX ? perpendicular : along;

            if (x < chunkMinX || x >= chunkMinX + 16
             || z < chunkMinZ || z >= chunkMinZ + 16) {
                continue;
            }

            level.setBlock(new BlockPos(x, y - 1, z),
                           Blocks.GRAVEL.defaultBlockState(), 2);
            clearGauge(level, x, y, z);

            int terrainY = terrain.heightAt(x, z);
            supportBuilder.support(level, x, y, z, terrainY, along, chunkMinX, chunkMinZ);
            tunnelBuilder.carve(level, branchLine, x, y, z, terrainY,
                                chunkMinX, chunkMinZ);
            placed = true;
        }

        trackBuilder.layTrack(level, terrain, branchLine, along, 0,
                              chunkMinX, chunkMinZ);
    }

    int stationY = profiler.heightAt(branchLine, to);
    stationBuilder.build(level, branch, stationY, chunkMinX, chunkMinZ);
    return placed;
}
```

Здесь `branchLine` с `index = 0` намеренно: ветка коротка, и её собственный джиттер не нужен — важно лишь получить согласованный профиль высоты вдоль неё.

- [ ] **Step 8: Проверить в игре**

Run: `./gradlew runClient`
Новый мир, сид `1`, `/tp 0 100 384` — это узел полусетки, откуда должно отходить ответвление.

Expected: от магистрали отходит однопутная ветка длиной 96–384 блока, заканчивается перроном с навесом и блоком Station. У ветки собственные насыпи и мосты.

- [ ] **Step 9: Запустить весь набор тестов и закоммитить**

Run: `./gradlew test`
Expected: PASS, 17 тестов.

```bash
git add src/main/java/dev/gensh/ironroads src/test/java/dev/gensh/ironroads
git commit -m "feat: single-track branches terminating at generated stations"
```

---

### Task 10: Конфигурация

Выносит захардкоженные константы в Forge-конфиг.

**Files:**
- Create: `src/main/java/dev/gensh/ironroads/IronRoadsConfig.java`
- Modify: `src/main/java/dev/gensh/ironroads/IronRoads.java`
- Modify: `src/main/java/dev/gensh/ironroads/worldgen/RailwayFeature.java`

**Interfaces:**
- Consumes: `IronRoads.MOD_ID`
- Produces: `IronRoadsConfig.SPEC` (`ForgeConfigSpec`) и статические поля `GRID_SPACING`, `JITTER_AMPLITUDE`, `MAX_GRADE_RUN`, `VIADUCT_MAX_HEIGHT`, `TUNNEL_MIN_DEPTH`, `SUPPORT_SPACING`, `MAINLINE_DOUBLE_TRACK`, `BRANCHES_ENABLED`, `STATIONS_ENABLED`

- [ ] **Step 1: Написать `IronRoadsConfig.java`**

```java
package dev.gensh.ironroads;

import net.minecraftforge.common.ForgeConfigSpec;

public final class IronRoadsConfig {

    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.IntValue GRID_SPACING;
    public static final ForgeConfigSpec.IntValue JITTER_AMPLITUDE;
    public static final ForgeConfigSpec.IntValue MAX_GRADE_RUN;
    public static final ForgeConfigSpec.IntValue VIADUCT_MAX_HEIGHT;
    public static final ForgeConfigSpec.IntValue TUNNEL_MIN_DEPTH;
    public static final ForgeConfigSpec.IntValue SUPPORT_SPACING;
    public static final ForgeConfigSpec.BooleanValue MAINLINE_DOUBLE_TRACK;
    public static final ForgeConfigSpec.BooleanValue BRANCHES_ENABLED;
    public static final ForgeConfigSpec.BooleanValue STATIONS_ENABLED;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        builder.comment("Геометрия сети").push("network");
        GRID_SPACING = builder
                .comment("Расстояние между магистралями в блоках.")
                .defineInRange("gridSpacing", 768, 256, 8192);
        JITTER_AMPLITUDE = builder
                .comment("Насколько сильно магистраль виляет от прямой, в блоках.",
                         "Слишком большое значение делает повороты круче 45 градусов.")
                .defineInRange("jitterAmplitude", 48, 0, 128);
        MAINLINE_DOUBLE_TRACK = builder
                .comment("Двухпутная магистраль вместо однопутной.")
                .define("mainlineDoubleTrack", true);
        builder.pop();

        builder.comment("Профиль и сооружения").push("engineering");
        MAX_GRADE_RUN = builder
                .comment("Длина, за которую путь поднимается на один блок.",
                         "12 означает уклон 1:12.")
                .defineInRange("maxGradeRun", 12, 4, 64);
        VIADUCT_MAX_HEIGHT = builder
                .comment("Выше этой высоты эстакада заменяется капитальным мостом.")
                .defineInRange("viaductMaxHeight", 24, 4, 128);
        TUNNEL_MIN_DEPTH = builder
                .comment("Глубже этого значения выемка превращается в тоннель.")
                .defineInRange("tunnelMinDepth", 6, 2, 64);
        SUPPORT_SPACING = builder
                .comment("Расстояние между опорами эстакады.")
                .defineInRange("supportSpacing", 8, 2, 32);
        builder.pop();

        builder.comment("Ответвления").push("branches");
        BRANCHES_ENABLED = builder.define("branchesEnabled", true);
        STATIONS_ENABLED = builder.define("stationsEnabled", true);
        builder.pop();

        SPEC = builder.build();
    }

    private IronRoadsConfig() {}
}
```

- [ ] **Step 2: Зарегистрировать конфиг в `IronRoads.java`**

В конструкторе, после `ModFeatures.register(bus)`:

```java
ModLoadingContext.get().registerConfig(ModConfig.Type.SERVER, IronRoadsConfig.SPEC);
```

Импорты:

```java
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.config.ModConfig;
```

Тип `SERVER` выбран намеренно: геометрия сети обязана совпадать у сервера и всех клиентов.

- [ ] **Step 3: Заменить константы в `RailwayFeature`**

Удалить поля `SPACING`, `JITTER`, `MAX_GRADE_RUN`. В `place()` читать конфиг:

```java
RailNetwork network = new RailNetwork(seed,
        IronRoadsConfig.GRID_SPACING.get(),
        IronRoadsConfig.JITTER_AMPLITUDE.get());
TrackProfiler profiler = new TrackProfiler(network,
        IronRoadsConfig.MAX_GRADE_RUN.get(), terrain);
SupportBuilder supportBuilder = new SupportBuilder(
        IronRoadsConfig.SUPPORT_SPACING.get(),
        IronRoadsConfig.VIADUCT_MAX_HEIGHT.get());
TunnelBuilder tunnelBuilder = new TunnelBuilder(
        IronRoadsConfig.TUNNEL_MIN_DEPTH.get(), BED_HALF_WIDTH);
```

Второй путь магистрали класть только при включённой двухпутности — в `placeSegment` заменить парную укладку на:

```java
trackBuilder.layTrack(level, terrain, line, along, -2, chunkMinX, chunkMinZ);
if (IronRoadsConfig.MAINLINE_DOUBLE_TRACK.get()) {
    trackBuilder.layTrack(level, terrain, line, along, +2, chunkMinX, chunkMinZ);
}
```

Обернуть блок генерации ответвлений в `if (IronRoadsConfig.BRANCHES_ENABLED.get())`, а вызов `stationBuilder.build` внутри `placeBranch` — в `if (IronRoadsConfig.STATIONS_ENABLED.get())`.

- [ ] **Step 4: Проверить, что конфиг применяется**

Run: `./gradlew runClient`, создать мир, выйти в меню.
Открыть `run/saves/<мир>/serverconfig/ironroads-server.toml`, поменять `gridSpacing` на 256, зайти обратно, слетать в неисследованную область.

Expected: файл конфига создан со всеми тремя секциями и комментариями; в новых чанках магистрали идут заметно чаще.

- [ ] **Step 5: Запустить весь набор тестов и закоммитить**

Run: `./gradlew test`
Expected: PASS, 17 тестов.

```bash
git add src/main/java/dev/gensh/ironroads
git commit -m "feat: server config for network geometry and engineering thresholds"
```

---

### Task 11: Приёмка фазы 1

**Files:**
- Create: `README.md`

**Interfaces:**
- Consumes: всё построенное задачами 1–10
- Produces: собранный `build/libs/ironroads-0.1.0.jar`, пригодный для установки

- [ ] **Step 1: Написать `README.md`**

```markdown
# Iron Roads

Генерирует бесконечную сеть железных дорог на путях Create.

## Требования

- Minecraft 1.20.1
- Forge 47.2.0+
- Create 0.5.1

Совместим с Create: Steam 'n' Rails, но не требует его.

## Что генерируется

- Двухпутные магистрали по сетке с шагом 768 блоков, плавно виляющие
- Однопутные ответвления к станциям
- Насыпи, эстакады на бревенчатых опорах, мосты через воду
- Выемки и освещённые тоннели
- Продольный профиль с уклоном не круче 1:12

Пути автоматически регистрируются в графе Create — по ним можно ехать сразу.

## Настройка

`saves/<мир>/serverconfig/ironroads-server.toml`

## Ограничение

Дороги появляются только во вновь генерируемых чанках.
```

- [ ] **Step 2: Полная сборка с нуля**

Run: `./gradlew clean build`
Expected: `BUILD SUCCESSFUL`, 17 тестов пройдено, собран `build/libs/ironroads-0.1.0.jar`.

- [ ] **Step 3: Приёмочный прогон в игре**

Run: `./gradlew runClient`, новый мир, сид `1`, креатив.

Пройти по списку:

1. `/tp 0 100 0` — перекрёсток магистралей, обе линии сходятся в узел.
2. Лететь вдоль оси Z 3000 блоков — ни одного разрыва пути, ни одной ступени на границе чанка.
3. Найти водоём на трассе — мост с опорами до дна.
4. Найти гору — тоннель со сводом и фонарями.
5. `/tp 0 100 384` — ответвление со станцией.
6. Поставить поезд на магистраль, задать расписание между двумя станциями, убедиться, что он доезжает.
7. `/tp 5000 100 5000` — сеть присутствует и в удалённой области.

- [ ] **Step 4: Закоммитить и пометить версию**

```bash
git add README.md
git commit -m "docs: readme for phase 1 release"
git tag v0.1.0
```

---

## Что сознательно не входит в фазу 1

- Ответвления к настоящим ванильным деревням — фаза 2
- Безье-кривые вместо ступенчатых подъёмов — фаза 3
- Сигналы Create и разъезды — фаза 3
- Декор Steam 'n' Rails — фаза 3
- Персистентная пометка сшитых чанков — только если профилировщик покажет проблему
