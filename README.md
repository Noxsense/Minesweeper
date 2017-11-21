# Minesweeper

Small Minesweeper Project with GUI and a Solver (wip).

## Screenshots

### Desktop Version
[<img src="screenshots/Create_Or_Select.png" width="200">](screenshots/Create_Or_Select.png)
[<img src="screenshots/Current_Game.png" width="200">](screenshots/Current_Game.png)
[<img src="screenshots/Statistics.png" width="200">](screenshots/Statistics.png)

### Android Version
[<img src="screenshots/Android--Select_Game.png" width="300">](artwork/screenshots/drawer.png)
[<img src="screenshots/Android--Current_Game.png" width="300">](screenshots/Android--Current_Game.png)


## Compiling

### Compiling Desktop GUI
This will create a `core.jar` in `sources/build/libs`

In  directory `sources` run for building:
```
gradle :core:build
```

### Compiling Android

In  directory `sources`:

```
gradle assembleDebug
```


## Directories:
```
.
└── sources
    ├── android
    │   ├── src
    │   │   └── main
    │   │       ├── AndroidManifest.xml
    │   │       ├── java
    │   │       └── res
    │   └── build.gradle
    ├── core
    │   ├── src
    │   │   ├── main
    │   │   │   └── java
    │   │   │       └── nox
    │   │   │           └── minesweeper
    │   │   │               ├── desktop
    │   │   │               └── logic
    │   │   └── test
    │   └── build.gradle
    ├── build.gradle
    └── settings.gradle
```
