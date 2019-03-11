/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.tinkerpop.machine.beam;

import org.apache.tinkerpop.machine.functions.MapFunction;
import org.apache.tinkerpop.machine.functions.NestedFunction;
import org.apache.tinkerpop.machine.pipes.Pipes;
import org.apache.tinkerpop.machine.traversers.CompleteTraverserFactory;
import org.apache.tinkerpop.machine.traversers.Traverser;

import java.util.NoSuchElementException;

/**
 * @author Marko A. Rodriguez (http://markorodriguez.com)
 */
public class MapFn<C, S, E> extends AbstractFn<C, S, E> {

    private final MapFunction<C, S, E> mapFunction;
    private boolean first = true;

    public MapFn(final MapFunction<C, S, E> mapFunction) {
       super(mapFunction);
        this.mapFunction = mapFunction;
    }

    @ProcessElement
    public void processElement(final @Element Traverser<C, S> traverser, final OutputReceiver<Traverser<C, E>> output) {
        if (this.first) {
            if (this.mapFunction instanceof NestedFunction) {
                Pipes beam  = new Pipes(((NestedFunction) this.mapFunction).getFunctions(), new CompleteTraverserFactory());
                ((NestedFunction) this.mapFunction).setProcessor(beam);
                while (!this.traversers.isEmpty()) {
                    beam.addStart(this.traversers.remove());
                }
            }
            this.first = false;
        }
        try {
            output.output(traverser.map(this.mapFunction));
        } catch(final NoSuchElementException e) {
            // do nothing
        }
    }
}
