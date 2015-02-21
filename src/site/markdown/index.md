## About

The Nutshell Communication Library is designed for network communication by using the messaging communication-style. It's a higher-level framework, where the application developer defines an application protocol by XML and code the corresponding messages and event handling. The library hides transport details from the developer.

It offers a plug-in serialization from the [Kryo](https://github.com/EsotericSoftware/kryo). Currently it's bundled with it.

Please read the [Instructions](instructions.html) for information how to use it.

### Contents of this page
* [Installation](#installation)
	* [Maven](#maven)
* [Notice](#notice)

### Installation

#### Maven

You can build the jar file and documentation with maven:

```bat
mvn clean package site site:run
```

Open the project documentation in your web browser on http://localhost:9000 
or open it without site:run under

> target/site/index.html

 
### Notice

> Please read [Notice](Notice.html) and [Dependencies](dependencies.html).
