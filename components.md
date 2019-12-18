---
layout: components
title: Components
permalink: /components
description: The processing components for your project.
---

## Default BioMedICUS pipeline

By default BioMedICUS runs the following components in the following order:

- [Sentences Detector]({{ '/components#biomedicus-sentences' | relative_url }})
- [Part of Speech Tagger]({{ '/components#biomedicus-tnt-tagger' | relative_url }})
- [Acronym Detector]({{ '/components#biomedicus-acronyms' | relative_url }})
- [Concept Detector]({{ '/components#biomedicus-concepts' | relative_url }})

## Other processors

By default, normalization is run as part of the concept detector, but it can also be deployed as a [standalone processor]({{ '/components#biomedicus-normalizer' | relative_url }}).

In addition, BioMedICUS provides functionality for transforming RTF documents into plaintext documents as input for the system via the [RTF Reader]({{ '/components#biomedicus-rtf-processor' | relative_url }}).
