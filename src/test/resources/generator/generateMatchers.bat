SET WORKING_DIR="G:\workspace\xmlunit-nsc\xmlunit-nsc\src\test\resources\generator"
SET DEST_PACKAGE=org.custommonkey.xmlunit.matchers
SET DEST_CLASS=XmlUnitMatchers
SET REL_PATH=../../../..

cd /D %WORKING_DIR%
java -cp hamcrest-all-1.3.jar;%REL_PATH%/target/classes org.hamcrest.generator.config.XmlConfigurator matchers_config.xml %REL_PATH%/src/main/java %DEST_PACKAGE%.%DEST_CLASS% %REL_PATH%/src/main/java/
pause