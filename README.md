# Cloudstate JVM Support

### Generic JVM Server Support

Provides basic abstractions for creating JVM language support servers

## Overview

The purpose of this project is to provide public interfaces and basic abstractions to make it easier for other languages supporting Cloudstate to use these bindings to provide support for Cloudstate in their own language.

For this purpose, this repository created a fork of the java-support implementation from the commit that takes into account the following issues:

* Configuration API: https://github.com/cloudstateio/cloudstate/issues/191 and https://github.com/cloudstateio/cloudstate/pull/294
* CDI environments: https://github.com/cloudstateio/cloudstate/issues/219, https://github.com/cloudstateio/cloudstate/pull/227 and https://github.com/cloudstateio/cloudstate/pull/282

Some premises must be met for all code that is exposed for use by the support languages:

* The types of input and output of interface methods must be based on Java types. This is due to the fact that all JVM languages interchange better with these types instead of Scala types.
* Utility classes that support annotation reflection must support passing the annotation type. Today they are fixed in the source code and the support languages could not benefit from them.
