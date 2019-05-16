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

/**
 * This class represents Ethereum specific Constants.
 */

public class EthereumConstants {

    public static final String ETHEREUM_URI = "uri";
    public static final String FILTER = "filter";
    public static final String REPLAY_TRANSACTION_FROM_BLOCK = "from.block";
    public static final String REPLAY_TRANSACTION_TO_BLOCK = "to.block";
    public static final String POLLING_INTERVAL = "polling.interval";
    public static final String DEFAULT_POLLING_INTERVAL = "3600";
    public static final String DEFAULT_FROM_BLOCK = "earliest";
    public static final String DEFAULT_TO_BLOCK = "latest";

    public static final String FILTER_NEW_BLOCK = "newBlock";
    public static final String FILTER_NEW_TRANSACTION = "newTransaction";
    public static final String FILTER_REPLAY_TRANSACTION = "replayTransaction";
    public static final String FILTER_PENDING_TRANSACTION = "pendingTransaction";

    public static final String TRANSACTION_HASH = "transactionHash";
    public static final String TRANSACTION_SENDER = "from";
    public static final String TRANSACTION_RECIPIENT = "to";
    public static final String TRANSACTION_AMOUNT = "value";
    public static final String TRANSACTION_NONCE = "nonce";
    public static final String TRANSACTION_GAS = "gas";
    public static final String TRANSACTION_GAS_PRICE = "gasPrice";
    public static final String TRANSACTION_INPUT_STRING = "input";
    public static final String TRANSACTION_INDEX = "transactionIndex";

    public static final String BLOCK_HASH = "blockHash";
    public static final String BLOCK_NUMBER = "blockNumber";
    public static final String BLOCK_SIZE = "size";
    public static final String BLOCK_DIFFICULTY = "difficulty";
    public static final String BLOCK_TOTAL_DIFFICULTY = "totalDifficulty";
    public static final String BLOCK_NONCE = "nonce";
    public static final String BLOCK_GAS_LIMIT = "gasLimit";
    public static final String BLOCK_GAS_USED = "gasUsed";
    public static final String BLOCK_PARENT_HASH = "parentHash";
    public static final String BLOCK_MIX_HASH = "mixHash";
    public static final String BLOCK_MINER = "miner";
    public static final String BLOCK_LOGS_BLOOM = "logsBloom";
    public static final String BLOCK_STATE_ROOT = "stateRoot";
    public static final String BLOCK_RECEIPT_ROOT = "receiptRoot";
    public static final String BLOCK_TRANSACTIONS_ROOT = "transactionsRoot";
    public static final String BLOCK_SHA3_UNCLES = "sha3Uncles";

    private EthereumConstants() {
    }
}
