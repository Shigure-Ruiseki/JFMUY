package ruiseki.jfmuy.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import javax.annotation.Nonnull;

import cpw.mods.fml.common.discovery.ASMDataTable;
import ruiseki.jfmuy.api.IModPlugin;
import ruiseki.jfmuy.api.JFMUYPlugin;

public class AnnotatedInstanceUtil {

    private AnnotatedInstanceUtil() {

    }

    public static List<IModPlugin> getModPlugins(@Nonnull ASMDataTable asmDataTable) {
        return getInstances(asmDataTable, JFMUYPlugin.class, IModPlugin.class);
    }

    private static <T> List<T> getInstances(@Nonnull ASMDataTable asmDataTable, Class annotationClass,
        Class<T> instanceClass) {
        String annotationClassName = annotationClass.getCanonicalName();
        Set<ASMDataTable.ASMData> asmDatas = asmDataTable.getAll(annotationClassName);
        List<T> instances = new ArrayList<>();
        for (ASMDataTable.ASMData asmData : asmDatas) {
            try {
                Class<?> asmClass = Class.forName(asmData.getClassName());
                Class<? extends T> asmInstanceClass = asmClass.asSubclass(instanceClass);
                T instance = asmInstanceClass.newInstance();
                instances.add(instance);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
                Log.error("Failed to load: {}", asmData.getClassName(), e);
            }
        }
        return instances;
    }
}
