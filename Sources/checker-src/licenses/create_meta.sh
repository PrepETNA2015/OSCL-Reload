#!/bin/bash

function get_compatible() {
    if [ "$1" == "cpl" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl bsd-sun"
#	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl" cddl

    elif [ "$1" == "eclipse" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl bsd-sun"
#	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl" cddl

    elif [ "$1" == "mit" ]; then
	iscompatible="mit gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse ms-lcl nokos perlartistic osl bsd-sun"
#	iscompatible="mit gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse ms-lcl nokos perlartistic osl" bsd

    elif [ "$1" == "ms-lcl" ]; then
	iscompatible="ms-lcl bsd x11 ms-cl sleepycat mit bsd-sun"

    elif [ "$1" == "nokos" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit bsd-sun"
#	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit" mpl

    elif [ "$1" == "osl" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit bsd-sun"
#	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit" mpl

    elif [ "$1" == "perlartistic" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit bsd-sun"
#	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit" artistic

    elif [ "$1" == "gpl" ]; then
	iscompatible="gpl lgpl bsd x11 sspl ms-pl sleepycat mit bsd-sun"

    elif [ "$1" == "lgpl" ]; then
	iscompatible="gpl lgpl bsd x11 sspl sleepycat mit ms-pl bsd-sun"

    elif [ "$1" == "bsd" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat mit cpl eclipse ms-lcl nokos perlartistic osl bsd-sun"

    elif [ "$1" == "bsd-sun" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat mit cpl eclipse ms-lcl nokos perlartistic osl bsd-sun"

    elif [ "$1" == "x11" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse mit ms-lcl nokos perlartistic osl bsd-sun"

    elif [ "$1" == "mpl" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit bsd-sun"

    elif [ "$1" == "cddl" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl bsd-sun"

    elif [ "$1" == "php" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic bsd-sun"

    elif [ "$1" == "apache" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic bsd-sun"

    elif [ "$1" == "ipl" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic bsd-sun"

    elif [ "$1" == "sspl" ]; then
	iscompatible="gpl lgpl bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic bsd-sun"

    elif [ "$1" == "artistic" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit bsd-sun"

    elif [ "$1" == "ms-pl" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-rl sleepycat cpl eclipse mit nokos perlartistic osl bsd-sun"

    elif [ "$1" == "ms-cl" ]; then
	iscompatible="ms-lcl bsd x11 ms-cl sleepycat mit bsd-sun"

    elif [ "$1" == "ms-rl" ]; then
	iscompatible="bsd x11 ms-pl ms-rl sleepycat mit bsd-sun"

    elif [ "$1" == "sleepycat" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl cpl eclipse mit ms-lcl nokos perlartistic osl bsd-sun"
    fi
}


#Generate the meta files
rm *.meta

for license in cpl eclipse mit ms-lcl nokos perlartistic osl gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat; do
#Add the "isCompatible property"
    get_compatible "$license"

    for i in `ls $license*.txt`; do
	mybase=${i%%[.]txt}
	echo "#This is the meta file for $i." >> $mybase.meta
	echo -n "isCompatible : " >> $mybase.meta

	for j in `echo $iscompatible`; do
	    for k in `ls $j*.txt`; do

		compatiblebase=${k%%[.]txt}
		if [ "$compatiblebase" != "$mybase" ]; then
		    echo -n "$compatiblebase " >> $mybase.meta
		fi
	    done
	done
	echo "" >> $mybase.meta

#Add an empty line
	echo "" >> $mybase.meta
    done

#Now add the tags
    for i in `ls $license*.txt`; do
	mybase=${i%%[.]txt}
	mytags=""
	echo -n "tags : " >> $mybase.meta
	for j in `grep -o -E "<[a-zA-Z_ ]+>" "$i"`; do
	    if [ -z `echo "$mytags" | grep -o -E "$j"` ]; then
		mytags="$mytags $j"
	    fi
	done
	echo "$mytags" >> $mybase.meta
	echo "" >> $mybase.meta
    done
done

#Add forbidden phrases
for phrase in "shareware" "not for commercial use" "patent pending" "patented" "all rights reserved"; do
    mybase=`echo -n "$phrase" | sed -r -e "s: :_:g"`

    echo "$phrase" > $mybase-f.txt
    echo "#meta file for $mybase-f.txt" > $mybase-f.meta
    echo -n "isCompatible : " >> $mybase-f.meta

    for license in `ls *.txt`; do
	if [ "$license" != "$mybase-f.txt" ]; then
	    if [ `grep -i -E -l "\<$phrase\>" "$license" | wc -w` -gt 0 ]; then
		echo -n "${license%%[.]txt} " >> $mybase-f.meta
	    fi
	fi
    done
    echo "" >> $mybase-f.meta
done

