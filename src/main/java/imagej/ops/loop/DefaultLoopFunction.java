/*
 * #%L
 * ImageJ OPS: a framework for reusable algorithms.
 * %%
 * Copyright (C) 2014 Board of Regents of the University of
 * Wisconsin-Madison and University of Konstanz.
 * %%
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDERS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 * #L%
 */

package imagej.ops.loop;

import imagej.ops.Function;
import imagej.ops.Op;
import imagej.ops.join.FunctionJoiner;

import java.util.ArrayList;

import org.scijava.plugin.Plugin;

/**
 * Applies a {@link Function} multiple times to an image.
 * 
 * @author Christian Dietz
 */
@Plugin(type = Op.class, name = Loop.NAME)
public class DefaultLoopFunction<A> extends
	AbstractLoopFunction<Function<A, A>, A>
{

	@Override
	public A compute(final A input, final A output) {

		final ArrayList<Function<A, A>> functions =
			new ArrayList<Function<A, A>>(n);
		for (int i = 0; i < n; i++)
			functions.add(function);

		final FunctionJoiner<A> functionJoiner = new FunctionJoiner<A>();
		functionJoiner.setFunctions(functions);
		functionJoiner.setBuffer(buffer);

		return functionJoiner.compute(input, output);
	}
}
