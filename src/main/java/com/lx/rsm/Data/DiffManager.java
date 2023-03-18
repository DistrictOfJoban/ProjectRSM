package com.lx.rsm.Data;

import java.util.HashMap;

public class DiffManager {
    private final HashMap<String, String> previousDatas;

    public DiffManager() {
        previousDatas = new HashMap<>();
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
