This is a Kotlin Multiplatform project targeting Android, iOS, Server, built with the [Kotlin Toolchain](https://kotlin-toolchain.org/dev/).

- [/app/androidApp](./app/androidApp) contains the Android application with a single `MainActivity` and a Compose preview.
- [/app/iosApp](./app/iosApp) contains the iOS application.
- [/app/shared](./app/shared) contains the shared Compose entry point, `App()`, and the trainer home screen with a calendar, appointments, tasks, and client statistics. The screen uses local sample data; added appointments survive Activity recreation but are not stored in a database. Other destinations currently open informational dialogs.
- [/core](./core) contains the greeting helper used by the server.
- [/server](./server) contains the Ktor server application.

The `kotlin` (macOS/Linux) and `kotlin.bat` (Windows) scripts in the project root are self-bootstrapping wrappers for the Kotlin Toolchain: they download the pinned toolchain version on first use, so no separate installation is required. Build the whole project with `./kotlin build`.

### Running

Use the run configurations provided by the run widget in your IDE's toolbar. You can also run each module from the command line:

- Android app: `./kotlin run -m androidApp`
- iOS app: `./kotlin run -m iosApp`
- Server: `./kotlin run -m server`

---

Learn more about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Kotlin Toolchain](https://kotlin-toolchain.org/dev/), [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/), [Ktor](https://ktor.io/).
