#http://latlongtrace.com/?path=m%60mkF%7Czwv@?iBb@%7B@j@aCp@uAZ%7DAGcEYgB?eBgBuAoE?eE%5DeFIsEGwEz@wCbAmDjBgB%5C?nB?v@q@f@I%60A

rm -f script
cat eul_run.kml | while read line
do
	coord=($(echo $line | grep -o -E "\-?[0-9]+\.[0-9]+"))
	lat=$(echo ${coord[1]} | sed 's/\./,/g')
	lon=$(echo ${coord[0]} | sed 's/\./,/g')
	echo  "geo fix $lon $lat" >> script
   # do something with $line here
done

(sleep 2
cat script | while read line
do
	echo $line
	sleep 0.1
done) | telnet localhost 5554
