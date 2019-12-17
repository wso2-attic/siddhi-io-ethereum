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
package org.wso2.extension.siddhi.io.ethereum.source;

import org.apache.log4j.Logger;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.websocket.WebSocketService;
import org.wso2.extension.siddhi.io.ethereum.util.EthereumConstants;
import org.wso2.extension.siddhi.io.ethereum.util.EthereumUtil;
import org.wso2.siddhi.annotation.Example;
import org.wso2.siddhi.annotation.Extension;
import org.wso2.siddhi.annotation.Parameter;
import org.wso2.siddhi.annotation.util.DataType;
import org.wso2.siddhi.core.config.SiddhiAppContext;
import org.wso2.siddhi.core.exception.ConnectionUnavailableException;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.input.source.SourceEventListener;
import org.wso2.siddhi.core.util.config.ConfigReader;
import org.wso2.siddhi.core.util.transport.OptionHolder;
import org.wso2.siddhi.query.api.definition.StreamDefinition;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.net.ConnectException;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Ethereum Source Implementation.
 */
@Extension(
        name = "ethereum",
        namespace = "source",
        description = "The ethereum source receives the events from the ethereum blockchain.\n" +
                "This only supports key-value mapping.\n\n" +
                "To receive the events from the ethereum blockchain, " +
                "You can specify all attributes which were included in the response of the following functions\n " +
                "https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getblockbyhash \n" +
                "https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gettransactionbyhash \n" +
                "To start the client follow the document here,\n" +
                "https://docs.web3j.io/getting_started.html",
        parameters = {
                @Parameter(name = EthereumConstants.ETHEREUM_URI,
                        description = "The URI that is used to connect to ethereum blockchain. " +
                                "If no URI is specified, an error is logged in the CLI. e.g., `ws://localhost:8546`",
                        type = DataType.STRING),
                @Parameter(name = EthereumConstants.FILTER,
                        description = "This parameter specifies the classes of filter supported in ethereum." +
                                " eg., `newBlock`, `newTransaction`, `pendingTransaction` and `replayTransaction`." +
                                " When this parameter is set to `replayTransaction`, the `from.block`, and " +
                                "`to.block` parameters are initialized. ",
                        type = DataType.STRING),
                @Parameter(name = EthereumConstants.REPLAY_TRANSACTION_FROM_BLOCK,
                        description = "This parameter specifies the first block in the range of blocks" +
                                " specified. It can take default block parameter values eg.,`earliest`." +
                                " When `filter` parameter is set to `replayTransaction`, the `from.block`" +
                                " parameter is initialized. ",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = EthereumConstants.DEFAULT_FROM_BLOCK),
                @Parameter(name = EthereumConstants.REPLAY_TRANSACTION_TO_BLOCK,
                        description = "This parameter specifies the last block in the range of blocks" +
                                " specified. It can take default block parameter values eg., `latest`." +
                                "When `filter` parameter is set to `replayTransaction`, the `to.block`" +
                                " parameter is initialized. ",
                        type = DataType.STRING,
                        optional = true,
                        defaultValue = EthereumConstants.DEFAULT_TO_BLOCK),
                @Parameter(name = EthereumConstants.POLLING_INTERVAL,
                        description = "The polling time between two polls in milliseconds. ",
                        type = DataType.LONG,
                        optional = true,
                        defaultValue = EthereumConstants.DEFAULT_POLLING_INTERVAL)
        },
        examples = {
                @Example(
                        description = "In this example, the ethereum source listens to transaction history " +
                                "ranges from earliest to latest with the polling interval of 60 seconds. ",
                        syntax = "@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', " +
                                "filter='replayTransaction', from.block= 'earliest', to.block= 'latest'," +
                                "polling.interval='60000')\n" +
                                "define stream inputStream(from String, to String, value int);"
                ),
                @Example(
                        description = "In this example, the ethereum source polls the transactions" +
                                " that are pending every 60 seconds. ",
                        syntax = "@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546'," +
                                " filter='pendingTransaction', polling.interval='60000')\n" +
                                "define stream inputStream(from String, to String, value int);"
                ),
                @Example(
                        description = "In this example, the ethereum source polls the new blocks every 60 seconds " +
                                "as they are added to the blockchain. ",
                        syntax = "@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546'," +
                                " filter='newBlock', polling.interval='60000')\n" +
                                "define stream inputStream(blockNumber int, nonce int, " +
                                "blockHash String);"
                ),
                @Example(
                        description = "In this example, the ethereum source polls the new transactions every 60 " +
                                "seconds as they are added to the blockchain. ",
                        syntax = "@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', " +
                                "filter='newTransaction', polling.interval='60000')\n" +
                                "define stream inputStream(from String, to String, value int);"
                )
        }
)

public class EthereumSource extends Source {
    private static final Logger log = Logger.getLogger(EthereumSource.class);
    private SourceEventListener sourceEventListener;
    private String listenerUri;
    private String filterOption;
    private long pollingInterval;
    private String fromBlock;
    private String toBlock;
    private StreamDefinition streamDefinition;
    private ScheduledExecutorService scheduledExecutorService;
    private SiddhiAppContext siddhiAppContext;
    private Web3j web3j;
    private EthereumUtil filter;
    private WebSocketService webSocketService;

    @Override
    public void init(SourceEventListener sourceEventListener, OptionHolder optionHolder,
                     String[] requestedTransportPropertyNames, ConfigReader configReader,
                     SiddhiAppContext siddhiAppContext) {
        this.sourceEventListener = sourceEventListener;
        this.siddhiAppContext = siddhiAppContext;
        this.streamDefinition = sourceEventListener.getStreamDefinition();
        this.listenerUri = optionHolder.validateAndGetStaticValue(EthereumConstants.ETHEREUM_URI);
        this.filterOption = optionHolder.validateAndGetStaticValue(EthereumConstants.FILTER);
        this.fromBlock = optionHolder.validateAndGetStaticValue(EthereumConstants.REPLAY_TRANSACTION_FROM_BLOCK,
                EthereumConstants.DEFAULT_FROM_BLOCK);
        this.toBlock = optionHolder.validateAndGetStaticValue(EthereumConstants.REPLAY_TRANSACTION_TO_BLOCK,
                EthereumConstants.DEFAULT_TO_BLOCK);
        this.pollingInterval = validatePollingInterval(optionHolder, sourceEventListener.getStreamDefinition().getId());
        scheduledExecutorService = siddhiAppContext.getScheduledExecutorService();
        validateFilterOption(filterOption, sourceEventListener.getStreamDefinition().getId());
    }

    //to validate polling interval
    private long validatePollingInterval(OptionHolder optionHolder, String streamID) {
        try {
            return Long.parseLong(optionHolder.validateAndGetStaticValue(EthereumConstants.POLLING_INTERVAL,
                    EthereumConstants.DEFAULT_POLLING_INTERVAL));
        } catch (NumberFormatException e) {
            throw new SiddhiAppCreationException(streamID + "Polling interval accepts only positive values in " +
                    siddhiAppContext.getName());
        }
    }

    //to validate filter name
    private void validateFilterOption(String filterName, String streamID) {
        if (!filterName.equals(EthereumConstants.FILTER_NEW_BLOCK) &&
                !filterName.equals(EthereumConstants.FILTER_NEW_TRANSACTION) &&
                !filterName.equals(EthereumConstants.FILTER_PENDING_TRANSACTION) &&
                !filterName.equals(EthereumConstants.FILTER_REPLAY_TRANSACTION)) {
            throw new SiddhiAppValidationException("Expected 'newBlock' or 'newTransaction' or 'pendingTransaction' " +
                    "or 'replayTransaction' for filter, but got '" + filterName + "' in " + streamID + " for " +
                    siddhiAppContext.getName());
        }
    }

    @Override
    public Class[] getOutputEventClasses() {
        return new Class[]{Map.class};
    }

    @Override
    public void connect(ConnectionCallback connectionCallback) throws ConnectionUnavailableException {
        try {
            if (webSocketService == null) {
                webSocketService = new WebSocketService(listenerUri, true);
                webSocketService.connect();
            }
            web3j = Web3j.build(webSocketService, pollingInterval, scheduledExecutorService);

        } catch (ConnectException e) {
            throw new ConnectionUnavailableException("Failed to connect to the Ethereum blockchain." +
                    siddhiAppContext.getName() + " " + this.streamDefinition.getId(), e);
        } finally {
            webSocketService = null;
        }
        this.filter = new EthereumUtil();
        filter.createFilter(filterOption, fromBlock, toBlock, sourceEventListener, web3j);
    }

    @Override
    public void disconnect() {
        if (filter != null) {
            filter.closeSubscription();
        }
        if (web3j != null) {
            web3j.shutdown();
        }
        if (webSocketService != null) {
            webSocketService.close();
        }
        scheduledExecutorService.shutdown();
    }

    @Override
    public void destroy() {
    }

    @Override
    public void pause() {
        filter.pause();
    }

    @Override
    public void resume() {
        filter.resume();
    }

    @Override
    public Map<String, Object> currentState() {
        return null;
    }

    @Override
    public void restoreState(Map<String, Object> map) {
    }
}
