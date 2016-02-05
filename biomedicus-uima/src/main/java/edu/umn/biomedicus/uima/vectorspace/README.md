Term Vectors
============

This is a package for outputting vectors of terms for classification tasks. It is primarily done in a two step process:

1. TermVectorSpaceAnnotator selects terms and stores a vector of all the non-zero term counts on the CAS, it also 
builds the term space, which is stored in memory and contains an index of all the terms.
2. ArffTextTermVectorWriter writes a WEKA ARFF file containing all of the term vectors.

TermAdapter
-----------

TermVectorSpaceAnnotator accesses terms using an instance of the interface TermAdapter. TermAdapters take a CAS object
and turn it into a stream of terms. TermAdapters are instantiated by the vector space annotator using their factory 
class, which takes an array of string arguments and returns an instance of the TermAdapter. Right now there are two 
implementations of TermAdapters:

- CoveredTextTermAdapter
    - argument 0 factoryClass: edu.umn.biomedicus.uima.vectorspace.CoveredTextTermAdapter
    - argument 1: the fully qualified UIMA type name of the annotation to pull covered text from.
- FeatureTermAdapter
    - argument 0 factoryClass: edu.umn.biomedicus.uima.vectorspace.FeatureTermAdapterFactory
    - argument 1: the fully qualified UIMA type name of the feature structure.
    - argument 2: the feature base name to pull feature values from.
    
ExclusionFilter
---------------

TermVectorSpaceAnnotator has the ability to filter terms at specific locations using classes called exclusion filters.
There is currently 1 implementation of an ExclusionFilter:

- FeatureValueExclusionFilter
    - argument 0 factoryClass: edu.umn.biomedicus.uima.vectorspace.FeatureValueExclusionFilterFactory
    - argument 1: the fully qualified UIMA type name of the Annotation.
    - argument 2: feature base name.
    - argument 3: feature value to check for a match. 
    - argument 4: whether it should exclude on matches "true" or whether it should exclude matches on "false".


Configuration and Running
-------------------------

In order to facilitate running, two aggregate analysis engines are provided which combine the two analysis engines above
and are preconfigured to use one of the term adapters:

- edu.umn.biomedicus.ae.aggregate.CoveredTextDocumentTermVectors - provides the ability to select an annotation type to 
    create terms out of all. 
    
- edu.umn.biomedicus.ae.aggregate.FeatureValueTermVectors - provides the ability to select a feature value to collect as
    terms across an entire document.

To run, configure your pipeline with readers and analysis engines to perform the analysis you need, including one that 
sets the category field on the ClinicalNoteAnnotation. Then select one of the two aggregate analysis engines to output
the ARFF file. 

Example CPEs
------------

Located in the examples folder, there are three CPEs:
nonNegatedConceptCuiTermVectorsCPE.xml - concept cuis that are not contained in negated terms.
tokenCoveredTextTermVectorsCPE.xml - all tokens as terms.
tokenNormalFormsTermVectorsCPE.xml - all token normal forms.