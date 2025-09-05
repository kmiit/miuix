# Build Requirements

## Network Access

This project requires network access to the following repositories for complete functionality:

- **Google Maven Repository** (`dl.google.com`) - Required for Android Gradle Plugin and Android dependencies
- **Maven Central** (`repo1.maven.org`) - Required for most dependencies
- **Gradle Plugin Portal** (`plugins.gradle.org`) - Required for Gradle plugins

## generateIconSvgs Task

The `generateIconSvgs` task is available via:

```bash
./gradlew :docs:icon-gen:generateIconSvgs
```

This task:
1. Scans ImageVector definitions in `top.yukonga.miuix.kmp.icon.icons` package
2. Exports SVG files to `build/icon-svgs/` directory
3. Is automatically run as part of documentation generation (dokka tasks)

## Network-Restricted Environments

If you're working in an environment with restricted network access:

1. Ensure access to Google's Maven repositories is enabled
2. Or use a local Maven repository mirror that includes Android Gradle Plugin artifacts
3. The Android Gradle Plugin version is configured in `gradle/libs.versions.toml`

## Fixed Issues

- ✅ Removed invalid `com.android.settings` plugin reference
- ✅ Fixed Android Gradle Plugin version from invalid `8.12.1` to valid `8.1.2`
- ✅ Cleaned up settings.gradle.kts plugin configuration
- ✅ Preserved original Android module structure