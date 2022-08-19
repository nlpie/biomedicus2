---
layout: doc
title: RTF Processing
description: Processing Rich Text Format documents using BioMedICUS
subpage: Guides
---
## About

This tutorial will guide you on processing Rich Text Format documents using BioMedICUS, as well as converting RTF to plain text using BioMedICUS.

## Prerequisites

Before starting this tutorial, [install BioMedICUS using these instructions.](installation) and familiarize yourself with deploying and running BioMedICUS.

## Processing RTF

How to process RTF documents using the full BioMedICUS pipeline.

### Deploy the default BioMedICUS processors

The following command runs a script that will start up all of the BioMedICUS services for processing clinical notes:

```bash
biomedicus deploy --download-data --rtf
```

### Process a directory of RTF files using BioMedICUS

After deploying BioMedICUS, you can process a directory of documents using the following command:

```bash
biomedicus run --rtf --include-label-text /path/to/input_dir -o /path/to/output_dir
```

This will processing all documents in the input directory and its child directories with the extension ".rtf" and output serialized

## Converting RTF to Plain Text

How to convert RTF documents to plain text using the rtf-to-text utilities.

### Deploy the RTF conversion processor

The following command runs a script that will start up the BioMedICUS service for RTF text conversion:

```bash
biomedicus deploy-rtf-to-text
```

### Process a directory of RTF into plain text files

After deploying the BioMedICUS RTF conversion processor, you can process a directory of documents using the following command:

```bash
biomedicus run-rtf-to-text /path/to/input_dir /path/to/output_dir
```
