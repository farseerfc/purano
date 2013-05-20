CLASSPATH := $(CLASSPATH):bin/:lib/*
export CLASSPATH

build:
	ant all

run: build
	java jp.ac.osakau.farseerfc.purano.reflect.ClassFinder
