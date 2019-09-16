/**
 * Copyright 2013 Lennart Koopmann <lennart@torch.sh>
 *
 * This file is part of Graylog2.
 *
 * Graylog2 is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Graylog2 is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Graylog2.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package org.graylog2.inputs.raw.udp;

import com.codahale.metrics.Gauge;
import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.graylog2.inputs.raw.RawInputBase;
import org.graylog2.plugin.inputs.MisfireException;
import org.jboss.netty.bootstrap.ConnectionlessBootstrap;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.FixedReceiveBufferSizePredictorFactory;
import org.jboss.netty.channel.socket.nio.NioDatagramChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
public class RawUDPInput extends RawInputBase {

    private static final Logger LOG = LoggerFactory.getLogger(RawUDPInput.class);

    public static final String NAME = "Raw/Plaintext UDP";

    @Override
    public void launch() throws MisfireException {
        // Register throughput counter gauges.
        for(Map.Entry<String,Gauge<Long>> gauge : throughputCounter.gauges().entrySet()) {
            graylogServer.metrics().register(MetricRegistry.name(getUniqueReadableId(), gauge.getKey()), gauge.getValue());
        }

        final ExecutorService workerThreadPool = Executors.newCachedThreadPool(
                new ThreadFactoryBuilder()
                        .setNameFormat("input-" + getId() + "-rawudp-worker-%d")
                        .build());

        bootstrap = new ConnectionlessBootstrap(new NioDatagramChannelFactory(workerThreadPool));
        bootstrap.setOption("receiveBufferSizePredictorFactory", new FixedReceiveBufferSizePredictorFactory(8192));
        bootstrap.setPipelineFactory(new RawUDPPipelineFactory(graylogServer, configuration, this, throughputCounter));
        bootstrap.setOption("receiveBufferSize", getRecvBufferSize());

        try {
            channel = ((ConnectionlessBootstrap) bootstrap).bind(socketAddress);
            LOG.info("Started UDP raw/plaintext input on {}", socketAddress);
        } catch (ChannelException e) {
            String msg = "Could not bind UDP raw/plaintext input to address " + socketAddress;
            LOG.error(msg, e);
            throw new MisfireException(msg, e);
        }
    }

    @Override
    public String getName() {
        return NAME;
    }

}
