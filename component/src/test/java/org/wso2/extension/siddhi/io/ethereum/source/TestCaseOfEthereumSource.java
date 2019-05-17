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
import org.testng.Assert;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;
import org.wso2.siddhi.core.SiddhiAppRuntime;
import org.wso2.siddhi.core.SiddhiManager;
import org.wso2.siddhi.core.event.Event;
import org.wso2.siddhi.core.exception.SiddhiAppCreationException;
import org.wso2.siddhi.core.stream.input.source.Source;
import org.wso2.siddhi.core.stream.output.StreamCallback;
import org.wso2.siddhi.core.util.EventPrinter;
import org.wso2.siddhi.core.util.SiddhiTestHelper;
import org.wso2.siddhi.query.api.exception.SiddhiAppValidationException;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TestCaseOfEthereumSource {

    private Logger log = Logger.getLogger(TestCaseOfEthereumSource.class);
    private AtomicInteger eventCount = new AtomicInteger(0);
    private AtomicBoolean eventArrived = new AtomicBoolean(false);
    private int waitTime = 3000;
    private int timeout = 60000;

    @BeforeMethod
    public void init() {

        eventCount.set(0);
        eventArrived.set(false);
    }

    @Test
    public void ethereumFilterNewBlockTest1() throws InterruptedException {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of Filtering new blocks.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri = 'ws://localhost:8546', \n" +
                "filter = 'newBlock', \n" +
                "polling.interval = '6000') \n" +
                "define stream inputStream(blockNumber int, blockHash string, nonce int," +
                " gasLimit int);\n";

        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);

        siddhiAppRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {

                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });

        siddhiAppRuntime.start();
        SiddhiTestHelper.waitForEvents(waitTime, 1, eventCount, timeout);
        Assert.assertTrue(eventArrived.get());
        siddhiManager.shutdown();
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void ethereumFilterNewTransactionTest2() throws InterruptedException {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of Filtering new transactions.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri='ws://localhost:8546', \n" +
                "filter='newTransaction', \n" +
                "polling.interval='1000') \n" +
                "define stream inputStream(transactionHash String, from String, to String, value int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {

                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });
        siddhiAppRuntime.start();
        SiddhiTestHelper.waitForEvents(waitTime, 1, eventCount, timeout);
        Assert.assertTrue(eventArrived.get());
        siddhiManager.shutdown();
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void ethereumFilterPendingTransactionTest3() throws InterruptedException {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of Filtering pending transactions.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri='ws://localhost:8546', \n" +
                "polling.interval='1000', \n" +
                "filter='pendingTransaction') \n" +
                "define stream inputStream(transactionHash String, from string, to string, value int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {

                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });
        siddhiAppRuntime.start();
        SiddhiTestHelper.waitForEvents(waitTime, 1, eventCount, timeout);
        Assert.assertTrue(eventArrived.get());
        siddhiManager.shutdown();
        siddhiAppRuntime.shutdown();
    }

    @Test
    public void ethereumFilterReplayTransactionTest4() throws InterruptedException {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of replay transactions in the Blockchain.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri='ws://localhost:8546', \n" +
                "filter='replayTransaction', \n" +
                "polling.interval='10000') \n" +
                "define stream inputStream(transactionHash String, blockNumber int, blockHash String, from String," +
                " to String, value int, nonce int, gas int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {

                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });
        siddhiAppRuntime.start();
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Assert.assertTrue(eventArrived.get());
        siddhiManager.shutdown();
        siddhiAppRuntime.shutdown();
    }

    // if the map annotation is not included in the siddhi app
    @Test(expectedExceptions = SiddhiAppCreationException.class)
    public void ethereumCreationExceptionTest5() {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of Siddhi App Creation Exception.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "uri='ws://localhost:8546', \n" +
                "filter='replayTransaction', \n" +
                "polling.interval='1000') \n" +
                "define stream inputStream(transactionHash String, blockNumber int, blockHash String, from String," +
                " to String, value int, nonce int, gas int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
    }

    // for invalid `filter`
    @Test(expectedExceptions = SiddhiAppValidationException.class)
    public void ethereumFilterCreationTest6() {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case of Siddhi App Validation for filter creation.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri='ws://localhost:8546', \n" +
                "filter='replyTransaction', \n" +
                "polling.interval='1000') \n" +
                "define stream inputStream(transactionHash String, blockNumber int, blockHash String, from String," +
                " to String, value int, nonce int, gas int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        siddhiAppRuntime.start();
        siddhiManager.shutdown();
    }

    // pause and resume
    @Test
    public void ethereumPauseAndResume() throws InterruptedException {

        log.info("---------------------------------------------------------------------------------------------");
        log.info("Ethereum Source Test Case for pause and resume.");
        log.info("---------------------------------------------------------------------------------------------");
        SiddhiManager siddhiManager = new SiddhiManager();
        String siddhiApp = "@App:name('test') \n" +
                "@source(type='ethereum', \n" +
                "@map(type='keyvalue'), \n" +
                "uri='ws://localhost:8546', \n" +
                "filter='newBlock', \n" +
                "polling.interval='1000') \n" +
                "define stream inputStream(blockHash String, blockNumber int, nonce int);\n";
        SiddhiAppRuntime siddhiAppRuntime = siddhiManager.createSiddhiAppRuntime(siddhiApp);
        Collection<List<Source>> sources = siddhiAppRuntime.getSources();
        siddhiAppRuntime.addCallback("inputStream", new StreamCallback() {
            @Override
            public void receive(Event[] events) {

                EventPrinter.print(events);
                for (Event event : events) {
                    eventCount.getAndIncrement();
                    eventArrived.set(true);
                }
            }
        });
        siddhiAppRuntime.start();
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);

        log.info("pause the event consumption");
        sources.forEach(e -> e.forEach(Source::pause));
        Thread.sleep(10000);
        sources.forEach(e -> e.forEach(Source::resume));
        log.info("resume the event consumption");
        eventArrived.set(false);
        SiddhiTestHelper.waitForEvents(waitTime, 2, eventCount, timeout);
        Thread.sleep(10000);
        Assert.assertTrue(eventArrived.get());
        siddhiManager.shutdown();
    }
}


