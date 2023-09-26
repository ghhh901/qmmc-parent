package com.ghhh.qmmc.acl.util;

import com.ghhh.qmmc.model.acl.Permission;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {
    public static List<Permission> bulid(List<Permission> allPermission){
        List<Permission> trees = new ArrayList<>();
        for (Permission per : allPermission) {
            if (per.getPid() == 0){
                per.setLevel(1);
                trees.add(findChildren(per,allPermission));
            }
        }
        return trees;
    }

    private static Permission findChildren(Permission per, List<Permission> allPermission) {
        per.setChildren(new ArrayList<>());
        for (Permission it: allPermission) {
            if (per.getId().longValue() == it.getPid().longValue()){
                it.setLevel(per.getLevel()+1);
                if (it.getChildren() == null){
                    it.setChildren(new ArrayList<>());
                }
                per.getChildren().add(findChildren(it,allPermission));
            }
        }
        
        return per;
    }


}
