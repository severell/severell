<p align="center">
<img src="https://www.severell.com/static/images/logo.png" width="400"> 
</p>

<p align="center">
<a href="https://codecov.io/gh/severell/severell"><img src="https://codecov.io/gh/severell/severell/branch/master/graph/badge.svg?token=VSUOKY7HR1"/></a>
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

#### Initializer
Visit [Initializer](https://initializer.severell.com/) to get your base project. Enter the required information and extract the project folder from the downloaded ZIP

#### Maven
The other way to get started is by using the Maven archetype command.
```
mvn -B archetype:generate
    -DarchetypeGroupId=com.severell 
    -DarchetypeArtifactId=severell-archetype 
    -DarchetypeVersion=0.0.1
    -DgroupId=***YOUR-GROUPID***
    -DartifactId=***YOUR-ARTIFACTID***
    -Dversion=1.0-SNAPSHOT
```

### Getting Started

Once you have created a new project you can import it into your favorite IDE as a maven project.
Check out [this link](https://www.severell.com/docs/ide-configuration) for detailed instructions on setting up your IDE.






