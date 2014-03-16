#!/bin/bash


#str=`echo "I love you more than I can say :)" | grep -o -E "say :"`
#echo "$str"


#Now Create the license template text
for license in `(cd original_text && ls *.txt)`; do
    sed -r -e "s:( *Copyright \([cC]\) *)((19|20)[0-9]{2}[-, ]*)+:\1 <year> :g" "original_text/$license" > "$license"
done

