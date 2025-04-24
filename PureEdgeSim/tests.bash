#!/bin/bash

# Check if required arguments are provided
if [ $# -lt 3 ]; then
    echo "Usage: $0 <BASE_FOLDER> <MAIN_CLASS> <REPEAT_COUNT>"
    exit 1
fi

# Input parameters
BASE_FOLDER="$1"
MAIN_CLASS="$2"
REPEAT_COUNT="$3"

# Maven project directory
PROJECT_DIR="."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Maven is not installed. Please install it before running this script."
    exit 1
fi

# Navigate to the project directory
cd "$PROJECT_DIR" || { echo "Project directory not found!"; exit 1; }

# Array of values for the folder
VALUES_1=("0.05" "0.1")
SUFFIXES=("R" "UR")

# Run the Maven project for all combinations, using the specified repeat count
for VALUE_1 in "${VALUES_1[@]}"; do
    for SUFFIX in "${SUFFIXES[@]}"; do
        # Create the full folder name, adding _LIN before the suffix
        FOLDER_NAME="${BASE_FOLDER}_25.0_${VALUE_1}_LIN_${SUFFIX}/"
        
        for ((i = 1; i <= REPEAT_COUNT; i++)); do
            echo "Starting execution $i with folder: $FOLDER_NAME in background..."
            mvn exec:java -Dexec.mainClass="$MAIN_CLASS" -Dexec.args="$FOLDER_NAME" > /dev/null 2>&1 &
            
            PID=$!
            echo "Execution $i started with PID: $PID (value: $VALUE_1, suffix: $SUFFIX)"
            
            # Pause for 1 second
            sleep 1
        done
    done
done

echo "All executions have been started."
