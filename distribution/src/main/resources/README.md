Overview
========================================================================================================================

The BioMedical Information Collection and Understanding System (BioMedICUS) leverages open source solutions for
text analysis and provides new analytic tools for processing and analyzing text of biomedical and clinical reports.
The system is being developed by our biomedical NLP/IE program at the University of Minnesota.
This is a collaborative project that aims to serve biomedical and clinical researchers, allowing for customization
with different texts.


Install Guide
========================================================================================================================

Along with the source code, we also release distribution of all the files required to immediately start running. You can
download this file at [http://athena.ahc.umn.edu/biomedicus-downloads/](http://athena.ahc.umn.edu/biomedicus-downloads/).
You will also need download the appropriate model data archive. See "Downloading Large Model Files".

Prerequisites
------------------------------------------------------------------------------------------------------------------------

The following 3rd-party tools need to be installed to download, compile and run BioMedICUS:

 1.  Java SE Development Kit 8: [download](http://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

 2.  Git: [download](http://git-scm.com) \(optional, only required for checking out the project source code\)

 3.  Maven: [download](http://maven.apache.org) \(optional, only required for building the project from source\)

Environment Variables
------------------------------------------------------------------------------------------------------------------------

The following environment variables need to be set:

 1. JAVA_HOME: 

    *   On OS X, after installing Java SE Development Kit, you can find the path to the installation by typing this
    command:

        /usr/libexec/java_home

    *   Open the .bash_profile file in your favorite text editor and add the following statement:

        export JAVA_HOME="path-to-java-home-from-previous-step"

    *   Save the .bash_profile, close it and then run the following:

        source .bash_profile

    *   Try 'echo $JAVA_HOME' to make sure the environment is pointing to the right Java version (currently 1.8.0)
 

Getting BioMedICUS
------------------------------------------------------------------------------------------------------------------------

If you wish to compile BioMedICUS from source, instead of using the prepackaged version above, the following 
instructions are provided.

At a command prompt, change directories to where you want to install BioMedICUS, and then enter:

    git clone https://bitbucket.org/nlpie/biomedicus.git

Notes: 

*    FYI: This clone command will create a subdirectory called 'biomedicus' in the current working directory and create
     a git repository under that subdirectory.


Downloading Large Model Files
------------------------------------------------------------------------------------------------------------------------

After having checked out biomedicus from git using the steps above, you will need to download the models file for the
version of biomedicus you are running. You can download the data zip at
[http://athena.ahc.umn.edu/biomedicus-downloads/](http://athena.ahc.umn.edu/biomedicus-downloads/).
There are two options, the first is an open data set which is freely licensed UMLS data, the second zip is a umls data
set which uses SNOMED data and requires you to log in to a NIH UTS account to verify your UMLS Metathesaurus License.
After extracting, set the BIOMEDICUS_OPTS java parameter biomedicus.path.data to the location of the
extracted data. If you are using a release version of BioMedICUS, you may instead set the path.data configuration
property in config/biomedicus.properties to the location of the BioMedICUS data.

    export BIOMEDICUS_OPTS=-Dbiomedicus.path.data=[location of data]


Building BioMedICUS
------------------------------------------------------------------------------------------------------------------------

After cloning completes, run the following to build BioMedICUS

    cd biomedicus
    mvn clean install


Running BioMedICUS
------------------------------------------------------------------------------------------------------------------------

Then maven install phase will create a "release" folder in the root directory of BioMedICUS. This root directory
contains all resources necessary to run biomedicus.

*   config: configuration files such as .properties

*   data: models and large data files used by annotators

*   **desc: UIMA descriptor files**

    -   ae - location for pipelines/aggregates of multiple annotators
    -   ae/annotator - standard location for annotators
    -   ae/writer - location for writers to output analyzed documents in a specific format
    -   ae/util - utility analysis engines
    -   cr - location for Collection Readers, to read in documents
    -   tools - descriptors for analysis engines in the tools module.

*   lib: Compiled and packaged modules of biomedicus and third-party libraries necessary to run biomedicus

*   bin: scripts for running biomedicus tools or uima tools with the correct classpath for biomedicus

    -   AV.sh run the UIMA Annotation Viewer.
        [UIMA help](https://uima.apache.org/d/uimaj-2.6.0/tools.html#ugr.tools.annotation_viewer)
    -   CPM.sh run the UIMA Collection Processing Engine Configurator.
        [UIMA help](https://uima.apache.org/d/uimaj-2.6.0/tools.html#ugr.tools.cpe)
    -   CVD.sh run the UIMA CAS Visual Debugger.
        [UIMA help](https://uima.apache.org/d/uimaj-2.6.0/tools.html#ugr.tools.cvd)
    -   runClass.sh run any executable java class within the biomedicus classpath
    -   runCPE.sh run a cpe desciptor directly without opening the visual interface

After successful installation is completed, the simplest way to start is to use the default annotation pipeline with a
UIMA provided graphical user interface - Collection Processing Manager (CPM). The simplest format for the input files is
plain text; however, our system also currently has collection readers defined for certain XML formats as well as text
stored in a database. Here, we will show how to run BioMedICUS on a collection of plain text files. To run the pipeline,
first, you will need to create a local directory containing only the files to be processed
(e.g., ./examples/plaintext/input) and move the files to be processed into the newly created directory. We would also
recommend creating an output directory in a nearby location that will contain the output of the pipeline
(e.g., ./examples/plaintext/output).

Next, navigate to the "bin" directory under the BioMedICUS installation directory:

    cd ./release/bin

Next, run the CPM.sh shell script:

    ./CPM.sh

If you have a cpe descriptor file already created, you can load the CPM descriptor by browsing to the "File" menu in the
CPM and selecting "Open CPE Descriptor." From there a file dialog will popup and you can browse to your CPE Descriptor
file.

You can also run a CPE directly without launching the UI by using the runCPE.sh script:

    ./runCPE.sh /path/to/CpeDescriptor.xml

To make it easier to start for users new to BioMedICUS and UIMA, we have included an "examples/plaintext" folder with
the BioMedICUS distribution. This folder contains an "input" and "output" subfolders. The ./examples/plaintext/input
folder has a few plain ASCII text files. The ./examples/plaintext/ folder also contains a predefined
PlainTextCPM_Example.launch descriptor file that can be used to start the CPM or be loaded after the CPM has been
started. This launch configuration has relative paths defined - relative to the current working directory from which
CPM.sh is started. So, the best way to use it is to first switch to the "scripts" directory. Try the following:

    cd ./release/scripts
    ./CPM.sh

Follow the instructions for opening a CPE Descriptor and select
${BIOMEDICUS_HOME}/release/examples/plaintext/PlainTextCPM_Example.xml

After the process completes, you can view the results with the Annotation Viewer (AV.sh) like this:

    ./AV.sh

When AV starts, you can point it to the output directory ../examples/plaintext/output. You will also need to point AV to
the type system that was used by the BioMedICUS pipeline, the type system is outputted to the same directory as the XMI
files.



IntelliJ IDEA Setup
------------------------------------------------------------------------------------------------------------------------

IntelliJ is Java IDE with a Community Edition licensed under the Apache 2.0 license developed by JetBrains S.R.O. You
can find it [here](https://www.jetbrains.com/idea/)

1. Select Import Project
2. Browse to the folder that you cloned the biomedicus code into
3. Select 'Import from external model' and 'Maven'


Eclipse Setup
------------------------------------------------------------------------------------------------------------------------

Prerequisites: Install m2e. [link](http://eclipse.org/m2e/). Recent versions of Eclipse have maven included.

1.  Open eclipse with a workspace folder that is not the BioMedICUS root directory.
2.  Right click in the package explorer and select 'Import'
3.  Expand the 'Maven' folder and select 'Existing Maven Projects'
4.  Select the root directory of the BioMedICUS project using the 'Browse' button
5.  Confirm that 'pom.xml' and a list of modules appear in the 'Projects' pane
6.  Select 'Finish'
7.  Under the 'Biomedicus' project in the 'developer' folder there are .launch files for the Annotation Viewer and the
    Collection Processing Engine Configurator, they should be automatically included in the Run and Debug favorites.


Changing settings
------------------------------------------------------------------------------------------------------------------------

UIMA defines an annotator's settings in an XML descriptor file. These files are located in the
release/desc folder. To change the behavior of any of the annotators,
the available settings can be changed in the files within that folder.

The config/biomedicus.properties contains some system-wide per-launch constants such as the location of data models.

About Us
========================================================================================================================
 BioMedICUS is developed by the
 [University of Minnesota Institute for Health Informatics NLP/IE Group](http://www.bmhi.umn.edu/ihi/research/nlpie/)
 with assistance from the [Open Health Natural Language Processing \(OHNLP\) Consortium](http://ohnlp.org/index.php/Main_Page).


Other Resources
========================================================================================================================

### BioMedICUS

 *   [Demo](http://athena.ahc.umn.edu/biomedicus/)
 *   [Source Code](https://bitbucket.org/nlpie/biomedicus)

### NLP-TAB

 *   [Demo](http://athena.ahc.umn.edu/nlptab)
 *   [Java Source Code](http://bitbucket.org/nlpie/nlptab)
 *   [Web-app Source Code](http://bitbucket.org/nlpie/nlptab-webapp)
 *   [Corpus](http://bitbucket.org/nlpie/nlptab-corpus)

### NLP/IE Group Resources

 *   [Website](http://www.bmhi.umn.edu/ihi/research/nlpie/resources/index.htm)
 *   [Demos](http://athena.ahc.umn.edu/)


Acknowledgements
========================================================================================================================

Funding for this work was provided by:

 *	1 R01 LM011364-01 NIH-NLM
 *	1 R01 GM102282-01A1 NIH-NIGMS
 *	U54 RR026066-01A2 NIH-NCRR