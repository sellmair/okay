# This is a Sunday afternoon toy project!

This is just a fun toy project on 'how I would build' a Kotlin build system.

# Test this project

To install the build tool, please execute

```shell
./install
```

## Build the test project (and it's a submodule 'library')

```shell
cd testProject
okay build
```

## Run code from the test project

```shell
cd testProject
okay run
```

## Run different main method from the test project

```shell
cd testProject
okay run echo
```

## Run different main method from the test project (and provide params to the main method)

```shell
cd testProject
okay run echo firstArgument secondArgument thirdArgument="is a charm"
```

## Clean the test project

```shell
cd testProject
okay clean
```