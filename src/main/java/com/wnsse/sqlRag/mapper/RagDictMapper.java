package com.wnsse.sqlRag.mapper;

import com.wnsse.sqlRag.entity.RagDict;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import java.util.List;

@Mapper
public interface RagDictMapper {

    @Select("SELECT id, pid, name, type, `desc`, tag_type FROM rag_dict ORDER BY id")
    List<RagDict> selectAll();
}