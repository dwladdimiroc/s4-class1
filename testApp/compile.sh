clear
./s4 s4r -a=topology.Topology -b=`pwd`/build.gradle testApp
./s4 deploy -s4r=`pwd`/build/libs/testApp.s4r -c=cluster1 -appName=testApp
