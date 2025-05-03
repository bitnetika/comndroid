# comndroid

A collection of common Android utilities, extensions, and base classes.

## Minimum Requirements
- Android API Level 21+
- Gradle 8.0+

## Installation

### Step 1: Add JitPack Repository
Add this to your **project-level** `build.gradle` (`settings.gradle` in newer projects):
```groovy
dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        maven { url 'https://jitpack.io' } // for settings.gradle.kts maven { url = uri("https://jitpack.io") }
        google()
        mavenCentral()
    }
}
```

## Step 2: Add Dependency
In your **module-level** build.gradle (usually app/build.gradle):
```groovy
dependencies {
    implementation 'com.github.bitnetika:comndroid:1.0.0-alpha2'
}
```

## Troubleshooting
Failed to resolve?
- Verify network connectivity 
- Ensure you're not behind a corporate firewall blocking JitPack 
- Check for typos in the dependency declaration

## Build issues?
Visit the [JitPack build logs](https://jitpack.io/#bitnetika/comndroid/1.0.0-alpha2) to see if the build succeeded.

## License
> Copyright 2025 Bitnetika
> 
> Licensed under the Apache License, Version 2.0 (the "License");
> you may not use this file except in compliance with the License.
> You may obtain a copy of the License at
> 
>    http://www.apache.org/licenses/LICENSE-2.0