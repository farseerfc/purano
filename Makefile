CLASSPATH := $(CLASSPATH):bin/:lib/*
export CLASSPATH

build:
	ant build

run: build
	java jp.ac.osakau.farseerfc.purano.reflect.ClassFinder
