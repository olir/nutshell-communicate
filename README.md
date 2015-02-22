# About

Nutshell Communication is a Java library designed for low-latency network communication and using the messaging communication-style, instead of remote procedure calls. It's a higher-level framework, where the application developer defines an application protocol by XML and code the corresponding messages and event handling. The library hides transport details from the developer. The library us using a session layer protocol which covers authentication (client sends binary data your server has to handle), synchronized state-switching and in future it offers session-recovery (session gets "stale" while connection is down). The library offers also a plug-in serialization implemented by using [Kryo](https://github.com/EsotericSoftware/kryo).

## Disclaimer

A couple of other networking libraries exist. I was not satisfied with my first library written 1999, because it used many threads and was very low level. 
So the idea of the new library is to offer a high-level API including [OSI-Level 5 and 6](http://en.wikipedia.org/wiki/OSI_model) for servers that 
need to steady broadcast events towards many clients. 

If you need guaranteed delivery or transactions, please look out for profession messaging products that provide queuing.
In the case you need more control over network or RPC, please take a look at [KryoNet](https://github.com/EsotericSoftware/kryonet); 
it may suit your needs, at least you will find there more suggestions for other libraries.

## Contents
* [Installation](#installation)
    * [Maven](#maven)
* HOWTO
* [Notice](#notice)

## Installation

### Maven

You can build the jar file and documentation with maven:

> mvn clean package site site:run

Open the project documentation in your web browser on http://localhost:9000 
or open it without site:run under

> target/site/index.html
 
## HOWTO

Please read [Instructions](src/site/markdown/instructions.md).
 
## Notice

> Please read [Notice](Notice.html) and "Dependencies" in site docs.
