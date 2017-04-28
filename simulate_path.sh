#http://latlongtrace.com/?path=m%60mkF%7Czwv@?iBb@%7B@j@aCp@uAZ%7DAGcEYgB?eBgBuAoE?eE%5DeFIsEGwEz@wCbAmDjBgB%5C?nB?v@q@f@I%60A

rm -f script
(cat eul_run.kml | while read line
do
	coord=($(echo $line | grep -o -E "\-?[0-9]+\.[0-9]+"))
	lat=$(echo ${coord[1]} | sed 's/\./,/g')
	lon=$(echo ${coord[0]} | sed 's/\./,/g')
	sleep 2
	echo  "geo fix $lon $lat"
   # do something with $line here
done) | telnet localhost 5554

