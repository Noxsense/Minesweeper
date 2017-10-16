
PROJECT = Minesweeper
PACKAGE = nox/minesweeper

# files and directories.
MAIN    = nox.minesweeper.desktop.Minesweeper
SRC     = src
LOGIC   = $(SRC)/$(PACKAGE)/logic
TESTS   = $(SRC)/$(PACKAGE)/tests
DESKTOP = $(SRC)/$(PACKAGE)/desktop
ANDROID = $(SRC)/$(PACKAGE)/android
LABELS  = $(DESKTOP)/labels.csv
BIN     = bin
CLASSES = bin/classes
APK     = bin/Minesweeper-debug.apk

# tools.
JAVAC = javac $(JTAGS) $(BTAGS)
JTAGS = -Xdiags:verbose -Xlint:deprecation -Xlint:unchecked #-Xlint:all
BTAGS = -d $(CLASSES) -cp $$CLASSPATH:$(CLASSES) # build tags
JARC  = jar vcfe
MKDIR = mkdir -p


#
# Run
#
run: desktop
	@cd $(CLASSES) && java $(MAIN)


android-install: $(APK)
	@adb devices && adb install -r $(APK)


run-jar: $(BIN)/$(PROJECT).jar $(CLASSES)/$(LABELS)
	@cd $(CLASSES) && java -jar $(PROJECT).jar


test: tests
	@cd $(CLASSES) && java nox.minesweeper.tests.MinesweeperTest


# 
#
# Build
#
android: $(APK)
$(APK): $(CLASSES)/$(PACKAGE)/logic/*.class $(ANDROID) res/*/*
	@ant debug


desktop: $(CLASSES)/$(PACKAGE)/desktop/*.class
$(CLASSES)/$(PACKAGE)/desktop/*.class: $(CLASSES)/$(PACKAGE)/logic/*.class $(DESKTOP)
	@$(JAVAC) $(DESKTOP)/*.java && echo "Desktop GUI is built"


tests: $(CLASSES)/$(PACKAGE)/tests/*.class
$(CLASSES)/$(PACKAGE)/tests/*.class: $(CLASSES)/$(PACKAGE)/logic/*.class $(TESTS)
	@$(JAVAC) $(TESTS)/*.java && echo "Tests are built"


logic: $(CLASSES)/$(PACKAGE)/logic/*.class
$(CLASSES)/$(PACKAGE)/logic/*.class: $(CLASSES) $(LOGIC)
	@$(JAVAC) $(LOGIC)/*.java && echo "Logic is built"


$(BIN)/$(PROJECT).jar: build
	@cd $(CLASSES) && $(JARC) $(PROJECT).jar $(MAIN) *


#
# Extras
#
$(BIN): $(CLASSES)

$(CLASSES):
	@$(MKDIR) $(CLASSES)
	

$(CLASSES)/$(LABELS): $(CLASSES) $(LABELS)
	@cp -vr $(LABELS) $(CLASSES)


#
# Clean
#
clean: $(BIN)
	@ant clean
