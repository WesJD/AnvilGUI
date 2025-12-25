# Contributing to AnvilGUI

The project uses the Maven. Run `mvn clean install` using Java 21 to build the project.

## Formatting

The project uses the [Spotless Maven Plugin](https://github.com/diffplug/spotless/tree/main/plugin-maven) to
enforce style guidelines. You will not be able to build the project if your code does not meet the guidelines.
To fix all code formatting issues, run `mvn spotless:apply`.

## Deployment

The code is built and deployed to `mvn.wesjd.net` via GH actions whenever a push to `master` occurs.
