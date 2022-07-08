# sustainability-analysis-tool

[![scalafmt](https://github.com/fyndalf/sustainability-analysis-tool/actions/workflows/scala-lint.yml/badge.svg)](https://github.com/fyndalf/sustainability-analysis-tool/actions/workflows/scala-lint.yml)

Unless you're actually reviewing my thesis, it's probably not worthwhile checking out this code, but I hope you enjoy your stay here anyways!

## Overview

This is a CLI tool meant to be used in conjunction with the framework outlined in my [Master's thesis](https://github.com/fyndalf/master-thesis).
It is meant to analyse cost-driver enriched event logs, analyse them, and enhance related process models.
Also, comparison between logs before and after process-redesign is supported. 


## Usage and Installation

### Usage

```
Usage: sustainability-analysis-tool [--process-model <path>] [--second-log <path>] [--second-config <path>] [--relative] [--average_difference] <log-file> <cost-variant-config>

Analyse cost-driver enriched event logs and processes

Options and flags:
    --help
        Display this help text.
    --process-model <path>
        Process Model of the first simulation run
    --second-log <path>
        Second log file after re-design
    --second-config <path>
        Second log file after re-design
    --relative
        Perform a relative comparison of process costs instead of absolute comparison.
    --average_difference
        Perform a difference analysis based on average differences. If not, it is done in relation to a difference of 0.
```

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console`
will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

### Installation

Either clone this repository and interact with it using `sbt run`, or download one from the
[release](https://github.com/fyndalf/sustainability-analysis-tool/releases) section of this repository.

### Requirements

In order for the tool to run, a working installation of `Java 16` is required, as well as `SBT 1.6.2` (or later)
and `Scala 3.1.2`. SBT should be able to pick up the required Scala version automatically.

For development, using `IntelliJ IDEA 2022.1.2` or newer, including the Scala3 plugin, is recommended.

### Packaging

A `.jar` file can be built by calling

```bash
$ sbt assembly
```

The binary will reside inside the `target` folder, and can be used instead of explicitly using SBT. For this,
only `Java 16` is required. If one were to use `sbt package`, scala dependencies would be missing and scala would be
required instead of java only. Then, the binary can be called with

```bash
$ java -jar sustainability-analysis-tool.jar <...>
```

