# API Docs - v1.0.0-SNAPSHOT

## Source

### ethereum *<a target="_blank" href="https://wso2.github.io/siddhi/documentation/siddhi-4.0/#source">(Source)</a>*

<p style="word-wrap: break-word">The ethereum source receives the events from the ethereum blockchain.<br>This only supports key-value mapping.<br><br>To receive the events from the ethereum blockchain, You can specify all attributes which were included in the response of the following functions<br>&nbsp;https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_getblockbyhash <br>https://github.com/ethereum/wiki/wiki/JSON-RPC#eth_gettransactionbyhash <br>To start the client follow the document here,<br>https://docs.web3j.io/getting_started.html</p>

<span id="syntax" class="md-typeset" style="display: block; font-weight: bold;">Syntax</span>
```
@source(type="ethereum", uri="<STRING>", filter="<STRING>", from.block="<STRING>", to.block="<STRING>", polling.interval="<LONG>", @map(...)))
```

<span id="query-parameters" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">QUERY PARAMETERS</span>
<table>
    <tr>
        <th>Name</th>
        <th style="min-width: 20em">Description</th>
        <th>Default Value</th>
        <th>Possible Data Types</th>
        <th>Optional</th>
        <th>Dynamic</th>
    </tr>
    <tr>
        <td style="vertical-align: top">uri</td>
        <td style="vertical-align: top; word-wrap: break-word">The URI that is used to connect to ethereum blockchain. If no URI is specified, an error is logged in the CLI. e.g., <code>ws://localhost:8546</code></td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">filter</td>
        <td style="vertical-align: top; word-wrap: break-word">This parameter specifies the classes of filter supported in ethereum. eg., <code>newBlock</code>, <code>newTransaction</code>, <code>pendingTransaction</code> and <code>replayTransaction</code>. When this parameter is set to <code>replayTransaction</code>, the <code>from.block</code>, and <code>to.block</code> parameters are initialized. </td>
        <td style="vertical-align: top"></td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">No</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">from.block</td>
        <td style="vertical-align: top; word-wrap: break-word">This parameter specifies the first block in the range of blocks specified. It can take default block parameter values eg.,<code>earliest</code>. When <code>filter</code> parameter is set to <code>replayTransaction</code>, the <code>from.block</code> parameter is initialized. </td>
        <td style="vertical-align: top">earliest</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">to.block</td>
        <td style="vertical-align: top; word-wrap: break-word">This parameter specifies the last block in the range of blocks specified. It can take default block parameter values eg., <code>latest</code>.When <code>filter</code> parameter is set to <code>replayTransaction</code>, the <code>to.block</code> parameter is initialized. </td>
        <td style="vertical-align: top">latest</td>
        <td style="vertical-align: top">STRING</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
    <tr>
        <td style="vertical-align: top">polling.interval</td>
        <td style="vertical-align: top; word-wrap: break-word">The polling time between two polls in milliseconds. </td>
        <td style="vertical-align: top">3600</td>
        <td style="vertical-align: top">LONG</td>
        <td style="vertical-align: top">Yes</td>
        <td style="vertical-align: top">No</td>
    </tr>
</table>

<span id="examples" class="md-typeset" style="display: block; font-weight: bold;">Examples</span>
<span id="example-1" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 1</span>
```
@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', filter='replayTransaction', from.block= 'earliest', to.block= 'latest',polling.interval='60000')
define stream inputStream(from String, to String, value int);
```
<p style="word-wrap: break-word">In this example, the ethereum source listens to transaction history ranges from earliest to latest with the polling interval of 60 seconds. </p>

<span id="example-2" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 2</span>
```
@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', filter='pendingTransaction', polling.interval='60000')
define stream inputStream(from String, to String, value int);
```
<p style="word-wrap: break-word">In this example, the ethereum source polls the transactions that are pending every 60 seconds. </p>

<span id="example-3" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 3</span>
```
@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', filter='newBlock', polling.interval='60000')
define stream inputStream(blockNumber int, nonce int, blockHash String);
```
<p style="word-wrap: break-word">In this example, the ethereum source polls the new blocks every 60 seconds as they are added to the blockchain. </p>

<span id="example-4" class="md-typeset" style="display: block; color: rgba(0, 0, 0, 0.54); font-size: 12.8px; font-weight: bold;">EXAMPLE 4</span>
```
@source(type='ethereum', @map(type='keyvalue'), uri='ws://localhost:8546', filter='newTransaction', polling.interval='60000')
define stream inputStream(from String, to String, value int);
```
<p style="word-wrap: break-word">In this example, the ethereum source polls the new transactions every 60 seconds as they are added to the blockchain. </p>

