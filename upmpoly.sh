#!/bin/bash

# Blockchain Project upmPoly 2023
# Participants: João Diogo Martins Romão, Marius Hauenstein and Niccolò Revel Garrone

#define environment variables
export FABRIC_CFG_PATH=$PWD/../config/
export CORE_PEER_TLS_ENABLED=true
export CORE_PEER_LOCALMSPID="Org1MSP"
export CORE_PEER_TLS_ROOTCERT_FILE=${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt
export CORE_PEER_MSPCONFIGPATH=${PWD}/organizations/peerOrganizations/org1.example.com/users/Admin@org1.example.com/msp
export CORE_PEER_ADDRESS=localhost:7051

# initialize the blockchain with some initial values
function invokeChaincode() {
  f="{\"function\":\"$1\",\"Args\":[\"$2\",\"$3\",\"$4\",\"$5\"]}"
  echo $f
  peer chaincode invoke -o localhost:7050 --ordererTLSHostnameOverride orderer.example.com --tls --cafile ${PWD}/organizations/ordererOrganizations/example.com/orderers/orderer.example.com/msp/tlscacerts/tlsca.example.com-cert.pem -C mychannel -n upmpoly --peerAddresses localhost:7051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org1.example.com/peers/peer0.org1.example.com/tls/ca.crt --peerAddresses localhost:9051 --tlsRootCertFiles ${PWD}/organizations/peerOrganizations/org2.example.com/peers/peer0.org2.example.com/tls/ca.crt -c $f
  sleep 5
}

function readChaincode() {
  f="{\"Args\":[\"$1\"]}"
  echo $f
  peer chaincode query -C mychannel -n upmpoly -c $f
  sleep 5
}

## Parse mode
if [[ $# -lt 1 ]] ; then
  echo "-----Initialize ledger-----"
  invokeChaincode InitLedger
  echo "-----Create new player-----"
  invokeChaincode Player player4 Ronaldo 11
  echo "-----Create new faculty-----"
  invokeChaincode Faculty faculty4 Biology 500000 600
  echo "-----Get all players-----"
  readChaincode GetAllPlayers
  echo "-----Get all faculties-----"
  readChaincode GetAllFaculties
  echo "-----Player 3 buys faculty 1-----"
  invokeChaincode buyFaculty player3 faculty1
  echo "-----Player 2 pays rent for faculty 1-----"
  invokeChaincode payRental faculty1 player2
  echo "-----Get all players to see credits-----"
  readChaincode GetAllPlayers
  echo "-----Player 1 should pay rental fee for faculty 1 but gets eliminated-----"
  invokeChaincode payRental faculty1 player1
  echo "-----Get all players to see credits-----"
  readChaincode GetAllPlayers
  echo "-----Player 2 buys faculty 1-----"
  invokeChaincode tradeFaculty faculty1 player2 1000
  echo "-----Get owner of faculty 1-----"
  invokeChaincode getOwner faculty1
  echo "-----Get money of player 2-----"
  invokeChaincode getMoney player2
  echo "-----Get status of a player-----"
    invokeChaincode isEliminated player1
  echo "-----Get all active players-----"
  invokeChaincode getPlayers  
else
  MODE=$1
  shift
fi

if [ "${MODE}" == "InitLedger" ]; then
  invokeChaincode InitLedger
elif [ "${MODE}" == "Player" ]; then
  invokeChaincode Player $1 $2 $3
elif [ "${MODE}" == "Faculty" ]; then
  invokeChaincode Faculty $1 $2 $3 $4
elif [ "${MODE}" == "buyFaculty" ]; then
  invokeChaincode buyFaculty $1 $2 $3
elif [ "${MODE}" == "payRental" ]; then
  invokeChaincode payRental $1 $2 $3
elif [ "${MODE}" == "tradeFaculty" ]; then
  invokeChaincode tradeFaculty $1 $2 $3
elif [ "${MODE}" == "getOwner" ]; then
  invokeChaincode getOwner $1 $2 $3
elif [ "${MODE}" == "getMoney" ]; then
  invokeChaincode getMoney $1 $2 $3
elif [ "${MODE}" == "isEliminated" ]; then
  invokeChaincode isEliminated $1 $2 $3
elif [ "${MODE}" == "getPlayers" ]; then
  invokeChaincode getPlayers $1 $2 $3
elif [ "${MODE}" == "" ]; then
  echo No specific mode!
else
  echo Function not found!
  exit 1
fi
