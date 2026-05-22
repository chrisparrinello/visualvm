# VisualVM (master) sources repository

VisualVM is a visual tool integrating commandline JDK tools and lightweight profiling capabilities. See https://visualvm.github.io for details, downloads and documentation.

This fork adds a **headless OQL CLI** (`headless-oql`) and a **Gradle** build alongside the upstream Ant/NetBeans workflow.

## Git remotes (fork workflow)

| Remote | URL |
|--------|-----|
| `origin` | `git@github.com:chrisparrinello/visualvm.git` (this fork) |
| `upstream` | `git@github.com:oracle/visualvm.git` (Oracle) |

```bash
git fetch upstream
git merge upstream/master   # or rebase, as you prefer
```

## Headless OQL CLI (Gradle)

Run OQL queries against an HPROF heap dump without starting the VisualVM GUI. Dependencies are resolved from **Maven Central** (no NetBeans platform zip required).

### Requirements

- JDK **17+** to build and run `headless-oql`
- Gradle wrapper included (`./gradlew`)

### Build and install

```bash
./gradlew :headless-oql:installDist
```

The launcher is installed at:

`headless-oql/build/install/headless-oql/bin/headless-oql`

### Usage

```bash
headless-oql --heap /path/to/heap.hprof --query /path/to/query.oql
headless-oql --heap /path/to/heap.hprof -e "select s from java.lang.String s"
headless-oql --help
```

### Tests

```bash
./gradlew :headless-oql:test
```

Set `useMavenCentralLibs=false` in [`gradle.properties`](gradle.properties) to use locally built profiler JARs from the Ant suite (after building `lib.profiler.heap` and `profiler.oql` modules).

## Full VisualVM GUI (Ant + Gradle helpers)

The main VisualVM application is still built with **Ant** and the **NetBeans Platform 22** (hybrid approach).

### Get the tools

- Apache Ant 1.9.15 or above
- JDK 8+ for building VisualVM (JDK 17+ for `headless-oql` only)

### Configure the NetBeans platform

Either:

1. **Gradle (recommended):** download and extract the platform zip automatically:

   ```bash
   ./gradlew downloadNetbeansPlatform
   ```

   This installs the platform under `visualvm/visualvm/netbeans` (same layout as the manual step below).

2. **Manual:** download and extract [NetBeans Platform 22](https://github.com/oracle/visualvm/releases/download/2.2.1/nb220_platform_20260201.zip) into `visualvm/visualvm` (creates `visualvm/visualvm/netbeans`).

3. **From source:** build a patched platform with [`visualvm/build-nb.sh`](visualvm/build-nb.sh) (used by Oracle for releases).

### Build and run (Ant)

```bash
./gradlew buildVisualvm    # runs: ant build-zip in visualvm/visualvm
./gradlew runVisualvm      # runs: ant run
```

Or directly:

```bash
cd visualvm/visualvm
ant build-zip
ant run
```

### Build and run plugins

Use `ant build` or `ant run` in the `visualvm/plugins` directory (unchanged from upstream). This builds the core zip into `visualvm/visualvm/dist/visualvm.zip` and installs plugins.

## Project layout

| Path | Description |
|------|-------------|
| `headless-oql/` | Gradle application: headless OQL runner |
| `visualvm/visualvm/` | Ant suite: core VisualVM modules |
| `visualvm/plugins/` | Ant suite: VisualVM plugins |
| `gradle.properties` | Shared versions and platform zip URL |

## Contributing

We highly appreciate any feedback! Please let us know your ideas, missing features, or bugs found. Either [file a RFE/bug](https://github.com/oracle/visualvm/issues/new/choose) or [leave us a message](https://visualvm.github.io/feedback.html). For legal reasons, Oracle upstream does not accept external pull requests. See [CONTRIBUTING](./CONTRIBUTING.md) for details.

## Security

Please consult the [security guide](./SECURITY.md) for our responsible security vulnerability disclosure process

## License

Copyright (c) 2017, 2025 Oracle and/or its affiliates.
Released under the GNU General Public License, version 2, with the Classpath Exception.
