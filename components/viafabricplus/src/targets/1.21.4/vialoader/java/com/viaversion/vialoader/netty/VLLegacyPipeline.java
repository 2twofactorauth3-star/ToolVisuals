package com.viaversion.vialoader.netty;

import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.connection.UserConnection;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.protocol.version.VersionProvider;
import com.viaversion.viaversion.api.protocol.version.VersionType;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import net.raphimc.vialegacy.netty.PreNettyLengthPrepender;
import net.raphimc.vialegacy.netty.PreNettyLengthRemover;

public abstract class VLLegacyPipeline extends ChannelInboundHandlerAdapter {

    public static final String VIA_DECODER_NAME = "via-decoder";
    public static final String VIA_ENCODER_NAME = "via-encoder";

    public static final String VIALEGACY_PRE_NETTY_LENGTH_PREPENDER_NAME = "vialegacy-pre-netty-length-prepender";
    public static final String VIALEGACY_PRE_NETTY_LENGTH_REMOVER_NAME = "vialegacy-pre-netty-length-remover";


    protected final UserConnection connection;
    protected final ProtocolVersion version;

    public VLLegacyPipeline(final UserConnection connection) {
        this(connection, Via.getManager().getProviders().get(VersionProvider.class).getServerProtocol(connection));
    }

    public VLLegacyPipeline(final UserConnection connection, final ProtocolVersion version) {
        this.connection = connection;
        this.version = version;
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) {
        ctx.pipeline().addBefore(this.packetDecoderName(), VIA_DECODER_NAME, this.createViaDecoder());
        ctx.pipeline().addBefore(this.packetEncoderName(), VIA_ENCODER_NAME, this.createViaEncoder());

        if (this.connection.isClientSide()) {
            final ProtocolVersion r1_6_4 = ProtocolVersion.getProtocol(VersionType.RELEASE_INITIAL, 78);
            if (ProtocolVersion.isRegistered(r1_6_4.getVersionType(), r1_6_4.getOriginalVersion()) && this.version.olderThanOrEqualTo(r1_6_4)) {
                ctx.pipeline().addBefore(this.lengthSplitterName(), VIALEGACY_PRE_NETTY_LENGTH_PREPENDER_NAME, this.createViaLegacyPreNettyLengthPrepender());
                ctx.pipeline().addBefore(this.lengthPrependerName(), VIALEGACY_PRE_NETTY_LENGTH_REMOVER_NAME, this.createViaLegacyPreNettyLengthRemover());
            }
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (CompressionReorderEvent.INSTANCE.equals(evt)) {
            final int decoderIndex = ctx.pipeline().names().indexOf(decompressName());
            if (decoderIndex == -1) return;

            if (decoderIndex > ctx.pipeline().names().indexOf(VIA_DECODER_NAME)) {
                final ChannelHandler decoder = ctx.pipeline().get(VIA_DECODER_NAME);
                final ChannelHandler encoder = ctx.pipeline().get(VIA_ENCODER_NAME);

                ctx.pipeline().remove(decoder);
                ctx.pipeline().remove(encoder);

                ctx.pipeline().addAfter(decompressName(), VIA_DECODER_NAME, decoder);
                ctx.pipeline().addAfter(compressName(), VIA_ENCODER_NAME, encoder);
            }
        }

        super.userEventTriggered(ctx, evt);
    }

    protected ChannelHandler createViaDecoder() {
        return new ViaDecoder(this.connection);
    }

    protected ChannelHandler createViaEncoder() {
        return new ViaEncoder(this.connection);
    }

    protected ChannelHandler createViaLegacyPreNettyLengthPrepender() {
        return new PreNettyLengthPrepender(this.connection);
    }

    protected ChannelHandler createViaLegacyPreNettyLengthRemover() {
        return new PreNettyLengthRemover(this.connection);
    }

    protected void replaceLengthSplitter(final ChannelHandlerContext ctx, final ChannelHandler handler) {
        ctx.pipeline().replace(this.lengthSplitterName(), this.lengthSplitterName(), handler);
    }

    protected abstract String decompressName();

    protected abstract String compressName();

    protected abstract String packetDecoderName();

    protected abstract String packetEncoderName();

    protected abstract String lengthSplitterName();

    protected abstract String lengthPrependerName();

}
