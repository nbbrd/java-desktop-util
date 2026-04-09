# AGENTS.md — java-desktop-util

## Overview

`java-desktop-util` is a collection of desktop utilities for Java 8+, developed by the National Bank of Belgium and licensed under the EUPL. It provides reusable Swing components, OS-level desktop integration, time-series charting, favicon fetching, and Font Awesome icon support, packaged as independent Maven modules published to Maven Central.

## Architecture

The project is a multi-module Maven build with JPMS support:

| Module                      | JPMS name               | Purpose                                                                                                               |
|-----------------------------|-------------------------|-----------------------------------------------------------------------------------------------------------------------|
| `java-desktop-util-fa`      | _(none)_                | Font Awesome icons as a Swing `Icon` enum                                                                             |
| `java-desktop-util-chart`   | _(none)_                | Time-series chart API (`TimeSeriesChart`, `ColorScheme`) with JFreeChart-based Swing renderer                         |
| `java-desktop-util-swing`   | `nbbrd.desktop.swing`   | General-purpose Swing utilities: auto-completion, data transfer, grid/table/list components, misc widgets             |
| `java-desktop-util-os`      | `nbbrd.desktop.os`      | OS-level `Desktop` abstraction with platform implementations (Windows, macOS, XDG/Linux, AWT fallback) loaded via SPI |
| `java-desktop-util-favicon` | `nbbrd.desktop.favicon` | Async favicon fetching with `FaviconSupplier` SPI; built-in providers: Google, IconHorse                              |
| `java-desktop-util-demo`    | _(none)_                | Interactive demo application                                                                                          |
| `java-desktop-util-bom`     | _(none)_                | Bill of Materials for consumers                                                                                       |

Key design patterns:
- **SPI via `java-service-util`**: extensible points (`Desktop.Factory`, `FaviconSupplier`) are declared with `uses`/`provides` in `module-info.java` and wired at runtime via `ServiceLoader`.
- **Public API vs internals**: public types live in the root package or `spi` sub-package; implementation classes are under `internal.*` and never exported.
- **No cross-module dependencies** between the utility modules (fa, chart, swing, os, favicon are independent of each other).


## Build & Test

```shell
mvn clean install                 # full build + tests + enforcer checks
mvn clean install -Pyolo          # skip all checks (fast local iteration)
mvn test -pl <module-name> -Pyolo # fast test a single module
mvn test -pl <module-name> -am    # full test a single module
```

- **Java 8 target** with JPMS `module-info.java` compiled separately on JDK 9+ (see `java8-with-jpms` profile in root POM)
- **JUnit 5** with parallel execution enabled (`junit.jupiter.execution.parallel.enabled=true`); **AssertJ** for assertions

## Key Conventions

- **Lombok**: use lombok annotations when possible. Config in `lombok.config`: `addNullAnnotations=jspecify`, `builder.className=Builder`
- **Nullability**: `@org.jspecify.annotations.Nullable` for nullable; `@lombok.NonNull` for non-null parameters. Return types use `@Nullable` or the `OrNull` suffix (e.g., `getThingOrNull`)
- **Design annotations** use annotations from `java-design-util` such as `@VisibleForTesting`, `@StaticFactoryMethod`, `@DirectImpl`, `@MightBeGenerated`, `@MightBePromoted`
- **Internal packages**: `internal.<project>.*` are implementation details; public API lives in the root and `spi` packages
- **Static analysis**: `forbiddenapis` (no `jdk-unsafe`, `jdk-deprecated`, `jdk-internal`, `jdk-non-portable`, `jdk-reflection`), `modernizer`
- **Reproducible builds**: `project.build.outputTimestamp` is set in the root POM
- **Formatting/style**: 
  - Use IntelliJ IDEA default code style for Java
  - Follow existing formatting and match naming conventions exactly
  - Follow the principles of "Effective Java"
  - Follow the principles of "Clean Code"
- **Java/JVM**: 
  - Target version defined in root POM properties; some modules may require higher versions
  - Use modern Java feature compatible with defined version

## Agent behavior

- Do respect existing architecture, coding style, and conventions
- Do prefer minimal, reviewable changes
- Do preserve backward compatibility
- Do not introduce new dependencies without justification
- Do not rewrite large sections for cleanliness
- Do not reformat code
- Do not propose additional features or changes beyond the scope of the task
