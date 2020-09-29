# Contributing Guide

## How Can I Contribute?

We are 100% open to contribution and would love to hear any ideas you may have. Look at out isues board to see if there are any current issues you may be able to 
tackle. If you have a feature request please create an issue and lets have a discussion about it. Please be open to contributing at least some of the code for your
feature request. 

### Contributing Workflow

Before you begin contributing, there are a few steps needed.

* Create or comment on an issue that you want to contribute towards.
* Fork the repository
* Make Changes
* Submit Pull Request

### Installing Locally For Development

#### Requirements
* Maven
* Java >= 11

#### MacOS

Fork the repository and clone it locally. Once you have cloned the repository run `mvn clean install`. You will now have the project installed in your local Maven 
repository. Make your changes and then run `mvn clean install` again.

Now you can create a test project to test your changes. Run the following command in different directory to create a new test project.

```
mvn -B archetype:generate
    -DarchetypeGroupId=com.severell 
    -DarchetypeArtifactId=severell-archetype 
    -DarchetypeVersion=0.0.1-SNAPSHOT 
    -DgroupId=***YOUR-GROUPID***
    -DartifactId=***YOUR-ARTIFACTID***
    -Dversion=1.0-SNAPSHOT
```

#### Windows
I do not have a windows machine. If someone if able to write this part for me that would be an AMAZING contribution.
