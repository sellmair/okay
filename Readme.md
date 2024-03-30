# okay: Kotlin Build Tool Experiment

This project is experimental/educational. 
It tries to explore the approach of a 'suspending task schedular' based build system.

## Demo Video (YouTube: (https://youtu.be/3Uywkd6v3T0))
[![Watch the Demo](https://img.youtube.com/vi/3Uywkd6v3T0/maxresdefault.jpg)](https://youtu.be/3Uywkd6v3T0)


## Test this project

To install the build tool, please execute

```shell
./install
```

### Build the test project (and it's a submodule 'library')

```shell
cd testProject
okay build
```

### Run code from the test project

```shell
cd testProject
okay run
```

### Run different main method from the test project

```shell
cd testProject
okay run echo
```

### Run different main method from the test project (and provide params to the main method)

```shell
cd testProject
okay run echo firstArgument secondArgument thirdArgument="is a charm"
```

### Build/Package an executable distribution
```shell
cd testProject
okay package
```

Execute it:
```shell
java -jar testProject/build/package/testProject.jar
```

```shell
cd testProject
okay run echo firstArgument secondArgument thirdArgument="is a charm"
```


### Clean the test project

```shell
cd testProject
okay clean
```