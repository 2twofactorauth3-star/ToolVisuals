package com.viaversion.vialoader.util;

import com.viaversion.viaversion.api.platform.PlatformTask;
import com.viaversion.viaversion.api.scheduler.Task;

public record VLTask(Task task) implements PlatformTask<Task> {

    @Override
    public void cancel() {
        this.task.cancel();
    }

}
