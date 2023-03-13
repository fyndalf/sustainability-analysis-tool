# sustainability-analysis-tool

[![scalafmt](https://github.com/fyndalf/sustainability-analysis-tool/actions/workflows/scala-lint.yml/badge.svg)](https://github.com/fyndalf/sustainability-analysis-tool/actions/workflows/scala-lint.yml)


## Overview

This is a CLI tool meant to be used in conjunction with the framework outlined in my [Master's thesis](http://dx.doi.org/10.13140/RG.2.2.16323.27688).
It is meant to analyse cost-driver enriched event logs, analyse them, and enhance related process models in order to assess and reduce their environmental impact.
Also, a visualized comparison between logs before and after process-redesign is supported.

## Application

### Simulation with Scylla

⚠️ important points below ⚠️
- Either at least one event log needs to have been generated using a fork of the business process simulation engine [Scylla](https://github.com/fyndalf/scylla/tree/thesis-implementation), or it must have been extracted from an POIS. In both cases, the format will need to follow the .XES standard and provide attditional information:

```xml
<trace>
    	<string key="concept:name" value="trace_id"/>
    	<string key="cost:variant" value="one variant to be assessed"/>
    	...
    	<event>
    		<string key="concept:name" value="activity name"/>
    		<string key="lifecycle:transition" value="start"/>
    		<date key="time:timestamp" value="2022-05-30T18:09:24+02:00"/>
    	</event>
    	<event>
    		<string key="cost:driver" value="name of abstract driver that is concretized in cost variant above"/>
    		<string key="concept:name" value="another activity name"/>
    		<string key="lifecycle:transition" value="complete"/>
    		<date key="time:timestamp" value="2022-05-30T18:19:24+02:00"/>
    	</event>
    	...
</trace>
```

- Additionally, only BPMN diagrams created with [bpmn.io](https://demo.bpmn.io) can be simulated with Scylla, due to internal parsing and namespace issues. Thus, this tool also relies on it.
- In the model, cost drivers are prefixed by "Cost Driver: ", followed by the name of the abstract cost driver.
- When modelling abstract cost drivers, make sure to only have *one* data object per abstract cost driver in the model. Further, the associations are directed _from_ cost drivers _to_ activities.
- Cost config files follow the following XML format, where the names of abstract cost drivers needs to correspond to those of the process model:

```xml
<?xml version="1.0" encoding="UTF-8"?>
<costVariantConfig>
    <variant id="one variant to be simulated" frequency="between 0 and 1">
        <driver id="name of abstract cost driver" cost="concrete cost score, e.g. 0.0001"/>
        ...
    </variant>
    <variant id="another variant to be simulated" frequency="between 0 and 1">
        <driver id="name of abstract cost driver" cost="potentially different concrete cost score, e.g. 0.00099"/>
        ...
    </variant>
    ...
</costVariantConfig>
```

### Analysis with this Tool

Using this tool and event logs either simulated or extracted, the following analyses can be made:
- what are the average environmental impacts of a process recorded by an event log?
  - the impacts of individual activities can be highlighted in comparison to the average impact per activity in a process model
- what are the differences in average environmental impacts between two event logs (i.e., for example between before and after process re-design)?
  - the differences can be expressed both in absolute and in relative terms
  - the differences can additionally be highlighted in a process model, indicating improvement or deterioration

Based on this, new iterations with potentially new cost variant configs and simulations runs can be conducted.

## Usage and Installation

### Usage

```
Usage: sustainability-analysis-tool [--process-model <path>] [--second-log <path>] [--second-config <path>] [--relative] [--average_difference] <log-file> <cost-variant-config>

Analyse cost-driver enriched event logs and processes

Options and flags:
    --help
        Display this help text.
    --process-model <path>
        Process Model of the first process execution
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

