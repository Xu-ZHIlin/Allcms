package cn.edu.guet.cms2.util;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class PageRequest {
    private int currentPage = 1;
    private int pageSize = 10;
    private List<Param> params = new ArrayList<>();

    public Param getParam(String name) {
        for (Param param : this.params) {
            if (name != null && name.equals(param.getName())) {
                return param;
            }
        }
        return null;
    }

    public String getParamValue(String name) {
        Param param = getParam(name);
        if (param != null) {
            return param.getValue();
        }
        return null;
    }
}