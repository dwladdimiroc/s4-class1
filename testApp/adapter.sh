clear
./s4 newCluster -c=cluster2 -nbTasks=1 -flp=13000
./s4 deploy -appClass=adapter.Adapter -p=s4.adapter.output.stream=inputStream -c=cluster2 -appName=adapter
./s4 adapter -c=cluster2
