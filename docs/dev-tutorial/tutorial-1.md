---
layout: doc
title: Part 1. Creating a Processor
description: Learn to create a Processor which analyzes and adds to BioMedICUS's output
subpage: Documentation
---

## About

In this first tutorial, we will be creating an MTAP processor for BioMedICUS. A processor is an independently scalable pipeline component which performs some sort of work on Documents and existing Labels, adds new labels to the Document, reads documents from a data store, or writes results to a destination.

In this example we will be looking at BioMedICUS-labeled UMLS concepts to find instances of the T121 "Pharmacologic Substance" type and create a new Label "medication sentence" over each sentence where that type occurs.

## Prerequisites

Before starting this tutorial, [install BioMedICUS using these instructions.](../../installation)

BioMedICUS uses a framework we developed called MTAP as its data model. You can find more about MTAP on [this web site](https://nlpie.github.io/mtap/docs), including short [instructions on creating a generic python processor](https://nlpie.github.io/mtap/docs/tutorials/python.html). We will also be creating a processor in this tutorial, albeit one designed specifically to work with BioMedICUS.


## Creating the Processor File

First, if it isn't already active, we'll start by activating the BioMedICUS virtual environment as shown in the installation instructions.

**Linux / MacOS**
```bash
source biomedicus_venv/bin/activate
```



Next, we'll create the file which will contain the processor, calling it ``medications.py``. Open this file using the text editor of your choice.

## Adding the MTAP Processor Skeleton

The first step is to place the skeleton of what will become our processor. This skeleton is a well-defined entry point and class structure used by MTAP to deploy our code in a way that it can be deployed as a service and called using remote requests.

```python
import mtap


@mtap.processor('biomedicus-medications-tutorial')
class MedicationsProcessor(mtap.DocumentProcessor):
    def process_document(self, document, params):
        # Empty for now
        pass


if __name__ == '__main__':
    mtap.run_processor(MedicationsProcessor())
```

This is everything we need to deploy the processor and send it documents, although it doesn't do anything yet. The following steps provide an implementation that processes the documents that this processor receives.

## Setting up the Data Model

There are two objects we'll need to work with on the document--a way to add new labels, and a way to retrieve the ``sentences`` and ``umls_concepts`` label indices that BioMedICUS has added to the documents.

First, we'll set up the label indices that we need to inspect. Replace the
```python
# Empty for now
    pass
```
block with the following:
```python
sentences = document.labels['sentences']
umls_concepts = document.labels['umls_concepts']
```
These are both ``LabelIndex`` objects, which represent a collection of all the labels of a specific type that have been added to the document by BioMedICUS before this processor is called.

<div class='alert alert-info' role='alert'>
Curious about the <code class="highligher-rogue">LabelIndex</code> type and its functionality? Learn about them from the <a href="https://nlpie.github.io/mtap-python-api/mtap.html#mtap.data.LabelIndex" class="alert-link">MTAP documentation</a>.
</div>


Second, we'll create the ``Labeler`` for our new ``medication_sentences`` index. Beneath the index objects add the following:
```python
with document.get_labeler('medication_sentences') as MedicationSentence:
    pass
```

This creates a function ``MedicationSentence`` which can be used to construct new labels.

## Iteration Logic

Now we will step through sentences one at a time, also iterating over the UMLS concepts at the same time. We will use for-in iteration for the sentences and a filter function along with for-in iteration for the concepts. Replace ``pass`` with the following:

```python
for sentence in sentences:
    medication_concepts = []
    for concept in umls_concepts.inside(sentence):
        pass
    # create label
```

The ``medication_concepts`` list is where we will store our results. To perform our test replace ``pass`` with the following:

```python
if concept.tui == 'T121':
    medication_concepts.append(concept)
```

<div class='alert alert-info' role='alert'>
The concept type, including its tui field, is explained on the <a href="../../components" class="alert-link">BioMedICUS Components</a> page.
</div>

Finally, we will use the ``MedicationSentence`` labeler to create the actual labels by replacing ``#create label`` with the following:

```python
if len(medication_concepts) > 0:
    MedicationSentence(sentence.start_index, sentence.end_index,
                       concepts=medication_concepts)
```

This creates the label which is automatically stored by the labeler. When the ``with`` block used to initialize the labeler is exited, all the stored labels are uploaded to the events service which stores all information about the document.

## Complete example

After all this work the processor should look like the following:

```python
import mtap


@mtap.processor('biomedicus-medications-tutorial')
class MedicationsProcessor(mtap.DocumentProcessor):
    def process_document(self, document, params):
        sentences = document.labels['sentences']
        umls_concepts = document.labels['umls_concepts']
        with document.get_labeler('medication_sentences') as MedicationSentence:
            for sentence in sentences:
                medication_concepts = []
                for concept in umls_concepts.inside(sentence):
                    if concept.tui == 'T121':
                        medication_concepts.append(concept)
                if len(medication_concepts) > 0:
                    MedicationSentence(sentence.start_index, sentence.end_index,
                                       concepts=medication_concepts)


if __name__ == '__main__':
    mtap.run_processor(MedicationsProcessor())
```



## Next Steps

See [Part 2 of the developer tutorial: Deploying your processor with BioMedICUS](tutorial-2)
