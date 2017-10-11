#!/bin/bash

export BIOMEDICUS_XMX="2g"

"$( dirname "${BASH_SOURCE[0]}" )/runClass.sh" edu.umn.biomedicus.uima.util.SimpleRunCPE $@
