<p align="center">
<img src="https://www.severell.com/static/images/logo.png" width="400"> 
</p>

<p align="center">
<a href="https://codecov.io/gh/mitchdennett/severell"><img src="https://img.shields.io/codecov/c/gh/mitchdennett/severell?label=codecov&logo=codecov"></a>
<img src="https://github.com/mitchdennett/severell-core/workflows/Java%20CI%20with%20Maven/badge.svg"> 
</p>

# Severell Framework

Severell is a web application framework designed to make developing web apps in Java fun and easy.
We have built a great starting point for you to just dive straight into developing your application. 

### Requirements
* Maven
* Java >= 11

### Documentation
A more detailed documentation site can be found [here](https://www.severell.com)

### Installation
There are two ways to get started with Severell. 

#### CLI Tool
First you need to download the Severell CLI tool and put it in your path. You can download the binary over [here](https://github.com/mitchdennett/severell-cli/releases/download/0.0.1-alpha.1/severell-cli)
. It only works on MacOS currently. Plans for a Windows and Linux version soon. 

Run `severell-cli` to make sure you have installed it correctly. 

To create your first Severell project you can use the `create` command

Next you can run `severell-cli create myapp` to create a new Maven project.

#### Maven
The other way to get started is by using the Maven archetype command.
```
mvn -B archetype:generate
    -DarchetypeGroupId=com.severell 
    -DarchetypeArtifactId=severell-archetype 
    -DarchetypeVersion=0.0.1-SNAPSHOT 
    -DgroupId=***YOUR-GROUPID***
    -DartifactId=***YOUR-ARTIFACTID***
    -Dversion=1.0-SNAPSHOT
```

### Getting Started

Once you have created a new project you can import it into your favorite IDE as a maven project.
Check out [this link](https://www.severell.com/docs/ide-configuration) for detailed instructions on setting up your IDE.






