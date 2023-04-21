package com.lx.rsm.Data;

import it.unimi.dsi.fastutil.objects.Object2ObjectArrayMap;
import it.unimi.dsi.fastutil.objects.Object2ObjectMap;

public class DiffManager {
    private final Object2ObjectMap<String, String> previousDatas;

    public DiffManager() {
        previousDatas = new Object2ObjectArrayMap<>();
    }

    public boolean needUpdate(String uuid, String data) {
        String prevData = previousDatas.get(uuid);
        if(prevData == null) return true;
        return !prevData.equals(data);
    }

    public boolean haveRecordAndDeleteIfAny(String uuid) {
        boolean result = previousDatas.containsKey(uuid);
        if(result) {
            previousDatas.remove(uuid);
        }
        return result;
    }

    public void storeDifference(String uuid, String data) {
        previousDatas.put(uuid, data);
    }
}
