---
layout: default
title: Docker
nav_order: 5
---

## About

We've made available a Docker image containing all the prerequisites and model files necessary to run BioMedICUS. Using this Docker image, you can immediately start processing documents and even extending the pipeline with your own processing components.

## Pre-requisites

The following pre-requisites are required for this guide.

 - Docker Engine version greater than 17.06
    - You can get Docker Engine either via installing [Docker Desktop](https://docs.docker.com/get-docker/) or by 
      [installing  the engine directly on Linux.](https://docs.docker.com/engine/install/)
    - You can tell your current engine version with the command ``docker version``.

{: .warning}
If you are using Docker Desktop on either macOS or Windows, the images are run using virtualization and the default memory allocation may be insufficient. BioMedICUS requires at least 10 GB of memory to run. You can change Docker Desktop's memory allocation using the Resources page in Settings.

## Getting Started

To start, create a directory that we will mount to the docker image. This directory will contain folders for both the input and the output of the BioMedICUS system, as well as any configuration changes we wish to make and additional processors we wish to run.

```bash
mkdir b9
cd b9
```

## Running the Image

After that we can launch the docker image using the following command:

```bash
docker run -it -d -v $(pwd):/b9/ -w /b9/ --name b9 nlpie/biomedicus:latest
```

{: .highlight }
This will start a new container with the name ``b9`` and the folder we just created mounted on the image as ``/b9/`` and set to the working directory (``-w /b9/``).

This will take some amount of time to start all of the BioMedICUS processors, to follow its progress you may use the following command:

```bash
docker logs -f b9
```

When you see the following line of output, it is done deploying:

```bash
Done deploying all servers.
```

## Processing Documents

Once the image has started and finished deploying the servers, make a directory called ``in`` in the original directory you created and place any documents you wish to process in that directory.

The following command will process those documents on the image:

```bash
docker exec -it b9 b9client run --include-label-text in -o out
```

{: .highlight }
``docker exec`` runs a new command in an existing container, ``b9`` is the container name, and ``b9client run --include-label-text in -o out`` is the command being run. Since we mounted our folder on the image earlier and changed the working directory to that folder the ``in`` and newly created ``out`` folders will be accessible on the host machine.

## Modifying the Pipeline

Suppose you've created a processor you wish to include in the BioMedICUS pipeline like in [Part 1 of the Developer Tutorial](dev-tutorial/tutorial-1). First, copy the processor, in this case called ``medications.py``, to the mounted directory. Next, with the docker image running, execute the following commands in a terminal window:

```bash
docker exec -it b9 b9 write-config deploy
docker exec -it b9 b9client write-config run
```

These commands write two files to our mounted directory that we will need to modify. First, edit the file ``biomedicus_deploy_config.yml`` which contains the data about which processors to deploy by hosting their servers on launch. Modify the file so the end looks like this:

```yaml
  - implementation: java
    entry_point: edu.umn.biomedicus.sections.RuleBasedSectionHeaderDetector
    port: 51000
  - implementation: python
    entry_point: medications
    port: 52000
```

Next edit the file ``biomedicus_default_pipeline.yml`` which contains information about which processors to run when we process documents. Add a new component so that the end of the file looks like this:

```yaml
  - name: biomedicus-section-headers
    address: localhost:51000
  - name: biomedicus-medications-tutorial
    address: localhost:51100
```

Now shutdown and remove the b9 container

```bash
docker rm --force b9
```

Now to start up BioMedICUS using the modified deployment configuration run the following command:

```bash
docker run -it -d -v $(pwd):/b9/ -w /b9/ --name b9 nlpie/biomedicus:latest --config biomedicus_deploy_config.yml
```

After the services finish launching you can process documents using the modified pipeline configuration with the following command:

```bash
docker exec -it b9 b9client run --config biomedicus_default_pipeline.yml --include-label-text in -o out
```

## Processing RTF

From the previous section you may have noticed that you can modify the deployment command by appending arguments to the ``docker run`` command. Using this method it is also possible to enable RTF processing:

```bash
docker run -it -d -v $(pwd):/b9/ -w /b9/ --name b9 nlpie/biomedicus:latest --rtf
```

Or even RTF processing with a custom deployment configuration:

```bash
docker run -it -d -v $(pwd):/b9/ -w /b9/ --name b9 nlpie/biomedicus:latest --rtf --config biomedicus_deploy_config.yml
```

To process rtf add the rtf flag to the ``docker exec`` command to run the pipeline:

```bash
docker exec -it b9 b9client run --rtf --include-label-text in -o out
```

## Appendix A: Exporting the Image for Systems with Restricted Networks

Sometimes it may be necessary to run BioMedICUS on a system that does not have unrestricted access to the internet, and would not be able to download the BioMedICUS image. First, after launching the BioMedICUS container on a computer at least once, you can export that container using the following command:

```bash
docker export b9 | gzip > biomedicus-latest.tgz
```

And then after transferring it to the server which has restricted internet access you can import as an image using the following command:

```bash
zcat biomedicus-latest.tgz | docker import - nlpie/biomedicus:latest
```

From there, the ``docker run`` command at the start of this guide will work.

## Appendix B: Using the NGINX Reverse Proxy Docker-compose to Host BioMedICUS

In the BioMedICUS repository we make available [a docker-compose configuration which allows hosting the BioMedICUS system.](https://github.com/nlpie/biomedicus3/tree/main/tools/docker) This configuration contains a NGINX reverse proxy which routes processing requests to their respective services from a single port. To run, download the files in that directory and execute the following command:

```bash
docker compose up
```

Then anyone on that computer who has installed the minimal ``biomedicus_client`` python package can run the biomedicus pipeline against those running services:

```bash
b9client run --address 127.0.0.1:8080 ~/in -o ~/out
```
