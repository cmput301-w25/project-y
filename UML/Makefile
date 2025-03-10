TARGET=output.puml
OUTPUT=output.png
SOURCE=../code/app/src/main/java/com/example/y

compile: $(TARGET)
	java -DPLANTUML_LIMIT_SIZE=99999999 -jar plantuml.jar $(TARGET)

uml:
	java -jar java2plantuml.jar $(SOURCE)
	sed -i '2i left to right direction' $(TARGET)
	java -DPLANTUML_LIMIT_SIZE=99999999 -jar plantuml.jar $(TARGET)
