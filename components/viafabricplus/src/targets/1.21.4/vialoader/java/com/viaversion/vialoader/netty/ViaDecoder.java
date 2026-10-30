package com.viaversion.vialoader.netty;

import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.exception.CancelCodecException;
import com.viaversion.viaversion.exception.CancelDecoderException;
import com.viaversion.viaversion.util.PipelineUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;

import java.util.List;

public class ViaDecoder extends MessageToMessageDecoder<ByteBuf> {

    protected final UserConnection connection;

    public ViaDecoder(final UserConnection connection) {
        this.connection = connection;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        if (!this.connection.checkIncomingPacket()) {
            throw CancelDecoderException.generate(null);
        }
        if (!this.connection.shouldTransformPacket()) {
            out.add(in.retain());
            return;
        }

        final ByteBuf transformedBuf = ctx.alloc().buffer().writeBytes(in);
        try {
            this.connection.transformIncoming(transformedBuf, CancelDecoderException::generate);
            out.add(transformedBuf.retain());
        } finally {
            transformedBuf.release();
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
