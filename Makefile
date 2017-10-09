
PROJECT = Minesweeper

# files and directories.
MAIN    = nox.minesweeper.desktop.Minesweeper
SRC     = src/nox/minesweeper
LOGIC   = $(SRC)/logic
DESKTOP = $(SRC)/desktop
LABELS  = $(DESKTOP)/labels.csv
TMP     = /tmp/Minesweeper
BIN     = $(TMP)/bin
CLASSES = $(BIN)/nox/minesweeper/*/*.class

# tools.
JAVAC = javac $(JTAGS) $(BTAGS)
jtags = -Xdiags:verbose -Xlint:deprecation -Xlint:unchecked #-Xlint:all
BTAGS = -d $(BIN) -cp $$CLASSPATH:$(BIN) # build tags
JARC  = jar vcfe


# Run
run: $(CLASSES) $(BIN)/$(LABELS)
	@cd $(BIN) && java $(MAIN)


android:
	ant debug


# Build
build: $(CLASSES)


# Jar File
run-jar: $(TMP)/$(PROJECT).jar $(BIN)/$(LABELS)
	cd $(BIN) && java -jar $(PROJECT).jar


$(TMP)/$(PROJECT).jar: $(CLASSES)
	cd $(BIN) && $(JARC) $(BIN)/$(PROJECT).jar $(MAIN) *


# Compile classes
logic: $(BIN) $(LOGIC)
	@$(JAVAC) $(LOGIC)/*.java


desktop: logic $(DESKTOP)
	@$(JAVAC) $(DESKTOP)/*.java


$(CLASSES): logic desktop


# Create Bin directory
$(BIN):
	@mkdir -p $(BIN)
	

$(BIN)/$(LABELS): $(BIN) $(LABELS)
	@cp -vr $(LABELS) $(BIN)


# Clean
clean: $(BIN)
	@rm -vfr $(TMP)
	@ant clean
