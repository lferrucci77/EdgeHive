#!/bin/bash

# Directory del progetto Maven
PROJECT_DIR="."

# Verifica se Maven � installato
if ! command -v mvn &> /dev/null; then
    echo "Maven non � installato. Installalo prima di eseguire lo script."
    exit 1
fi

# Spostati nella directory del progetto
cd "$PROJECT_DIR" || { echo "Directory del progetto non trovata!"; exit 1; }

# Parametri per la classe principale
MAIN_CLASS="PaperScenario.PaperScenarioMain"

# Nome base della cartella
BASE_FOLDER="PureEdgeSim/PaperScenario_Settings_8_2000"

# Array di valori per la cartella
VALUES_1=("0.05" "0.1")
SUFFIXES=("R" "UR")

# Esegui il progetto Maven per tutte le combinazioni, 5 volte ciascuna
for VALUE_1 in "${VALUES_1[@]}"; do
    for SUFFIX in "${SUFFIXES[@]}"; do
        # Crea il nome completo della cartella, aggiungendo _LIN prima del suffisso
        FOLDER_NAME="${BASE_FOLDER}_25.0_${VALUE_1}_LIN_${SUFFIX}/"
            
        # Esegui la combinazione 5 volte
        for i in {1..5}; do
            echo "Avvio esecuzione $i con la cartella: $FOLDER_NAME in background..."
            mvn exec:java -Dexec.mainClass="$MAIN_CLASS" -Dexec.args="$FOLDER_NAME" > /dev/null 2>&1 &
               
            PID=$!
            echo "Esecuzione $i avviata con PID: $PID (valori $VALUE_2, $VALUE_1, e suffisso $SUFFIX)"
                
            # Pausa di 1 secondo
            sleep 1
        done
    done
done

echo "Tutte le esecuzioni sono state avviate."