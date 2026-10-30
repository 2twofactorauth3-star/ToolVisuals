package com.viaversion.viafabricplus.screen.impl;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import com.viaversion.viafabricplus.ViaFabricPlusImpl;
import com.viaversion.viafabricplus.screen.VFPScreen;
import com.viaversion.viaversion.util.DumpUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

public final class ReportIssuesScreen extends VFPScreen {

    public static final ReportIssuesScreen INSTANCE = new ReportIssuesScreen();

    private final Map<String, Runnable> actions = new LinkedHashMap<>();

    private long delay = -1;

    public ReportIssuesScreen() {
        super(Text.translatable("screen.viafabricplus.report_issues"), true);

        if (!actions.isEmpty()) {
            return;
        }
        actions.put("report.viafabricplus.bug_report", () -> {
            Util.getOperatingSystem().open(URI.create("https://github.com/ViaVersion/ViaFabricPlus/issues/new?assignees=&labels=bug&projects=&template=bug_report.yml"));
            this.setupSubtitle(Text.translatable("report.viafabricplus.bug_report.response"));
        });
        actions.put("report.viafabricplus.feature_request", () -> {
            Util.getOperatingSystem().open(URI.create("https://github.com/ViaVersion/ViaFabricPlus/issues/new?assignees=&labels=enhancement&projects=&template=feature_request.yml"));
            this.setupSubtitle(Text.translatable("report.viafabricplus.feature_request.response"));
        });
        actions.put("report.viafabricplus.create_via_dump", () -> DumpUtil.postDump(client.getSession().getUuidOrNull()).whenComplete((s, throwable) -> {
            if (throwable != null) {
                this.setupSubtitle(Text.translatable("report.viafabricplus.create_via_dump.failed"));
                ViaFabricPlusImpl.INSTANCE.logger().error("Failed to create a dump", throwable);
                return;
            }
            this.setupSubtitle(Text.translatable("report.viafabricplus.create_via_dump.success"));
            client.keyboard.setClipboard(s);
        }));
        actions.put("report.viafabricplus.open_logs", () -> {
            Util.getOperatingSystem().open(new File(client.runDirectory, "logs")  );
            this.setupSubtitle(Text.translatable("report.viafabricplus.open_logs.response"));
        });
    }

    @Override
    protected void init() {
        super.init();
        this.setupDefaultSubtitle();

        int i = 0;
        for (Map.Entry<String, Runnable> entry : actions.entrySet()) {
            this.addDrawableChild(ButtonWidget.builder(Text.translatable(entry.getKey()), button -> entry.getValue().run()).
                    position(this.width / 2 - 100, this.height / 2 - 25 + i * (20 + 3)).size(200, 20).build());
            i++;
        }
    }

    @Override
    public void setupSubtitle(@Nullable Text subtitle) {
        super.setupSubtitle(subtitle);

        this.delay = System.currentTimeMillis();
    }

    @Override
    public void tick() {
        super.tick();
        if (this.delay != -1 && System.currentTimeMillis() - this.delay > 5000  ) {
            this.setupDefaultSubtitle();
            this.delay = -1;
        }
    }

}
