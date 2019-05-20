/*
 *  Copyright (c) 2019, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 *  WSO2 Inc. licenses this file to you under the Apache License,
 *  Version 2.0 (the "License"); you may not use this file except
 *  in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.extension.siddhi.io.ethereum.util;

import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.core.DefaultBlockParameter;
import org.web3j.protocol.core.methods.response.EthBlock;
import org.web3j.protocol.core.methods.response.Transaction;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import rx.Subscription;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * This class is for creating filter
 **/
public class EthereumUtil {
    private static final Logger log = Logger.getLogger(EthereumUtil.class);
    private static Map<String, Object> filterMap;
    private Subscription subscription;
    private boolean paused = false;
    private ReentrantLock lock = new ReentrantLock();
    private Condition condition = lock.newCondition();

    public EthereumUtil() {
    }

    //called to create map for new transactions and replay transaction
    private static Map<String, Object> createMapForTransaction(Transaction transaction) {
        Map<String, Object> transactionMap = new HashMap<>();
        transactionMap.put(EthereumConstants.TRANSACTION_HASH, transaction.getHash());
        transactionMap.put(EthereumConstants.BLOCK_HASH, transaction.getBlockHash());
        transactionMap.put(EthereumConstants.BLOCK_NUMBER, transaction.getBlockNumber().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_SENDER, transaction.getFrom());
        transactionMap.put(EthereumConstants.TRANSACTION_RECIPIENT, transaction.getTo());
        transactionMap.put(EthereumConstants.TRANSACTION_AMOUNT, transaction.getValue().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_NONCE, transaction.getNonce().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_GAS, transaction.getGas().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_GAS_PRICE, transaction.getGasPrice().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_INDEX, transaction.getTransactionIndex().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_INPUT_STRING, transaction.getInput());
        return transactionMap;
    }

    //called to create map for pending transaction neglecting the null elements of a transaction.
    private static Map<String, Object> createMapForPendingTransaction(Transaction transaction) {
        Map<String, Object> transactionMap = new HashMap<>();
        transactionMap.put(EthereumConstants.TRANSACTION_HASH, transaction.getHash());
        transactionMap.put(EthereumConstants.TRANSACTION_SENDER, transaction.getFrom());
        transactionMap.put(EthereumConstants.TRANSACTION_RECIPIENT, transaction.getTo());
        transactionMap.put(EthereumConstants.TRANSACTION_AMOUNT, transaction.getValue().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_GAS_PRICE, transaction.getGasPrice().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_GAS, transaction.getGas().intValue());
        transactionMap.put(EthereumConstants.TRANSACTION_INPUT_STRING, transaction.getInput());
        return transactionMap;
    }

    //called to create map for new block
    private static Map<String, Object> createMapForNewBlock(EthBlock.Block block) {
        Map<String, Object> blockMap = new HashMap<>();
        blockMap.put(EthereumConstants.BLOCK_HASH, block.getHash());
        blockMap.put(EthereumConstants.BLOCK_NUMBER, block.getNumber().intValue());
        blockMap.put(EthereumConstants.BLOCK_DIFFICULTY, block.getDifficulty().intValue());
        blockMap.put(EthereumConstants.BLOCK_TOTAL_DIFFICULTY, block.getTotalDifficulty().intValue());
        blockMap.put(EthereumConstants.BLOCK_GAS_LIMIT, block.getGasLimit().intValue());
        blockMap.put(EthereumConstants.BLOCK_GAS_USED, block.getGasUsed().intValue());
        blockMap.put(EthereumConstants.BLOCK_PARENT_HASH, block.getParentHash());
        blockMap.put(EthereumConstants.BLOCK_MIX_HASH, block.getMixHash());
        blockMap.put(EthereumConstants.BLOCK_NONCE, block.getNonce().intValue());
        blockMap.put(EthereumConstants.BLOCK_SIZE, block.getSize().intValue());
        blockMap.put(EthereumConstants.BLOCK_MINER, block.getMiner());
        blockMap.put(EthereumConstants.BLOCK_RECEIPT_ROOT, block.getReceiptsRoot());
        blockMap.put(EthereumConstants.BLOCK_STATE_ROOT, block.getStateRoot());
        blockMap.put(EthereumConstants.BLOCK_TRANSACTIONS_ROOT, block.getTransactionsRoot());
        blockMap.put(EthereumConstants.BLOCK_LOGS_BLOOM, block.getLogsBloom());
        blockMap.put(EthereumConstants.BLOCK_SHA3_UNCLES, block.getSha3Uncles());
        return blockMap;
    }

    // called to create filtering
    public void createFilter(String filterName, String fromBlock, String toBlock,
                             SourceEventListener sourceEventListener, Web3j web3j) {
        switch (filterName) {
            case EthereumConstants.FILTER_NEW_BLOCK:
                // create an instance that emits newly created blocks on blockchain.
                subscription = web3j.blockObservable(true).subscribe(ethBlock -> {
                    EthBlock.Block block = ethBlock.getBlock();
                    filterMap = EthereumUtil.createMapForNewBlock(block);
                    listen();
                    if (!filterMap.isEmpty()) {
                        sourceEventListener.onEvent(filterMap, null);
                    }
                });
                break;
            case EthereumConstants.FILTER_NEW_TRANSACTION:
                // create an instance that emits all new transactions as they are comfirmed on the blockchain.
                subscription = web3j.transactionObservable().subscribe(tx -> {
                    filterMap = EthereumUtil.createMapForTransaction(tx);
                    listen();
                    if (!filterMap.isEmpty()) {
                        sourceEventListener.onEvent(filterMap, null);
                    }
                });
                break;
            case EthereumConstants.FILTER_PENDING_TRANSACTION:
                /* create an instance that emits all pending transactions that have yet to be placed
                 into a block on the blockchain.*/
                subscription = web3j.pendingTransactionObservable().subscribe(tx -> {
                    filterMap = EthereumUtil.createMapForPendingTransaction(tx);
                    listen();
                    if (!filterMap.isEmpty()) {
                        sourceEventListener.onEvent(filterMap, null);
                    }
                });
                break;
            case EthereumConstants.FILTER_REPLAY_TRANSACTION:
                /* create an instance that emits all transactions from the blockchain contained
                within the requested range*/
                DefaultBlockParameter startBlock = DefaultBlockParameter.valueOf(fromBlock);
                DefaultBlockParameter endBlock = DefaultBlockParameter.valueOf(toBlock);
                subscription = web3j.replayTransactionsObservable(startBlock, endBlock).subscribe(tx -> {
                    filterMap = EthereumUtil.createMapForTransaction(tx);
                    listen();
                    if (!filterMap.isEmpty()) {
                        sourceEventListener.onEvent(filterMap, null);
                    }
                });
                break;
            default: // validated within init() method
        }
    }

    //called to close the subscription for observable
    public void closeSubscription() {
        if (subscription != null) {
            subscription.unsubscribe();
        }
    }

    //called to make request to listener thread
    private void listen() {
        if (paused) {
            lock.lock();
            try {
                while (paused) {
                    condition.await();
                }
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            } finally {
                lock.unlock();
            }
        }
    }

    //called to pause event consumption
    public void pause() {
        paused = true;
    }

    //called to resume event consumption
    public void resume() {
        paused = false;
        try {
            lock.lock();
            condition.signalAll();
        } finally {
            lock.unlock();
        }
    }

}
