package com.flashminds.flyingchess.entity;

import com.flashminds.flyingchess.dataPack.DataPack;
import com.flashminds.flyingchess.dataPack.Target;

import java.util.HashMap;

/**
 * Created by karthur on 2016/4/24.
 *
 * Edited by IACJ on 2018/4/18
 */
public class MsgHandler {
    HashMap<Integer, Target> targets= new HashMap<>();;

    public void registerActivity(int datapack_commond, Target target) {
        targets.put(datapack_commond, target);
    }

    protected void processDataPack(DataPack dataPack) {
        if (targets.containsKey(dataPack.getCommand())) {
            targets.get(dataPack.getCommand()).processDataPack(dataPack);
        }
    }
}

