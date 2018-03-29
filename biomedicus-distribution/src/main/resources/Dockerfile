FROM openjdk:8

RUN apt-get update && apt-get install -y --no-install-recommends \
        python-pip \
        python-dev \
        wget \
        && \
    apt-get clean && \
    rm -rf /var/lib/apt/lists/*

RUN curl -O https://bootstrap.pypa.io/get-pip.py && \
    python get-pip.py && \
    rm get-pip.py

RUN pip --no-cache-dir install \
    http://storage.googleapis.com/tensorflow/linux/cpu/tensorflow-1.6.0-cp27-none-linux_x86_64.whl

COPY . /usr/share/biomedicus
WORKDIR /usr/share/biomedicus

RUN curl -LO https://github.com/nlpie/biomedicus/releases/download/v1.8.4/biomedicus-distribution-1.8.4-release.zip && \
    unzip biomedicus-distribution-1.8.4-release.zip -d /usr/share/biomedicus/data && \
    rm biomedicus-distribution-1.8.4-release.zip

ENTRYPOINT /bin/bash
