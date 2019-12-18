---
layout: page
title: Installation
description: Getting started with BioMedICUS
permalink: /installation
---

## Prerequisites

- [Python 3.5+](https://www.python.org/)
- [Java JDK 8.0+](https://adoptopenjdk.net/index.html). Note, you will need to have the ["java" command on the your "$PATH"](https://www.java.com/en/download/help/path.xml).

## Installation

```bash
pip install biomedicus\[torch]
```

## Deploying the default BioMedICUS Pipeline

The following command runs a script that will start up all of the BioMedICUS services for processing clinical notes:

```bash
biomedicus deploy --download-data
```

## Processing a directory of text files using BioMedICUS

After deploying BioMedICUS, you can process a directory of documents using the following command:

```bash
biomedicus run /path/to/input_dir /path/to/output_dir
```

This will process the documents in the directory using BioMedICUS and save the results as json-serialized MTAP Events to output directory.

## Launching individual processors

Individual processors can be launched via their entry point on the [Components Page]({{ '/components' | relative_url }})

### Examples

#### Python

```bash
python -m biomedicus.sentences.bi_lstm processor -p 9091 --events localhost:9090
```
