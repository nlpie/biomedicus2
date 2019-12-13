---
layout: home
home_title: BioMedICUS
description: The BioMedical Information Collection and Understanding System
permalink: /
---
BioMedICUS is a system for large-scale text analysis and processing of
biomedical and clinical reports. The system is being developed by the Natural
Language Processing and Information Extraction Program at the University of
Minnesota Institute for Health Informatics.

### Project Goals
- Scalability and performance. We use BioMedICUS to process millions of notes here at the
University of Minnesota. To do this we need BioMedICUS to have high throughput and to support both
machine-level and distributed parallelization.
- Usability. We try to minimize dependencies and prerequisites that BioMedICUS requires. We release
under the permissive Apache 2.0 license and pay close attention to intellectual property issues.

To see what tasks the system supports, look at
[Components and Outputs]({{ "/components" | relative_url }}). If you are
looking for a jumping-in point, see
[Installation]({{ "/installation" | relative_url }}).

## Features
### RTF Reader
BioMedICUS has an
[RTF Reader]({{"/components#biomedicus-rtf-processor" | relative_url }}),
which has the ability to read and process notes that are encoding in RTF. In addition, BioMedICUS
uses RTF formatting information downstream to improve other components.

### Acronym Detection
Included in the standard pipeline is an
[acronym detector]({{"/components#biomedicus-acronyms" | relative_url }}),
which has the ability to detect and expand acronyms to their equivalent long forms.

### Concept Detection
BioMedICUS includes a fast
[concept detector]({{ "/components#biomedicus-concepts" | relative_url }})
which labels instances of UMLS Metathesaurus concepts in text.

## Downloads
For downloads see the [releases page](https://github.com/nlpie/biomedicus3/releases) on GitHub. We
also make more comprehensive models that require you to have a UMLS license available
[here](http://nlpie.umn.edu/downloads/).

## Contact and Support
For issues or enhancement requests, feel free to submit to the Issues tab on GitHub.

BioMedICUS has a [gitter chat](https://gitter.im/biomedicus/biomedicus) for contacting developers
with questions, suggestions or feedback.

## About Us
BioMedICUS is developed by the
[University of Minnesota Institute for Health Informatics NLP/IE Group](http://www.bmhi.umn.edu/ihi/research/nlpie/)
with assistance from the
[Open Health Natural Language Processing \(OHNLP\) Consortium](http://ohnlp.org/index.php/Main_Page).


## Acknowledgements
Funding for this work was provided by:
 *	1 R01 LM011364-01 NIH-NLM
 *	1 R01 GM102282-01A1 NIH-NIGMS
 *	U54 RR026066-01A2 NIH-NCRR

The following people have made code contributions not represented in the commit history:
 *  Robert Bill
 *  Arun Kumar
 *  Serguei Pakhomov
 *  Yan Wang
