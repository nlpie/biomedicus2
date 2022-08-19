---
layout: doc
title: Installation
description: Getting started with BioMedICUS
subpage: Guides
redirect_from: /installation
---

## Prerequisites

- [Python 3.5+](https://www.python.org/)
- [Java JDK 9.0+](https://adoptopenjdk.net/index.html). Note, you will need to have the ["java" command on the your "$PATH"](https://www.java.com/en/download/help/path.xml).

## Create a Virtual Environment

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

## Install PyTorch Libraries

BioMedICUS requires PyTorch, a machine learning framework. Installation instructions for PyTorch can be found [here](https://pytorch.org/get-started/locally/). Select your platform and "Pip", and "None" for CUDA unless you have a NVIDIA graphics card and have installed the [CUDA toolkit](https://developer.nvidia.com/cuda-downloads).

## Install BioMedICUS

```bash
pip3 install biomedicus
```

## Deploy the default BioMedICUS processors

The following command runs a script that will start up all of the BioMedICUS services for processing clinical notes:

```bash
biomedicus deploy --download-data
```

## Process a directory of text files using BioMedICUS

After deploying BioMedICUS, you can process a directory of documents using the following command:

```bash
biomedicus run --include-label-text /path/to/input_dir -o /path/to/output_dir
```

This will process the documents in the directory using BioMedICUS and save the results as json-serialized MTAP Events to output directory.

## Viewing results

The default BioMedICUS pipeline and run command will serialize the documents as json. By default the files are not prettified, but you can do that by running the following:

```bash
python -m json.tool /path/to/output_file.json
```
