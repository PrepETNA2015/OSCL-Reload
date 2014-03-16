#!/bin/bash

#
#for license in `ls *.txt`; do
#    if [ ! -e "${license%%[.]txt}.meta" ]; then
#	echo "$license"
#    fi
#done
#
function get_compatible() {
    if [ "$1" == "cpl" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl"
#	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl" cddl

    elif [ "$1" == "eclipse" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl"
#	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl" cddl

    elif [ "$1" == "mit" ]; then
	iscompatible="mit gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse ms-lcl nokos perlartistic osl"
#	iscompatible="mit gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse ms-lcl nokos perlartistic osl" bsd

    elif [ "$1" == "ms-lcl" ]; then
	iscompatible="ms-lcl bsd x11 ms-cl sleepycat mit"

    elif [ "$1" == "nokos" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit"
#	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit" mpl

    elif [ "$1" == "osl" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit"
#	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit" mpl

    elif [ "$1" == "perlartistic" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit"
#	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit" artistic

    elif [ "$1" == "gpl" ]; then
	iscompatible="gpl lgpl bsd x11 sspl ms-pl sleepycat mit"

    elif [ "$1" == "lgpl" ]; then
	iscompatible="gpl lgpl bsd x11 sspl sleepycat mit ms-pl"

    elif [ "$1" == "bsd" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat mit cpl eclipse ms-lcl nokos perlartistic osl"

    elif [ "$1" == "x11" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat cpl eclipse mit ms-lcl nokos perlartistic osl"

    elif [ "$1" == "mpl" ]; then
	iscompatible="nokos bsd x11 mpl cddl ms-pl sleepycat osl cpl eclipse mit"

    elif [ "$1" == "cddl" ]; then
	iscompatible="cpl eclipse bsd x11 mpl cddl ms-pl sleepycat mit nokos osl"

    elif [ "$1" == "php" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic"

    elif [ "$1" == "apache" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic"

    elif [ "$1" == "ipl" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic"

    elif [ "$1" == "sspl" ]; then
	iscompatible="gpl lgpl bsd x11 php apache ipl sspl artistic ms-pl sleepycat mit perlartistic"

    elif [ "$1" == "artistic" ]; then
	iscompatible="bsd x11 php apache ipl sspl artistic ms-pl sleepycat perlartistic mit"

    elif [ "$1" == "ms-pl" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-rl sleepycat cpl eclipse mit nokos perlartistic osl"

    elif [ "$1" == "ms-cl" ]; then
	iscompatible="ms-lcl bsd x11 ms-cl sleepycat mit"

    elif [ "$1" == "ms-rl" ]; then
	iscompatible="bsd x11 ms-pl ms-rl sleepycat mit"

    elif [ "$1" == "sleepycat" ]; then
	iscompatible="gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl cpl eclipse mit ms-lcl nokos perlartistic osl"
    fi
}

for license in cpl eclipse mit ms-lcl nokos perlartistic osl gpl lgpl bsd x11 mpl cddl php apache ipl sspl artistic ms-pl ms-cl ms-rl sleepycat; do
    echo "Licenses that are recognized by $license but do not recognize $license:"
    get_compatible "$license"
    host_compatible="$iscompatible"
    for i in `echo $host_compatible`; do
	get_compatible "$i"
	if [ `echo "$iscompatible" | grep -o -E "$license" | wc -w` -eq 0 ]; then
	    echo -n "$i "
	fi
    done
    echo ""
done

