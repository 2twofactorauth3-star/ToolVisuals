package com.viaversion.vialoader.netty;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.MessageToMessageEncoder;

import java.util.List;

public class ViaEncoder extends MessageToMessageEncoder<ByteBuf> {

    protected final UserConnection connection;

    public ViaEncoder(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!this.connection.checkOutgoingPacket()) {
            throw CancelEncoderException.generate(null);
        }
        if (!this.connection.shouldTransformPacket()) {
            out.add(in.retain());
            return;
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            this.connection.transformOutgoing(transformedBuf, CancelEncoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
        }
    }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        try {
            super.write(ctx, msg, promise);
        } catch (Throwable e) {
            if (!PipelineUtil.containsCause(e, CancelCodecException.class)) {
                throw e;
            } else {
                promise.setSuccess();
            }
        }
    }

    @Override
    public boolean isSharable() {

        return this.connection != null;
    }
}
