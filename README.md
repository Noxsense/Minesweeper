# Minesweeper

Small Minesweeper Project with GUI and a Solver (wip).

## Screenshots
![Screenshot: Home view](screenshots/SCR-20170929_235948--First_Home.png)
![Screenshot: Select Running Game](screenshots/SCR-20170930_000414--Select_Running_Game.png)
![Screenshot: Game](screenshots/SCR-20170930_000402--Mark_Bigger_Field.png)
![Screenshot: Small Game](screenshots/SCR-20170930_000549--Lost_Marked_Field.png)
![Screenshot: Statisitcs](screenshots/SCR-20170930_000742--Statistics.png)


## Directories:
```
.
├── res
│   └── labels.csv
├── src
│   └── nox
│       └── minesweeper
│           ├── Field.java
│           ├── GameField.java
│           ├── LabelsFiller.java
│           ├── Labels.java
│           ├── Minesweeper.java
│           ├── Solver.java
│           ├── PlayGround.java
├── screenshots
```

Where res/labels.csv contains the labels for the gui.


## Compiling
In main directory run for building:
```
make build
```

and for running:
```
make run
```
