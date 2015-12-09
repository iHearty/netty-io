package cn.togeek.netty;

import com.google.protobuf.ByteString;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufProcessor;
import io.netty.buffer.Unpooled;

public class WrapperTest {
   public static void main(String[] args) {
      String msg1 =
         "/* * Licensed to Elasticsearch under one or more contributor * license agreements. See the NOTICE file distributed with * this work for additional information regarding copyright * ownership. Elasticsearch licenses this file to you under * the Apache License, Version 2.0 (the License); you may * not use this file except in compliance with the License. * You may obtain a copy of the License at * *    http://www.apache.org/licenses/LICENSE-2.0 * * Unless required by applicable law or agreed to in writing, * software distributed under the License is distributed on an * AS IS BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY * KIND, either express or implied.  See the License for the * specific language governing permissions and limitations * under the License. */package org.elasticsearch.transport.netty;import org.elasticsearch.ExceptionsHelper;import org.elasticsearch.Version;import org.elasticsearch.cluster.node.DiscoveryNode;import org.elasticsearch.common.Booleans;import org.elasticsearch.common.Strings;import org.elasticsearch.common.bytes.ReleasablePagedBytesReference;import org.elasticsearch.common.component.AbstractLifecycleComponent;import org.elasticsearch.common.compress.CompressorFactory;import org.elasticsearch.common.inject.Inject;import org.elasticsearch.common.io.stream.NamedWriteableRegistry;import org.elasticsearch.common.io.stream.ReleasableBytesStreamOutput;import org.elasticsearch.common.io.stream.StreamOutput;import org.elasticsearch.common.lease.Releasables;import org.elasticsearch.common.math.MathUtils;import org.elasticsearch.common.metrics.CounterMetric;import org.elasticsearch.common.netty.NettyUtils;import org.elasticsearch.common.netty.OpenChannelsHandler;import org.elasticsearch.common.netty.ReleaseChannelFutureListener;import org.elasticsearch.common.network.NetworkAddress;import org.elasticsearch.common.network.NetworkService;import org.elasticsearch.common.network.NetworkUtils;import org.elasticsearch.common.settings.Settings;import org.elasticsearch.common.transport.BoundTransportAddress;import org.elasticsearch.common.transport.InetSocketTransportAddress;import org.elasticsearch.common.transport.PortsRange;import org.elasticsearch.common.transport.TransportAddress;import org.elasticsearch.common.unit.ByteSizeValue;import org.elasticsearch.common.unit.TimeValue;import org.elasticsearch.common.util.BigArrays;import org.elasticsearch.common.util.concurrent.AbstractRunnable;import org.elasticsearch.common.util.concurrent.EsExecutors;import org.elasticsearch.common.util.concurrent.KeyedLock;import org.elasticsearch.monitor.jvm.JvmInfo;import org.elasticsearch.threadpool.ThreadPool;import org.elasticsearch.transport.BindTransportException;import org.elasticsearch.transport.BytesTransportRequest;import org.elasticsearch.transport.ConnectTransportException;import org.elasticsearch.transport.NodeNotConnectedException;import org.elasticsearch.transport.Transport;import org.elasticsearch.transport.TransportException;import org.elasticsearch.transport.TransportRequest;import org.elasticsearch.transport.TransportRequestOptions;import org.elasticsearch.transport.TransportServiceAdapter;import org.elasticsearch.transport.support.TransportStatus;import org.jboss.netty.bootstrap.ClientBootstrap;import org.jboss.netty.bootstrap.ServerBootstrap;import org.jboss.netty.buffer.ChannelBuffer;import org.jboss.netty.buffer.ChannelBuffers;import org.jboss.netty.channel.AdaptiveReceiveBufferSizePredictorFactory;import org.jboss.netty.channel.Channel;import org.jboss.netty.channel.ChannelFuture;import org.jboss.netty.channel.ChannelFutureListener;import org.jboss.netty.channel.ChannelHandlerContext;import org.jboss.netty.channel.ChannelPipeline;import org.jboss.netty.channel.ChannelPipelineFactory;import org.jboss.netty.channel.Channels;import org.jboss.netty.channel.ExceptionEvent;import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;import org.jboss.netty.channel.ReceiveBufferSizePredictorFactory;import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;import org.jboss.netty.channel.socket.nio.NioWorkerPool;import org.jboss.netty.channel.socket.oio.OioClientSocketChannelFactory;import org.jboss.netty.channel.socket.oio.OioServerSocketChannelFactory;import org.jboss.netty.util.HashedWheelTimer;import java.io.IOException;import java.net.InetAddress;import java.net.InetSocketAddress;import java.net.SocketAddress;import java.net.UnknownHostException;import java.nio.channels.CancelledKeyException;import java.nio.charset.StandardCharsets;import java.util.ArrayList;import java.util.Arrays;import java.util.Collections;import java.util.HashMap;import java.util.HashSet;import java.util.Iterator;import java.util.List;import java.util.Map;import java.util.Objects;import java.util.Set;import java.util.concurrent.ConcurrentMap;import java.util.concurrent.CountDownLatch;import java.util.concurrent.Executors;import java.util.concurrent.ThreadFactory;import java.util.concurrent.TimeUnit;import java.util.concurrent.atomic.AtomicInteger;import java.util.concurrent.atomic.AtomicReference;import java.util.concurrent.locks.ReadWriteLock;import java.util.concurrent.locks.ReentrantReadWriteLock;import java.util.regex.Matcher;import java.util.regex.Pattern;import static java.util.Collections.unmodifiableMap;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_BLOCKING;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_BLOCKING_CLIENT;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_BLOCKING_SERVER;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_CONNECT_TIMEOUT;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_DEFAULT_CONNECT_TIMEOUT;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_DEFAULT_RECEIVE_BUFFER_SIZE;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_DEFAULT_SEND_BUFFER_SIZE;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_KEEP_ALIVE;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_NO_DELAY;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_RECEIVE_BUFFER_SIZE;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_REUSE_ADDRESS;import static org.elasticsearch.common.network.NetworkService.TcpSettings.TCP_SEND_BUFFER_SIZE;import static org.elasticsearch.common.settings.Settings.settingsBuilder;import static org.elasticsearch.common.transport.NetworkExceptionHelper.isCloseConnectionException;import static org.elasticsearch.common.transport.NetworkExceptionHelper.isConnectException;import static org.elasticsearch.common.util.concurrent.ConcurrentCollections.newConcurrentMap;import static org.elasticsearch.common.util.concurrent.EsExecutors.daemonThreadFactory;/** * There are 4 types of connections per node, low/med/high/ping. Low if for batch oriented APIs (like recovery or * batch) with high payload that will cause regular request. (like search or single index) to take * longer. Med is for the typical search / single doc index. And High for things like cluster state. Ping is reserved for * sending out ping requests to other nodes. */public class NettyTransport extends AbstractLifecycleComponent<Transport> implements Transport {    static {        NettyUtils.setup();    }";
      msg1 += msg1;
      msg1 += msg1;
      msg1 += msg1;
      msg1 += msg1;
      msg1 += msg1;
      msg1 += msg1;

      byte[] bytes1 = msg1.getBytes();
      final ByteBuf buffer2 = Unpooled.wrappedBuffer(bytes1);

      long t3 = System.currentTimeMillis();
      buffer2.forEachByte(0, buffer2.capacity(), new ByteBufProcessor() {
         @Override
         public boolean process(byte value) throws Exception {
            // System.out.println(value + " , " + buffer2.capacity());
            return true;
         }
      });
      long t4 = System.currentTimeMillis();

      System.out.println(" --- " + (t4 - t3));

      long t5 = System.currentTimeMillis();
      ByteString byteString = ByteString.copyFrom(buffer2.nioBuffer());
      long t6 = System.currentTimeMillis();
      System.out.println(" --- " + (t6 - t5));

      long t9 = System.currentTimeMillis();
      final ByteBuf buffer4 =
         Unpooled.copiedBuffer(byteString.asReadOnlyByteBuffer());
      buffer4.forEachByte(0, buffer4.capacity(), new ByteBufProcessor() {
         @Override
         public boolean process(byte value) throws Exception {
            // System.out.println(value + " , " + buffer2.capacity());
            return true;
         }
      });
      long t10 = System.currentTimeMillis();
      System.out.println(" --- " + (t10 - t9));

      long t7 = System.currentTimeMillis();
      final ByteBuf buffer3 =
         Unpooled.wrappedBuffer(byteString.asReadOnlyByteBuffer());
      buffer3.forEachByte(0, buffer3.capacity(), new ByteBufProcessor() {
         @Override
         public boolean process(byte value) throws Exception {
            // System.out.println(value + " , " + buffer2.capacity());
            return true;
         }
      });
      long t8 = System.currentTimeMillis();
      System.out.println(" --- " + (t8 - t7));
   }
}
