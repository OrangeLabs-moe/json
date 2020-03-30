# Non-recursive JSON parsing library

## Features

* Can parse extremely deep structures
* Can parse long numbers

## Warning

Because this library is non-recursive, converting objects with loops to string takes infinite time.

To avoid this, use `toStringAsync()` with timeout.

```java
jsonObjectWithLoop.toStringAsync().get(2, TimeUnit.SECONDS);
```

## Install

```groovy
maven {
    url 'https://maven.orangelabs.moe/'
}

dependencies {
    compile 'moe.orangelabs:json:5.0'
}
```

## Benchmarking

`./gradlew clean jmh --stacktrace --no-daemon --console=plain`