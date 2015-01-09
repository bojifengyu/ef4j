#!/bin/bash

bash compile.sh
mvn -Dtest=DynamicArrayTest test
mvn -Dtest=LongDynamicArrayTest test
mvn -Dtest=IntegerPrefixSumDynamicArrayTest test
mvn -Dtest=EliasFanoAppendOnlyMonotoneLongSequenceTest test
mvn -Dtest=EliasFanoAdaptiveAppendOnlyMonotoneLongSequenceTest test
mvn -Dtest=EliasFanoDynamicMonotoneLongSequenceTest test