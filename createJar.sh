cd bin
cp ../printer.properties .
cp ../java.policy.applet .
cp -r ../lib/ostermillerutils_1_07_00.jar .
jar -xf ostermillerutils_1_07_00.jar
#cp -r ../lib .
#echo "Class-Path: lib/ostermillerutils_1_07_00.jar ./printer.properties
#Main-Class: PrinterLauncher
#" > Manifest.txt
rm TracPrinter.jar
#jar cvmf Manifest.txt TracPrinter.jar *
jar cvf TracPrinter.jar *
jarsigner TracPrinter.jar TracPrinter
rm printer.properties
rm java.policy.applet
mv TracPrinter.jar ../
cd ..
chmod u+x TracPrinter.jar

