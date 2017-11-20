
PROJECT = Minesweeper
PACKAGE = nox/minesweeper

# files and directories.
MAIN    = nox.minesweeper.desktop.Minesweeper
RES     = src/main/res
SRC     = src/main/java
LOGIC   = $(SRC)/$(PACKAGE)/logic
TESTS   = $(SRC)/$(PACKAGE)/tests
DESKTOP = $(SRC)/$(PACKAGE)/desktop
ANDROID = $(SRC)/$(PACKAGE)/android
LABELS  = $(DESKTOP)/labels.csv
BIN     = bin
CLASSES = bin/classes
APK     = bin/Minesweeper-debug.apk

# tools.
JAVA  = java
JAVAC = javac $(JTAGS) $(BTAGS)
JTAGS = -Xdiags:verbose -Xlint:deprecation -Xlint:unchecked #-Xlint:all
BTAGS = -d $(CLASSES) -cp $$CLASSPATH:$(CLASSES) # build tags
JARC  = jar vcfe
MKDIR = mkdir -p


#
# Run
#
run: desktop
	@cd $(CLASSES) && $(JAVA) $(MAIN)


android-install: $(APK)
	@gradle installDebug


run-jar: $(BIN)/$(PROJECT).jar $(CLASSES)/$(LABELS)
	@cd $(CLASSES) && $(JAVA) -jar $(PROJECT).jar


test: tests
	@cd $(CLASSES) && $(JAVA) nox.minesweeper.tests.MinesweeperTest


# 
#
# Build
#
android: $(APK)
$(APK): $(CLASSES)/$(PACKAGE)/logic/*.class $(ANDROID) $(RES)
	@gradle assembleDebug


desktop: $(CLASSES)/$(PACKAGE)/desktop/*.class
$(CLASSES)/$(PACKAGE)/desktop/*.class: $(CLASSES)/$(PACKAGE)/logic/*.class $(DESKTOP)
	@$(JAVAC) $(DESKTOP)/*.$(JAVA) && echo "Desktop GUI is built"


tests: $(CLASSES)/$(PACKAGE)/tests/*.class
$(CLASSES)/$(PACKAGE)/tests/*.class: $(CLASSES)/$(PACKAGE)/logic/*.class $(TESTS)
	@$(JAVAC) $(TESTS)/*.$(JAVA) && echo "Tests are built"


logic: $(CLASSES)/$(PACKAGE)/logic/*.class
$(CLASSES)/$(PACKAGE)/logic/*.class: $(CLASSES) $(LOGIC)
	@$(JAVAC) $(LOGIC)/*.$(JAVA) && echo "Logic is built"


$(BIN)/$(PROJECT).jar: build
	@cd $(CLASSES) && $(JARC) $(PROJECT).jar $(MAIN) *


#
# Extras
#

$(CLASSES):
	@$(MKDIR) $(CLASSES)
	

$(CLASSES)/$(LABELS): $(CLASSES) $(LABELS)
	@cp -vr $(LABELS) $(CLASSES)


#
# Clean
#
clean:
	@gradle clean
	@rm -vrf $(CLASSES)
