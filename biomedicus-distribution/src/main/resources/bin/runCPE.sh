#!/bin/bash

BIOMEDICUS_XMX="12g"

"$( dirname "${BASH_SOURCE[0]}" )/runClass.sh" edu.umn.biomedicus.uima.util.SimpleRunCPE $@
