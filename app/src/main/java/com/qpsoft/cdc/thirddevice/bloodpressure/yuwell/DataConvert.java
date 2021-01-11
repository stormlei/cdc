package com.qpsoft.cdc.thirddevice.bloodpressure.yuwell;

/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;


@SuppressWarnings({"WeakerAccess", "unused", "UnusedReturnValue"})
public class DataConvert {

    public @interface ValueFormat {}

    public @interface IntFormat {}

    public @interface LongFormat {}

    public @interface FloatFormat {}

    private static char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();

    /**
     * DataParser value format type uint8
     */
    public final static int FORMAT_UINT8 = 0x11;

    /**
     * DataParser value format type uint16
     */
    public final static int FORMAT_UINT16 = 0x12;

    /**
     * DataParser value format type uint24
     */
    public final static int FORMAT_UINT24 = 0x13;

    /**
     * DataParser value format type uint32
     */
    public final static int FORMAT_UINT32 = 0x14;

    /**
     * DataParser value format type sint8
     */
    public final static int FORMAT_SINT8 = 0x21;

    /**
     * DataParser value format type sint16
     */
    public final static int FORMAT_SINT16 = 0x22;

    /**
     * DataParser value format type sint24
     */
    public final static int FORMAT_SINT24 = 0x23;

    /**
     * DataParser value format type sint32
     */
    public final static int FORMAT_SINT32 = 0x24;

    /**
     * DataParser value format type sfloat (16-bit float, IEEE-11073)
     */
    public final static int FORMAT_SFLOAT = 0x32;

    /**
     * DataParser value format type float (32-bit float, IEEE-11073)
     */
    public final static int FORMAT_FLOAT = 0x34;

    protected byte[] mValue;

    public DataConvert() {
        this.mValue = null;
    }

    public DataConvert(@Nullable final byte[] value) {
        this.mValue = value;
    }

    public static DataConvert from(@NonNull final String value) {
        return new DataConvert(value.getBytes()); // UTF-8
    }

    public static DataConvert opCode(final byte opCode) {
        return new DataConvert(new byte[] { opCode });
    }

    public static DataConvert opCode(final byte opCode, final byte parameter) {
        return new DataConvert(new byte[] { opCode, parameter });
    }

    /**
     * Returns the underlying byte array.
     *
     * @return DataParser received.
     */
    @Nullable
    public byte[] getValue() {
        return mValue;
    }

    /**
     * Return the stored value of this characteristic.
     * <p>See {@link #getValue} for details.
     *
     * @param offset Offset at which the string value can be found.
     * @return Cached value of the characteristic
     */
    @Nullable
    public String getStringValue(@IntRange(from = 0) final int offset) {
        if (mValue == null || offset > mValue.length)
            return null;
        final byte[] strBytes = new byte[mValue.length - offset];
        for (int i = 0; i != (mValue.length - offset); ++i)
            strBytes[i] = mValue[offset+i];
        return new String(strBytes);
    }

    /**
     * Returns the size of underlying byte array.
     *
     * @return Length of the data.
     */
    public int size() {
        return mValue != null ? mValue.length : 0;
    }

    @Override
    public String toString() {
        if (size() == 0)
            return "";

        final char[] out = new char[mValue.length * 3 - 1];
        for (int j = 0; j < mValue.length; j++) {
            int v = mValue[j] & 0xFF;
            out[j * 3] = HEX_ARRAY[v >>> 4];
            out[j * 3 + 1] = HEX_ARRAY[v & 0x0F];
            if (j != mValue.length - 1)
                out[j * 3 + 2] = '-';
        }
        return "(0x) " + new String(out);
    }

    /**
     * Returns a byte at the given offset from the byte array.
     *
     * @param offset Offset at which the byte value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    @Nullable
    public Byte getByte(@IntRange(from = 0) final int offset) {
        if (offset + 1 > size()) return null;

        return mValue[offset];
    }

    /**
     * Returns an integer value from the byte array.
     * <p>
     * <p>The formatType parameter determines how the value
     * is to be interpreted. For example, setting formatType to
     * {@link #FORMAT_UINT16} specifies that the first two bytes of the
     * value at the given offset are interpreted to generate the
     * return value.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the integer value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    @Nullable
    public Integer getIntValue(@IntFormat final int formatType,
                               @IntRange(from = 0) final int offset) {
        if ((offset + getTypeLen(formatType)) > size()) return null;

        switch (formatType) {
            case FORMAT_UINT8:
                return unsignedByteToInt(mValue[offset]);

            case FORMAT_UINT16:
                return unsignedBytesToInt(mValue[offset], mValue[offset + 1]);

            case FORMAT_UINT24:
                return unsignedBytesToInt(mValue[offset], mValue[offset + 1],
                        mValue[offset + 2], (byte) 0);

            case FORMAT_UINT32:
                return unsignedBytesToInt(mValue[offset], mValue[offset + 1],
                        mValue[offset + 2], mValue[offset + 3]);

            case FORMAT_SINT8:
                return unsignedToSigned(unsignedByteToInt(mValue[offset]), 8);

            case FORMAT_SINT16:
                return unsignedToSigned(unsignedBytesToInt(mValue[offset],
                        mValue[offset + 1]), 16);

            case FORMAT_SINT24:
                return unsignedToSigned(unsignedBytesToInt(mValue[offset],
                        mValue[offset + 1], mValue[offset + 2], (byte) 0), 24);

            case FORMAT_SINT32:
                return unsignedToSigned(unsignedBytesToInt(mValue[offset],
                        mValue[offset + 1], mValue[offset + 2], mValue[offset + 3]), 32);
        }

        return null;
    }

    /**
     * Returns a long value from the byte array.
     * <p>Only {@link #FORMAT_UINT32} and {@link #FORMAT_SINT32} are supported.
     * <p>The formatType parameter determines how the value
     * is to be interpreted. For example, setting formatType to
     * {@link #FORMAT_UINT32} specifies that the first four bytes of the
     * value at the given offset are interpreted to generate the
     * return value.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the integer value can be found.
     * @return Cached value or null of offset exceeds value size.
     */
    @Nullable
    public Long getLongValue(@LongFormat final int formatType,
                             @IntRange(from = 0) final int offset) {
        if ((offset + getTypeLen(formatType)) > size()) return null;

        switch (formatType) {
            case FORMAT_SINT32:
                return unsignedToSigned(unsignedBytesToLong(mValue[offset],
                        mValue[offset + 1], mValue[offset + 2], mValue[offset + 3]), 32);

            case FORMAT_UINT32:
                return unsignedBytesToLong(mValue[offset], mValue[offset + 1],
                        mValue[offset + 2], mValue[offset + 3]);
        }

        return null;
    }

    @Retention(RetentionPolicy.CLASS)
    @Target({ElementType.METHOD, ElementType.PARAMETER, ElementType.FIELD, ElementType.LOCAL_VARIABLE, ElementType.ANNOTATION_TYPE})
    public @interface IntRange {
        long from() default -9223372036854775808L;

        long to() default 9223372036854775807L;
    }

    /**
     * Returns an float value from the given byte array.
     *
     * @param formatType The format type used to interpret the value.
     * @param offset     Offset at which the float value can be found.
     * @return Cached value at a given offset or null if the requested offset exceeds the value size.
     */
    @Nullable
    public Float getFloatValue(@FloatFormat final int formatType,
                               @IntRange(from = 0) final int offset) {
        if ((offset + getTypeLen(formatType)) > size()) return null;

        switch (formatType) {
            case FORMAT_SFLOAT:
                if (mValue[offset + 1] == 0x07 && mValue[offset] == (byte) 0xFE)
                    return Float.POSITIVE_INFINITY;
                if ((mValue[offset + 1] == 0x07 && mValue[offset] == (byte) 0xFF) ||
                        (mValue[offset + 1] == 0x08 && mValue[offset] == 0x00) ||
                        (mValue[offset + 1] == 0x08 && mValue[offset] == 0x01))
                    return Float.NaN;
                if (mValue[offset + 1] == 0x08 && mValue[offset] == 0x02)
                    return Float.NEGATIVE_INFINITY;

                return bytesToFloat(mValue[offset], mValue[offset + 1]);

            case FORMAT_FLOAT:
                if (mValue[offset + 3] == 0x00) {
                    if (mValue[offset + 2] == 0x7F && mValue[offset + 1] == (byte) 0xFF) {
                        if (mValue[offset] == (byte) 0xFE)
                            return Float.POSITIVE_INFINITY;
                        if (mValue[offset] == (byte) 0xFF)
                            return Float.NaN;
                    } else if (mValue[offset + 2] == (byte) 0x80 && mValue[offset + 1] == 0x00) {
                        if (mValue[offset] == 0x00 || mValue[offset] == 0x01)
                            return Float.NaN;
                        if (mValue[offset] == 0x02)
                            return Float.NEGATIVE_INFINITY;
                    }
                }

                return bytesToFloat(mValue[offset], mValue[offset + 1],
                        mValue[offset + 2], mValue[offset + 3]);
        }

        return null;
    }

    /**
     * Returns the size of a give value type.
     */
    public static int getTypeLen(@ValueFormat final int formatType) {
        return formatType & 0xF;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static int unsignedByteToInt(final byte b) {
        return b & 0xFF;
    }

    /**
     * Convert a signed byte to an unsigned int.
     */
    private static long unsignedByteToLong(final byte b) {
        return b & 0xFFL;
    }

    /**
     * Convert signed bytes to a 16-bit unsigned int.
     */
    private static int unsignedBytesToInt(final byte b0, final byte b1) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8));
    }

    /**
     * Convert signed bytes to a 32-bit unsigned int.
     */
    private static int unsignedBytesToInt(final byte b0, final byte b1, final byte b2, final byte b3) {
        return (unsignedByteToInt(b0) + (unsignedByteToInt(b1) << 8))
                + (unsignedByteToInt(b2) << 16) + (unsignedByteToInt(b3) << 24);
    }

    /**
     * Convert signed bytes to a 32-bit unsigned long.
     */
    private static long unsignedBytesToLong(final byte b0, final byte b1, final byte b2, final byte b3) {
        return (unsignedByteToLong(b0) + (unsignedByteToLong(b1) << 8))
                + (unsignedByteToLong(b2) << 16) + (unsignedByteToLong(b3) << 24);
    }

    /**
     * Convert signed bytes to a 16-bit short float value.
     */
    private static float bytesToFloat(final byte b0, final byte b1) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + ((unsignedByteToInt(b1) & 0x0F) << 8), 12);
        int exponent = unsignedToSigned(unsignedByteToInt(b1) >> 4, 4);
        return (float) (mantissa * Math.pow(10, exponent));
    }

    /**
     * Convert signed bytes to a 32-bit short float value.
     */
    private static float bytesToFloat(final byte b0, final byte b1, final byte b2, final byte b3) {
        int mantissa = unsignedToSigned(unsignedByteToInt(b0)
                + (unsignedByteToInt(b1) << 8)
                + (unsignedByteToInt(b2) << 16), 24);
        return (float) (mantissa * Math.pow(10, b3));
    }

    /**
     * Convert an unsigned integer value to a two's-complement encoded
     * signed value.
     */
    private static int unsignedToSigned(int unsigned, final int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }

    /**
     * Convert an unsigned long value to a two's-complement encoded
     * signed value.
     */
    private static long unsignedToSigned(long unsigned, final int size) {
        if ((unsigned & (1 << size - 1)) != 0) {
            unsigned = -1 * ((1 << size - 1) - (unsigned & ((1 << size - 1) - 1)));
        }
        return unsigned;
    }
}
