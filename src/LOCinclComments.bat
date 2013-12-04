'with comments
dir /b *.java | java -cp LOC.jar textui.LOC -d .\

'without comments
dir /b *.java | java -cp LOC.jar textui.LOC -n -d .\

SET CLASSPATH=.;P:\cumming\research\java\dpm.cumming.latest\src