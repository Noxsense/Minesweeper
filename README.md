# Minesweeper

Small Minesweeper Project with GUI and a Solver (wip).

## Screenshots
![Screenshot: Home view](screenshots/Home.png)
![Screenshot: Select Running Game](screenshots/Create_Or_Select.png)
![Screenshot: Game](screenshots/Current_Game.png)
![Screenshot: Statisitcs](screenshots/Statistics.png)


## Directories:
```
.
├── res/
│   └── labels.csv
├── screenshots/
├──src/
   └── nox
       └── minesweeper/
           ├── desktop/
           │   ├── GameField.java
           │   ├── LabelsFiller.java
           │   ├── Labels.java
           │   └── Minesweeper.java
           └── logic/
               ├── Field.java
               ├── ParsedField.java
               ├── PlayGround.java
               └── Solver.java
```


Where res/labels.csv contains the labels for the gui.


## Compiling Desktop GUI
This will create class files in /tmp/Minesweeper
In main directory run for building:
```
make build
```

and for running:
```
make run
```
