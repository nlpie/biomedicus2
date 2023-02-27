---
layout: default
title: 3. Running the Pipeline
parent: Developer Tutorial
nav_order: 3
---

## About

In the final part of our guide that started with [creating our own processor](tutorial-1) we will be using the deployed processor to process some documents.

## Writing the pipeline configuration file

We'll start by writing out the configuration file for the pipeline.

```bash
b9client write-config pipeline
```

This creates a file named ``biomedicus_default_pipeline.yml`` which we will edit to add our processor we wrote and deployed earlier. Open that file in your favorite text editor.

{: .note }
This requires the BioMedICUS virtual environment created during installation to be active.

## Adding our processor

Again, the change we need to make here are small, the block starting with

```yaml
components:
  - processor_id: biomedicus-sentences
    address: localhost:50300
```

is a list of the processors and their addresses in the order which they will be run. If you remember from our [previous tutorial](tutorial-2), we assigned our new processor the port 52000. Now we will add it to the list of processors at the end of the file:

```yaml
  - processor_id: biomedicus-medications-tutorial
    address: localhost:52000
```

## Running the pipeline

First, we need some documents to process, if you don't have any you can use an [MTSamples.com corpus we have made available](https://github.com/nlpie/nlptab-corpus). Download these documents and extract them to a folder.

We're finally ready to run our processor we created along with the rest of the BioMedICUS pipeline, which you can do with the following command:

```bash
b9client run --config biomedicus_default_pipeline.yml --include-label-text INPUT_DIRECTORY -o OUTPUT_DIRECTORY
```

Replace the input directory with the directory where the documents you want processed are and the output directory with the directory where you want results stored.

{: .note }
Note this requires that the BioMedICUS processors be deployed and running in another terminal window or tab.

## Viewing results

The default BioMedICUS pipeline and run command will serialize the documents as json. By default the files are not prettified, but you can do that by running the following:

```bash
python -m json.tool 97_98.txt.json
```

This command will print out the json file prettified, in that file you can find the "medication_sentences" label index containing examples like the following:

```json
{
    "start_index": 1354,
    "end_index": 1529,
    "identifier": 7,
    "fields": {},
    "reference_ids": {
        "concepts": [
            "umls_concepts:683",
            "umls_concepts:684",
            "umls_concepts:685",
            "umls_concepts:686",
            "umls_concepts:687",
            "umls_concepts:688",
            "umls_concepts:689",
            "umls_concepts:690",
            "umls_concepts:691",
            "umls_concepts:692",
            "umls_concepts:693",
            "umls_concepts:694",
            "umls_concepts:695",
            "umls_concepts:696",
            "umls_concepts:697",
            "umls_concepts:698",
            "umls_concepts:699",
            "umls_concepts:700",
            "umls_concepts:701",
            "umls_concepts:702",
            "umls_concepts:703",
            "umls_concepts:704",
            "umls_concepts:705"
        ]
    },
    "_text": "She was taking Remeron 15 mg q.h.s., Ambien 5 mg q.h.s. on a p.r.n. basis, Ativan 0.25 mg every 6 hours on a p.r.n. basis, and Klonopin 0.25 mg at night while she was at home."
}
```

You can see the start_index and end_index of where the label occurs in text, as well as a list of concepts in the sentence. Finally the text of the sentence is shown as ``_text`` because we used the ``--include-label-text`` flag while running.

## Conclusion

This concludes our tutorial on how to create a processor that runs with BioMedICUS. Now that you have the basics down, the possibilities are endless for what you can do. Good luck!
