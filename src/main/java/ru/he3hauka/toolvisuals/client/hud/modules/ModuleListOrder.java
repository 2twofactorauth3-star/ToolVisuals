package ru.he3hauka.toolvisuals.client.hud.modules;

import java.util.Comparator;
import java.util.List;
import java.util.function.ToDoubleFunction;

import ru.he3hauka.toolvisuals.client.module.VisualModule;

public final class ModuleListOrder {
    private ModuleListOrder() {}
     
    public static List<VisualModule> sort(List<VisualModule> modules,int order,ToDoubleFunction<VisualModule> width) {
        Comparator<VisualModule> byName = Comparator.comparing(m -> m.title,String.CASE_INSENSITIVE_ORDER);
        Comparator<VisualModule> comparator = switch(order) {
            case 1 -> Comparator.comparingDouble(width).thenComparing(byName);
            case 2 -> byName;
            case 3 -> Comparator.comparingInt(Enum::ordinal);
            default -> Comparator.comparingDouble(width).reversed().thenComparing(byName);
        };
        return modules.stream().sorted(comparator).toList();
    }
}
