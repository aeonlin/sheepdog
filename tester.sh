#!/bin/bash
rm test.result
# Remember to disable the other two strategies!!!!
NUM_SHEEP=1
NUM_TIMES_TESTED=0

# Keep the number of dogs static and only increase the number of sheep
for i in 1 2 4 7 11 22 32 100
do
	# Increment the number of sheep by 5, this can/should be changed
	while [ $NUM_SHEEP -le 500 ]
	do
		echo "Num sheep: $NUM_SHEEP, Num Dogs: $i" >> test.result
		# For an average, run the configuration 10 times for variance
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

