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
This will create class files in `bin/classes/Minesweeper`

In main directory run for building:
```
make build
```

and for running:
```
make run
```

### Compiling Android

```
gradle assembleDebug
```

or alternatively
```
make android
```



## Directories:
```
.
├── build.gradle
├── Makefile
└── src
    └── main
        ├── AndroidManifest.xml
        ├── java
        │   └── nox
        │       └── minesweeper
        │           ├── android
        │           ├── desktop
        │           ├── logic
        │           └── tests
        └── res
```
