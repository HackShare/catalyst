/*
 * Copyright 2015 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package net.kuujo.catalyst.transport;

import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.Epoll;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import net.kuujo.catalyst.util.Assert;
import net.kuujo.catalyst.util.concurrent.CopycatThreadFactory;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadFactory;

/**
 * Netty protocol.
 *
 * @author <a href="http://github.com/kuujo">Jordan Halterman</a>
 */
public class NettyTransport implements Transport {
  private final EventLoopGroup eventLoopGroup;
  private final Map<UUID, NettyClient> clients = new ConcurrentHashMap<>();
  private final Map<UUID, NettyServer> servers = new ConcurrentHashMap<>();

  public NettyTransport() {
    this(Runtime.getRuntime().availableProcessors());
  }

  /**
   * @throws IllegalArgumentException if {@code threads} is not positive
   * @param threads
   */
  public NettyTransport(int threads) {
    Assert.arg(threads > 0, "threads must be positive");

    ThreadFactory threadFactory = new CopycatThreadFactory("catalyst-event-loop-%d");
    if (Epoll.isAvailable()) {
      eventLoopGroup = new EpollEventLoopGroup(threads, threadFactory);
    } else {
      eventLoopGroup = new NioEventLoopGroup(threads, threadFactory);
    }
  }

  @Override
  public Client client(UUID id) {
    return clients.computeIfAbsent(Assert.notNull(id, "id"), i -> new NettyClient(Assert.notNull(id, "id"), eventLoopGroup));
  }

  @Override
  public Server server(UUID id) {
    return servers.computeIfAbsent(Assert.notNull(id, "id"), i -> new NettyServer(id, eventLoopGroup));
  }

  @Override
  public CompletableFuture<Void> close() {
    int i = 0;

    CompletableFuture[] futures = new CompletableFuture[clients.size() + servers.size()];
    for (Client client : clients.values()) {
      futures[i++] = client.close();
    }

    for (Server server : servers.values()) {
      futures[i++] = server.close();
    }

    return CompletableFuture.allOf(futures).thenRun(eventLoopGroup::shutdownGracefully);
  }

}
