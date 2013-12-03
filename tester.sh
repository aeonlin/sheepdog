#!/bin/bash
rm test.result

NUM_SHEEP=1
NUM_TIMES_TESTED=0

for i in 1 2 4 7 11 22 32 100
do
	while [ $NUM_SHEEP -le 500 ]
	do
		echo "Num sheep: $NUM_SHEEP, Num Dogs: $i" >> test.result
		while [ $NUM_TIMES_TESTED -le 10 ]
		do
		java sheepdog.sim.Sheepdog g7 $i $NUM_SHEEP 1 false false 2>&1 | grep "ticks" >> test.result
		NUM_TIMES_TESTED=$(($NUM_TIMES_TESTED + 1))
		done
		echo " " >> test.result
		NUM_SHEEP=$(($NUM_SHEEP + 5))
		NUM_TIMES_TESTED=0
	done
done

