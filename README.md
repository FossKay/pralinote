# Pralinote

> This project is not affiliated with PRONOTE nor Index Education

An unofficial native Android app that interacts with the french PRONOTE school website.
Only student profiles are supported for now.

## Screenshots

| Profile List |             Homework             |              Timetable              | Grades |
|:---:|:--------------------------------:|:-----------------------------------:|:---:|
| ![Profile List](assets/Profiles.png) | ![Homework](assets/Homework.png) | ![Timetable](assets/Timetable2.png) | ![Grades](assets/Grades.png) |

## Contributing

Internally, this app uses UniFFI with a modified version of [pronote-rs](https://github.com/FossKay/pronote-rs).
To build the app, you must have installed `cargo` (the rust toolchain) and a JDK (Java Development Kit).

In `model/AppClient.kt`, there is a `MOCK` constant.
Toggling it on will enable sample data, the app shouldn't make any Internet connection.

Run
```bash
cd app/pronote-rs
./build
cd ../..
./gradlew assembleRelease
```
