package com.wnsse.sqlRag.entity;

import lombok.Data;
import java.util.List;

@Data
public class RagDict {
    private Integer id;
    private Integer pid;
    private String name;
    private String type;
    private String desc;
    private String tagType;
    private List<RagDict> children;
}