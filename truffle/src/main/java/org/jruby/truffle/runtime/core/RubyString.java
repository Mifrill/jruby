/*
 * Copyright (c) 2013, 2015 Oracle and/or its affiliates. All rights reserved. This
 * code is released under a tri EPL/GPL/LGPL license. You can use it,
 * redistribute it and/or modify it under the terms of the:
 *
 * Eclipse Public License version 1.0
 * GNU General Public License version 2
 * GNU Lesser General Public License version 2.1
 */
package org.jruby.truffle.runtime.core;

import com.oracle.truffle.api.CompilerDirectives.TruffleBoundary;
import org.jcodings.Encoding;
import org.jruby.runtime.Helpers;
import org.jruby.util.ByteList;
import org.jruby.util.CodeRangeable;
import org.jruby.util.StringSupport;

public class RubyString extends RubyBasicObject implements CodeRangeable {

    public ByteList bytes;
    public int codeRange = StringSupport.CR_UNKNOWN;

    public RubyString(RubyClass stringClass, ByteList bytes) {
        super(stringClass);
        this.bytes = bytes;
    }

    @Override
    @TruffleBoundary
    public String toString() {
        return Helpers.decodeByteList(getContext().getRuntime(), bytes);
    }

    @Override
    public int getCodeRange() {
        return codeRange;
    }

    @Override
    @TruffleBoundary
    public int scanForCodeRange() {
        int cr = getCodeRange();

        if (cr == StringSupport.CR_UNKNOWN) {
            cr = slowCodeRangeScan();
            setCodeRange(cr);
        }

        return cr;
    }

    @Override
    public boolean isCodeRangeValid() {
        return codeRange == StringSupport.CR_VALID;
    }

    @Override
    public final void setCodeRange(int codeRange) {
        this.codeRange = codeRange;
    }

    @Override
    public final void clearCodeRange() {
        codeRange = StringSupport.CR_UNKNOWN;
    }

    @Override
    public final void keepCodeRange() {
        if (getCodeRange() == StringSupport.CR_BROKEN) {
            clearCodeRange();
        }
    }

    @Override
    public final void modify() {
        // TODO (nirvdrum 16-Feb-15): This should check whether the underlying ByteList is being shared and copy if necessary.
        bytes.invalidate();
    }

    @Override
    public final void modify(int length) {
        // TODO (nirvdrum Jan. 13, 2015): This should check whether the underlying ByteList is being shared and copy if necessary.
        bytes.ensure(length);
        bytes.invalidate();
    }

    @Override
    public final void modifyAndKeepCodeRange() {
        modify();
        keepCodeRange();
    }

    @Override
    @TruffleBoundary
    public Encoding checkEncoding(CodeRangeable other) {
        final Encoding encoding = StringSupport.areCompatible(this, other);

        // TODO (nirvdrum 23-Mar-15) We need to raise a proper Truffle+JRuby exception here, rather than a non-Truffle JRuby exception.
        if (encoding == null) {
            throw getContext().getRuntime().newEncodingCompatibilityError(
                    String.format("incompatible character encodings: %s and %s",
                            getByteList().getEncoding().toString(),
                            other.getByteList().getEncoding().toString()));
        }

        return encoding;
    }

    @Override
    public ByteList getByteList() {
        return bytes;
    }

    @TruffleBoundary
    private int slowCodeRangeScan() {
        return StringSupport.codeRangeScan(bytes.getEncoding(), bytes);
    }

}
