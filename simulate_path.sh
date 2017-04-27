#http://latlongtrace.com/?path=m%60mkF%7Czwv@?iBb@%7B@j@aCp@uAZ%7DAGcEYgB?eBgBuAoE?eE%5DeFIsEGwEz@wCbAmDjBgB%5C?nB?v@q@f@I%60A


rm -f script
cat path.txt | while read line
do
	lat=$(echo $(echo $line | grep -o '.*,' | sed 's/,//g;s/\./,/g'))
	lon=$(echo $(echo $line | grep -o ',.*' | sed 's/,//g;s/\./,/g'))
	echo  "geo fix $lon $lat" >> script
   # do something with $line here
done

(sleep 2
cat script | while read line
do
	echo $line
	sleep 2
done) | telnet localhost 5554
