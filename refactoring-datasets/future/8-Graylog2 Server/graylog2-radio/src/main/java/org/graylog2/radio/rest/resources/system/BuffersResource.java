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
package org.graylog2.radio.rest.resources.system;

import com.codahale.metrics.annotation.Timed;
import com.google.common.collect.Maps;
import org.graylog2.plugin.buffers.BufferWatermark;
import org.graylog2.radio.Radio;
import org.graylog2.radio.rest.resources.RestResource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Map;

/**
 * @author Lennart Koopmann <lennart@torch.sh>
 */
@Path("/system/buffers")
public class BuffersResource extends RestResource {

    @GET @Timed
    @Produces(MediaType.APPLICATION_JSON)
    public String utilization() {
        Map<String, Object> result = Maps.newHashMap();
        result.put("buffers", buffers(radio));
        result.put("master_caches", masterCaches(radio));

        return json(result);
    }

    private Map<String, Object> masterCaches(Radio radio) {
        Map<String, Object> caches = Maps.newHashMap();
        Map<String, Object> input = Maps.newHashMap();

        input.put("size", radio.getInputCache().size());

        caches.put("input", input);

        return caches;
    }

    private Map<String, Object> buffers(Radio radio) {
        Map<String, Object> buffers = Maps.newHashMap();
        Map<String, Object> input = Maps.newHashMap();

        BufferWatermark pWm = new BufferWatermark(
                radio.getConfiguration().getRingSize(),
                radio.processBufferWatermark()
        );

        input.put("utilization_percent", pWm.getUtilizationPercentage());
        input.put("utilization", pWm.getUtilization());

        buffers.put("input", input);

        return buffers;
    }

}
