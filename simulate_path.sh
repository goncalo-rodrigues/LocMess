echo "Usage: ./simulate_path.sh <path_file> <avd_port>"
echo ""

# Put the arguments with more programmer friendly names
path=$1
port=$2

rm -f script
(cat $path | while read line
do
	coord=($(echo $line | grep -o -E "\-?[0-9]+\.[0-9]+"))
	lat=$(echo ${coord[1]} | sed 's/\./,/g')
	lon=$(echo ${coord[0]} | sed 's/\./,/g')
	sleep 0.1
	echo  "geo fix $lon $lat"
   # do something with $line here
done) | telnet localhost $port
