---
layout: doc
title: Custom Document Input
description: Reading from Databases and Other Sources
subpage: Guides
---
## About

This guide will teach you how to processing documents using BioMedICUS from a
database or some other custom source. In this example we will be using a
sqlite database of documents.


## Pre-Requisites

Before starting this tutorial, [install BioMedICUS using these instructions.](../installation)

This tutorial assumes we are in the active virtual environment from the
installation guide.


## First Steps

In this tutorial we will be running BioMedICUS from a Python script file. Start
off by creating a file ``sql_pipeline.py`` in your favorite text editor or IDE.


## Instantiating the Default Pipeline

We will start by creating an instance of the default BioMedICUS pipeline. This
is done by parsing the pipeline's options from the command line, done here by
using the ``parents=[default_pipeline.argument_parser()]`` argument when
creating a new parser. We will also add our own argument ``input_file`` which
will be the path to the input sqlite file.

```python
import sqlite3
from argparse import ArgumentParser

from biomedicus.deployment import default_pipeline
from mtap import Event

if __name__ == '__main__':
    parser = ArgumentParser(add_help=True, parents=[default_pipeline.argument_parser()])
    parser.add_argument('input_file')
    args = parser.parse_args()
    with default_pipeline.from_args(args) as default_pipeline:
        pass
```

## Creating a sqlite Document Source

Next, we will create a document source. Update the above code starting
with ``with default_pipeline`` to the following, replacing the ``pass``
statement:

```python
with default_pipeline.from_args(args) as default_pipeline:
  client = pipeline.events_client
  con = sqlite3.connect(args.input_file)
  cur = con.cursor()

  def source():
      for name, text in cur.execute("SELECT NAME, TEXT FROM DOCUMENTS"):
          with Event(event_id=name, client=client) as e:
              doc = e.create_document('plaintext', text)
              yield doc
```

This is a Python generator function which will read a document from the
database, create an MTAP ``Document`` object, and then yield that object. This
function will be used by the pipeline to provide all the documents it needs to
process.

<div class='alert alert-info' role='alert'>
Note here that we've pre-populated the SQL <code class="highligher-rogue">SELECT</code> statement with the <code class="highligher-rogue">NAME</code> and <code class="highligher-rogue">TEXT</code> fields and the <code class="highligher-rogue">DOCUMENTS</code> table. If you have a database in mind you can substitute your own fields and table or even parameterize them using the <code class="highlighter-rogue">ArgumentParser</code>.
</div>

## Passing the Document Source to Pipeline

Finally, we will pass the source to the ``run_multithread`` method which will
run the pipeline on the documents. First, though, we use a ``SELECT`` statement
and ``COUNT`` to count the number of documents, and will pass that to the
method, using the total argument. This is completely optional and only serves to
enable a nice progress bar. After we have finished processing the documents, we
use ``print_times`` to print statistics about the different processors run
times, and will close our sqlite connection using ``con.close()``.

```python
with default_pipeline.from_args(args) as pipeline:
    ...

    def source():
        ...

    count, = next(cur.execute("SELECT COUNT(*) FROM DOCUMENTS"))
    pipeline.run_multithread(source(), total=count)
    pipeline.print_times()
    con.close()
```

<div class='alert alert-info' role='alert'>
Notice we use the Pipeline's <code class="highligher-rogue">run_multithread</code> method here. You can learn more about this method in the <a href="https://nlpie.github.io/mtap-python-api/mtap.html#mtap.Pipeline.run_multithread" class="alert-link">MTAP documentation</a>.
</div>

We're done with this file now. Save the changes to the file.

## Final Script

The script in its final state is shown below:

```python
from argparse import ArgumentParser
import sqlite3

from biomedicus import default_pipeline
from mtap import Event

if __name__ == '__main__':
    parser = ArgumentParser(add_help=True, parents=[default_pipeline.argument_parser()])
    parser.add_argument('input_file')
    args = parser.parse_args()
    with default_pipeline.from_args(args) as pipeline:
        client = pipeline.events_client
        con = sqlite3.connect(args.input_file)
        cur = con.cursor()

        def source():
            for name, text in cur.execute("SELECT NAME, TEXT FROM DOCUMENTS"):
                with Event(event_id=name, client=client) as e:
                    doc = e.create_document('plaintext', text)
                    yield doc

        count, = next(cur.execute("SELECT COUNT(*) FROM DOCUMENTS"))
        pipeline.run_multithread(source(), total=count)
        pipeline.print_times()
        con.close()
```

## Running the Pipeline

Before we can run the pipeline script we just created we need to first deploy
the BioMedICUS processors. In one console window in a BioMedICUS virtual
environment run the following:

```bash
b9 deploy
```

Once the ``Done deploying all servers`` line shows up, open another console
window and enter the BioMedICUS virtual environment. We will need a sqlite
database containing some notes, I've made one available
[here](/resources/example.db3). This file contains 15 de-identified free notes
from the [MTSamples Website](https://mtsamples.com). Download the database file
and place in the working directory with your script. To process these 15 notes
using the script we just created, run the following:

```bash
python sql_pipeline.py example.db3 --include-label-text
```

This will create a folder named ``output`` in the directory and place 15 json
files containing the serialized results of processing. To view the serialized
output you can use the following:

```bash
python -m json.tool output/97_98.json
```

## Next Steps

Note that this method will work for more than just sqlite3 databases. Anything
that can be placed in a for loop that iterates over text samples can be used
instead of the sqlite cursor in this guide. For example, it could be splitting
a single file into multiple documents, or using a different type of database,
or even using a queue of documents provided by some other source such as a
service endpoint.

This method for creating pipelines using can also work in conjunction with
[RTF Processing](/guides/rtf-processing) and using your own
[custom pipeline components](/guides/dev-tutorial/tutorial-1).
