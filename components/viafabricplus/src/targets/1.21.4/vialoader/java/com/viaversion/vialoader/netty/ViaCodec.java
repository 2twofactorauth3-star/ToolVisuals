package com.viaversion.vialoader.netty;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.exception.CancelEncoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.ByteToMessageCodec;

import java.util.List;

public class ViaCodec extends ByteToMessageCodec<ByteBuf> {

    protected final UserConnection connection;

    public ViaCodec(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, ByteBuf in, ByteBuf out) {
        if (!this.connection.checkOutgoingPacket()) {
            throw CancelEncoderException.generate(null);
        }

        out.writeBytes(in);
        if (this.connection.shouldTransformPacket()) {
            this.connection.transformOutgoing(out, CancelEncoderException::generate);
        }
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!this.connection.checkIncomingPacket()) {
            throw CancelDecoderException.generate(null);
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            if (this.connection.shouldTransformPacket()) {
                this.connection.transformIncoming(transformedBuf, CancelDecoderException::generate);
            }
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
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            super.channelRead(ctx, msg);
        } catch (Throwable e) {
            if (!PipelineUtil.containsCause(e, CancelCodecException.class)) {
                throw e;
            }
        }
    }

    @Override
    public boolean isSharable() {

        return this.connection != null;
    }

}
