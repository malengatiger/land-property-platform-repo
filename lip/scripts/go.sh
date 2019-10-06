#!/usr/bin/env bash
# ----------- DEPLOY CORDA NODES
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda nodes
./scripts/deploy.sh

echo -------- ️ "🍀 🍀 🍀 🍀 🍀 🍀 🍀 " done deploying Corda nodes
# ------------ NOTARY NODE
echo 🕗  Sleeping for 5 seconds
sleep 5s # Waits 5 seconds.
echo \nWoke up, opening terminal for Notary Corda Node
ttab ./scripts/nnotary.sh

# ------------ REGULATOR NODE
echo 🕗 Sleeping for 5 seconds
sleep 5s # Waits 5 seconds.
echo Woke up, opening terminal for Regulator Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 Regulator
ttab ./scripts/nregulator.sh


# ------------ LipNetworkOperator NODE
echo 🕗  Sleeping for 5 seconds
sleep 5s # Waits 5 seconds.
echo 🔆 Woke up, 🔆 🔆 🔆  opening terminal for LipNetworkOperator Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 LipNetworkOperator
ttab ./scripts/nbno.sh


# ------------ Bank NODE
echo 🕗 Sleeping for 5 seconds
sleep 5s # Waits 5 seconds.
echo \n🔆 Woke up, 🔆 🔆 🔆  opening terminal for Bank Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 Bank
ttab ./scripts/nbank.sh
sleep 10s


# ------------ LandAffairs NODE
echo 🕗  Sleeping for 5 seconds ........
sleep 5s # Waits 5 seconds.
echo \n🔆 Woke up, 🔆 🔆 🔆 opening terminal for LandAffairs Corda Node
echo -------- ️ "👽 👽 👽 👽 👽 👽 👽 " deploying Corda node: 🍎 LandAffairs
ttab ./scripts/nland-affairs.sh

echo 🔵 SLEEPING 🍎 20 🍎 seconds to let nodes finish booting up 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵 🔵
sleep 20s

echo \n🔆 Woke up, 🧩 🧩 🧩 🧩 opening Regulator webserver
ttab ./scripts/wregulator.sh
sleep 5s

echo \n🔆 Woke up, 🧩 🧩 🧩 🧩  opening BNO webserver
ttab ./scripts/wbno.sh
sleep 5s

echo \n🔆 Woke up, 🧩 🧩 🧩 🧩  opening LandAffairs webserver
ttab ./scripts/wland-affairs.sh

sleep 5s
echo \n🔆 Woke up, 🧩 🧩 🧩 🧩  opening bank webserver
ttab ./scripts/wbank.sh

echo -------- ️ "🍀 🍀 🍀 🍀 🍀 🍀 🍀 " done deploying Corda nodes and associated webservers



