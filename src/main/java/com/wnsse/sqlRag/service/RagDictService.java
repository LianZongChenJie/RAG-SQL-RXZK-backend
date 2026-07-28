package com.wnsse.sqlRag.service;

import com.wnsse.sqlRag.entity.RagDict;
import com.wnsse.sqlRag.mapper.RagDictMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RagDictService {

    @Autowired
    private RagDictMapper ragDictMapper;

    public List<RagDict> getTreeList() {
        List<RagDict> all = ragDictMapper.selectAll();
        Map<Integer, List<RagDict>> childrenMap = all.stream()
                .filter(d -> d.getPid() != null && d.getPid() != 0)
                .collect(Collectors.groupingBy(RagDict::getPid));

        List<RagDict> roots = new ArrayList<>();
        for (RagDict dict : all) {
            if (dict.getPid() == null || dict.getPid() == 0) {
                roots.add(dict);
            }
            List<RagDict> children = childrenMap.get(dict.getId());
            if (children != null) {
                dict.setChildren(children);
            } else {
                dict.setChildren(new ArrayList<>());
            }
        }
        return roots;
    }
}