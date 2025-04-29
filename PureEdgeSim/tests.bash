#!/bin/bash

# Check if required arguments are provided
if [ $# -lt 4 ]; then
    echo "Usage: $0 <PROJECT_DIR> <BASE_FOLDER> <MAIN_CLASS> <REPEAT_COUNT>"
    exit 1
fi

# Input parameters
PROJECT_DIR="$1"
BASE_FOLDER="$2"
MAIN_CLASS="$3"
REPEAT_COUNT="$4"

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install it before running this script."
    exit 1
fi

# Navigate to the project directory
cd "$PROJECT_DIR" || { echo "Project directory not found!"; exit 1; }

# Arrays for parameter combinations
C_VAR=("25.0" "50.0" "75.0")
COEFFICIENTS=("0.05" "0.1")
SUFFIXES=("R" "UR")

# Run the Maven project for all combinations, using the specified repeat count
for CVAL in "${C_VAR[@]}"; do
    for COEF in "${COEFFICIENTS[@]}"; do
        for SUFFIX in "${SUFFIXES[@]}"; do
            # Create the full folder name
            FOLDER_NAME="${BASE_FOLDER}_${CVAL}_${COEF}_LIN_${SUFFIX}/"
            
            for ((i = 1; i <= REPEAT_COUNT; i++)); do
                echo "Starting execution $i with folder: $FOLDER_NAME in background..."
                mvn exec:java -Dexec.mainClass="$MAIN_CLASS" -Dexec.args="$FOLDER_NAME" > /dev/null 2>&1 &
                
                PID=$!
                echo "Execution $i started with PID: $PID (C: $CVAL, Coef: $COEF, Suffix: $SUFFIX)"
                
                sleep 1
            done
        done
    done
done

echo "All executions have been started."