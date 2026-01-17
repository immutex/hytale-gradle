# hytale-gradle

a Gradle plugin for Hytale that makes development of Hytale plugins easier, with a few key features:
- a task (runServer) to run a Hytale server with your plugin.
- automatically gets the HytaleServer.jar from your Hytale installation, with the option to manually add a path for it.

<details>
<summary>hytale-gradle</summary>

### basic usage

in `build.gradle.kts`:

```kotlin
plugins {
  // add the plugin
  id("com.immutex.hytale") version "0.1.0"
}

hytale {
    // optionally, you can configure the path of the Hytale server like this, if it's not found automatically.
    // hytaleHome = "/path/to/hytale"
}

// you can now run the Hytale server using the `runServer` task!
```

note: the plugin is currently under approval on Gradle, so it cannot be used yet unless you clone and publish it locally.
</details>
