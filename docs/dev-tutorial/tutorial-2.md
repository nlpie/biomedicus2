---
layout: doc
title: Part 2. Deploying the Processor
description: How to include your processor in a BioMedICUS deployment configuration.
subpage: Documentation
---

## About
Now that we've created the processor in [the previous tutorial](tutorial-1), how do we go about deploying a server that runs our processor along with the rest of the BioMedICUS components? In this tutorial, we will examine using MTAP deployment, which does just that.

## Writing out the default deployment script

As a starting point let's write the default BioMedICUS deployment script to our current directory using the following command:

```bash
biomedicus write-config deploy
```

<div class="alert alert-warning">
Note this requires the BioMedICUS virtual environment created during installation to be active.
</div>

This command will create a file named ``biomedicus_deploy_config.yml`` in the current folder. Open that file in your favorite editor.


## Adding the processor

This file contains some global configuration, configuration for the event service, and configuration shared by processors, but what we're interested in is the section that starts with:

```yaml
processors:
  - implementation: java
    enabled: no
    entry_point: edu.umn.biomedicus.rtf.RtfProcessor
    port: 50200
  - implementation: python
    entry_point: biomedicus.sentences.bi_lstm
    port: 50300
    pre_args: ['processor']
```

This is a list of processors to deploy, how to start them, and configuration. We will be adding our processor to the list by appending the following at the bottom:

```yaml
  - implementation: python
    entry_point: medications
    port: 52000
```

Here we're telling it to use Python, look for a module (file) named ``medications``, and to launch the processor server on port 52000. That's it for changes we need to make to this file, and you can save it.

## Deploying the processors

Now that we've saved our updated configuration file, we can deploy the entire pipeline, to do so run the following command:

```bash
biomedicus deploy --config biomedicus_deploy_config.yml
```

After you see

```
INFO:mtap.processing._service:Started processor server with id: "biomedicus-medications-tutorial"  on address: "127.0.0.1:52000"
Done deploying all servers.
```

That means that the deployment finished and all servers are running in your current terminal window in the foreground.

<div class='alert alert-info' role='alert'>
When you want to shut down all the servers press Ctrl+C and it will cleanly shut down all processors before exiting.
</div>

## What did we just do

BioMedICUS and MTAP work together to read this configuration file and then launch all the processors listed. When it runs it launches all the servers, each as their own process and manages those processes for their lifetime.

## Next Steps

See [Part 3 of the developer tutorial: Deploying your processor with BioMedICUS](tutorial-3)
