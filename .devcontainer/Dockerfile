# See here for image contents: https://github.com/microsoft/vscode-dev-containers/tree/v0.177.0/containers/cpp/.devcontainer/base.Dockerfile

# [Choice] Debian / Ubuntu version: debian-10, debian-9, ubuntu-20.04, ubuntu-18.04
ARG VARIANT="buster"
FROM mcr.microsoft.com/vscode/devcontainers/cpp:0-${VARIANT}

COPY --from=openjdk:8-jdk-buster /usr/local/openjdk-8 /usr/local/openjdk-8
COPY --from=openjdk:11-jdk-buster /usr/local/openjdk-11 /usr/local/openjdk-11
COPY --from=openjdk:16-jdk-buster /usr/local/openjdk-16 /usr/local/openjdk-16
COPY --from=openjdk:17-jdk-buster /usr/local/openjdk-17 /usr/local/openjdk-17

ENV JAVA_HOME="/usr/local/openjdk-17"
ENV PATH="${JAVA_HOME}/bin:${PATH}"

RUN echo "deb http://deb.debian.org/debian buster-backports main" >> /etc/apt/sources.list

# [Optional] Uncomment this section to install additional packages.
RUN apt-get update && export DEBIAN_FRONTEND=noninteractive \
 && apt-get -y install \
        autoconf \
        file \
        libasound2-dev \
        libcups2-dev \
        libcups2-dev \
        libffi-dev \
        libfontconfig1-dev \
        libfreetype6-dev \
        libharfbuzz-dev \
        libx11-dev \
        libxext-dev \
        libxrandr-dev \
        libxrender-dev \
        libxt-dev \
        libxtst-dev \
        gdb \
        linux-perf-5.10

RUN mkdir -p /opt/jtreg-5.1-b01 /opt/jtreg-4.2 \
 && curl https://ci.adoptopenjdk.net/job/jtreg/lastSuccessfulBuild/artifact/jtreg-5.1-b01.tar.gz | tar -xvzf - --strip-components 1 -C /opt/jtreg-5.1-b01 \
 && curl https://ci.adoptopenjdk.net/job/jtreg/lastSuccessfulBuild/artifact/jtreg-4.2.0-tip.tar.gz | tar -xvzf - --strip-components 1 -C /opt/jtreg-4.2
