---
layout: default
title: Scaling Configuration
nav_order: 3
---
## About

This guide will teach you about the various scaling options available for BioMedICUS.

## Prerequisites

You should have BioMedICUS installed according to the [installation guide.](installation)

## Deployment Scaleout

BioMedICUS inludes a configuration file for scaling deployed processors, to write this configuration file run the following command in a terminal:

```bash
b9 write-config scaleout_deploy
```

### Events Service Pooling

The first thing to notice is that the events service configuration has multiple addresses:

```yaml
events_service:
  enabled: yes
  addresses:
    - localhost:50100
    - localhost:50101
    - localhost:50102
    - localhost:50103
    - localhost:50104
    - localhost:50105
    - localhost:50106
    - localhost:50107
```

In this configuration, the event service is launched 8 times and all events services are used as potential endpoints for new documents created. The deployed processors are made aware of this and use a unique identifier per events service to determine which service needs to be accessed to look up the particular event and data it needs. 

{: .highlight }
This helps overcome an event service bottleneck resulting from the Python global interpreter lock. If after adding more workers to processors and the event service you are not seeing utilization scale it could be because of an event service bottleneck and you should either enable or increase the size of the events service pool by adding more addresses.

{: .warning }
This is currently incompatible with the NGINX reverse proxy docker-compose.

### Events Service Workers

The ``events_service.workers`` value is the number of threads the events service has working to respond to requests. Note that these are Python threads and Python has a Global Interpreter Lock (GIL), meaning they aren't actually concurrently executed, and the only gains here are when a thread has to wait for io.


### Processor Workers

Processors also have a number of workers to respond to requests. This is controlled by the ``shared_processor_config.workers`` setting and also the ``workers`` setting on each individual processor (example shown). The ``workers`` setting on the individual processors will override the shared setting.

```
  - implementation: java
    enabled: no
    entry_point: edu.umn.biomedicus.rtf.RtfProcessor
    port: 50200
    workers: 32
```

In Java, this determines the number of threads that are responding to document requests. In Python, it also determines the number of threads, but note that heavily compute-bound tasks won't see much improvement because of the Python GIL.

### Processor Instances

Processors also have an ``instances`` parameter. This parameter controls process-level parallelism, with the processor being launched the number of times specified by ``instances`` (example shown).

```
  - implementation: python
    entry_point: biomedicus.negation.deepen
    port: 50900
    instances: 4
```

This parameter can help overcome Python GIL-based bottlenecks. By default the instances have port numbers incremented from the first port number, but the ``port`` setting can also be replaced by a list. Note that you will need to update the run configuration to add the addresses of the additional servers.

{: .warning }
This setting will increase memory consumption by a multiple of the number of instances since instances do not share memory and will need to load the same processors.

{: .warning }
This is currently incompatible with the NGINX reverse proxy docker-compose.

### Multiprocessing Processors

Several of our Python processors, notably ``biomedicus.sentences.bi_lstm`` and ``biomedicus.dependencies.stanza_selective_parser`` support Python multiprocessing concurrency. This uses a process pool to handle requests in addition to the thread pool. This process pool can be enabled by specifying the ``--mp`` flag in ``additional_args``:

```yaml
  - implementation: python
    entry_point: biomedicus.sentences.bi_lstm
    port: 50300
    pre_args: ['processor']
    additional_args: ['--mp']
```

This can help overcome bottlenecks due to the Python GIL in compute-heavy tasks. You may want to use this option if you see heavy process utilization for these specific processors and scaling workers on these or other processors does not improve overall utilization.

## Pipeline Configuration

BioMedICUS also includes a scaleout configuration for the pipeline run by the ``b9lient run`` command. You can write this configuration to disk with the following command:

```bash
b9client write-config scaleout-pipeline
```

### Workers and Read-ahead

The ``mp_config.workers`` is the number of processes that will independently move events/documents through the pipeline, the concurrency level for the pipeline. The ``mp_config.read_ahead`` is the maximum number of documents the source thread should prepare ahead of time, i.e. read into memory. The read ahead setting helps prevent blocking from a worker process having to wait for documents to be read when it needs them.

### Specifying multi-instance processors

If you enabled multiple instances of a processor above and wish to use them, you can specify the addresses via a comma-separated list, example:

```bash
  - name: biomedicus-deepen
    address: localhost:50900,localhost:50901,localhost:50902,localhost:50903
```
