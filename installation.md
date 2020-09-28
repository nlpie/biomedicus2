---
layout: page
title: Installation
description: Getting started with BioMedICUS
permalink: /installation
---

## Prerequisites

- [Python 3.5+](https://www.python.org/)
- [Java JDK 9.0+](https://adoptopenjdk.net/index.html). Note, you will need to have the ["java" command on the your "$PATH"](https://www.java.com/en/download/help/path.xml).

## Virtual Environment

We recommend that you use a [Python 3 virtual environment](https://docs.python-guide.org/dev/virtualenvs/#lower-level-virtualenv), a local environment of installed packages, to avoid any dependency conflicts.

Linux / MacOS
```bash
pip3 install virtualenv
python3 -m virtualenv biomedicus_venv
source biomedicus_venv/bin/activate
```

Windows
```bat
pip3 install virtualenv
python3 -m virtualenv biomedicus_venv
biomedicus_venv\Scripts\activate
```

## PyTorch

BioMedICUS requires PyTorch, a machine learning framework. Installation instructions for PyTorch can be found [here](https://pytorch.org/get-started/locally/). Select your platform and "Pip", and "None" for CUDA unless you have a NVIDIA graphics card and have installed the [CUDA toolkit](https://developer.nvidia.com/cuda-downloads).

## Installation

```bash
pip3 install biomedicus
```

## Deploying the default BioMedICUS Pipeline

The following command runs a script that will start up all of the BioMedICUS services for processing clinical notes:

```bash
biomedicus deploy --download-data
```

## Processing a directory of text files using BioMedICUS

After deploying BioMedICUS, you can process a directory of documents using the following command:

```bash
biomedicus run --include-text /path/to/input_dir /path/to/output_dir
```

This will process the documents in the directory using BioMedICUS and save the results as json-serialized MTAP Events to output directory.
