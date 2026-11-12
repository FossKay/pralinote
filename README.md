# Pralinote

> This project is not affiliated with PRONOTE nor Index Education

[Lire ce README en français](README_FR.md)

An unofficial native Android app that interacts with the french PRONOTE school website.
Only student profiles are supported for now.

Differences with the official mobile app and Papillon:
- modern Android interface
- runs on native code with better performance
- optimized for PRONOTE, makes as few requests as possible

## Screenshots

| Profile List |             Homework             |              Timetable              | Grades |
|:---:|:--------------------------------:|:-----------------------------------:|:---:|
| ![Profile List](assets/Profiles.png) | ![Homework](assets/Homework.png) | ![Timetable](assets/Timetable.png) | ![Grades](assets/Grades.png) |

## Features

- Multiple accounts, password login, auto-login
- Homework (click on it to mark it as done/undone)
- Timetable with agenda integration
- Grades

Coming soon:
- QR code login
- Grade average, plots
- News

## Contributing

Internally, this app uses UniFFI with a modified version of [pronote-rs](https://github.com/FossKay/pronote-rs).
To build the app, you must have installed `cargo` (the rust toolchain) and a JDK (Java Development Kit).
[Android Studio](https://developer.android.com/studio) is recommended.

By default, on debug builds, the app uses sample data and shouldn't make any internet connections.
In `.../model/AppClient.kt`, toggle `NO_MOCK` on to modify this behavior.

To bump the `pronote-rs` version, configure the `PRONOTE_RS` environment variable and set it
to the root folder of the project, then run `app/pronote-rs/update` (Linux-only).
Sometimes, manual changes to `p_client.rs` will be needed.

Run `./gradlew assembleDebug` to build a debug version of the app.
For now, only Linux is supported for building Pralinote
(if you want to contribute, feel free to add `app/src/pronote-rs/build.bat` and adapt `app/build.gradle.kts`).

*A note on AI*: AI use is allowed but all the code must be handwritten and understandable.

## Legal notice
Index Education [indicated](https://www.index-education.com/fr/article-1760-recommandations-relatives-a-l-application-papillon.php) that as long as an unofficial app uses credentials that the user provided voluntarily and isn't commercial, it isn't prohibited.
> Cette pratique n'est pas illégale en soi, car justifiée par le fait que l'utilisateur renseigne volontairement ses informations de connexion et aussi par le fait que \[l'application (Papillon)] n'a pas de finalité commerciale.