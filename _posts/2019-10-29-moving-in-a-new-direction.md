---
layout: post
title: "Moving in a new direction"
author: Ben Knoll
---
Since its creation, BioMedICUS has been a [UIMA](https://uima.apache.org/)-based system. Last year, after much deliberation, we decided to move towards using our own homegrown framework, and subsequently away from using UIMA in BioMedICUS.

There were many requirements that were driving this decision, but a few of the most important ones were:

- **Python Support**: BioMedICUS's sentence detector is now written in Python using the Keras library. Our choice was either to shoehorn this into the existing Java UIMA infrastructure, or to find a way to make Python code interoperate with our Java code. We anticipate a future need for developing more advanced components that will utilize more upstream artifacts than the sentences component does, so full access to the type system is going to be necessary.
- **Ease of Use**: Here in our research group, we often work with researchers or students with minimal software development experience. Getting them started with UIMA has consistently been a time consuming process. From the outset, our own framework has been designed with this ease of use in mind.
- **Scalability / Deployment Flexibility**: Scalability for UIMA is provided by secondary libraries such as AS which uses a JMS/ActiveMQ/Spring stack, and DUCC (which I haven't used). #TODO pull from MTAP docs.

Enter [MTAP (Microservice Text Analysis Platform)](https://nlpie.github.io/mtap), which is our homegrown attempt to meet these requirements, as well as provide a lot of quality of life improvements around testing and distributing a NLP or text analysis system. Moving forward MTAP will be the foundational framework used in BioMedICUS. Over the next month we'll be releasing a new beta version of BioMedICUS utilizing this new framework while preserving most of the functionality of our UIMA-based system.
