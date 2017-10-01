#!/bin/bash

export BIOMEDICUS_XMX="12g"

"$( dirname "${BASH_SOURCE[0]}" )/runClass.sh" org.apache.uima.tools.cpm.CpmFrame
