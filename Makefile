
PROJECT = Minesweeper

# files and directories.
MAIN    = nox.minesweeper.desktop.Minesweeper
RES     = res
SRC     = src/nox/minesweeper
LOGIC   = $(SRC)/logic
DESKTOP = $(SRC)/desktop
TMP     = /tmp/Minesweeper
BIN     = $(TMP)/bin
CLASSES = $(BIN)/nox/minesweeper/*/*.class

# tools.
JAVAC = javac $(JTAGS) $(BTAGS)
jtags = -Xdiags:verbose -Xlint:deprecation -Xlint:unchecked #-Xlint:all
BTAGS = -d $(BIN) -cp $$CLASSPATH:$(BIN) # build tags
JARC  = jar vcfe


# Run
run: $(CLASSES) $(BIN)/$(RES)
	@cd $(BIN) && java $(MAIN)


# Build
build: $(CLASSES)


# Jar File
run-jar: $(TMP)/$(PROJECT).jar $(BIN)/$(RES)
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
	

$(BIN)/$(RES): $(BIN) $(RES)/*
	@cp -vr $(RES) $(BIN)


# Clean
clean: $(BIN)
	@rm -vfr $(TMP)
