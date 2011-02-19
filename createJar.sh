cd bin
cp ../printer.properties .
cp ../java.policy.applet .
rm TracPrinter.jar
jar cvf TracPrinter.jar *
jarsigner TracPrinter.jar TracPrinter
rm printer.properties
rm java.policy.applet
mv TracPrinter.jar ../
