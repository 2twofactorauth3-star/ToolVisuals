package com.viaversion.vialoader.netty;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelPipeline;
import net.raphimc.vialegacy.netty.PreNettyLengthCodec;

public abstract class VLPipeline extends ChannelInboundHandlerAdapter {

    public static final String VIA_CODEC_NAME = "via-codec";

    public static final String VIALEGACY_PRE_NETTY_LENGTH_CODEC_NAME = "vialegacy-pre-netty-length-codec";


    protected final UserConnection connection;
    protected final ProtocolVersion version;

    public VLPipeline(final UserConnection connection) {
        this(connection, Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(connection));
    }

    public VLPipeline(final UserConnection connection, final ProtocolVersion version) {
        this.connection = connection;
        this.version = version;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().addBefore(this.packetCodecName(), VIA_CODEC_NAME, this.createViaCodec());

        if (this.connection.isClientSide()) {
            final ProtocolVersion r1_6_4 = ProtocolVersion.getProtocol(VersionType.RELEASE_INITIAL, 78);
            if (ProtocolVersion.isRegistered(r1_6_4.getVersionType(), r1_6_4.getOriginalVersion()) && this.version.olderThanOrEqualTo(r1_6_4)) {
                ctx.pipeline().addBefore(this.lengthCodecName(), VIALEGACY_PRE_NETTY_LENGTH_CODEC_NAME, this.createViaLegacyPreNettyLengthCodec());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (CompressionReorderEvent.INSTANCE.equals(evt)) {
            final ChannelPipeline pipeline = ctx.pipeline();

            if (pipeline.names().indexOf(this.compressionCodecName()) > pipeline.names().indexOf(VIA_CODEC_NAME)) {
                pipeline.addAfter(this.compressionCodecName(), VIA_CODEC_NAME, pipeline.remove(VIA_CODEC_NAME));
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    protected ChannelHandler createViaCodec() {
        return new ViaCodec(this.connection);
    }

    protected ChannelHandler createViaLegacyPreNettyLengthCodec() {
        return new PreNettyLengthCodec(this.connection);
    }

    protected void replaceLengthCodec(final ChannelHandlerContext ctx, final ChannelHandler handler) {
        ctx.pipeline().replace(this.lengthCodecName(), this.lengthCodecName(), handler);
    }

    protected abstract String compressionCodecName();

    protected abstract String packetCodecName();

    protected abstract String lengthCodecName();

}
